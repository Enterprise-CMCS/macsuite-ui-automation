package com.macsuite.access_control.pages;

import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Page Object for general app dashboard operations and access control validation.
 * Works across all Salesforce apps (BCT, State Operations, etc.)
 */
public class AppDashboardPage {
    private static final Logger logger = LoggerFactory.getLogger(AppDashboardPage.class);
    private final Page page;

    public AppDashboardPage(Page page) {
        this.page = page;
    }

    /**
     * Verify app name is displayed (flexible multi-strategy approach for dashboards)
     */
    public void verifyAppNameDisplayed(String expectedAppName) throws Exception {
        logger.info("Verifying app name: {}", expectedAppName);

        // Wait for page to stabilize after navigation/login
        
        logger.info("Page loaded, attempting app name verification...");
        
        // Strategy 1: Check by title attribute anywhere on page (handles HTML entities)
        try {
            Locator titleLocator = page.getByTitle(expectedAppName);
            if (titleLocator.count() > 0 && titleLocator.first().isVisible()) {
                logger.info("✓ App name verified (by title attribute)");
                return;
            }
        } catch (Exception e) {
            logger.debug("Title attribute match failed: {}", e.getMessage());
        }
        
        // Strategy 2: Try standard Salesforce page header (if it exists)
        try {
            Locator headerElement = page.locator(".slds-page-header__title").first();
            if (headerElement.isVisible()) {
                String actualText = headerElement.textContent();
                logger.info("Found standard header with text: '{}'", actualText);
                
                if (actualText.contains(expectedAppName)) {
                    logger.info("✓ App name verified (in standard header)");
                    return;
                }
                
                // Check partial match
                if (matchesPartially(expectedAppName, actualText)) {
                    logger.info("✓ App name verified (partial match in header)");
                    return;
                }
            }
        } catch (Exception e) {
            logger.debug("Standard header not found or not visible: {}", e.getMessage());
        }
        
        // Strategy 3: Check any h1 or h2 headers
        try {
            Locator headerLocator = page.locator("h1, h2").filter(
                new Locator.FilterOptions().setHasText(expectedAppName)
            );
            if (headerLocator.count() > 0 && headerLocator.first().isVisible()) {
                logger.info("✓ App name verified (in h1/h2 header)");
                return;
            }
        } catch (Exception e) {
            logger.debug("H1/H2 header match failed: {}", e.getMessage());
        }
        
        // Strategy 4: Check page title/document title
        try {
            String pageTitle = page.title();
            logger.info("Page title: '{}'", pageTitle);
            
            if (pageTitle.contains(expectedAppName)) {
                logger.info("✓ App name verified (in page title)");
                return;
            }
            
            if (matchesPartially(expectedAppName, pageTitle)) {
                logger.info("✓ App name verified (partial match in page title)");
                return;
            }
        } catch (Exception e) {
            logger.debug("Page title check failed: {}", e.getMessage());
        }
        
        // Strategy 5: Broad text search - check if text exists anywhere on visible page
        try {
            Locator textLocator = page.getByText(expectedAppName, new Page.GetByTextOptions().setExact(false));
            if (textLocator.count() > 0) {
                logger.info("✓ App name verified (found in page text)");
                return;
            }
        } catch (Exception e) {
            logger.debug("Page text search failed: {}", e.getMessage());
        }
        
        // Strategy 6: Check for partial match anywhere on page
        try {
            String[] keywords = extractKeywords(expectedAppName);
            for (String keyword : keywords) {
                Locator keywordLocator = page.getByText(keyword, new Page.GetByTextOptions().setExact(false));
                if (keywordLocator.count() == 0) {
                    logger.debug("Keyword '{}' not found on page", keyword);
                    throw new AssertionError("Required keyword '" + keyword + "' from app name not found");
                }
            }
            logger.info("✓ App name verified (all keywords found on page)");
            return;
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            logger.debug("Keyword search failed: {}", e.getMessage());
        }
        
        // All strategies failed - provide detailed error
        logger.error("❌ App name verification failed after all strategies");
        logger.error("Expected: '{}'", expectedAppName);
        logger.error("Page title: '{}'", page.title());
        
        throw new AssertionError("App name '" + expectedAppName + "' not found on page after trying multiple strategies");
    }
    
