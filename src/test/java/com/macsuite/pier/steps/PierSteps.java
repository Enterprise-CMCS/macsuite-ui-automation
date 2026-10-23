package com.macsuite.pier.steps;

import java.util.Map;

import com.macsuite.common.ModuleSteps;
import com.macsuite.common.TemplateValueResolver;
import com.macsuite.pier.pages.PierIssuePage;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * PIER-specific step definitions for initial migration smoke coverage.
 */
public class PierSteps extends ModuleSteps<PierIssuePage> {
    @Override
    protected PierIssuePage createModulePage() {
        return new PierIssuePage(page());
    }

    @Then("I should see the PIER issue form")
    public void verifyPierIssueForm() {
        logger.info("Verifying PIER issue form");
        getModulePage().verifyPierIssueFormDisplayed();
    }

    @When("I fill the PIER issue details with:")
    public void fillPierIssueDetails(Map<String, String> issueData) {
        logger.info("Filling PIER issue details");
        getModulePage().fillPierIssueDetails(issueData);
        screenshot("PIER Issue Form Filled");
    }

    @When("I update the PIER issue details with:")
    public void updatePierIssueDetails(Map<String, String> updates) {
        logger.info("Updating PIER issue details");
        getModulePage().updatePierIssueDetails(updates);
    }

    @And("I should see the PIER issue details are correct:")
    public void verifyPierIssueDetails(Map<String, String> expectedData) {
        logger.info("Verifying PIER issue details");
        getModulePage().verifyPierIssueDetails(expectedData);
        screenshot("PIER Issue Details");
    }

    @When("I create a new PIER list view with name {string}")
    public void createNewPierListView(String listViewName) {
        logger.info("Creating new PIER list view: {}", listViewName);
        getModulePage().createNewPierListView(listViewName);
    }

    @When("I clone a PIER event")
    public void clonePierEvent() {
        logger.info("Cloning PIER event");
        getModulePage().clonePierEvent();
    }

    @Then("I should see the cloned PIER event record")
    public void verifyClonedPierEventRecord() {
        logger.info("Verifying cloned PIER event record");
        getModulePage().verifyClonedPierEvent();
        screenshot("PIER Cloned Record");
    }

    // ─── Home page header + state names ───────────────────────────────────────

    @Then("I should see PIER Tracker home page header details")
    public void verifyPierTrackerHomePageHeaderDetails() {
        logger.info("Verifying PIER Tracker home page header details");
        getModulePage().verifyPierTrackerHomePageHeaderDetails();
        screenshot("PIER Home Page Header");
    }

    @Then("I should see all state names displayed")
    public void verifyAllStateNamesDisplayed() {
        logger.info("Verifying all state names displayed");
        getModulePage().verifyAllStateNamesDisplayed("PIER All Accounts");
        screenshot("PIER State Names");
    }

    @Then("I should see all state names displayed for list view {string}")
    public void verifyAllStateNamesDisplayedForListView(String listViewName) {
       
        String resolvedListViewName = TemplateValueResolver.resolve(listViewName);
        logger.info("Verifying all state names displayed for list view: {}", resolvedListViewName);
        getModulePage().verifyAllStateNamesDisplayed(resolvedListViewName);
        screenshot("PIER State Names");
    }

    // ─── PIER Cases lifecycle ──────────────────────────────────────────────────

    @When("I navigate to PIER Cases")
    public void navigateToPierCases() {
        logger.info("Navigating to PIER Cases");
        getModulePage().navigateToPierCases();
    }

    @Then("I should see the PIER Case form")
    public void verifyPierCaseFormDisplayed() {
        logger.info("Verifying PIER Case form is displayed");
        getModulePage().verifyPierCaseFormDisplayed();
    }

    @Then("I should see mandatory field error messages on the blank PIER Case form")
    public void verifyPierCaseMandatoryFieldErrors() {
        logger.info("Verifying PIER Case mandatory field error messages");
        getModulePage().verifyPierCaseMandatoryFieldErrors();
        screenshot("PIER Case Mandatory Field Errors");
    }

    @When("I fill the PIER Case details with:")
    public void fillPierCaseDetails(Map<String, String> caseData) {
        logger.info("Filling PIER Case details");
        getModulePage().fillPierCaseDetails(caseData);
        screenshot("PIER Case Form Filled");
    }

    @When("I update the PIER Case details")
    public void updatePierCaseDetails() {
        logger.info("Updating PIER Case details");
        getModulePage().updatePierCaseDetails();
    }

    @When("I update the PIER Case details with:")
    public void updatePierCaseDetailsWith(DataTable dataTable) {
        logger.info("Updating PIER Case details with feature-driven values");
        getModulePage().updatePierCaseDetails(dataTable.asMap(String.class, String.class));
    }

    @When("I progress the PIER Case through stages")
    public void progressPierCaseThroughStages() {
        logger.info("Progressing PIER Case through stages");
        getModulePage().progressPierCaseThroughStages();
    }

    @Then("I should see the PIER Case stage updated")
    public void verifyPierCaseStageUpdated() {
        logger.info("Verifying PIER Case stage updated");
        getModulePage().verifyPierCaseStageUpdated();
        screenshot("PIER Case Stage Updated");
    }

    @When("I verify outreach activity tracking on the PIER Case")
    public void verifyOutreachActivityTrackingOnPierCase() {
        logger.info("Verifying outreach activity tracking on PIER Case");
        getModulePage().verifyOutreachActivityTracking();
        screenshot("PIER Case Outreach Activity");
    }

