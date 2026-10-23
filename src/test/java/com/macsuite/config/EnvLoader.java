package com.macsuite.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads environment variables from .env file
 * This should be called before any test execution
 */
public class EnvLoader {
    private static final Logger logger = LoggerFactory.getLogger(EnvLoader.class);
    private static Dotenv dotenv;
    private static boolean loaded = false;

    public static void load() {
        if (loaded) {
            return;
        }

        try {
            dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();

            // Dynamically load ALL keys from .env file as system properties
            // This eliminates the need to maintain a hard-coded list
            if (dotenv != null) {
                int count = 0;
                for (var entry : dotenv.entries()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value != null && !value.isEmpty()) {
                        System.setProperty(key, value);
                        logger.debug("Set property from .env: {}", key);
                        count++;
                    }
                }
                logger.info("Loaded {} environment variables from .env file", count);
            } else {
                logger.warn(".env file not found or empty");
            }

            logger.info("Environment variables loaded successfully");
            loaded = true;
        } catch (Exception e) {
            logger.warn("Failed to load .env file: {}", e.getMessage());
            dotenv = null;
        }
    }

    /**
     * Get an environment variable with fallback
     * Checks System.getenv(), System.getProperty(), and .env file
     */
    public static String get(String key, String defaultValue) {
        load();
        
        // Try system environment variable first
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        
        // Try system property
        value = System.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        
        // Try dotenv object
        if (dotenv != null) {
            value = dotenv.get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        
        return defaultValue;
    }

    /**
     * Get an environment variable (required)
     */
    public static String get(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable not found: " + key);
        }
        return value;
    }
}



