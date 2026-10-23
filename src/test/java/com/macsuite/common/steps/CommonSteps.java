package com.macsuite.common.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.StepBase;
import com.macsuite.common.pages.SalesforceBasePage;
import com.macsuite.common.pages.SfNavigator;
import com.macsuite.config.EnvironmentResolver;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.options.AriaRole;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CommonSteps extends StepBase {
    private static final Logger stepLogger = LoggerFactory.getLogger(CommonSteps.class);

    private SfNavigator nav;
    private SalesforceBasePage salesforceBasePage;
    private Page helpersPage;

    private SfNavigator nav() {
        refreshPageBoundHelpers();
        return nav;
    }

    private void refreshPageBoundHelpers() {
        Page currentPage = page();
        if (helpersPage != currentPage) {
            nav = new SfNavigator(currentPage);
            salesforceBasePage = new SalesforceBasePage(currentPage);
            helpersPage = currentPage;
        }
    }

    @Given("I navigate to MAC Suite Application in {string} environment")
    public void navigateToEnvironment(String environment) {
        LoginSteps.setCurrentEnvironment(environment);
        String appUrl = EnvironmentResolver.getEnvironmentUrl(environment);
        navigate(appUrl);
        stepLogger.info("Navigated to {} environment: {}", environment, appUrl);
    }

    @When("I open {string} app")
    public void openApp(String appName) {
        nav().selectSalesforceApp(appName);
    }

    @When("I navigate to Dashboard tab then I can verify the dashboard is loaded successfully")
    public void navigateToDashboardTab() {
        nav().verifyDashboardNavigation();
    }

    @When("I navigate to Reports tab then I can verify the reports are loaded successfully")
    public void navigateToReportsTab() {
        nav().verifyReportsNavigation();
    }

    @When("I click on {string} menu")
    public void clickOnHeaderMenu(String menuItem) {
        nav().clickHeaderMenuItem(menuItem);
    }

    @Then("I should see the {string} list view")
    public void verifyListView(String listViewName) {
        nav().selectListView(listViewName);
    }

    @Then("I can navigate to the random record")
    public void verifyRecordNavigation() {
        nav().verifyRecordNavigation();
    }

    @Then("I can validate the {string} table columns")
    public void validateTableColumns(String tableName, io.cucumber.datatable.DataTable dataTable) {
        nav().validateTableColumns(dataTable.asMaps(String.class, String.class).stream()
            .map(row -> row.get("Column Name"))
            .toList());
        screenshot("Validated " + tableName + " Table Columns");
    }

    @When("I select a random record from the list view")
    public void selectRandomRecord() {
        nav().selectRandomRecord();
    }

    @When("I select a random Account record from the list view")
    public void selectRandomAccountRecord() {
        nav().selectRandomAccountRecord();
    }

    @When("I click New button")
    public void clickNewButton() {
        page().getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Page.GetByRoleOptions()
                .setName("New").setExact(true)).first().click();

        // Wait for either a dialog or an inline form to appear
        Locator dialog = page().getByRole(AriaRole.DIALOG);
        Locator inlineForm = page().getByText("Fields marked with an asterisk are required.");
        try {
            dialog.first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        } catch (Exception ignored) {
            inlineForm.first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        }
    }

    @When("I click on {string} button")
    public void clickButton(String buttonName) {
        Locator button = page().getByRole(AriaRole.BUTTON)
            .filter(new Locator.FilterOptions().setHasText(buttonName)).first();
        int timeout = "Create Contract Action".equals(buttonName) ? 30000 : 10000;
        if (button.count() == 0 || !button.isVisible()) {
            Locator dashboardIframe = page().locator("iframe[title='dashboard']:visible").last();
            if (dashboardIframe.count() > 0) {
                FrameLocator dashboard = dashboardIframe.contentFrame();
                button = dashboard.getByRole(AriaRole.BUTTON)
                        .filter(new Locator.FilterOptions().setHasText(buttonName)).first();
            }
        }
        button.waitFor(new Locator.WaitForOptions().setTimeout(timeout));
        button.scrollIntoViewIfNeeded();
        button.click();
        stepLogger.info("Clicked button: {}", buttonName);
    }

    @When("I fill the {string} modal with:")
    public void fillModal(String modalName, java.util.Map<String, String> formData) {
        Locator dialog = page().getByRole(AriaRole.DIALOG).last();
        dialog.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        formData.forEach((fieldName, value) -> {
            Locator field = dialog.getByLabel(fieldName,
                    new Locator.GetByLabelOptions().setExact(false)).first();
            field.waitFor(new Locator.WaitForOptions().setTimeout(10000));
            field.scrollIntoViewIfNeeded();
            nav().fillField(fieldName, value);
        });
        stepLogger.info("Filled '{}' modal with {} fields", modalName, formData.size());
    }

    @When("I click the Save button")
    public void clickSaveButton() {
        nav().clickSaveButton();
    }

    @Then("I should see the success message")
    public void verifySuccessMessage() {
        nav().waitForToast("success");
        screenshot("Saved Successfully");
    }

    @Then("I should see the message {string}")
    public void verifySpecificMessage(String expectedMessage) {
        nav().waitForToast("success", expectedMessage);
    }

    @Then("I should see {string} message")
    public void verifyMessageShorthand(String expectedMessage) {
        verifySpecificMessage(expectedMessage);
    }

    @Then("I should see error toast {string}")
    public void verifyErrorToastMessage(String errorMessage) {
        nav().verifyErrorToastMessage(errorMessage);
        screenshot("Error Toast");
    }

    @Then("I should see error {string}")
    public void verifyErrorMessage(String errorMessage) {
        nav().dismissErrorBannerIfPresent();
        page().getByText(errorMessage, new com.microsoft.playwright.Page.GetByTextOptions().setExact(false))
                .first().waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(8000));
        screenshot("Error: " + errorMessage.substring(0, Math.min(40, errorMessage.length())));
    }

    @Then("the {string} should be fully loaded")
    public void verifyAppDashboardFullyLoaded(String appName) {
        nav().verifyDashboardHeaderDetails(appName);
        screenshot("Dashboard Loaded: " + appName);
    }

    @Then("I verify refresh time and admin details are displayed on the dashboard")
    public void I_verify_refresh_time_and_admin_details_are_displayed_on_the_dashboard() {
        stepLogger.info("Verifying refresh time and admin details are displayed on the dashboard");

        boolean detailsDisplayed = getSalesforceBasePage().verifyRefreshTimeAndAdminDetails();

        if (!detailsDisplayed) {
            throw new AssertionError(
                    "Refresh time and/or admin details are not displayed on the dashboard. Check logs for details.");
        }

        stepLogger.info("✓ Refresh time and admin details are displayed on the dashboard");
    }

    @Then("I verify the {string} header content")
    public void I_verify_the_header_content(String dashboardName) {
        stepLogger.info("Verifying the {} header content", dashboardName);
        getSalesforceBasePage().verifyDashboardHeaderDetails(dashboardName);

        stepLogger.info("✓ Header content for {} is displayed correctly", dashboardName);
    }

    @Then("I verify the App name under production release announcements section")
    public void I_verify_the_App_name_under_production_release_announcements_section() {
        stepLogger.info("Verifying the App name under production release announcements section");

        getSalesforceBasePage().verifyAppNameUnderReleaseAnnouncements();

        stepLogger.info("✓ App name is displayed under production release announcements section");
    }

    @Then("I verify details on the newly opened dashboard are displayed correctly")
    public void I_verify_details_on_the_newly_opened_dashboard_are_displayed_correctly() {
        stepLogger.info("Verifying details on the newly opened dashboard are displayed correctly");

        boolean detailsDisplayed = getSalesforceBasePage().verifyRefreshTimeAndAdminDetails_New();

        if (!detailsDisplayed) {
            throw new AssertionError(
                    "Refresh time and/or admin details are not displayed on the dashboard. Check logs for details.");
        }

        stepLogger.info("✓ Details on the newly opened dashboard are displayed correctly");
    }

    /**
     * Get SalesforceBasePage instance, creating it if needed
     * 
     * @return SalesforceBasePage instance
     */
    private SalesforceBasePage getSalesforceBasePage() {
        refreshPageBoundHelpers();
        return salesforceBasePage;
    }

    @When("I click on {string} tab")
    public void clickOnTab(String tabName) {
        page().getByRole(AriaRole.TAB, new com.microsoft.playwright.Page.GetByRoleOptions()
                .setName(tabName)).first().click();
        screenshot(tabName + " Tab");
    }


    @When("I select a random dashboard")
    public void selectRandomDashboard() {
        stepLogger.info("Opening a random dashboard");
        nav().openRandomDashboard();
        nav().openChildDashboard();
    }

    @When("I open report {string}")
    public void openReport(String reportName) {
        stepLogger.info("Opening report: {}", reportName);
        nav().openReport(reportName);
    }

    @When("I open a random report")
    public void openRandomReport() {
        stepLogger.info("Opening a random report");
        nav().openRandomReport();
        nav().openChildReport();
    }

    @Then("I should see report {string} with {string} total records")
    public void verifyReportContentWithRecords(String reportTitle, String expectedRecordCount) {
        stepLogger.info("Verifying report {} with {} total records", reportTitle, expectedRecordCount);
        nav().verifyReportContent(reportTitle, expectedRecordCount);
        screenshot("Report Content Verified");
    }

    @Then("I should see the report loaded with record count")
    public void verifyRandomReportLoaded() {
        stepLogger.info("Verifying random report loaded with record count");
        nav().verifyRandomReportLoaded();

    }

    @Then("I should see {string} displayed correctly")
    public void verifyMenuSectionDisplayed(String section) {
        nav().verifyMenuSectionDisplayed(section);
        screenshot(section + " Displayed");
    }

    @Then("I verify the {string} section is displayed")
    public void verifySectionDisplayed(String sectionName) {
        getSalesforceBasePage().verifySectionDisplayed(sectionName);
    }

    @Then("I should see a list view deleted confirmation")
    public void verifyListViewDeletedConfirmation() {
        nav().waitForToast("success");
    }

    @When("I wait for {int} seconds")
    public void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
            stepLogger.info("Waited for {} seconds", seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stepLogger.warn("Wait interrupted: {}", e.getMessage());
        }
    }
}