    /**
     * Check if actual text contains key parts of expected text (handles special characters)
     */
    private boolean matchesPartially(String expected, String actual) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String[] words = expected.split("\\s+");
        for (String word : words) {
            // Skip special characters and short words
            if (word.equals("&") || word.equals("and") || word.length() < 3) {
                continue;
            }
            if (!actual.contains(word)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Extract meaningful keywords from app name (filters out common words and special chars)
     */
    private String[] extractKeywords(String appName) {
        List<String> keywords = new ArrayList<>();
        String[] words = appName.split("\\s+");
        
        for (String word : words) {
            // Skip short words, special characters, and common words
            if (word.length() >= 4 && 
                !word.equals("&") && 
                !word.equalsIgnoreCase("and") &&
                !word.equalsIgnoreCase("the")) {
                keywords.add(word);
            }
        }
        
        return keywords.toArray(new String[0]);
    }

    /**
     * Get list of available modules/apps visible in app switcher
     */
    public List<String> getAvailableModules() throws Exception {
        logger.info("Getting available modules");
        
        List<String> modules = new ArrayList<>();

        // Try to open app switcher
        try {
            this.page.locator("button[title='App Launcher']").click();

            // P1: Try to get apps from "All Items" or "All Apps" section
            try {
                Locator allItemsSection = page.locator("h2:has-text('All Items'), h2:has-text('All Apps')");
                if (allItemsSection.count() > 0) {
                    // Get siblings or next elements after the header
                    Locator appLinks = page.locator("h2:has-text('All Items') ~ ul a, h2:has-text('All Apps') ~ ul a");
                    List<Locator> items = appLinks.all();
                    
                    for (Locator item : items) {
                        String text = item.textContent();
                        if (text != null && !text.trim().isEmpty()) {
                            modules.add(text.trim());
                        }
                    }
                    
                    if (!modules.isEmpty()) {
                        modules = filterSystemItems(modules);
                        logger.info("Found {} custom modules from 'All Items' section", modules.size());
                        return modules;
                    }
                }
            } catch (Exception e) {
                logger.debug("'All Items' section not found: {}", e.getMessage());
            }

            // P2: Get by specific app launcher selectors
            try {
                List<Locator> items = page.locator("one-app-launcher-menu-item a, one-app-launcher-app-tile a").all();
                for (Locator item : items) {
                    String text = item.textContent();
                    if (text != null && !text.trim().isEmpty()) {
                        modules.add(text.trim());
                    }
                }
                
                if (!modules.isEmpty()) {
                    modules = filterSystemItems(modules);
                    logger.info("Found {} custom modules via app launcher selectors", modules.size());
                    return modules;
                }
            } catch (Exception e) {
                logger.debug("App launcher selectors failed: {}", e.getMessage());
            }

            // P3: Get by data attributes (app tabs)
            try {
                List<Locator> items = page.locator("a[data-target-selection-name*='Tab']").all();
                for (Locator item : items) {
                    String text = item.textContent();
                    if (text != null && !text.trim().isEmpty()) {
                        modules.add(text.trim());
                    }
                }
                
                if (!modules.isEmpty()) {
                    modules = filterSystemItems(modules);
                    logger.info("Found {} custom modules via data attributes", modules.size());
                    return modules;
                }
            } catch (Exception e) {
                logger.debug("Data attribute selectors failed: {}", e.getMessage());
            }

            // P4: Fallback - get all links and filter aggressively
            try {
                List<Locator> items = new ArrayList<>();
                items.addAll(page.locator("role=listitem a").all());
                items.addAll(page.locator("role=option a").all());
                
                for (Locator item : items) {
                    String text = item.textContent();
                    if (text != null && !text.trim().isEmpty()) {
                        modules.add(text.trim());
                    }
                }
                
                if (!modules.isEmpty()) {
                    modules = filterSystemItems(modules);
                    logger.info("Found {} custom modules via fallback", modules.size());
                    return modules;
                }
            } catch (Exception e) {
                logger.debug("Fallback selectors failed: {}", e.getMessage());
            }

            logger.info("Found {} modules total after filtering", modules.size());
            return modules;

        } catch (Exception e) {
            logger.warn("Error getting modules from app switcher", e);
            return modules;
        }
    }
    
    /**
     * Filter out standard Salesforce items and system elements
     */
    private List<String> filterSystemItems(List<String> modules) {
        List<String> filtered = new ArrayList<>();
        
        // Common system/standard items to exclude
        Set<String> excludeKeywords = new HashSet<>(Arrays.asList(
            "favorites", "favorite", "global actions", "guidance", "help", "notifications",
            "view profile", "profile", "home", "chatter", "search", "setup",
            "accounts list", "contacts list", "contracts list", "opportunities list",
            "leads list", "reports list", "dashboards list", "files", "tasks", "calendar",
            "this item doesn't support", "see all", "more items"
        ));
        
        for (String module : modules) {
            String moduleLower = module.toLowerCase().trim();
            
            // Skip empty or very short names
            if (moduleLower.length() < 3) {
                continue;
            }
            
            // Skip if contains digits at the start (like "0Notifications", "0Favorites")
            if (moduleLower.matches("^\\d.*")) {
                continue;
            }
            
            // Skip if it's a duplicate concatenated text (like "ContractsContracts List")
            if (moduleLower.matches("^(\\w+)\\1.*")) {
                continue;
            }
            
            // Skip if it contains excluded keywords
            boolean shouldExclude = false;
            for (String keyword : excludeKeywords) {
                if (moduleLower.contains(keyword)) {
                    shouldExclude = true;
                    break;
                }
            }
            
            if (!shouldExclude) {
                filtered.add(module);
            }
        }
        
        return filtered;
    }

    /**
     * Get user role indicator text (e.g., "Viewing as BCT_QA" or "Viewing as SEI Admin")
     */
    public String getUserRoleIndicator() {
        logger.info("Getting user role indicator");
        
        // Close App Launcher if it's open (it might be covering the header)
        try {
            Locator appLauncherModal = page.locator(".appLauncher, [class*='appLauncher'], one-app-launcher-modal");
            if (appLauncherModal.count() > 0 && appLauncherModal.first().isVisible()) {
                logger.debug("App Launcher is open, closing it...");
                // Press Escape to close any open modal
                page.keyboard().press("Escape");
            }
        } catch (Exception e) {
            logger.debug("Could not check/close App Launcher: {}", e.getMessage());
        }
        
        // Wait for page to be fully loaded
        
        // Try multiple strategies to find the user role indicator
        
        // Strategy 1: Direct .runningUserName selector
        try {
            logger.debug("Strategy 1: Trying .runningUserName selector...");
            Locator runningUserLocator = page.locator(".runningUserName");
            runningUserLocator.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            
            String text = runningUserLocator.textContent().trim();
            logger.info("✓ Found user role indicator: {}", text);
            return text;
        } catch (Exception e) {
            logger.debug("Strategy 1 failed: {}", e.getMessage());
        }
        
        // Strategy 2: Look for "Viewing as" text anywhere
        try {
            logger.debug("Strategy 2: Searching for 'Viewing as' text...");
            Locator viewingAsLocator = page.getByText("Viewing as", new Page.GetByTextOptions().setExact(false));
            if (viewingAsLocator.count() > 0) {
                String text = viewingAsLocator.first().textContent().trim();
                logger.info("✓ Found user role indicator: {}", text);
                return text;
            }
        } catch (Exception e) {
            logger.debug("Strategy 2 failed: {}", e.getMessage());
        }
        
        // Strategy 3: Check all text content for "Viewing as" pattern
        try {
            logger.debug("Strategy 3: Using JavaScript to search page text...");
            Object result = page.evaluate("() => { " +
                "const text = document.body.innerText; " +
                "const match = text.match(/Viewing as [^\\n]+/); " +
                "return match ? match[0] : null; " +
            "}");
            
            if (result != null) {
                String text = result.toString().trim();
                logger.info("✓ Found user role indicator via JS: {}", text);
                return text;
            }
        } catch (Exception e) {
            logger.debug("Strategy 3 failed: {}", e.getMessage());
        }
        
        // Debug: Log what we can find
        try {
            logger.error("=== DEBUG: User role indicator not found ===");
            
            // Check if element exists in DOM
            Object exists = page.evaluate("() => document.querySelector('.runningUserName') !== null");
            logger.error(".runningUserName exists in DOM: {}", exists);
            
            // Get page text sample
            Object pageTextSample = page.evaluate("() => document.body.innerText.substring(0, 500)");
            logger.error("Page text sample (first 500 chars): {}", pageTextSample);
            
            // Check for user-related elements
            Object userElements = page.evaluate("() => { " +
                "const elements = document.querySelectorAll('[class*=\"user\"], [class*=\"User\"], .header, .slds-global-header'); " +
                "return Array.from(elements).slice(0, 5).map(el => el.className).join(', '); " +
            "}");
            logger.error("User-related elements found: {}", userElements);
            
        } catch (Exception debugEx) {
            logger.error("Debug inspection failed: {}", debugEx.getMessage());
        }
        
        logger.error("User role indicator not found after trying all strategies");
        throw new AssertionError("User role indicator not found on page. Check if .runningUserName element exists or if 'Viewing as' text is present.");
    }

    /**
     * Verify no forbidden content keywords appear on page
     */
    public void verifyNoForbiddenContent(List<String> forbiddenKeywords) throws Exception {
        logger.info("Verifying no forbidden content: {}", forbiddenKeywords);

        String pageContent = this.page.content().toLowerCase();
        for (String keyword : forbiddenKeywords) {
            if (pageContent.contains(keyword.toLowerCase())) {
                throw new AssertionError("Forbidden keyword found: " + keyword);
            }
        }

        logger.info("No forbidden content found");
    }

    /**
     * Get today's date for timestamp validation (Salesforce uses 7am UTC as "today" start)
     */
    public String getTodayRefreshTime() throws Exception {
        logger.info("Getting today's refresh time");
        
        try {
            Locator timeLocator = this.page.locator("[class*='refresh'], text=/7:00 AM/");
            String time = timeLocator.textContent();
            logger.info("Refresh time: {}", time);
            return time;
        } catch (Exception e) {
            logger.warn("Could not find refresh timestamp");
            return null;
        }
    }

    /**
     * Verify Salesforce lightning experience loaded
     */
    public void verifySalesforceLoaded() throws Exception {
        logger.info("Verifying Salesforce Lightning loaded");
        
        // Check for Salesforce-specific elements
//        this.page.locator("body[class*='slds']").waitFor();
//        this.page.locator("div[data-aura-app]").waitFor();
        page.waitForURL("**.my.salesforce.com");
        logger.info("Salesforce Lightning verified as loaded");
    }
}
