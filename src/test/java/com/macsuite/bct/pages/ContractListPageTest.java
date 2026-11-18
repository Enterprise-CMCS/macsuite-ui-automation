package com.macsuite.bct.pages;

import com.macsuite.core.PlaywrightManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ContractListPageTest {
    @DataProvider
    public Object[][] recordColumns() {
        return new Object[][] {
                { "Contract Action Number", false, "action" },
                { "Contract Action Number", true, "action" },
                { "Contract / Order Number", false, "contract" },
                { "Contract / Order Number", true, "contract" }
        };
    }

    @Test(dataProvider = "recordColumns")
    public void opensFirstRowLinkOnlyInRequestedColumn(String columnName, boolean reversed, String expectedRecord) {
        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(
                        new BrowserType.LaunchOptions().setHeadless(true));
                BrowserContext context = browser.newContext()) {
            String contractHeader = "<th role='columnheader'>Contract / Order Number</th>";
            String actionHeader = "<th role='columnheader'>Contract Action Number</th>";
            String headers = reversed ? actionHeader + contractHeader : contractHeader + actionHeader;
            if (reversed) {
                headers = headers.replace("<th role='columnheader'>", "<th role='columnheader'><span>")
                        .replace("</th>", "</span><button>Sort</button></th>");
            }
            StringBuilder rows = new StringBuilder();
            for (int row = 1; row <= 2; row++) {
                String contract = "<th><a href='/contract-" + row + "'>Contract " + row + "</a></th>";
                String action = "<td><a href='/action-" + row + "'>Action " + row + "</a></td>";
                rows.append("<tr><td>").append(row).append("</td>")
                        .append(reversed ? action + contract : contract + action).append("</tr>");
            }
            String listHtml = "<table role='grid'><thead><tr><th role='columnheader'>Row</th>" + headers
                    + "</tr></thead><tbody>" + rows + "</tbody></table>";
            context.route("https://records.test/**", route -> route.fulfill(
                    new com.microsoft.playwright.Route.FulfillOptions().setContentType("text/html")
                            .setBody(route.request().url().endsWith("/list") ? listHtml
                                    : "<button role='tab' aria-selected='true'>Details</button>")));
            Page source = context.newPage();
            source.navigate("https://records.test/list");
            try {
                new ContractListPage(source).clickFirstRecordInColumn(columnName);
                Assert.assertEquals(source.url(), "https://records.test/" + expectedRecord + "-1");
            } finally {
                PlaywrightManager.close();
            }
        }
    }
}
