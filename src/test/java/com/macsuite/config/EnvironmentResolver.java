package com.macsuite.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for resolving environment-specific URLs.
 * URLs are stored in .env file with format: APP_URL_{ENV}
 */
public class EnvironmentResolver {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentResolver.class);

    /**
     * Get the application URL for a specific environment.
     * Falls back to default APP_URL from TestConfig if environment-specific URL not found.
     *
     * @param environment Environment name (e.g., "UAT", "QA", "SEI")
     * @return Application URL for the specified environment
     * @throws IllegalStateException if no URL found for environment
     */
    public static String getEnvironmentUrl(String environment) {
        String envKey = "APP_URL_" + environment.toUpperCase();
        String url = EnvLoader.get(envKey);

        if (url != null && !url.isEmpty()) {
            logger.info("Using URL for environment '{}': {}", environment, url);
            return url;
        }

        // Fallback to default APP_URL from TestConfig
        logger.warn("No URL found for environment '{}' (key: {}). Using default URL from TestConfig.", environment, envKey);
        url = TestConfig.getDefaultAppUrl();

        if (url != null && !url.isEmpty()) {
            logger.info("Using default URL: {}", url);
            return url;
        }

        throw new IllegalStateException(
                String.format("No URL configured for environment '%s'. Expected key: %s or default APP_URL", 
                        environment, envKey)
        );
    }

    /**
     * Get the default application URL.
     *
     * @return Default application URL from TestConfig
     */
    public static String getDefaultUrl() {
        return TestConfig.getDefaultAppUrl();
    }

    /**
     * Validate that a URL is properly configured for an environment.
     *
     * @param environment Environment name
     * @return true if URL is configured, false otherwise
     */
    public static boolean isUrlConfigured(String environment) {
        String envKey = "APP_URL_" + environment.toUpperCase();
        String url = EnvLoader.get(envKey);
        return url != null && !url.isEmpty();
    }
}
