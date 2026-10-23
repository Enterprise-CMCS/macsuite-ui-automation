package com.macsuite.access_control.steps;

import com.macsuite.common.ModuleSteps;
import com.macsuite.common.steps.LoginSteps;
import com.macsuite.config.EnvironmentResolver;
import com.macsuite.access_control.pages.AppDashboardPage;
import io.cucumber.java.en.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Step definitions for Access Control User Validation scenarios
 */
public class AccessControlUserSteps extends ModuleSteps<AppDashboardPage> {
    private static final Logger logger = LoggerFactory.getLogger(AccessControlUserSteps.class);

    @Override
    protected AppDashboardPage createModulePage() {
        return new AppDashboardPage(page());
    }

    /**
     * Navigate to environment-specific URL (resolved from .env)
     */
    @Given("I navigate to {string} environment")
    public void navigateToEnvironment(String environment) {
        logger.info("Navigating to {} environment", environment);
        
        // Store environment for shared LoginSteps
        LoginSteps.setCurrentEnvironment(environment);
        
        // Resolve baseURL from environment configuration using EnvironmentResolver
        String baseURL = EnvironmentResolver.getEnvironmentUrl(environment);
        logger.info("Resolved URL for {}: {}", environment, baseURL);
        
        navigate(baseURL);
    }

    /**
     * Verify app name displayed in header
     */
    @Then("I should see {string} displayed in the app header")
    public void verifyAppNameDisplayedInHeader(String expectedApp) throws Exception {
        logger.info("Verifying app name: {}", expectedApp);
        getModulePage().verifyAppNameDisplayed(expectedApp);
        screenshot("App Header: " + expectedApp);
    }

    /**
     * Verify single module visible
     */
    @Then("I should see only one module available for {string} role")
    public void verifySingleModuleAvailable(String role) throws Exception {
        logger.info("Verifying single module for role: {}", role);
        List<String> modules = getModulePage().getAvailableModules();
        if (modules == null || modules.isEmpty()) {
            throw new AssertionError("No modules visible for role: " + role);
        }
        
        logger.info("Available modules: {}", modules);

        if (modules.size() > 3) {
            throw new AssertionError("Too many modules visible for " + role + ": " + modules.size() + " (expected max 3)");
        }

        logger.info("{} role has {} module(s)", role, modules.size());
    }

    /**
     * Verify no unauthorized modules visible
     */
    @Then("I should NOT see unauthorized modules")
    public void verifyNoUnauthorizedModulesVisible() throws Exception {
        logger.info("Verifying no unauthorized modules visible");
        String currentUser = LoginSteps.getCurrentUserRole();
        List<String> availableModules = getModulePage().getAvailableModules();
        List<String> forbiddenKeywords = new ArrayList<>();

        // Define forbidden content per role
        if (!currentUser.toUpperCase().contains("BCT")) {
            forbiddenKeywords.add("Budget & Contract Tracker");
            forbiddenKeywords.add("Contract");
        }
        if (!currentUser.toUpperCase().contains("SO")) {
            forbiddenKeywords.add("State Operations");
            forbiddenKeywords.add("Operations Dashboard");
        }
        if (!currentUser.toUpperCase().contains("TA")) {
            forbiddenKeywords.add("Technical Advisor");
            forbiddenKeywords.add("Analytics");
        }

        for (String module : availableModules) {
            for (String forbidden : forbiddenKeywords) {
                if (module.toLowerCase().contains(forbidden.toLowerCase())) {
                    throw new AssertionError("Forbidden module found: " + module + " for role: " + currentUser);
                }
            }
        }

        logger.info("No unauthorized modules found for role: {}", currentUser);
        screenshot("Access Control");
    }

    /**
     * Verify user authenticated as expected role
     */
    @Then("I should be authenticated as {string} user")
    public void verifyAuthenticatedAsRole(String expectedRole) throws Exception {
        logger.info("Verifying authenticated as: {}", expectedRole);
        String userIndicator = getModulePage().getUserRoleIndicator();

        if (!userIndicator.contains(expectedRole)) {
            logger.error("Role mismatch - Expected: '{}', but found: '{}'", expectedRole, userIndicator);
            throw new AssertionError(
                String.format("Expected user role '%s' but found '%s'", expectedRole, userIndicator)
            );
        }

        logger.info("✓ Verified authenticated as: {} (indicator: {})", expectedRole, userIndicator);
        screenshot("Authenticated As: " + expectedRole);
    }
}