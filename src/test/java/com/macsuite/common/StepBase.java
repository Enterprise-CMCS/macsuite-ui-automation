package com.macsuite.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.config.EnvLoader;
import com.macsuite.core.PlaywrightManager;
import com.microsoft.playwright.Page;

/**
 * Base class for step definitions.
 * Provides common utilities and access to PlaywrightManager without requiring Cucumber hooks injection.
 */
public class StepBase {
    protected static final Logger logger = LoggerFactory.getLogger(StepBase.class);
    
    /**
     * Get the current Page instance
     */
    protected Page page() {
        return PlaywrightManager.page();
    }
    
    /**
     * Navigate to a URL
     */
    protected void navigate(String url) {
        logger.info("Navigating to: {}", url);
        page().navigate(url);
    }

    /**
     * Get environment variable with fallback chain
     * @param key The environment variable key
     * @param defaultValue The default value if not found
     * @return The environment variable value
     */
    protected String getEnvVar(String key, String defaultValue) {
        return EnvLoader.get(key, defaultValue);
    }

    /**
     * Take a screenshot for debugging (always saves to disk).
     */
    // protected void takeScreenshot(String name) {
    //     PlaywrightManager.takeScreenshot(name);
    // }

    /**
     * Take a named interaction screenshot (attached to report + saved to disk).
     * Only fires when SCREENSHOT_MODE is "interaction" or "step".
     */
    protected void screenshot(String label) {
        ScreenshotHelper.take(label);
    }

    /**
     * Log the current URL
     */
    protected String currentUrl() {
        return page().url();
    }

    /**
     * Log the page title
     */
    protected String pageTitle() {
        return page().title();
    }




}
