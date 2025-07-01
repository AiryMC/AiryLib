package dev.airyy.AiryLib.core.config;

import dev.airyy.AiryLib.core.config.annotation.Config;
import dev.airyy.AiryLib.core.config.annotation.ConfigField;
import dev.airyy.AiryLib.core.config.parser.IConfigParser;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {

    private static final Map<Class<?>, IConfigParser<?>> parsers = new HashMap<>();

    public static <T> void registerParser(Class<T> clazz, IConfigParser<T> parser) {
        parsers.put(clazz, parser);
    }

    public static <T> T load(Class<T> clazz, File dataFolder) throws Exception {
        Config configAnno = clazz.getAnnotation(Config.class);
        if (configAnno == null) {
            throw new IllegalArgumentException("Missing @Config annotation on class '" + clazz.getSimpleName() + "'");
        }

        if (!dataFolder.exists()) dataFolder.mkdirs();

        File file = new File(dataFolder, configAnno.value());
        Yaml yaml = new Yaml();
        T instance = clazz.getDeclaredConstructor().newInstance();

        if (!file.exists()) {
            save(instance, dataFolder);
            return instance;
        }

        try (InputStream fileStream = new FileInputStream(file)) {
            Map<String, Object> data = yaml.load(fileStream);
            if (data == null) {
                data = new LinkedHashMap<>();
            }

            for (Field field : clazz.getDeclaredFields()) {
                ConfigField configField = field.getAnnotation(ConfigField.class);
                if (configField == null) continue;

                String key = !configField.value().isEmpty() ? configField.value() : field.getName();
                Object rawValue = getNested(data, key);

                if (rawValue != null || containsNestedKey(data, key)) {
                    field.setAccessible(true);
                    Class<?> type = field.getType();

                    if (parsers.containsKey(type)) {
                        IConfigParser<?> parser = parsers.get(type);
                        Object parsedValue = parser.parse(rawValue);
                        field.set(instance, parsedValue);
                    } else {
                        field.set(instance, rawValue);
                    }
                } else {
                    field.setAccessible(true);
                    field.set(instance, null);
                }
            }

            return instance;
        }
    }

    public static void save(Object configInstance, File dataFolder) throws Exception {
        Class<?> clazz = configInstance.getClass();
        Config configAnno = clazz.getAnnotation(Config.class);
        if (configAnno == null) return;

        if (!dataFolder.exists()) dataFolder.mkdirs();

        File file = new File(dataFolder, configAnno.value());
        Map<String, Object> data = new LinkedHashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            ConfigField configField = field.getAnnotation(ConfigField.class);
            if (configField == null) continue;

            String key = !configField.value().isEmpty() ? configField.value() : field.getName();
            field.setAccessible(true);
            Object fieldValue = field.get(configInstance);
            Class<?> type = field.getType();

            if (fieldValue == null)
                continue;

            if (parsers.containsKey(type)) {
                IConfigParser parser = parsers.get(type);
                Object serialized = parser.serialize(fieldValue);
                insertNested(data, key, serialized);
            } else {
                insertNested(data, key, fieldValue);
            }
        }

        // Save file to disk
        try (Writer writer = Files.newBufferedWriter(file.toPath())) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(data, writer);
        }
    }

    @SuppressWarnings("unchecked")
    private static void insertNested(Map<String, Object> root, String keyPath, Object value) {
        String[] parts = keyPath.split("\\.");
        Map<String, Object> current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            current = (Map<String, Object>) current.computeIfAbsent(part, k -> new LinkedHashMap<>());
        }

        current.put(parts[parts.length - 1], value);
    }

    @SuppressWarnings("unchecked")
    public static Object getNested(Map<String, Object> root, String keyPath) {
        String[] parts = keyPath.split("\\.");
        Object current = root;

        for (String part : parts) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(part);
            if (current == null) return null;
        }

        return current;
    }

    private static boolean containsNestedKey(Map<String, Object> root, String keyPath) {
        String[] parts = keyPath.split("\\.");
        Object current = root;

        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) return false;
            if (!map.containsKey(part)) return false;
            current = map.get(part);
        }

        return true;
    }

}
