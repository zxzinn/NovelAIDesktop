package com.zxzinn.novelai.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ConfigManager {
    private static final String CONFIG_FILE = "application.yaml";
    private static ConfigManager instance;
    private final Map<String, Object> config;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    private ConfigManager() {
        config = loadConfiguration();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private Map<String, Object> loadConfiguration() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Yaml yaml = new Yaml();
            return yaml.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) cache.computeIfAbsent(key, k -> {
            String[] parts = k.split("\\.");
            Map<String, Object> current = config;
            for (int i = 0; i < parts.length - 1; i++) {
                current = (Map<String, Object>) current.get(parts[i]);
                if (current == null) {
                    return null;
                }
            }
            return current.get(parts[parts.length - 1]);
        });
    }

    public String getString(String key) {
        return getValue(key);
    }

    public Integer getInteger(String key) {
        return getValue(key);
    }

    public Double getDouble(String key) {
        return getValue(key);
    }

    public Boolean getBoolean(String key) {
        return getValue(key);
    }
}