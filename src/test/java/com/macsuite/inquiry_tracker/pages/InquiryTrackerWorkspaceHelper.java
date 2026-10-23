package com.macsuite.inquiry_tracker.pages;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

class InquiryTrackerWorkspaceHelper {
    private static final Logger logger = LoggerFactory.getLogger(InquiryTrackerWorkspaceHelper.class);

    private final Page page;

    InquiryTrackerWorkspaceHelper(Page page) {
        this.page = page;
    }

    void openListViews() {
        openWorkspaceSubTab("List Views|Lists");
    }

    void verifyListViewControls() {
        Locator listViewControls = page
            .locator("[aria-label*='List View'], button[title*='List View'], .listViewPicker")
            .first();
        if (!waitForVisible(listViewControls, 10000)) {
            throw new AssertionError("CMCS TA list view controls were not found");
        }
    }

    void openDashboards() {
        openWorkspaceSubTab("Dashboards?|Dashboard");
    }

    void verifyDashboardContent() {
        Locator dashboardGrid = page
            .locator("[role='grid'][aria-label='Recent Dashboards'], table[aria-label='Recent Dashboards']")
            .first();

        if (waitForVisible(dashboardGrid, 10000)) {
            Locator dashboardRow = dashboardGrid
                .locator("tbody[data-rowgroup-body] tr[data-row-number], [data-label='Dashboard Name'] a[title]")
                .first();
            if (!waitForVisible(dashboardRow, 10000)) {
                throw new AssertionError("Visible CMCS TA dashboard content was not found");
            }
            return;
        }

        FrameLocator dashboardFrame = page
            .frameLocator("iframe[title*='dashboard'], iframe[title*='Dashboard']")
            .first();
        Locator dashboardWidget = dashboardFrame
            .locator(".widget-container, [role='grid'][aria-label='Recent Dashboards'], table[aria-label='Recent Dashboards']")
            .first();

        if (!waitForVisible(dashboardWidget, 15000)) {
            throw new AssertionError("Visible CMCS TA dashboard content was not found in the page or dashboard iframe");
        }
    }

    void openReports() {
        openWorkspaceSubTab("Reports?|Report");
    }

    void verifyReportsContent() {
        Locator reportsSurface = page.locator(
            "[role='banner'].slds-page-header:has-text('Reports'), "
                + "[role='grid'][aria-label='Recent Reports'], table[aria-label='Recent Reports'], "
                + "[data-label='Report Name'] a[title]")
            .first();

        if (waitForVisible(reportsSurface, 10000)) {
            return;
        }

        FrameLocator reportFrame = page
            .frameLocator("iframe[title*='report'], iframe[title*='Report']")
            .first();
        Locator reportFrameSurface = reportFrame
            .locator(".slds-page-header__title, [role='grid'][aria-label='Recent Reports'], table[aria-label='Recent Reports']")
            .first();

        if (!waitForVisible(reportFrameSurface, 15000)) {
            throw new AssertionError("Visible CMCS TA reports content was not found in the page or report iframe");
        }
    }

