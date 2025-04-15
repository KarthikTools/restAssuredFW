package com.restassured.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "application.properties";
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        try (InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warn("Unable to find {}", CONFIG_FILE);
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            logger.error("Error loading properties file", e);
        }
    }
    
    public static String getProperty(String key) {
        // First check system properties
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        
        // Then check environment variables
        value = System.getenv(key.replace('.', '_').toUpperCase());
        if (value != null) {
            return value;
        }
        
        // Finally check properties file
        return properties.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for property {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    public static long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid long value for property {}: {}", key, value);
            }
        }
        return defaultValue;
    }
} 