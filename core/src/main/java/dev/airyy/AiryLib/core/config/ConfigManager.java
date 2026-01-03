package dev.airyy.AiryLib.core.config;

import dev.airyy.AiryLib.core.config.annotation.Config;
import dev.airyy.AiryLib.core.config.annotation.ConfigField;
import dev.airyy.AiryLib.core.config.annotation.ConfigVersion;
import dev.airyy.AiryLib.core.config.migration.IConfigMigration;
import dev.airyy.AiryLib.core.config.parser.IConfigParser;
import dev.airyy.AiryLib.core.config.parser.ListParser;
import dev.airyy.AiryLib.core.config.parser.MapParser;
import dev.airyy.AiryLib.core.config.parser.SetParser;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

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
        Class<?> clazz = instance.getClass();

        File dataFolder = instance.getDataFolder();
        Config configAnno = instance.getClass().getAnnotation(Config.class);
        if (configAnno == null)
            throw new IllegalArgumentException("Missing @Config annotation on " + instance.getClass().getSimpleName());

        File file = setupConfigFile(instance, configAnno);

        YamlDocument config = loadYamlDocument(file, configAnno.value());

        boolean migrated = handleMigrations(instance, config);

        injectConfigFields(instance, config);

        // Save updated file if version mismatch or missing fields detected
        if (migrated || hasMissingFields(clazz, config)) {
            save(instance);
        }

        return instance;
    }

    private static <T extends BaseConfig> void injectConfigFields(T instance, YamlDocument config) throws IllegalAccessException {
        // Load all annotated config fields into instance
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class))
                continue;

            field.setAccessible(true);
            String key = getKey(field);
            Object rawValue = getNested(config, key);

            if (rawValue instanceof Section section) {
                rawValue = section.getStringRouteMappedValues(false);
            }

            if (rawValue == null) {
                setDefaultValue(instance, field);
                continue;
            }

            IConfigParser<?> parser = getParserForType(field.getGenericType());

            if (parser != null) {
                field.set(instance, parser.parse(rawValue));
            } else {
                field.set(instance, rawValue);
            }
        }
    }

    private static <T extends BaseConfig> void setDefaultValue(T instance, Field field) throws IllegalAccessException {
        if (List.class.isAssignableFrom(field.getType())) {
            field.set(instance, new ArrayList<>());
        } else if (Map.class.isAssignableFrom(field.getType())) {
            field.set(instance, new HashMap<>());
        } else {
            field.set(instance, null);
        }
    }

    private static <T extends BaseConfig> boolean handleMigrations(T instance, YamlDocument config) {
        Field versionField = getVersionField(instance.getClass());
        if (versionField == null)
            return false; // No versioning used

        try {
            versionField.setAccessible(true);
            int targetVersion = versionField.getInt(instance);
            int fileVersion = config.getInt(getKey(versionField), -1);

            if (fileVersion >= 0 && fileVersion < targetVersion) {
                System.out.println("Migrating config from v" + fileVersion + " to v" + targetVersion);
                for (IConfigMigration migration : instance.getMigrations()) {
                    if (migration.fromVersion() >= fileVersion && migration.fromVersion() < targetVersion) {
                        migration.migrate(config);
                    }
                }
                // Update version in config object
                insertNested(config, getKey(versionField), targetVersion);
                config.save();
                return true;
            }
        } catch (IllegalAccessException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Field getVersionField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigVersion.class)) {
                return field;
            }
        }
        return null;
    }

    private static <T extends BaseConfig> File setupConfigFile(T instance, Config configAnno) throws Exception {
        File dataFolder = instance.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File file = new File(dataFolder, configAnno.value());

        // Copy default if it doesn't exist
        if (!file.exists()) {
            String resourcePath = configAnno.value();
            System.out.println("Creating config from bundled default: " + resourcePath);
            try {
                copyDefaultFromResources(file, resourcePath);
            } catch (IOException e) {
                System.err.println("Could not copy default config. Saving blank fallback.");
                save(instance);
            }
        }
        return file;
    }

    private static YamlDocument loadYamlDocument(File file, String resourcePath) throws IOException {
        InputStream defaultStream = ConfigManager.class.getClassLoader().getResourceAsStream(resourcePath);
        return (defaultStream != null)
                ? YamlDocument.create(file, defaultStream)
                : YamlDocument.create(file);
    }

    public static <T extends BaseConfig> void save(T instance) throws Exception {
        Class<?> clazz = instance.getClass();
        Config configAnno = clazz.getAnnotation(Config.class);
        if (configAnno == null)
            return;

        File file = prepareFile(instance.getDataFolder(), configAnno.value());
        file.getParentFile().mkdirs();

        // Load existing document or create new one
        YamlDocument config = YamlDocument.create(file);

        updateDocumentFields(instance, config);

        config.save();
    }

    private static <T extends BaseConfig> void updateDocumentFields(T instance, YamlDocument config) throws IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class))
                continue;

            field.setAccessible(true);
            String key = getKey(field);
            Object rawValue = field.get(instance);

            if (rawValue == null) {
                config.remove(key); // Explicitly remove
            } else {
                Object serializedValue = serializeValue(field, rawValue);
                config.set(key, serializedValue);
            }
        }
    }

    private static File prepareFile(File dataFolder, String filePath) {
        File file = new File(dataFolder, filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    private static Object serializeValue(Field field, Object value) {
        if (value == null)
            return null;

        IConfigParser<Object> parser = (IConfigParser<Object>) getParserForType(field.getGenericType());

        if (parser != null) {
            return parser.serialize(value);
        }

        // If no parser exists, let the YAML engine try to handle it
        return value;
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
            if (!field.isAnnotationPresent(ConfigField.class))
                continue;

            String key = getKey(field);
            if (!containsNestedKey(config, key))
                return true;
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

    private static IConfigParser<?> getParserForType(Type type) {
        // Handle simple classes
        if (type instanceof Class<?> clazz) {
            return parsers.get(clazz);
        }

        // Handle generics
        if (type instanceof ParameterizedType pt) {
            Class<?> rawType = (Class<?>) pt.getRawType();
            Type[] args = pt.getActualTypeArguments();

            // Handle lists
            if (List.class.isAssignableFrom(rawType)) {
                IConfigParser<?> elementParser = getParserForType(args[0]);
                return (elementParser != null) ? new ListParser<>(elementParser) : null;
            }

            if (Set.class.isAssignableFrom(rawType)) {
                IConfigParser<?> elementParser = getParserForType(args[0]);
                return (elementParser != null) ? new SetParser<>(elementParser) : null;
            }

            // Handle maps
            if (Map.class.isAssignableFrom(rawType)) {
                IConfigParser<?> keyParser = getParserForType(args[0]);
                IConfigParser<?> valueParser = getParserForType(args[1]);
                return (keyParser != null && valueParser != null)
                        ? new MapParser<>(keyParser, valueParser) : null;
            }
        }

        return null;
    }
}
