package com.macsuite.common.pages;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginPageTest {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeMethod
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @DataProvider
    public Object[][] launcherDelays() {
        return new Object[][] {{0}, {500}};
    }

    @Test(dataProvider = "launcherDelays")
    public void waitsForAppLauncherAfterLogin(int delayMs) {
        page.setContent(loginForm()
            + "<script>document.querySelector('button').onclick = () => {"
            + "document.body.innerHTML = 'Signing in';"
            + "setTimeout(() => document.body.innerHTML = '<button title=\"App Launcher\">Apps</button>', "
            + delayMs + ");};</script>");

        new LoginPage(page).login("test-user", "test-password");

        Assert.assertTrue(page.getByTitle("App Launcher").isVisible());
    }

    @Test
    public void rejectsLoginThatNeverReachesAppLauncher() {
        page.setContent(loginForm()
            + "<script>document.querySelector('button').onclick = () => "
            + "document.body.innerHTML = 'Sign-in requires verification';</script>");

        IllegalStateException failure = Assert.expectThrows(IllegalStateException.class,
            () -> new LoginPage(page).login("test-user", "test-password"));

        Assert.assertTrue(failure.getMessage().contains("Salesforce login did not reach the App Launcher"));
        Assert.assertTrue(failure.getCause() instanceof TimeoutError);
    }

    private String loginForm() {
        return "<label>Username<input name='username'></label>"
            + "<label>Password<input name='pw' type='password'></label><button>Log In</button>";
    }
}