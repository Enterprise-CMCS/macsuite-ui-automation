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
        return new Object[][] {{0, null}, {500, null}, {0, ""}, {500, "not-a-base32-key"}};
    }

    @Test(dataProvider = "launcherDelays")
    public void waitsForAppLauncherAfterLogin(int delayMs, String totpSecret) {
        page.setContent(loginForm()
            + "<script>document.querySelector('button').onclick = () => {"
            + "document.body.innerHTML = 'Signing in';"
            + "setTimeout(() => document.body.innerHTML = '<button title=\"App Launcher\">Apps</button>', "
            + delayMs + ");};</script>");

        new LoginPage(page).login("test-user", "test-password", totpSecret);

        Assert.assertTrue(page.getByTitle("App Launcher").isVisible());
    }

    @Test
    public void supportsUsernameFirstLogin() {
        page.setContent("<label>Username<input name='username'></label><button>Log In to Sandbox</button>"
            + "<script>document.querySelector('button').onclick = () => {"
            + "if (!document.querySelector('[name=pw]')) {"
            + "document.querySelector('button').insertAdjacentHTML('beforebegin',"
            + "'<label>Password<input name=\"pw\" type=\"password\"></label>'); return;}"
            + "if (document.querySelector('[name=username]').value === 'test-user'"
            + " && document.querySelector('[name=pw]').value === 'test-password') {"
            + "document.body.innerHTML = '<button title=\"App Launcher\">Apps</button>';}};</script>");

        new LoginPage(page).login("test-user", "test-password");

        Assert.assertTrue(page.getByTitle("App Launcher").isVisible());
    }

    @Test
    public void reportsSalesforceCredentialRejection() {
        page.setContent(loginForm()
            + "<script>document.querySelector('button').onclick = () => {"
            + "document.body.innerHTML = '<div id=\"error\">Error: Please check your username and password. "
            + "If you still cannot log in, contact your Salesforce administrator.</div>';};</script>");

        IllegalStateException failure = Assert.expectThrows(IllegalStateException.class,
            () -> new LoginPage(page).login("test-user", "test-password"));

        Assert.assertTrue(failure.getMessage().contains("Salesforce rejected the submitted username/password"));
        Assert.assertNull(failure.getCause());
    }

    @Test
    public void completesDelayedTotpChallenge() {
        page.setContent(loginForm()
            + "<script>document.querySelector('button').onclick = () => {"
            + "document.body.innerHTML = 'Signing in'; setTimeout(() => {"
            + "document.body.innerHTML = '<label>Verification Code<input name=\"verificationCode\"></label>"
            + "<button>Verify</button>'; document.querySelector('button').onclick = () => {"
            + "const input = document.querySelector('input'); window.submittedCode = input.value;"
            + "window.codeWasMasked = input.type === 'password';"
            + "if (/^[0-9]{6}$/.test(input.value)) {"
            + "document.body.innerHTML = '<button title=\"App Launcher\">Apps</button>';}};}, 300);};</script>");

        new LoginPage(page).login("test-user", "test-password", "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");

        Assert.assertTrue(page.getByTitle("App Launcher").isVisible());
        Assert.assertEquals(page.evaluate("window.codeWasMasked"), true);
        Assert.assertTrue(((String) page.evaluate("window.submittedCode")).matches("[0-9]{6}"));
    }

    @Test
    public void reportsMissingTotpSecretOnlyWhenChallenged() {
        page.setContent(loginForm()
            + "<script>document.querySelector('button').onclick = () => {"
            + "document.body.innerHTML = '<label>Verification Code<input></label><button>Verify</button>';};</script>");

        IllegalStateException failure = Assert.expectThrows(IllegalStateException.class,
            () -> new LoginPage(page).login("test-user", "test-password", null));

        Assert.assertTrue(failure.getMessage().contains("no TOTP secret"));
        Assert.assertEquals(page.getByLabel("Verification Code").inputValue(), "");
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