package dev.airyy.AiryLib.core.config;

import dev.airyy.AiryLib.core.config.annotation.Config;
import dev.airyy.AiryLib.core.config.annotation.ConfigField;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {

    public static <T> T load(Class<T> clazz) throws Exception {
        Config configAnno = clazz.getAnnotation(Config.class);
        if (configAnno == null) {
            throw new IllegalArgumentException("Missing @Config annotation on class'" + clazz.getSimpleName() + "'");
        }

        File file = new File(configAnno.value());
        Yaml yaml = new Yaml();
        T instance = clazz.getDeclaredConstructor().newInstance();

        // If file does not exist create one
        if (!file.exists()) {
            save(instance);
            return instance;
        }

        // Now load from the disk file
        try (InputStream fileStream = new FileInputStream(file)) {
            Map<String, Object> data = yaml.load(fileStream);
            if (data == null) {
                data = new LinkedHashMap<>();
            }

            for (Field field : clazz.getDeclaredFields()) {
                ConfigField configField = field.getAnnotation(ConfigField.class);
                if (configField == null) continue;

                String key = !configField.value().isEmpty() ? configField.value() : field.getName();
                if (data.containsKey(key)) {
                    field.setAccessible(true);
                    field.set(instance, data.get(key));
                }
            }

            return instance;
        }
    }

    public static void save(Object configInstance) throws Exception {
        Class<?> clazz = configInstance.getClass();
        Config configAnno = clazz.getAnnotation(Config.class);
        if (configAnno == null) return;

        File file = new File(configAnno.value());
        Map<String, Object> data = new LinkedHashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            ConfigField configField = field.getAnnotation(ConfigField.class);
            if (configField == null) continue;

            String key = !configField.value().isEmpty() ? configField.value() : field.getName();
            field.setAccessible(true);
            Object value = field.get(configInstance);
            data.put(key, value);
        }

        // Save file to disk
        try (Writer writer = Files.newBufferedWriter(file.toPath())) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(data, writer);
        }
    }
}
