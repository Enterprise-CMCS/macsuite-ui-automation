package com.macsuite.outreach.steps;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.ModuleSteps;
import com.macsuite.outreach.pages.OutreachPage;
import com.microsoft.playwright.Locator;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for CMCS OutReach workflows.
 * Handles Emergency OutReach creation, submission, and status progression.
 */
public class OutreachSteps extends ModuleSteps<OutreachPage> {
    private static final Logger logger = LoggerFactory.getLogger(OutreachSteps.class);

    @Override
    protected OutreachPage createModulePage() {
        return new OutreachPage(page());
    }

    @Then("I should see the CMCS OutReach form")
    public void verifyOutreachFormDisplayed() {
        logger.info("Verifying CMCS OutReach form is displayed");
        getModulePage().verifyOutreachFormDisplayed();
    }

    @When("I click Create a New Event button")
    public void clickCreateNewEventButton() {
        logger.info("Clicking Create a New Event button");
        getModulePage().clickCreateNewEventButton();
    }

    @When("I select outreach event type {string} and click Next")
    public void selectOutreachEventTypeAndClickNext(String eventType) {
        logger.info("Selecting outreach event type and proceeding: {}", eventType);
        getModulePage().selectEventTypeAndClickNext(eventType);
    }

    @When("I fill Emergency OutReach details with:")
    public void fillEmergencyOutreachDetails(DataTable dataTable) {
        logger.info("Filling Emergency OutReach details");
        Map<String, String> fieldValues = dataTable.asMap(String.class, String.class);
        getModulePage().fillEmergencyOutreachDetails(fieldValues);
        screenshot("Emergency Outreach Form Filled");
    }

    @When("I click the Submit button")
    public void clickSubmitButton() {
        logger.info("Clicking Submit button");
        getModulePage().clickSubmitButton();
    }

    @When("I click the Request Review button")
    public void clickRequestReviewButton() {
        logger.info("Clicking Request Review button");
        getModulePage().clickRequestReviewButton();
    }

    @When("I click the Approve button")
    public void clickApproveButton() {
        logger.info("Clicking Approve button");
        getModulePage().clickApproveButton();
    }

    @When("I click the Publish button")
    public void clickPublishButton() {
        logger.info("Clicking Publish button");
        getModulePage().clickPublishButton();
    }

    @Then("the outreach record status should be {string}")
    public void verifyOutreachStatus(String expectedStatus) {
        logger.info("Verifying OutReach status is: {}", expectedStatus);
        getModulePage().verifyOutreachStatus(expectedStatus);
        screenshot("Outreach Status: " + expectedStatus);
    }

    // ─── General form ──────────────────────────────────────────────────────────

    @When("I fill General OutReach event details with:")
    public void fillGeneralOutreachDetails(Map<String, String> details) {
        logger.info("Filling General Outreach event details");
        getModulePage().fillGeneralOutreachDetails(details);
        screenshot("General Outreach Form Filled");
    }

    @Then("I should see the General Outreach event details are correct")
    public void verifyGeneralOutreachEventDetails() {
        logger.info("Verifying General Outreach event details");
        getModulePage().verifyGeneralOutreachEventDetails();
        screenshot("General Outreach Details");
    }

    // ─── Records navigation ────────────────────────────────────────────────────

    @When("I navigate to CMCS Outreach records")
    public void navigateToCmcsOutreachRecords() {
        logger.info("Navigating to CMCS Outreach records");
        getModulePage().navigateToCmcsOutreachRecords();
    }

    @Then("I should see outreach records listed")
    public void verifyOutreachRecordsListed() {
        logger.info("Verifying outreach records listed");
        getModulePage().verifyOutreachRecordsListed();
        screenshot("Outreach Records List");
    }

    @Given("I select first {string} with {string} status")
    public void iSelectFirstWithStatus(String topicName, String statusValue) {
        logger.info("Selecting first {} with {} status", topicName, statusValue);

        // 1. Target all data rows inside the Salesforce datatable body
        Locator tableRows = page().locator("table.slds-table tbody tr");

        // 2. Filter the rows down to the specific row that contains BOTH required text
        // values
        Locator targetRow = tableRows
                .filter(new Locator.FilterOptions().setHasText(topicName))
                .filter(new Locator.FilterOptions().setHasText(statusValue))
                .first(); // Ensures we grab only the first match

        // 3. Find the row selection checkbox inside that specific row and click it
        // Salesforce row checkboxes are typically wrapped inside a span or primitive
        // cell
        targetRow.locator("input[type='checkbox']").click();

        // 4. Click on the row link to open the record
        targetRow.locator("a").first().click();
        logger.info("Navigated to {} record with {} status", topicName, statusValue);
    }

