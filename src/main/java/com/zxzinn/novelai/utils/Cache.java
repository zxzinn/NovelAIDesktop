package com.zxzinn.novelai.utils;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.util.Properties;

@Log4j2
public class Cache {
    private static final String CACHE_FILE = "novelai_cache.properties";
    private static Cache instance;
    private final Properties properties;
    private final File cacheFile;

    private Cache() {
        properties = new Properties();
        cacheFile = new File(System.getProperty("user.dir"), CACHE_FILE);
        loadCache();
    }

    public static synchronized Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    private void loadCache() {
        if (cacheFile.exists()) {
            try (InputStream input = new FileInputStream(cacheFile)) {
                properties.load(input);
                log.info("Cache loaded successfully from {}", cacheFile.getAbsolutePath());
            } catch (IOException e) {
                log.warn("Could not load cache file. A new one will be created.", e);
            }
        } else {
            log.info("Cache file does not exist. A new one will be created.");
        }
    }

    public void saveCache() {
        try (OutputStream output = new FileOutputStream(cacheFile)) {
            properties.store(output, "NovelAI Generator Cache");
            log.info("Cache saved successfully to {}", cacheFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Could not save cache file", e);
        }
    }

    public void setParameter(String key, String value) {
        properties.setProperty(key, value);
        log.debug("Set cache parameter: {} = {}", key, value);
    }

    public String getParameter(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        log.debug("Get cache parameter: {} = {}", key, value);
        return value;
    }

    public void setPrompt(String type, String prompt) {
        properties.setProperty(type + "Prompt", prompt);
    }

    public String getPrompt(String type, String defaultValue) {
        return properties.getProperty(type + "Prompt", defaultValue);
    }
}