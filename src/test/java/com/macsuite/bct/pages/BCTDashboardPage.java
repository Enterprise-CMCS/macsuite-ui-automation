package com.macsuite.bct.pages;

import java.util.regex.Pattern;

import com.macsuite.common.pages.SfPage;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class BCTDashboardPage extends SfPage {

    public BCTDashboardPage(Page page) {
        super(page);
    }

    public void openDashboards() {
        openTab("Dashboards?|Dashboard");
    }

    public void openReports() {
        openTab("Reports?|Report");
    }

    public void verifyDashboardContent() {
        Locator grid = page
                .locator("[role='grid'][aria-label='Recent Dashboards'], table[aria-label='Recent Dashboards']")
                .first();
        if (isVisible(grid, 10000)) {
            grid.locator("tbody tr a[title], tbody tr [data-label='Dashboard Name'] a").first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(10000));
            logger.info("Dashboard content verified");
            return;
        }
        FrameLocator frame = page.frameLocator("iframe[title*='ashboard']").first();
        frame.locator(".widget-container, [role='grid'][aria-label='Recent Dashboards']")
                .first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        logger.info("Dashboard content verified (iframe)");
    }

    public void verifyReportsContent() {
        Locator surface = page.locator(
                "[role='grid'][aria-label='Recent Reports'], table[aria-label='Recent Reports']").first();
        if (isVisible(surface, 10000)) {
            logger.info("Reports content verified");
            return;
        }
        FrameLocator frame = page.frameLocator("iframe[title*='eport']").first();
        frame.locator("[role='grid'][aria-label='Recent Reports'], table[aria-label='Recent Reports']")
                .first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        logger.info("Reports content verified (iframe)");
    }

    public void openDashboardFolder(String folderName) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(folderName)).first().click();
        logger.info("Opened dashboard folder: {}", folderName);
    }

    public void verifyBreadcrumbsVisible() {
        Locator breadcrumbNav = page.getByRole(AriaRole.NAVIGATION, new Page.GetByRoleOptions().setName("Breadcrumbs"))
                .first();
        breadcrumbNav.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        // Verify breadcrumb list and items are present
        Locator breadcrumbList = breadcrumbNav.locator("ol.slds-breadcrumb");
        breadcrumbList.waitFor(new Locator.WaitForOptions().setTimeout(5000));

        // Verify at least one breadcrumb item exists
        Locator breadcrumbItems = breadcrumbList.locator("li.slds-breadcrumb__item");
        if (breadcrumbItems.count() == 0) {
            throw new AssertionError("No breadcrumb items found");
        }

        logger.info("Breadcrumbs navigation verified with {} item(s)", breadcrumbItems.count());
    }

    public void verifyReportsCountDisplayed(String expectedCount) {
        String expectedText = expectedCount + " item" + (expectedCount.equals("1") ? "" : "s");

        // Wait for the reports list to load
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // Target count element inside the test-listViewStatusInfo div
        Locator countElement = page.locator("div.test-listViewStatusInfo span.countSortedByFilteredBy")
                .last();

        countElement.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));

        String actualText = countElement.textContent().trim();
        if (!actualText.equals(expectedText)) {
            throw new AssertionError(String.format("Expected '%s' but found '%s'", expectedText, actualText));
        }

        logger.info("Reports count verified: {}", expectedText);
    }

    public void verifyBctHomePageHeaderDetails() {
        Locator.WaitForOptions timeout = new Locator.WaitForOptions().setTimeout(10000);
        page.getByText(Pattern.compile("CMCS Budget & Contract Tracker", Pattern.CASE_INSENSITIVE)).first()
                .waitFor(timeout);
        page.getByText(Pattern.compile("Viewing as SEI Admin", Pattern.CASE_INSENSITIVE)).first().waitFor(timeout);
        page.getByText(Pattern.compile("Production Release Announcements", Pattern.CASE_INSENSITIVE)).first()
                .waitFor(timeout);
        String todayMonth = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMM"));
        page.getByText(Pattern.compile(todayMonth, Pattern.CASE_INSENSITIVE)).first().waitFor(timeout);
        logger.info("✓ Verified BCT home page header details");
    }

    public void verifyDashboardTitle(String expectedTitle) {
        Locator title = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(expectedTitle).setExact(true)).first();
        title.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        highlightElement(title);
        logger.info("Verified dashboard title: {}", expectedTitle);
    }

    public void clickSearchButton() {
        Locator searchButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Search").setExact(true)).last();
        searchButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        highlightElement(searchButton);
        searchButton.click();
        logger.info("Clicked dashboard Search button");
    }

    public void filterDashboard(String dashboardName, java.util.Map<String, String> filters) {
        Locator dashboardTitle = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(dashboardName).setExact(true)).first();
        dashboardTitle.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(15000));

        for (java.util.Map.Entry<String, String> filter : filters.entrySet()) {
            String label = filter.getKey();
            String value = filter.getValue();
            String dateFilterId = switch (label) {
                case "P.O.P. Start Date" -> "filterStartDate";
                case "P.O.P. End Date" -> "filterEndDate";
                default -> null;
            };
            Locator field;
            if (dateFilterId != null) {
                field = page.locator("lightning-input[data-id='" + dateFilterId + "'] input").first();
            } else {
                Locator labeledFields = page.getByLabel(label, new Page.GetByLabelOptions().setExact(true));
                field = null;
                for (int i = 0; i < labeledFields.count(); i++) {
                    Locator candidate = labeledFields.nth(i);
                    if (candidate.isVisible()) {
                        field = candidate;
                        break;
                    }
                }
                if (field == null) {
                    throw new IllegalStateException("No visible dashboard filter control found for '" + label + "'");
                }
            }
            field.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10000));

            String tagName = ((String) field.evaluate("element => element.tagName")).toLowerCase();
            String role = field.getAttribute("role");
            String hasPopup = field.getAttribute("aria-haspopup");
            if ("select".equals(tagName)) {
                highlightElement(field);
                field.selectOption(new SelectOption().setLabel(value));
            } else if ("combobox".equals(role) || "listbox".equalsIgnoreCase(hasPopup)) {
                highlightElement(field);
                field.click();
                Locator option = page.getByRole(AriaRole.OPTION,
                        new Page.GetByRoleOptions().setName(value).setExact(true)).first();
                option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));
                highlightElement(option);
                option.click();
            } else if ("input".equals(tagName) || "textarea".equals(tagName)) {
                if (dateFilterId == null) {
                    helper.fillText(page.locator("body"), label, value);
                } else {
                    highlightElement(field);
                    field.fill(value);
                }
            } else {
                throw new IllegalStateException("Unsupported dashboard filter control for '" + label
                        + "' (tag=" + tagName + ", role=" + role + ", aria-haspopup=" + hasPopup + ")");
            }
            logger.info("Set dashboard filter '{}' to '{}'", label, value);
        }
    }

    public void verifyLegendAndContractTimelineCalendar() {
        Locator legendHeading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Legend").setExact(true)).first();
        legendHeading.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        highlightElement(legendHeading);

        Locator calendarHeading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Contract Timeline Calendar").setExact(true)).first();
        calendarHeading.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        highlightElement(calendarHeading);
        logger.info("Verified Legend and Contract Timeline Calendar headings");
    }

    private void openTab(String tabRegex) {
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions()
                .setName(Pattern.compile(tabRegex, Pattern.CASE_INSENSITIVE))).first().click();
    }

    private boolean isVisible(Locator locator, int timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return locator.isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }
}