    @When("I navigate to an existing Emergency Outreach SOE record")
    public void navigateToExistingEmergencyOutreachSoeRecord() {
        logger.info("Navigating to existing Emergency Outreach SOE record");
        getModulePage().navigateToExistingEmergencyOutreachSoeRecord();
    }

    // ─── State Outreach creation ───────────────────────────────────────────────

    @When("I create a State Outreach record linked to this general event")
    public void createStateOutreachLinkedToEvent() {
        logger.info("Creating State Outreach record linked to event");
        getModulePage().validateStateOutreach_MandatoryFields();
        getModulePage().createStateOutreachLinkedToEvent_General();
    }

    @When("I create a State Outreach record linked to this emergency event")
    public void createStateOutreachLinkedToEmergencyEvent() {
        logger.info("Creating State Outreach record linked to emergency event");
        getModulePage().validateStateOutreach_MandatoryFields();
        getModulePage().createStateOutreachLinkedToEmergencyEvent();
    }

    @Then("I should see the State Outreach record created successfully")
    public void verifyStateOutreachCreatedSuccessfully() {
        logger.info("Verifying State Outreach record was created successfully");
        getModulePage().verifyStateOutreachCreatedSuccessfully();
        screenshot("State Outreach Created");
    }

    @When("I open the first State Outreach related to the Outreach Event")
    public void openFirstStateOutreachRelatedToEvent() {
        logger.info("Opening first State Outreach related to the Outreach Event");
        getModulePage().openFirstStateOutreachRelatedToEvent();
    }

    @When("I set the State Lead and Date Initiated on the State Outreach")
    public void setStateLeadAndDateInitiated() {
        logger.info("Setting State Lead and Date Initiated");
        getModulePage().setStateLeadAndDateInitiated();
    }
    // ─── Color alerts ─────────────────────────────────────────────────────────

    @When("I select HHS declaration checkbox and URL")
    public void selectHHSDeclarationCheckboxAndURL() {
        logger.info("Selecting HHS declaration checkbox and URL");
        getModulePage().selectCheckBox_FillURL();
    }

    @Then("I should see the {int}-day alert indicator on the State Outreach")
    public void verifyDayAlert(int days) {
        logger.info("Setting HHS Secretary declaration dates");
        getModulePage().setHHS_DeclerationDates(days);
        logger.info("Verifying {}-day alert indicator", days);
        getModulePage().verifyColorOfAlertForDays(days);
        screenshot("Alert " + days + "-Day");
    }

    // ─── Mandatory field validation ────────────────────────────────────────────

    @When("I attempt to create a State Outreach without required fields")
    public void attemptCreateStateOutreachWithoutRequiredFields() {
        logger.info("Attempting State Outreach creation without required fields");
        getModulePage().attemptCreateStateOutreachWithoutRequiredFields();
    }

    @Then("I should see mandatory field error messages on the blank Emergency State Outreach form")
    public void verifyMandatoryFieldErrors() {
        logger.info("Verifying mandatory field error messages");
        getModulePage().verifyMandatoryFieldErrors();
        screenshot("Mandatory Field Errors");
    }

    @Then("I should see mandatory field error messages on the blank Emergency Outreach event form")
    public void verifyEmergencyEventMandatoryFieldErrors() {
        logger.info("Verifying Emergency Outreach event mandatory field error messages");
        getModulePage().verifyEmergencyEventMandatoryFieldErrors();
        screenshot("Emergency Event Mandatory Field Errors");
    }

    @Then("the removed Presidential Declaration fields should not display")
    public void verifyRemovedPresidentialDeclarationFieldsDoNotDisplay() {
        logger.info("Verifying removed Presidential Declaration fields do not display");
        getModulePage().verifyRemovedPresidentialDeclarationFieldsDoNotDisplay();
    }

    @When("I validate Secretary Declaration Date fields")
    public void validateSecretaryDeclarationDateFields() {
        logger.info("Validating Secretary Declaration Date fields");
        getModulePage().validateSecretaryDeclarationDateFields();
    }

    // ─── Email response ────────────────────────────────────────────────────────

    @When("I create an outreach email response with template {string}")
    public void createOutreachEmailResponse(String template) {
        logger.info("Creating outreach email response with template: {}", template);
        getModulePage().createOutreachEmailResponse(template);
    }

