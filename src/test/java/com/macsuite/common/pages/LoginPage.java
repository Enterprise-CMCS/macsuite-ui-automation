package com.macsuite.common.pages;

import com.macsuite.common.SfHelper;
import com.macsuite.common.TotpGenerator;
import com.macsuite.config.EnvLoader;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Page Object for Salesforce Login Page
 * Handles authentication with multiple selector strategies
 */
public class LoginPage {
    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    /**
     * Login with username and password
     * Uses semantic locators with fallback to name attributes
     */
    public void login(String username, String password) {
        login(username, password, EnvLoader.get("SALESFORCE_TOTP_SECRET", null));
    }

    public void login(String username, String password, String totpSecret) {
        logger.info("Logging in as: {}", username);
        
        try {
            Locator usernameField = usernameField();
            usernameField.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
            usernameField.fill(username);

            Locator passwordField = passwordField();
            if (!isVisible(passwordField, 1000)) {
                clickLoginSubmit();
                passwordField.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(15000));
            }

            passwordField.fill(password);
            clickLoginSubmit();
        } catch (Exception e) {
            // Fallback to name-based selectors (legacy)
            logger.debug("Semantic selectors failed, using name attributes");
            page.fill("[name='username']", username);
            if (!isVisible(page.locator("[name='pw'], [name='password'], #password").first(), 1000)) {
                page.click("input#Login[type='submit'], input[name='Login'][type='submit'], [type='submit']");
            }
            page.locator("[name='pw'], [name='password'], #password").first()
                .waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(15000));
            page.fill("[name='pw'], [name='password'], #password", password);
            page.click("input#Login[type='submit'], input[name='Login'][type='submit'], [type='submit']");
        }
        
        Locator appLauncher = page.getByTitle("App Launcher");
        Locator credentialError = page.locator("[id='error']")
            .filter(new Locator.FilterOptions().setHasText("Please check your username and password."));
        // Locator verificationCode = page.getByLabel("Verification Code",
        //     new Page.GetByLabelOptions().setExact(true)).first();
        try {
            appLauncher.or(credentialError).first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(60000));
        } catch (com.microsoft.playwright.TimeoutError e) {
            throw new IllegalStateException(
                "Salesforce login did not reach the App Launcher within 60 seconds. "
                    + "Check the failed-step screenshot and trace for a login error, identity verification/MFA, "
                    + "or an IP/session restriction on the CI runner. Verify the credentials and application URL "
                    + "configured in GitHub Actions secrets.", e);
        }
        if (credentialError.isVisible()) {
            throw new IllegalStateException(
                "Salesforce rejected the submitted username/password. Check Salesforce Setup > Login History "
                    + "for this CI attempt to identify the rejection reason. Verify that the GitHub Actions "
                    + "credentials belong to the sandbox configured by APP_URL_UAT; also check account lockout "
                    + "and login restrictions. Increasing the App Launcher timeout will not resolve this rejection.");
        }
        // if (verificationCode.isVisible()) {
        //     completeTotpChallenge(verificationCode, appLauncher, totpSecret);
        // }
        logger.info("Login successful");
    }

    private void completeTotpChallenge(Locator verificationCode, Locator appLauncher, String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Salesforce verification-code challenge detected, but no TOTP secret "
                + "is configured. Set the matching role/environment TOTP_SECRET or SALESFORCE_TOTP_SECRET "
                + "to the Base32 key registered for this Salesforce user.");
        }
        SfHelper helper = new SfHelper(page);
        try {
            helper.fillSensitiveText(verificationCode, "Verification Code", TotpGenerator.generateCurrentCode(secret));
            helper.clickButton("Verify");
            appLauncher.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(60000));
        } catch (com.microsoft.playwright.TimeoutError exception) {
            throw new IllegalStateException("Salesforce TOTP verification did not reach the App Launcher. "
                + "Check the authenticator enrollment, configured Base32 key, and runner clock synchronization.");
        } finally {
            if (!page.isClosed()) {
                try {
                    if (verificationCode.isVisible()) {
                        helper.fillSensitiveText(verificationCode, "Verification Code", "");
                    }
                } catch (RuntimeException exception) {
                    logger.warn("Could not clear the masked verification-code field.");
                }
            }
        }
    }

    private Locator usernameField() {
        return page.getByLabel("Username")
            .or(page.locator("input[name='username'], input#username"))
            .first();
    }

    private Locator passwordField() {
        return page.getByLabel("Password")
            .or(page.locator("input[name='pw'], input[name='password'], input#password"))
            .first();
    }

    private void clickLoginSubmit() {
        Locator loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName(Pattern.compile("^\\s*Log In( to Sandbox)?\\s*$", Pattern.CASE_INSENSITIVE)))
            .or(page.locator("input#Login[type='submit'], input[name='Login'][type='submit'], input[type='submit'][value='Log In to Sandbox'], input[type='submit'][value='Log In']"))
            .first();

        loginButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(15000));
        loginButton.click();
    }

    private boolean isVisible(Locator locator, int timeoutMs) {
        try {
            return locator.isVisible(new Locator.IsVisibleOptions().setTimeout(timeoutMs));
        } catch (Exception ignored) {
            return false;
        }
    }
}
