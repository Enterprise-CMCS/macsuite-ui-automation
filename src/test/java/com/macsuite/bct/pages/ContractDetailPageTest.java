package com.macsuite.bct.pages;

import com.macsuite.core.PlaywrightManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ContractDetailPageTest {
    @DataProvider
    public Object[][] navigationModes() {
        return new Object[][] { { "intercepted" }, { "popup" }, { "same-tab" } };
    }

    @Test(dataProvider = "navigationModes")
    public void returnsToRecordAndSelectsOptionYears(String mode) {
        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(
                        new BrowserType.LaunchOptions().setHeadless(true));
                BrowserContext context = browser.newContext()) {
            String contractHtml = """
                    <button role="tab" aria-selected="true">Details</button>
                    <button role="tab" aria-selected="false"
                      onclick="this.setAttribute('aria-selected', 'true')">Contract Option Years</button>
                    """;
            String target = "same-tab".equals(mode) ? "_self" : "_blank";
            String handler = "intercepted".equals(mode)
                    ? "event.preventDefault(); location.href=this.href;" : "";
            context.route("https://navigation.test/**", route -> route.fulfill(
                    new com.microsoft.playwright.Route.FulfillOptions()
                            .setContentType("text/html")
                            .setBody(route.request().url().endsWith("/contract") ? contractHtml
                                    : "<a href='/contract' target='" + target + "' onclick=\"" + handler
                                            + "\">Contract-Test</a>")));
            Page source = context.newPage();
            source.navigate("https://navigation.test/action");
            try {
                ContractDetailPage record = new ContractDetailPage(source).navigateBackToRecordLink();
                record.navigateToTab("Contract Option Years");

                Page active = PlaywrightManager.page();
                Assert.assertEquals(active.url(), "https://navigation.test/contract");
                Assert.assertEquals(context.pages().size(), "popup".equals(mode) ? 2 : 1);
                Assert.assertEquals(active.getByRole(AriaRole.TAB,
                        new Page.GetByRoleOptions().setName("Contract Option Years"))
                        .getAttribute("aria-selected"), "true");
            } finally {
                PlaywrightManager.close();
            }
        }
    }
}