    @When("I preview the outreach email content")
    public void previewOutreachEmailContent() {
        logger.info("Previewing outreach email content");
        getModulePage().previewOutreachEmailContent();
    }

    @When("I send the outreach email response")
    public void sendOutreachEmailResponse() {
        logger.info("Sending outreach email response");
        getModulePage().sendOutreachEmailResponse();
    }

    @Then("I should see the emailed date recorded")
    public void verifyEmailedDateRecorded() {
        logger.info("Verifying emailed date recorded");
        getModulePage().verifyEmailedDateRecorded();
        screenshot("Email Date Recorded");
    }

    @When("I navigate back to the State Outreach record")
    public void navigateBackToStateOutreach() {
        logger.info("Navigating back to State Outreach record");
        getModulePage().navigateBackToStateOutreach();
    }

    // ─── Close / reopen / withdrawn lifecycle ─────────────────────────────────

    @When("I navigate to the Outreach Event from the State Outreach")
    public void navigateToOutreachEventFromStateOutreach() {
        logger.info("Navigating to Outreach Event from State Outreach");
        getModulePage().navigateToOutreachEventFromStateOutreach();
    }

    @When("I initiate closing the outreach event")
    public void initiateClosingOutreachEvent() {
        logger.info("Initiating closing the outreach event");
        getModulePage().initiateClosingOutreachEvent();
    }

    @When("I verify outreach cannot close without confirmation")
    public void verifyOutreachCannotCloseWithoutConfirmation() {
        logger.info("Verifying outreach cannot close without confirmation");
        getModulePage().verifyOutreachCannotCloseWithoutConfirmation();
    }

    @Then("I should see the close date recorded")
    public void verifyCloseDateRecorded() {
        logger.info("Verifying close date recorded");
        getModulePage().verifyCloseDateRecorded();
        screenshot("Close Date Recorded");
    }

    @Then("I verify close date cannot be a future date for {string}")
    public void verifyCloseDateCannotBeFuture(String recordType) {
        logger.info("Verifying close date cannot be future for: {}", recordType);
        getModulePage().verifyCloseDateCannotBeFuture(recordType);
        screenshot("Close Date Error: " + recordType);
    }

    @When("I verify the final {string} status")
    public void verifyFinalStatus(String status) {
        logger.info("Verifying final status: {}", status);
        getModulePage().verifyFinalStatus(status);
    }

    @Then("the outreach event status should be {string}")
    public void verifyOutreachEventStatus(String expectedStatus) {
        logger.info("Verifying outreach event status: {}", expectedStatus);
        getModulePage().verifyOutreachEventStatus(expectedStatus);
        screenshot("Outreach Event Status: " + expectedStatus);
    }

    @When("I update the outreach status to {string} with reason {string}")
    public void updateOutreachStatusToWithReason(String status, String reason) {
        logger.info("Updating outreach status to '{}' with reason: {}", status, reason);
        getModulePage().updateOutreachStatusTo(status, reason);
        screenshot("Status Updated: " + status);
    }

    @When("I update the outreach status to {string}")
    public void updateOutreachStatusToSimple(String status) {
        logger.info("Updating outreach status to: {}", status);
        getModulePage().updateOutreachStatusTo(status, null);
        screenshot("Status Updated: " + status);
    }

    // ─── Q&A ──────────────────────────────────────────────────────────────────

    @When("I add a Q&A entry to the State Outreach")
    public void addQAEntryToStateOutreach() {
        logger.info("Adding Q&A entry to State Outreach");
        getModulePage().addQAEntryToStateOutreach();
    }

    // ─── CMCS TA linkage ──────────────────────────────────────────────────────

    @When("I create a CMCS TA inquiry linked to the Emergency Outreach")
    public void createCmcsTaInquiryLinkedToEmergencyOutreach() {
        logger.info("Creating CMCS TA inquiry linked to Emergency Outreach");
        getModulePage().createCmcsTaInquiryLinkedToEmergencyOutreach();
    }

    @Then("I should see the CMCS TA inquiry created successfully")
    public void verifyCmcsTaInquiryCreatedSuccessfully() {
        logger.info("Verifying CMCS TA inquiry created successfully");
        getModulePage().verifyCmcsTaInquiryCreatedSuccessfully();
        screenshot("CMCS TA Inquiry Linked");
    }

    @Then("I should be able to update State Outreach info from the linked CMCS TA")
    public void verifyStateOutreachInfoUpdatableFromCmcsTa() {
        logger.info("Verifying State Outreach info updatable from CMCS TA");
        getModulePage().verifyStateOutreachInfoUpdatableFromCmcsTa();
        screenshot("SO Info From CMCS TA");
    }

