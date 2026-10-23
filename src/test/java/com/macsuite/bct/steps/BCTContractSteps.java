package com.macsuite.bct.steps;

import java.util.List;
import java.util.Map;

import com.macsuite.bct.pages.BCTDashboardPage;
import com.macsuite.bct.pages.ContractDetailPage;
import com.macsuite.bct.pages.ContractFormPage;
import com.macsuite.bct.pages.ContractListPage;
import com.macsuite.common.StepBase;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class BCTContractSteps extends StepBase {

    private ContractFormPage formPage;
    private ContractDetailPage detailPage;
    private ContractListPage listPage;
    private BCTDashboardPage dashboardPage;
    private String actualListViewName; // Store the actual list view name after timestamp replacement

    private ContractFormPage formPage() {
        if (formPage == null)
            formPage = new ContractFormPage(page());
        return formPage;
    }

    private ContractDetailPage detailPage() {
        if (detailPage == null)
            detailPage = new ContractDetailPage(page());
        return detailPage;
    }

    private ContractListPage listPage() {
        if (listPage == null)
            listPage = new ContractListPage(page());
        return listPage;
    }

    private BCTDashboardPage dashboardPage() {
        if (dashboardPage == null)
            dashboardPage = new BCTDashboardPage(page());
        return dashboardPage;
    }

    @When("I fill the contract form with:")
    public void fillContractForm(Map<String, String> formData) {
        formPage().fillForm(formData);
        screenshot("Contract Form Filled");
    }

    @When("I fill the {string} section with:")
    public void fillContractSection(String sectionName, Map<String, String> data) {
        formPage().fillForm(data);
    }

    @Then("I should see the contract details are correct")
    public void verifyContractDetails() {
        formPage().verifyContractCreated();
        screenshot("Contract Created");
    }

    @When("I click the first Contract Order Number link")
    public void clickFirstContractOrderNumberLink() {
        detailPage = detailPage().clickFirstContractOrderNumberLink();
    }

    @When("I edit the contract details")
    public void editContractDetails() {
        detailPage().navigateToTab("Details");
    }
    
    @When("I edit the details page")
    public void editDetailsPage() {
        detailPage().navigateToTab("Details");
        detailPage().clickRecordEditButton();
    }

    @When("I update the following fields:")
    public void updateContractFields(Map<String, String> updates) {
        detailPage().editContractFields(updates);
    }

    @And("I update the {string} section with:")
    public void updateContractSection(String sectionName, Map<String, String> data) {
        detailPage().updateSection(sectionName, data);
    }

    @Then("I should see the success message for updates")
    public void verifyUpdateSuccessMessage() {
        detailPage().verifyUpdateSuccess();
    }

    @Then("I should see the success message for Created")
    public void verifyCreateSuccessMessage() {
        detailPage().verifyCreateSuccess();
    }

    @When("I navigate to the {string} tab")
    public void navigateToTab(String tabName) {
        detailPage().navigateToTab(tabName);
    }

    @When("I navigate back in the browser")
    public void navigateBackInBrowser() {
        detailPage().navigateBackInBrowser();
    }

    @When("I navigate back to the record link")
    public void navigateBackToRecordLink() {
        detailPage = detailPage().navigateBackToRecordLink();
    }

    @When("I add a note with text {string}")
    public void addNoteWithText(String noteText) {
        detailPage().addNote(noteText);
    }

    @Then("I should see the note {string} displayed")
    public void verifyNoteDisplayed(String noteText) {
        detailPage().verifyNoteExists(noteText);
        screenshot("Note Displayed");
    }

    @Then("I should see field history for the following changes:")
    public void verifyFieldHistoryForChanges(List<String> expectedFields) {
        detailPage().verifyFieldHistoryTracking(expectedFields);
        screenshot("Field History");
    }

    @When("I select list view {string}")
    public void selectListView(String listViewName) {
        listPage().selectListView(listViewName);
    }

    @Then("Verify {string} list view is displayed")
    public void verifyListViewIsDisplayed(String listViewName) {
        listPage().selectListView(listViewName);
    }

    @When("I create a new list view with name {string}")
    public void createNewListView(String listViewName) {
        // Replace ${timestamp} placeholder with actual timestamp (MMDDYYHHMMSS format)
        listViewName = com.macsuite.common.TemplateValueResolver.resolve(listViewName);
        actualListViewName = listViewName; // Store for later verification
        listPage().createNewListView(listViewName);
    }

   

    @When("I create a {string} list with name {string}")
    public void createOrCloneListView(String action, String listViewName) {
        // Replace ${timestamp} placeholder with actual timestamp (MMDDYYHHMMSS format)
        listViewName = com.macsuite.common.TemplateValueResolver.resolve(listViewName);
        actualListViewName = listViewName; // Store for later verification
        listPage().createOrCloneListView(action, listViewName);
    }

    @Then("I should see the list view {string} is active")
    public void verifyListViewIsActive(String listViewName) {
        // Use the stored actual name if it contains any placeholder (${timestamp} or
        // ${timestamp-short})
        if (listViewName.contains("${") && actualListViewName != null) {
            listViewName = actualListViewName;
        }
        listPage().verifyListViewCloned(listViewName);
    }

    @When("I delete the current list view")
    public void deleteCurrentListView() {
        listPage().deleteCurrentListView();
    }

    @Then("I should see the contract header shows:")
    public void verifyContractHeaderShows(Map<String, String> expectedFields) {
        detailPage().verifyContractHeaderDetails(expectedFields);
    }

    @Then("I should see the contract number and order number in the header")
    public void verifyContractNumberAndOrderNumberInHeader() {
        detailPage().verifyContractNumberAndOrderNumberInHeader();
    }

    @When("I open the contract actions menu")
    public void openContractActionsMenu() {
        detailPage().openContractActionsMenu();
    }

    @When("I fill the Create Contract Action modal with:")
    public void fillCreateContractActionModal(Map<String, String> formData) {
        formPage().fillForm(formData);
    }

    @When("I select the {string} checkbox")
    public void selectCheckbox(String label) {
        detailPage().selectCheckboxInActiveDialog(label);
    }

    @Then("I should see the contract actions menu is displayed")
    public void verifyContractActionsMenuIsDisplayed() {
        detailPage().verifyContractActionsMenuIsDisplayed();
        screenshot("Contract Actions Menu");
    }

    @Then("I click on Contract Action Number under Contract Actions table")
    public void clickContractActionNumberLink() {
        detailPage().clickContractActionNumberLink();
    }

    @Then("I should not see New button")
    public void verifyNewButtonNotVisible() {
        detailPage().verifyButtonVisibility("New", false);
    }

    @Then("I should not see Edit button")
    public void verifyEditButtonNotVisible() {
        detailPage().verifyButtonVisibility("Edit", false);
    }

    @Then("I should not see Create Contract Action button")
    public void verifyCreateContractActionButtonNotVisible() {
        detailPage().verifyButtonVisibility("Create Contract Action", false);
    }

    @Then("I verify {string} button is displayed")
    public void verifyButtonIsDisplayed(String buttonName) {
        detailPage().verifyButtonVisibility(buttonName, true);
    }

    @Then("I should see the {string} button is displayed")
    public void verifyButtonIsDisplayedWithFeatureWording(String buttonName) {
        verifyButtonIsDisplayed(buttonName);
    }

    @Then("I should see the summary value {string} is displayed")
    public void verifySummaryValueDisplayed(String summaryName) {
        detailPage().verifySummaryValueDisplayed(summaryName);
    }

    @Then("I should see the {string} table displayed")
    public void verifyTableDisplayed(String tableName) {
        detailPage().verifyTableDisplayed(tableName);
    }

    @Then("I verify the {string} table columns are correct:")
    public void verifyTableColumnsAreCorrect(String tableName, DataTable expectedColumnsTable) {
        List<List<String>> rows = expectedColumnsTable.cells();
        List<String> expectedColumns;
        if (rows.size() == 1) {
            expectedColumns = rows.get(0);
        } else if (rows.stream().allMatch(row -> row.size() == 1)) {
            expectedColumns = rows.stream().map(row -> row.get(0)).toList();
        } else {
            throw new IllegalArgumentException(
                    "Expected table columns as a single row or a single-column list");
        }
        detailPage().verifyTableColumns(tableName, expectedColumns);
    }

    @When("I hover over the dropdown icon")
    public void hoverDropdownIcon() {
        detailPage().hoverDropdownIcon();
    }

    @Then("I should see the message {string} displayed")
    public void verifyTextMessageDisplayed(String message) {
        detailPage().verifyMessageDisplayed(message);
    }

    @When("I click View All")
    public void clickViewAll() {
        detailPage().clickViewAll();
    }

    @Then("I sum the {string} column in the {string} table")
    public void sumColumnInTable(String columnName, String tableName) {
        detailPage().sumColumnInTable(columnName, tableName);
    }

    @Then("match it with the {string} displayed")
    public void matchColumnTotalWithDisplayedMetric(String totalLabel) {
        detailPage().verifyTotalMatchesDisplayed(totalLabel);
    }

    @When("I open dashboard folder {string}")
    public void openDashboardFolder(String folderName) {
        dashboardPage().openDashboardFolder(folderName);
        screenshot("Dashboard Folder: " + folderName);
    }

    @Then("I should see breadcrumbs navigation")
    public void verifyBreadcrumbsNavigation() {
        dashboardPage().verifyBreadcrumbsVisible();
        screenshot("Breadcrumbs Navigation");
    }

    @Then("I should see {string} reports displayed")
    public void verifyReportsCountDisplayed(String expectedCount) {
        dashboardPage().verifyReportsCountDisplayed(expectedCount);
        screenshot(expectedCount + " Reports Displayed");
    }

    @When("I filter {string} with:")
    public void filterDashboard(String dashboardName, Map<String, String> filters) {
        dashboardPage().filterDashboard(dashboardName, filters);
    }

    @When("I click the dashboard Search button")
    public void clickDashboardSearchButton() {
        dashboardPage().clickSearchButton();
    }

    @Then("I verify {string} dashboard title is displayed")
    public void verifyDashboardTitleIsDisplayed(String dashboardTitle) {
        dashboardPage().verifyDashboardTitle(dashboardTitle);
        screenshot("Dashboard Title: " + dashboardTitle);
    }

    @Then("I should see the dashboard Legend and Contract Timeline Calendar")
    public void verifyDashboardLegendAndContractTimelineCalendar() {
        dashboardPage().verifyLegendAndContractTimelineCalendar();
        screenshot("Dashboard Legend and Contract Timeline Calendar");
    }

    @Then("I search for new list view")
    public void I_search_for_new_list_view() {
        listPage().searchNewListView();
    }
 @Then("I search for cloned list view")
    public void I_search_for_cloned_list_view() {
        listPage().searchClonedListView();
    }


}
