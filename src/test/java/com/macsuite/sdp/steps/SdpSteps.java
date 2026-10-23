package com.macsuite.sdp.steps;

import com.macsuite.common.ModuleSteps;
import com.macsuite.sdp.pages.SdpPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step definitions for SDP (State Directed Payments) scenarios.
 * Covers record lifecycle, mandatory field validation, status transitions,
 * naming convention, PDF generation, dashboards, and Rating Period iframe.
 */
public class SdpSteps extends ModuleSteps<SdpPage> {
    private static final Logger logger = LoggerFactory.getLogger(SdpSteps.class);

    @Override
    protected SdpPage createModulePage() {
        return new SdpPage(page());
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    @When("I navigate to the SDP record creation form")
    public void navigateToSdpRecordCreationForm() {
        logger.info("Navigating to SDP record creation form");
        getModulePage().navigateToSdpRecordForm();
    }

    // ─── TC_001: Mandatory field validation ──────────────────────────────────

    @Then("I should see mandatory field error messages on the blank SDP record form")
    public void verifyMandatoryFieldErrors() {
        logger.info("Verifying mandatory field error messages");
        getModulePage().verifyMandatoryFieldErrors();
        screenshot("SDP Mandatory Field Errors");
    }

    @When("I verify input field {string} accepts text and shows validation")
    public void verifyInputField(String fieldName) {
        logger.info("Verifying input field: {}", fieldName);
        getModulePage().verifyInputFieldFunctionality(fieldName);
    }

    @When("I verify dropdown field {string} with option {string}")
    public void verifyDropdownField(String fieldName, String option) {
        logger.info("Verifying dropdown field '{}' with option '{}'", fieldName, option);
        getModulePage().verifyDropdownFieldFunctionality(fieldName, option);
    }

    @When("I verify search field {string} search and selection")
    public void verifySearchField(String fieldName) {
        logger.info("Verifying search field: {}", fieldName);
        getModulePage().verifySearchFieldFunctionality(fieldName);
    }

    @When("I verify date field {string} with offset {int}")
    public void verifyDateField(String fieldName, int offsetDays) {
        logger.info("Verifying date field '{}' with offset {}", fieldName, offsetDays);
        getModulePage().verifyDateFieldFunctionality(fieldName, offsetDays);
    }

    @When("I handle the error snag on the SDP form")
    public void handleErrorSnag() {
        logger.info("Handling error snag on SDP form");
        getModulePage().handleErrorSnag();
    }

    @When("I drag the {string} option to selected")
    public void dragOptionToSelected(String optionName) {
        logger.info("Dragging option '{}' to selected", optionName);
        getModulePage().dragOptionToSelected(optionName);
    }

    @When("I verify drag option field {string}")
    public void verifyDragOptionField(String fieldName) {
        logger.info("Verifying drag option field: {}", fieldName);
        getModulePage().verifyDragOptionField(fieldName);
    }

    @When("I close the SDP record form without saving")
    public void closeSdpRecordFormWithoutSaving() {
        logger.info("Closing SDP record form without saving");
        getModulePage().closeSdpRecordFormWithoutSaving();
    }

    @Then("I should see the SDP record form closed")
    public void verifySdpRecordFormClosed() {
        logger.info("Verifying SDP record form is closed");
        getModulePage().verifySdpRecordFormClosed();
        screenshot("SDP Form Closed");
    }

    // ─── TC_002: Payment date boundary ───────────────────────────────────────

    @When("I fill in the SDP record details without a status but requiring a parent record")
    public void fillSdpDetailsNoStatusWithParent() {
        logger.info("Filling SDP record details — no status, with parent record");
        getModulePage().fillSdpRecordDetails("", true);
    }

    @When("I validate Payment Arrangement Start Date boundary values")
    public void validatePaymentArrangementStartDateBoundary() {
        logger.info("Validating Payment Arrangement Start Date boundary values");
        getModulePage().validatePaymentArrangementStartDateBoundary();
        screenshot("Payment Date Boundary Validation");
    }

    // ─── TC_003 / TC_004: Create SDP record with status ──────────────────────

    @When("I fill in the SDP record details with status {string} requiring a parent record")
    public void fillSdpDetailsWithStatusAndParent(String status) {
        logger.info("Filling SDP record details — status='{}', with parent record", status);
        getModulePage().fillSdpRecordDetails(status, true);
    }

    @When("I select Key Contacts for the SDP record")
    public void selectKeyContactsForSdpRecord() {
        logger.info("Selecting Key Contacts for SDP record");
        getModulePage().selectKeyContactsForSdpRecord();
        screenshot("SDP Key Contacts Selected");
    }

    @When("I select a Provider Type for the SDP record")
    public void selectProviderTypeForSdpRecord() {
        logger.info("Selecting Provider Type for SDP record");
        getModulePage().selectProviderTypeForSdpRecord();
    }

    @Then("I should see the SDP record updated with Key Contacts and Provider Type")
    public void verifySdpRecordUpdatedWithKeyContactsAndProviderType() {
        logger.info("Verifying SDP record updated with Key Contacts and Provider Type");
        getModulePage().verifySdpRecordUpdatedWithKeyContactsAndProviderType();
        screenshot("SDP Key Contacts and Provider Type");
    }

    // ─── TC_005: Control Name duplicate + Naming Convention ──────────────────

    @When("I fill in the SDP record details with an empty status and requiring a parent record")
    public void fillSdpDetailsEmptyStatusWithParent() {
        logger.info("Filling SDP record details — empty status, with parent record");
        getModulePage().fillSdpRecordDetails("", true);
    }

    @When("I verify the Control Name duplicate error message")
    public void verifyControlNameDuplicateError() {
        logger.info("Verifying Control Name duplicate error message");
        getModulePage().verifyControlNameDuplicateError();
        screenshot("SDP Control Name Duplicate Error");
    }

    @When("I verify naming convention updates after Rating Period End Date change")
    public void verifyNamingConventionAfterRatingPeriodEndDateChange() {
        logger.info("Verifying naming convention after Rating Period End Date change");
        getModulePage().verifyNamingConventionAfterRatingPeriodEndDateChange();
        screenshot("SDP Naming Convention Updated");
    }

    // ─── TC_006: Review Type transitions + CMS Clock + Most Recent Action ────

    @When("I fill in the SDP record details with no status and no parent record requirement")
    public void fillSdpDetailsNoStatusNoParent() {
        logger.info("Filling SDP record details — no status, no parent record");
        getModulePage().fillSdpRecordDetails("", false);
    }

    @When("I change the Review Type to {string}")
    public void changeReviewType(String reviewType) {
        logger.info("Changing Review Type to: {}", reviewType);
        getModulePage().changeReviewType(reviewType);
    }

    @Then("I should see a parent record alert message for Amendment")
    public void verifyParentRecordAlertForAmendment() {
        logger.info("Verifying parent record alert for Amendment");
        getModulePage().verifyParentRecordAlertForAmendment();
        screenshot("SDP Parent Record Alert Amendment");
    }

    @Then("I should see a parent record error message for Renewal")
    public void verifyParentRecordErrorForRenewal() {
        logger.info("Verifying parent record error for Renewal");
        getModulePage().verifyParentRecordErrorForRenewal();
        screenshot("SDP Parent Record Error Renewal");
    }

    @When("I link a parent SDP record and verify child record updates with the chosen Review Type")
    public void linkParentSdpRecordAndVerifyChildRecordUpdates() {
        logger.info("Linking parent SDP record and verifying child record updates");
        getModulePage().linkParentRecordAndVerifyChildRecordUpdates();
        screenshot("SDP Parent Record Linked");
    }

    @Then("I should see the CMS Clock field displayed on the SDP record")
    public void verifyCmsClockFieldDisplayed() {
        logger.info("Verifying CMS Clock field displayed");
        getModulePage().verifyCmsClockFieldDisplayed();
        screenshot("SDP CMS Clock");
    }

    @Then("I should see the most recent action activity on the SDP record")
    public void verifyMostRecentActionActivity() {
        logger.info("Verifying Most Recent Action activity");
        getModulePage().verifyMostRecentActionActivity();
        screenshot("SDP Most Recent Action");
    }

    // ─── TC_007: PDF generation ───────────────────────────────────────────────

    @When("I generate a PDF for the SDP record")
    public void generatePdfForSdpRecord() {
        logger.info("Generating PDF for SDP record");
        getModulePage().generatePdf();
    }

    @Then("I should see the PDF created successfully")
    public void verifyPdfCreatedSuccessfully() {
        logger.info("Verifying PDF created successfully");
        getModulePage().verifyPdfCreated();
        screenshot("SDP PDF Created");
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @When("I navigate to the SDP dashboard page")
    public void navigateToSdpDashboardPage() {
        logger.info("Navigating to SDP dashboard page");
        getModulePage().navigateToDashboardPage();
    }

    @Then("I should see all SDP dashboards displayed")
    public void verifyAllSdpDashboardsDisplayed() {
        logger.info("Verifying all SDP dashboards displayed");
        getModulePage().verifyAllSdpDashboardsDisplayed();
        screenshot("SDP All Dashboards");
    }

    @And("I should see the {string} report count widget")
    public void verifyReportCountWidget(String widgetName) {
        logger.info("Verifying report count widget: {}", widgetName);
        getModulePage().verifyReportCountWidget(widgetName);
        screenshot("SDP Widget: " + widgetName);
    }

    // ─── Rating Period ────────────────────────────────────────────────────────

    @When("I navigate to the SDP Rating Period screen")
    public void navigateToSdpRatingPeriodScreen() {
        logger.info("Navigating to SDP Rating Period screen");
        getModulePage().navigateToSdpRatingPeriodScreen();
    }

    @When("I switch to the SDP Rating Period iframe")
    public void switchToSdpRatingPeriodIframe() {
        logger.info("Switching to SDP Rating Period iframe");
        getModulePage().switchToSdpRatingPeriodIframe();
    }

    @Then("I should see {string} status count displayed correctly")
    public void verifyStatusCountDisplayed(String status) {
        logger.info("Verifying status count displayed for: {}", status);
        getModulePage().verifyStatusCountDisplayed(status);
        screenshot("SDP Status Count: " + status);
    }

    @Then("I should see combined {string} status count displayed correctly")
    public void verifyCombinedStatusCountDisplayed(String combinedStatus) {
        logger.info("Verifying combined status count for: {}", combinedStatus);
        getModulePage().verifyCombinedStatusCountDisplayed(combinedStatus);
        screenshot("SDP Combined Status: " + combinedStatus);
    }
}
