package com.macsuite.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Configuration - Default settings for test execution
 * These are application defaults, not environment-specific secrets
 */
public class TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);
    
    // ===== Default Test Configuration =====
    
    /**
     * Default user role for testing
     * Options: BCT, SO, TA, PIER, PRA, HMH, VE, MFP, WFTCL, PRAREADONLY
     */
    public static final String DEFAULT_USER = "BCT";
    
    /**
     * Default environment for testing
     * Options: QA, UAT, SEI
     */
    public static final String DEFAULT_ENVIRONMENT = "UAT";
    
    /**
     * Default headless mode (false = show browser, true = headless)
     */
    public static final boolean DEFAULT_HEADLESS = false;
    
    /**
     * Default application URL (UAT)
     */
    public static final String DEFAULT_APP_URL = "https://cmsapps5--cmcsmcuat.sandbox.my.salesforce.com";
    
    /**
     * Slow mode delay in milliseconds (0 = disabled)
     * Can be overridden via -DSLOW_MODE system property
     */
    public static final int DEFAULT_SLOW_MODE_DELAY = 1500;
    
    /**
     * Default timeout for element waits (milliseconds)
     */
    public static final int DEFAULT_TIMEOUT = 30000;

    /**
     * Short timeout for quick assertions (milliseconds)
     */
    public static final int SHORT_TIMEOUT = 5000;

    /**
     * Medium timeout for moderately slow operations (milliseconds)
     */
    public static final int MEDIUM_TIMEOUT = 10000;
    
    /**
     * Screenshot capture mode.
     * "error"       — attach a screenshot only on scenario failure (lean reports)
     * "interaction" — attach screenshots at named checkpoints in step definitions
     * "step"        — attach a screenshot after every Cucumber step (verbose)
     * Can be overridden via -DSCREENSHOT_MODE system property
     */
    public static final String DEFAULT_SCREENSHOT_MODE = "interaction";

    /**
     * Highlight elements before interaction (for debugging/visibility)
     * Set to true to see which elements are being interacted with
     */
    public static final boolean HIGHLIGHT_ELEMENTS = true;
    
    /**
     * Highlight duration in milliseconds
     */
    public static final int HIGHLIGHT_DURATION = 200;
    
    /**
     * Get screenshot mode. Returns "step" or "error".
     * Can be overridden via -DSCREENSHOT_MODE system property
     */
    public static String getScreenshotMode() {
        return System.getProperty("SCREENSHOT_MODE", DEFAULT_SCREENSHOT_MODE).toLowerCase();
    }

    /**
     * Get element highlighting setting
     * Can be overridden via -DHIGHLIGHT_ELEMENTS system property
     */
    public static boolean isHighlightEnabled() {
        String highlight = System.getProperty("HIGHLIGHT_ELEMENTS");
        if (highlight != null) {
            return Boolean.parseBoolean(highlight);
        }
        return HIGHLIGHT_ELEMENTS;
    }
    
    /**
     * Get headless mode setting
     * Can be overridden via -DHEADLESS system property or HEADLESS env var
     */
    public static boolean isHeadless() {
        String headless = System.getProperty("HEADLESS", System.getenv("HEADLESS"));
        if (headless != null) {
            return Boolean.parseBoolean(headless);
        }
        return DEFAULT_HEADLESS;
    }
    
    /**
     * Get default application URL
     * Can be overridden via -DAPP_URL system property or APP_URL env var
     */
    public static String getDefaultAppUrl() {
        String url = System.getProperty("APP_URL", System.getenv("APP_URL"));
        return (url != null && !url.isEmpty()) ? url : DEFAULT_APP_URL;
    }
    
    /**
     * Get the default user role
     * Can be overridden via -DDEFAULT_USER system property
     */
    public static String getDefaultUser() {
        return System.getProperty("DEFAULT_USER", DEFAULT_USER);
    }
    
    /**
     * Get the default environment
     * Can be overridden via -DDEFAULT_ENVIRONMENT system property
     */
    public static String getDefaultEnvironment() {
        return System.getProperty("DEFAULT_ENVIRONMENT", DEFAULT_ENVIRONMENT);
    }
    
    /**
     * Get slow mode delay
     * Can be overridden via -DSLOW_MODE system property
     */
    public static int getSlowModeDelay() {
        try {
            return Integer.parseInt(System.getProperty("SLOW_MODE", String.valueOf(DEFAULT_SLOW_MODE_DELAY)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid SLOW_MODE value '{}', using default: {}", System.getProperty("SLOW_MODE"), DEFAULT_SLOW_MODE_DELAY);
            return DEFAULT_SLOW_MODE_DELAY;
        }
    }
    
    /**
     * Get default timeout
     * Can be overridden via -DDEFAULT_TIMEOUT system property
     */
    public static int getDefaultTimeout() {
        try {
            return Integer.parseInt(System.getProperty("DEFAULT_TIMEOUT", String.valueOf(DEFAULT_TIMEOUT)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid DEFAULT_TIMEOUT value '{}', using default: {}", System.getProperty("DEFAULT_TIMEOUT"), DEFAULT_TIMEOUT);
            return DEFAULT_TIMEOUT;
        }
    }
    
    /**
     * Log current configuration
     */
    public static void logConfig() {
        logger.info("=== Test Configuration ===");
        logger.info("Default User: {}", getDefaultUser());
        logger.info("Default Environment: {}", getDefaultEnvironment());
        logger.info("Headless Mode: {}", isHeadless());
        logger.info("Default App URL: {}", getDefaultAppUrl());
        logger.info("Slow Mode Delay: {}ms", getSlowModeDelay());
        logger.info("Default Timeout: {}ms", getDefaultTimeout());
        logger.info("==========================");
    }
}