    void selectCmcsTaListView(String listViewName) {
        logger.info("Selecting CMCS TA list view: {}", listViewName);
        openListViewPicker();
        Locator listViewOption = page.getByText(listViewName, new Page.GetByTextOptions().setExact(true)).first();
        try {
            listViewOption.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (Exception e) {
            throw new AssertionError("List view option not found: " + listViewName);
        }
        listViewOption.click();
        logger.info("Selected list view: {}", listViewName);
    }

    void createNewCmcsTaListView(String listViewName) {
        logger.info("Creating new CMCS TA list view: {}", listViewName);
        clickListViewMenuItem("^New$", "New menu item not found");
        
        // Wait for the New List View dialog to appear
        Locator nameInput = page.getByLabel("List Name", new Page.GetByLabelOptions().setExact(false)).first();
        if (nameInput.count() == 0) {
            nameInput = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName(Pattern.compile("List.*Name", Pattern.CASE_INSENSITIVE))).first();
        }

        try {
            nameInput.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (Exception e) {
            throw new AssertionError("List view name input field not found");
        }

        nameInput.fill(listViewName);
        clickDialogButton("Save", "Save button not found in new list view dialog");
        logger.info("Created CMCS TA list view: {}", listViewName);
    }

    void cloneCurrentCmcsTaListView() {
        logger.info("Cloning current CMCS TA list view with default settings");
        clickListViewMenuItem("Clone", "Clone menu item not found");
        clickDialogButton("Save", "Save button not found in clone dialog");
        logger.info("Cloned CMCS TA list view");
    }

    void verifyClonedCmcsTaListViewActive() {
        logger.info("Verifying cloned CMCS TA list view is active");
        // P3 strategy: Use stable CSS class with text filter to wait for "Copy of" prefix
        // Playwright will automatically retry until the title contains the expected text
        Locator listViewTitle = page.locator(".slds-page-header__title")
            .filter(new Locator.FilterOptions().setHasText("Copy of"))
            .first();
        try {
            listViewTitle.waitFor(new Locator.WaitForOptions().setTimeout(15000));
            String actualTitle = listViewTitle.textContent();
            logger.info("✓ Verified cloned list view is active: {}", actualTitle);
        } catch (Exception e) {
            // Provide helpful error with actual title
            Locator anyTitle = page.locator(".slds-page-header__title").first();
            String actualTitle = anyTitle.textContent();
            throw new AssertionError("Cloned list view not active. Expected title to contain 'Copy of', but found: '" + actualTitle + "'");
        }
    }

    void verifyCmcsTaListViewActive(String listViewName) {
        logger.info("Verifying CMCS TA list view '{}' is active", listViewName);
        // P3 strategy: Use stable CSS class with text filter to wait for correct title
        // Playwright will automatically retry until the title contains the expected text
        Locator listViewTitle = page.locator(".slds-page-header__title")
            .filter(new Locator.FilterOptions().setHasText(listViewName))
            .first();
        try {
            listViewTitle.waitFor(new Locator.WaitForOptions().setTimeout(15000));
            logger.info("✓ Verified list view '{}' is active", listViewName);
        } catch (Exception e) {
            // Provide helpful error with actual title
            Locator anyTitle = page.locator(".slds-page-header__title").first();
            String actualTitle = anyTitle.textContent();
            throw new AssertionError("List view '" + listViewName + "' not active. Current title: '" + actualTitle + "'");
        }
    }

    void deleteCurrentCmcsTaListView() {
        logger.info("Deleting current CMCS TA list view");
        clickListViewMenuItem("Delete", "Delete menu item not found");
        clickDialogButton("Delete", "Delete confirmation button not found");
        logger.info("Deleted CMCS TA list view");
    }

    void cloneCurrentInquiry() {
        logger.info("Cloning current inquiry");
        Locator showMoreButton = findButton(Pattern.compile("Show more actions|Show Actions", Pattern.CASE_INSENSITIVE));
        try {
            showMoreButton.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (Exception e) {
            throw new AssertionError("Show more actions button not found");
        }
        showMoreButton.click();

        Locator cloneAction = page.getByRole(AriaRole.MENUITEM,
            new Page.GetByRoleOptions().setName(Pattern.compile("Clone", Pattern.CASE_INSENSITIVE))).first();
        try {
            cloneAction.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (Exception e) {
            throw new AssertionError("Clone action not found in actions menu");
        }
        cloneAction.click();
    }

    void verifyClonedInquiryFormDisplayed() {
        verifyHeadingVisible(Pattern.compile("Clone|New CMCS", Pattern.CASE_INSENSITIVE));
    }

    void updateInquiryTitle(String newTitle) {
        page.getByLabel("Inquiry Title").fill(newTitle);
    }

    private void openWorkspaceSubTab(String tabRegex) {
        Locator tab = page.getByRole(AriaRole.TAB,
            new Page.GetByRoleOptions().setName(Pattern.compile(tabRegex, Pattern.CASE_INSENSITIVE))).first();
        
        // Try role-based locator first, fall back to text match if not found
        try {
            tab.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        } catch (Exception e) {
            tab = page.getByText(Pattern.compile(tabRegex, Pattern.CASE_INSENSITIVE)).first();
            try {
                tab.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            } catch (Exception ex) {
                throw new AssertionError("Workspace tab not found for: " + tabRegex);
            }
        }
        tab.click();
    }

    private Locator findButton(Pattern namePattern) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(namePattern)).first();
    }

    private void openListViewPicker() {
        Locator listViewPicker = findButton(Pattern.compile("Select a List View", Pattern.CASE_INSENSITIVE));
        try {
            listViewPicker.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (Exception e) {
            throw new AssertionError("List view picker button not found");
        }
        listViewPicker.click();
    }

    private void openListViewControlsMenu() {
        Locator listViewControlsButton = findButton(Pattern.compile("List View Controls", Pattern.CASE_INSENSITIVE));
        try {
            listViewControlsButton.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        } catch (Exception e) {
            throw new AssertionError("List View Controls button not found or not visible within timeout");
        }
        listViewControlsButton.click();
    }

    private void clickListViewMenuItem(String menuPattern, String errorMessage) {
        openListViewControlsMenu();
        Locator menuItem = page.getByRole(AriaRole.MENUITEM,
            new Page.GetByRoleOptions().setName(Pattern.compile(menuPattern, Pattern.CASE_INSENSITIVE))).first();
        try {
            menuItem.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (Exception e) {
            throw new AssertionError(errorMessage);
        }
        menuItem.click();
    }

    private void clickDialogButton(String buttonName, String errorMessage) {
        Locator button = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(buttonName).setExact(true)).first();
        try {
            button.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (Exception e) {
            throw new AssertionError(errorMessage);
        }
        button.click();
    }

    private void verifyHeadingVisible(Pattern pattern) {
        Locator heading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(pattern)).first();
        if (!waitForVisible(heading, 10000)) {
            throw new AssertionError("Expected heading not visible: " + pattern.pattern());
        }
    }

    private boolean waitForVisible(Locator locator, int timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return locator.isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }
}