    @When("I update the PIER Case year to {string}")
    public void updatePierCaseYear(String year) {
        logger.info("Updating PIER Case start year to: {}", year);
        getModulePage().updatePierCaseYear(year);
    }

    @When("I update the PIER Case details a second time")
    public void updatePierCaseDetailsSecondTime() {
        logger.info("Updating PIER Case details a second time");
        getModulePage().updatePierCaseDetailsSecondTime();
    }

    @When("I update the PIER Case details a second time with:")
    public void updatePierCaseDetailsSecondTimeWith(DataTable dataTable) {
        logger.info("Updating PIER Case details a second time with feature-driven values");
        getModulePage().updatePierCaseDetailsSecondTime(dataTable.asMap(String.class, String.class));
    }

    @When("I update the PIER Case owner to {string}")
    public void changePierCaseOwner(String ownerName) {
        logger.info("Updating PIER Case owner to: {}", ownerName);
        getModulePage().changePierCaseOwner(ownerName);
    }

    @When("I click the Change Owner button")
    public void clickChangeOwnerButton() {
        logger.info("Clicking the Change Owner button");
        getModulePage().clickChangeOwnerButton();
    }

    @When("I navigate to the field history tab on the PIER Case")
    public void navigateToCaseFieldHistoryTab() {
        logger.info("Navigating to field history tab on PIER Case");
        getModulePage().navigateToCaseFieldHistoryTab();
    }

    @Then("I should see PIER Case field history entries recorded")
    public void verifyPierCaseFieldHistoryEntries() {
        logger.info("Verifying PIER Case field history entries");
        getModulePage().verifyPierCaseFieldHistory();
        screenshot("PIER Case Field History");
    }

    @Then("I should see PIER Issue field history entries recorded")
    public void verifyPierIssueFieldHistoryEntries() {
        logger.info("Verifying PIER Issue field history entries");
        getModulePage().verifyIssueFieldHistory();
        screenshot("PIER Issue Field History");
    }

    // ─── PIER Issue extended lifecycle ────────────────────────────────────────

    @When("I attempt to save an empty PIER issue form")
    public void attemptSaveEmptyPierIssueForm() {
        logger.info("Attempting to save empty PIER issue form");
        getModulePage().attemptSaveEmptyPierIssueForm();
    }

    @Then("I should see mandatory field error messages on the blank PIER Issue form")
    public void verifyPierIssueMandatoryFieldErrors() {
        logger.info("Verifying PIER Issue mandatory field error messages");
        getModulePage().verifyPierIssueMandatoryFieldErrors();
        screenshot("PIER Issue Mandatory Field Errors");
    }

    @Then("I should see start year validation error {string}")
    public void verifyStartYearValidationError(String expectedError) {
        logger.info("Verifying start year validation error: {}", expectedError);
        getModulePage().verifyStartYearValidationError(expectedError);
        screenshot("PIER Issue Year Error");
    }

    @When("I update the PIER issue year to {string}")
    public void updatePierIssueYear(String year) {
        logger.info("Updating PIER issue start year to: {}", year);
        getModulePage().updatePierIssueYear(year);
    }

    @When("I update the PIER Issue profile section fields")
    public void updatePierIssueProfileSectionFields() {
        logger.info("Updating PIER Issue profile section fields");
        getModulePage().updateProfileSectionFields();
    }

    @When("I verify the forward data activity section on the PIER Issue")
    public void verifyForwardDataActivitySection() {
        logger.info("Verifying forward data activity section on PIER Issue");
        getModulePage().verifyForwardDataActivitySection();
        screenshot("PIER Forward Data Section");
    }

    @When("I verify the historical data activity section on the PIER Issue")
    public void verifyHistoricalDataActivitySection() {
        logger.info("Verifying historical data activity section on PIER Issue");
        getModulePage().verifyHistoricalDataActivitySection();
        screenshot("PIER Historical Data Section");
    }

    @When("I update the PIER issue with all field details")
    public void updatePierIssueWithAllFieldDetails() {
        logger.info("Updating PIER issue with all field details");
        getModulePage().updateIssueDetailsExtended();
    }

    @When("I update the PIER issue with:")
    public void updatePierIssueWith(DataTable dataTable) {
        logger.info("Updating PIER issue with feature-driven details");
        getModulePage().updateIssueDetailsExtended(dataTable.asMap(String.class, String.class));
    }

    @When("I navigate to the field history tab on the PIER Issue")
    public void navigateToIssueFieldHistoryTab() {
        logger.info("Navigating to field history tab on PIER Issue");
        getModulePage().navigateToIssueFieldHistoryTab();
    }

    @Then("I Deselect the Fixed Data")
    public void I_Deselect_the_Forward_Data() {
        logger.info("Deselecting the Forward Data");
        getModulePage().Check_UnCheck_FixedDate("Deselect Fixed Data");
    }

    @Then("I Select the Fixed Data and deselect the State will not fix Data")
    public void I_Select_the_Fixed_Data_and_deselect_the_State_will_not_fix_Data() {
        logger.info("Selecting the Fixed Data and deselecting the State will not fix Data");
        getModulePage().clickOnEdit();
        getModulePage().Check_UnCheck_FixedDate("Select Fixed Data");
        getModulePage().check_UnCheck_State_Will_Not_Fix("Deselect State will not fix Data");
    }

    @Then("I will validate the fixed data already marked error message")
    public void validateExpectedErrorMessage() {
        logger.info("Validating the expected error message");
        getModulePage().validateFixed_Data_Already_Marked_Error();
    }

}
