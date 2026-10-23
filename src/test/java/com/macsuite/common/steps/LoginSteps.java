package com.macsuite.common.steps;

import com.macsuite.common.StepBase;
import com.macsuite.common.pages.LoginPage;
import com.macsuite.config.CredentialProvider;
import com.macsuite.config.TestConfig;
import com.macsuite.core.PlaywrightManager;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared login step definitions used across all modules.
 * Handles authentication with role-based credentials.
 */
public class LoginSteps extends StepBase {
    private static final Logger logger = LoggerFactory.getLogger(LoginSteps.class);
    private static ThreadLocal<String> currentEnvironment = new ThreadLocal<>();
    private static ThreadLocal<String> currentUserRole = new ThreadLocal<>();
    
    /**
     * Store environment for later use by login step
     */
    public static void setCurrentEnvironment(String environment) {
        currentEnvironment.set(environment);
    }
    
    /**
     * Get the stored environment (used by other step classes before login)
     */
    public static String getCurrentEnvironment() {
        String env = currentEnvironment.get();
        return (env != null) ? env : TestConfig.getDefaultEnvironment();
    }
    
    /**
     * Get the current user role (used by other step classes after login)
     */
    public static String getCurrentUserRole() {
        return currentUserRole.get();
    }
    
    /**
     * Clear stored environment and user after scenario
     */
    public static void clear() {
        currentEnvironment.remove();
        currentUserRole.remove();
    }

    /**
     * Login as specific user role (resolves credentials from .env).
     *
     * <p>When the system property {@code USE_AUTH_STATE=true} is set, this step will try to
     * reuse a previously saved Salesforce session stored under
     * {@code target/auth-state/{env}/{role}.json}.  If the file is present and less than
     * 2 hours old the full login form is bypassed; otherwise a fresh login is performed and
     * the new auth state is saved for the next run.
     */
    @When("I login as {string} user")
    public void loginAsUser(String userRole) {
        String environment = getCurrentEnvironment();
        logger.info("Logging in as {} user in {} environment", userRole, environment);

        boolean useAuthState = Boolean.parseBoolean(System.getProperty("USE_AUTH_STATE", "false"));
        boolean authStateUsed = false;

        if (useAuthState) {
            String authStatePath = String.format("target/auth-state/%s/%s.json",
                    environment.toLowerCase(), userRole.toLowerCase());
            java.nio.file.Path authPath = java.nio.file.Paths.get(authStatePath);
            try {
                if (java.nio.file.Files.exists(authPath)) {
                    long ageMs = System.currentTimeMillis()
                            - java.nio.file.Files.getLastModifiedTime(authPath).toMillis();
                    if (ageMs < 2L * 60 * 60 * 1000) {
                        // Capture the URL the navigate step already loaded
                        String currentUrl = PlaywrightManager.page().url();
                        PlaywrightManager.reinitContext(authStatePath);
                        // Re-navigate: auth cookies make Salesforce skip the login page
                        PlaywrightManager.page().navigate(currentUrl);
                        authStateUsed = true;
                        logger.info("Auth state reused for {}/{} — skipping full login", environment, userRole);
                    } else {
                        logger.info("Auth state stale ({}min old) — performing fresh login",
                                ageMs / 60_000);
                    }
                }
            } catch (Exception e) {
                logger.warn("Auth state reuse failed ({}), falling back to full login", e.getMessage());
                authStateUsed = false;
            }
        }

        if (!authStateUsed) {
            CredentialProvider.Credentials credentials = CredentialProvider.getCredentials(userRole, environment);
            LoginPage loginPage = new LoginPage(page());
            loginPage.login(credentials.getUsername(), credentials.getPassword());

            if (useAuthState) {
                String authStatePath = String.format("target/auth-state/%s/%s.json",
                        environment.toLowerCase(), userRole.toLowerCase());
                PlaywrightManager.saveAuthState(authStatePath);
            }
        }

        currentUserRole.set(userRole);
        logger.info("Successfully logged in as {} user", userRole);
        screenshot("Logged In: " + userRole);
    }
}
