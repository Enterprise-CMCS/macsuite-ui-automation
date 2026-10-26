package com.macsuite.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for resolving user credentials based on role and environment.
 * Credentials are stored in .env file with format: {ROLE}_{ENV}_USERNAME and {ROLE}_{ENV}_PASSWORD
 */
public class CredentialProvider {
    private static final Logger logger = LoggerFactory.getLogger(CredentialProvider.class);

    /**
     * Data class to hold username and password
     */
    public static class Credentials {
        private final String username;
        private final String password;
        private final String totpSecret;

        public Credentials(String username, String password) {
            this(username, password, null);
        }

        public Credentials(String username, String password, String totpSecret) {
            this.username = username;
            this.password = password;
            this.totpSecret = totpSecret;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getTotpSecret() {
            return totpSecret;
        }

        @Override
        public String toString() {
            return "Credentials{username='" + username + "', password='***'}";
        }
    }

    /**
     * Get credentials for a specific role and environment.
     * Falls back to default APP_USERNAME and APP_PASSWORD if role-specific credentials not found.
     *
     * @param role        User role (e.g., "BCT", "SO", "TA")
     * @param environment Environment (e.g., "UAT", "QA", "SEI")
     * @return Credentials object with username and password
     * @throws IllegalStateException if no credentials found
     */
    public static Credentials getCredentials(String role, String environment) {
        String usernameKey = role.toUpperCase() + "_" + environment.toUpperCase() + "_USERNAME";
        String passwordKey = role.toUpperCase() + "_" + environment.toUpperCase() + "_PASSWORD";

        String username = EnvLoader.get(usernameKey);
        String password = EnvLoader.get(passwordKey);

        if (username != null && password != null) {
            logger.info("Using credentials for role: {} in environment: {}", role, environment);
            String totpSecret = EnvLoader.get(role.toUpperCase() + "_" + environment.toUpperCase()
                + "_TOTP_SECRET", null);
            if (totpSecret == null || totpSecret.isBlank()) {
                totpSecret = EnvLoader.get("SALESFORCE_TOTP_SECRET", null);
            }
            return new Credentials(username, password, totpSecret);
        }

        // Fallback to default credentials
        logger.warn("No credentials found for role: {} in environment: {}. Trying fallback credentials.", role, environment);
        username = EnvLoader.get("APP_USERNAME");
        password = EnvLoader.get("APP_PASSWORD");

        if (username != null && password != null) {
            logger.info("Using fallback APP_USERNAME and APP_PASSWORD");
            return new Credentials(username, password, EnvLoader.get("SALESFORCE_TOTP_SECRET", null));
        }

        throw new IllegalStateException(
                String.format("No credentials found for role '%s' in environment '%s'. " +
                              "Expected keys: %s and %s, or fallback APP_USERNAME and APP_PASSWORD",
                        role, environment, usernameKey, passwordKey)
        );
    }

    /**
     * Get credentials using default role and environment from TestConfig.
     *
     * @return Credentials object with username and password
     */
    public static Credentials getDefaultCredentials() {
        return getCredentials(TestConfig.getDefaultUser(), TestConfig.getDefaultEnvironment());
    }
}
