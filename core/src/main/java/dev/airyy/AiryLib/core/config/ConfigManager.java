package dev.airyy.AiryLib.core.config;

import dev.airyy.AiryLib.core.config.annotation.Config;
import dev.airyy.AiryLib.core.config.annotation.ConfigField;
import dev.airyy.AiryLib.core.config.annotation.ConfigVersion;
import dev.airyy.AiryLib.core.config.migration.IConfigMigration;
import dev.airyy.AiryLib.core.config.parser.IConfigParser;
import dev.airyy.AiryLib.core.config.parser.ListParser;
import dev.dejvokep.boostedyaml.YamlDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
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

        // If the file doesn't exist, copy default config from resources (with comments preserved)
        if (!file.exists()) {
            String resourcePath = configAnno.value(); // path inside resources (e.g. "defaults/config.yml")
            System.out.println("Creating config from bundled default: " + resourcePath);

            try {
                copyDefaultFromResources(file, resourcePath);
                System.out.println("Copied default config from resources: " + resourcePath);
            } catch (IOException e) {
                System.err.println("Could not copy default config from resources, saving blank config instead.");
                save(instance); // fallback if default not found
            }
        }

        // Load YAML data from the config file using BoostedYAML (preserves comments)
        InputStream defaultStream = ConfigManager.class.getClassLoader()
                .getResourceAsStream(configAnno.value());

        YamlDocument config;
        if (defaultStream != null) {
            config = YamlDocument.create(file, defaultStream);
        } else {
            config = YamlDocument.create(file);
        }

        // Load config version from file
        int fileVersion = -1;
        int targetVersion = -1;
        Field versionField = null;

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigVersion.class)) {
                versionField = field;
                field.setAccessible(true);
                targetVersion = field.getInt(instance);
                Object value = getNested(config, getKey(field));
                if (value instanceof Number number) {
                    fileVersion = number.intValue();
                }
                break;
            }
        }

        // Perform migrations if needed
        if (fileVersion >= 0 && fileVersion < targetVersion) {
            for (IConfigMigration migration : instance.getMigrations()) {
                if (migration.fromVersion() >= fileVersion && migration.fromVersion() < targetVersion) {
                    migration.migrate(config);
                }
            }
            insertNested(config, getKey(versionField), targetVersion);
            config.save();
        }

        // Load all annotated config fields into instance
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;
            field.setAccessible(true);

            String key = getKey(field);
            Object rawValue = getNested(config, key);
            Class<?> type = field.getType();

            if (rawValue == null) {
                field.set(instance, null); // or skip to keep default Java values if you prefer
            } else if (parsers.containsKey(type)) {
                IConfigParser<?> parser = parsers.get(type);
                field.set(instance, parser.parse(rawValue));
            } else if (List.class.isAssignableFrom(type)) {
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

        // Save updated file if version mismatch or missing fields detected
        boolean needsUpdate = (fileVersion != targetVersion) || hasMissingFields(instance.getClass(), config);
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

        // Load existing document or create new one
        YamlDocument config;
        if (file.exists()) {
            config = YamlDocument.create(file);
        } else {
            // Optionally load from resource or create empty document
            config = YamlDocument.create(file);
        }

        // Set all annotated fields into the document
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;
            field.setAccessible(true);

            String key = getKey(field);
            Object value = field.get(instance);
            if (value == null) {
                config.set(key, null); // Clear key if null
                continue;
            }

            Type genericType = field.getGenericType();
            Class<?> fieldType = field.getType();

            if (parsers.containsKey(fieldType)) {
                IConfigParser<Object> parser = (IConfigParser<Object>) parsers.get(fieldType);
                value = parser.serialize(value);
                config.set(key, value);

            } else if (List.class.isAssignableFrom(fieldType) && genericType instanceof ParameterizedType pt) {
                Type itemType = pt.getActualTypeArguments()[0];
                if (itemType instanceof Class<?> itemClass) {
                    IConfigParser<?> elementParser = parsers.get(itemClass);
                    if (elementParser != null) {
                        Object serializedList = serializeList((List<?>) value, elementParser);
                        config.set(key, serializedList);
                    } else {
                        System.err.println("No parser registered for list element type: " + itemClass.getName());
                        config.set(key, value); // fallback
                    }
                } else {
                    config.set(key, value); // fallback
                }
            } else {
                config.set(key, value);
            }
        }

        // Save document - this preserves comments and formatting
        config.save();
    }


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

    private static void insertNested(YamlDocument config, String path, Object value) {
        config.set(path, value);
    }

    private static Object getNested(YamlDocument config, String path) {
        return config.get(path);
    }

    private static boolean hasMissingFields(Class<?> clazz, YamlDocument config) {
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;
            String key = getKey(field);
            if (!containsNestedKey(config, key)) return true;
        }
        return false;
    }

    private static boolean containsNestedKey(YamlDocument config, String path) {
        return config.get(path) != null;
    }

    private static void copyDefaultFromResources(File destination, String resourcePath) throws IOException {
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Default config resource not found: " + resourcePath);
            }
            destination.getParentFile().mkdirs();
            Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