    // ─── Field history ─────────────────────────────────────────────────────────

    @When("I update fields on the Emergency Outreach details screen")
    public void updateFieldsOnEmergencyOutreachDetailsScreen() {
        logger.info("Updating fields on Emergency Outreach details screen");
        getModulePage().updateFieldsOnEmergencyOutreachDetailsScreen();
    }

    @Then("I should see field history recorded on the {string} record")
    public void verifyFieldHistoryOnRecord(String recordName) {
        logger.info("Verifying field history on {} record", recordName);
        getModulePage().verifyFieldHistoryOnRecord();
        screenshot("Field History " + recordName);
    }

    @When("I update fields on the Emergency State Outreach record")
    public void updateFieldsOnEmergencyStateOutreach() {
        logger.info("Updating fields on Emergency State Outreach record");
        getModulePage().updateFieldsOnStateOutreach();
    }

    @When("I update the State Outreach Response and verify field history for {string} type")
    public void updateStateOutreachResponseAndVerifyFieldHistory(String type) {
        logger.info("Updating State Outreach Response and verifying field history for: {}", type);
        getModulePage().updateStateOutreachResponseAndVerifyFieldHistory(type);
       
    }

     @When("I update the State Outreach Response and verify field history for general type")
    public void updateStateOutreachResponseAndVerifyFieldHistory_General() {
        logger.info("Updating State Outreach Response and verifying field history for general type");
        getModulePage().updateStateOutreach_Response_VerifyFieldHistory_Tracking("general");
    }

    @When("I update fields on the Outreach Event details screen")
    public void updateFieldsOnOutreachEventDetailsScreen() {
        logger.info("Updating fields on Outreach Event details screen");
        getModulePage().updateFieldsOnOutreachEventDetailsScreen();
    }

    @When("I update fields on the State Outreach record")
    public void updateFieldsOnStateOutreachRecord() {
        logger.info("Updating fields on State Outreach record");
        getModulePage().updateFieldsOnStateOutreach();
    }

    // ─── Add State Outreach functionality ─────────────────────────────────────

    @When("I verify Add State Outreach functionality")
    public void verifyAddStateOutreachFunctionality() {
        logger.info("Verifying Add State Outreach functionality");
        getModulePage().verifyAddStateOutreachFunctionality();
    }

    @Then("I should see the State Outreach record added")
    public void verifyStateOutreachAdded() {
        logger.info("Verifying State Outreach record added");
        getModulePage().verifyStateOutreachAdded();
        screenshot("State Outreach Added");
    }

    // ─── List view / dashboard / reports ──────────────────────────────────────

    @When("I create a new Outreach list view with name {string}")
    public void createNewOutreachListView(String listViewName) {
        logger.info("Creating new Outreach list view: {}", listViewName);
        getModulePage().createNewOutreachListView(listViewName);
    }

    @When("I clone an Outreach event")
    public void cloneOutreachEvent() {
        logger.info("Cloning Outreach event");
        getModulePage().cloneOutreachEvent();
    }

    @Then("I should see the cloned Outreach event record")
    public void verifyClonedOutreachEvent() {
        logger.info("Verifying cloned Outreach event record");
        getModulePage().verifyClonedOutreachEvent();
        screenshot("Cloned Outreach Event");
    }

    @When("I navigate to the All Issues outreach report")
    public void navigateToAllIssuesOutreachReport() {
        logger.info("Navigating to All Issues outreach report");
        getModulePage().navigateToAllIssuesReport();
    }

    @Then("I should see the All Issues report displayed")
    public void verifyAllIssuesReportDisplayed() {
        logger.info("Verifying All Issues report displayed");
        getModulePage().verifyAllIssuesReportDisplayed();
        screenshot("All Issues Report");
    }

    @Then("I validate the mandatory fields on the General Outreach event details screen")
    public void I_validate_the_mandatory_fields_on_the_General_Outreach_event_details_screen() {
        logger.info("Validating mandatory fields on the General Outreach event details screen");
        getModulePage().validateMandatoryFieldsOnGeneralOutreachEventDetailsScreen();
        screenshot("Mandatory Fields Validation");
    }

    @Then("I upload the file")
    public void I_upload_the_file() {
        logger.info("Uploading the file");

        getModulePage().verifyFileUploadFunctionality();
        screenshot("File Uploaded");
    }
}
