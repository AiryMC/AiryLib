package dev.airyy.AiryLib.core.config;

import dev.airyy.AiryLib.core.config.annotation.Config;
import dev.airyy.AiryLib.core.config.annotation.ConfigField;
import dev.airyy.AiryLib.core.config.annotation.ConfigVersion;
import dev.airyy.AiryLib.core.config.migration.IConfigMigration;
import dev.airyy.AiryLib.core.config.parser.IConfigParser;
import dev.airyy.AiryLib.core.config.parser.ListParser;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private static final Map<Class<?>, IConfigParser<?>> parsers = new HashMap<>();

    public static <T extends BaseConfig> T createConfig(Class<T> clazz, File dataFolder) throws Exception {
        return clazz.getDeclaredConstructor(File.class).newInstance(dataFolder);
    }

    public static <T> void registerParser(Class<T> clazz, IConfigParser<T> parser) {
        parsers.put(clazz, parser);
    }

    public static <T extends BaseConfig> T reload(T config) throws Exception {
        return load(config);
    }

    public static <T extends BaseConfig> T load(T instance) throws Exception {
        File dataFolder = instance.getDataFolder();
        Config configAnno = instance.getClass().getAnnotation(Config.class);
        if (configAnno == null)
            throw new IllegalArgumentException("Missing @Config annotation on " + instance.getClass().getSimpleName());

        if (!dataFolder.exists()) dataFolder.mkdirs();
        File file = new File(dataFolder, configAnno.value());

        // If the file doesn't exist, save default config and return
        if (!file.exists()) {
            save(instance);
            return instance;
        }

        Yaml yaml = new Yaml();
        Map<String, Object> data;
        try (InputStream in = new FileInputStream(file)) {
            data = yaml.load(in);
        }

        if (data == null) data = new LinkedHashMap<>();

        // Load config version from file
        int fileVersion = -1;
        int targetVersion = -1;
        Field versionField = null;

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigVersion.class)) {
                versionField = field;
                field.setAccessible(true);
                targetVersion = field.getInt(instance);
                Object value = getNested(data, getKey(field));
                if (value instanceof Number number) {
                    fileVersion = number.intValue();
                }
                break;
            }
        }

        if (fileVersion >= 0 && fileVersion < targetVersion) {
            for (IConfigMigration migration : instance.getMigrations()) {
                if (migration.fromVersion() >= fileVersion && migration.fromVersion() < targetVersion) {
                    migration.migrate(data);
                }
            }
            // Update version in data
            if (versionField != null)
                insertNested(data, getKey(versionField), targetVersion);
        }

        // Load all fields into memory
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;
            field.setAccessible(true);

            String key = getKey(field);
            Object rawValue = getNested(data, key);

            Class<?> type = field.getType();
            if (rawValue == null) {

                field.set(instance, null);

            } else if (parsers.containsKey(type)) {

                IConfigParser<?> parser = parsers.get(type);
                field.set(instance, parser.parse(rawValue));

            } else if (List.class.isAssignableFrom(type)) {
                // Check for a parser for the parameterized type in the List
                Type genericType = field.getGenericType();

                if (genericType instanceof ParameterizedType pt) {
                    Type itemType = pt.getActualTypeArguments()[0];

                    if (itemType instanceof Class<?> itemClass) {
                        IConfigParser<?> elementParser = parsers.get(itemClass);
                        if (elementParser != null) {
                            IConfigParser<?> listParser = new ListParser<>(elementParser);
                            Object parsed = listParser.parse(rawValue);
                            field.set(instance, parsed);
                            continue;
                        } else {
                            System.err.println("No parser found for list element type: " + itemClass.getName());
                        }
                    } else {
                        System.err.println("Unsupported list item type: " + itemType);
                    }
                }

            } else {
                field.set(instance, rawValue);
            }
        }

        // Save updated file if version mismatch or fields missing
        boolean needsUpdate = (fileVersion != targetVersion) || hasMissingFields(instance.getClass(), data);
        if (needsUpdate) {
            save(instance);
        }

        return instance;
    }


    public static <T extends BaseConfig> void save(T instance) throws Exception {
        File dataFolder = instance.getDataFolder();
        Class<?> clazz = instance.getClass();
        Config configAnno = clazz.getAnnotation(Config.class);
        if (configAnno == null) return;

        File file = new File(dataFolder, configAnno.value());
        file.getParentFile().mkdirs();

        Map<String, Object> data = new LinkedHashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;
            field.setAccessible(true);

            String key = getKey(field);
            Object value = field.get(instance);
            if (value == null) continue;

            Type genericType = field.getGenericType();
            Class<?> fieldType = field.getType();

            if (parsers.containsKey(fieldType)) {
                IConfigParser<Object> parser = (IConfigParser<Object>) parsers.get(fieldType);
                value = parser.serialize(value);

            } else if (List.class.isAssignableFrom(fieldType) && genericType instanceof ParameterizedType pt) {
                Type itemType = pt.getActualTypeArguments()[0];
                if (itemType instanceof Class<?> itemClass) {
                    IConfigParser<?> elementParser = parsers.get(itemClass);
                    if (elementParser != null) {
                        System.out.println("Using element parser for " + itemClass.getName());
                        value = serializeList((List<?>) value, elementParser);
                    } else {
                        System.err.println("No parser registered for list element type: " + itemClass.getName());
                    }

                }
            }

            insertNested(data, key, value);
        }

        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(opts);

        try (Writer writer = Files.newBufferedWriter(file.toPath())) {
            yaml.dump(data, writer);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Object serializeList(List<?> rawList, IConfigParser<T> elementParser) {
        ListParser<T> listParser = new ListParser<>(elementParser);
        return listParser.serialize((List<T>) rawList);
    }

    private static String getKey(Field field) {
        ConfigField configField = field.getAnnotation(ConfigField.class);
        return (configField != null && !configField.value().isEmpty())
                ? configField.value()
                : field.getName();
    }

    @SuppressWarnings("unchecked")
    private static void insertNested(Map<String, Object> root, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new LinkedHashMap<>());
        }
        current.put(parts[parts.length - 1], value);
    }

    private static Object getNested(Map<String, Object> root, String path) {
        String[] parts = path.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(part);
            if (current == null) return null;
        }
        return current;
    }

    private static boolean hasMissingFields(Class<?> clazz, Map<String, Object> data) {
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;
            String key = getKey(field);
            if (!containsNestedKey(data, key)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean containsNestedKey(Map<String, Object> root, String keyPath) {
        String[] parts = keyPath.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (!(current instanceof Map)) return false;
            Map<String, Object> map = (Map<String, Object>) current;
            if (!map.containsKey(part)) return false;
            current = map.get(part);
        }
        return true;
    }

}
