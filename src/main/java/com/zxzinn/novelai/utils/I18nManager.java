package com.zxzinn.novelai.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Log4j2
public class I18nManager {
    private static final String JSON_FILE_BASE_NAME = "lang/";
    private static Map languageMap;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        loadLanguage(Locale.getDefault());
    }

    public static void loadLanguage(Locale locale) {
        String languageTag = locale.getLanguage() + "_" + locale.getCountry().toLowerCase();
        String jsonFileName = JSON_FILE_BASE_NAME + languageTag + ".json";

        try (InputStream inputStream = I18nManager.class.getClassLoader().getResourceAsStream(jsonFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Language file not found: " + jsonFileName);
            }
            languageMap = objectMapper.readValue(inputStream, Map.class);
            log.info("Loaded language file: {}", jsonFileName);
        } catch (Exception e) {
            log.error("Failed to load language file: {}. Error: {}", jsonFileName, e.getMessage());
            loadFallbackLanguage();
        }
    }

    private static void loadFallbackLanguage() {
        try (InputStream inputStream = I18nManager.class.getClassLoader().getResourceAsStream(JSON_FILE_BASE_NAME + "en_us.json")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Fallback language file not found");
            }
            languageMap = objectMapper.readValue(inputStream, Map.class);
            log.info("Loaded fallback language file: en_us.json");
        } catch (Exception e) {
            log.error("Failed to load fallback language file. Error: {}", e.getMessage());
            languageMap = new HashMap<>();
        }
    }

    public static String getString(String key) {
        String value = (String) languageMap.get(key);
        if (value == null) {
            log.warn("Missing language key: {}", key);
            return "!" + key + "!";
        }
        return value;
    }

    public static String getString(String key, Object... args) {
        String value = getString(key);
        try {
            return MessageFormat.format(value, args);
        } catch (Exception e) {
            log.error("Error formatting string for key: {}. Error: {}", key, e.getMessage());
            return value;
        }
    }
}