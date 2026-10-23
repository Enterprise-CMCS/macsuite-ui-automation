package com.macsuite.inquiry_tracker.steps;

import java.util.List;
import java.util.Map;

import com.macsuite.common.ModuleSteps;
import com.macsuite.inquiry_tracker.pages.InquiryTrackerPage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for CMCS Inquiry Tracker workflows.
 */
public class InquiryTrackerSteps extends ModuleSteps<InquiryTrackerPage> {

    @Override
    protected InquiryTrackerPage createModulePage() {
        return new InquiryTrackerPage(page());
    }

    @When("I start a new CMCS inquiry")
    public void startNewCmcsInquiry() {
        getModulePage().startNewInquiry();
    }

    @Then("I should see CMCS TA required validation messages on the blank inquiry form")
    public void verifyCmcsTaRequiredValidationMessagesOnBlankInquiryForm() {
        getModulePage().verifyBlankInquiryRequiredMessages();
        screenshot("Blank Form Validation");
    }

    @When("I select the first CMCS inquiry from the list")
    public void selectFirstCmcsInquiryFromList() {
        getModulePage().selectFirstInquiryFromList();
    }

    @When("I select CMCS inquiry {string} from the list")
    public void selectCmcsInquiryByIdFromList(String inquiryId) {
        getModulePage().selectInquiryById(inquiryId);
    }

    @Given("I select first {string} and in {string}")
    public void selectFirstCmcsInquiryByTypeAndStage(String inquiryType, String stage) {
        getModulePage().selectFirstInquiryFromList(inquiryType, stage);
    }

    @When("I fill CMCS inquiry details with:")
    public void fillCmcsInquiryDetails(Map<String, String> inquiryData) {
        getModulePage().fillInquiryDetails(inquiryData);
        screenshot("Inquiry Form Filled");
    }

    @When("I fill CMCS inquiry details dynamically with:")
    public void fillCmcsInquiryDetailsDynamically(Map<String, String> inquiryData) {
        getModulePage().fillInquiryDetails_Dynamically(inquiryData);
        screenshot("Inquiry Form Filled Dynamically");
    }

    @Then("I should see CMCS inquiry type {string} on the details page")
    public void verifyCmcsInquiryTypeOnDetailsPage(String inquiryType) {
        getModulePage().verifyInquiryTypeOnDetailsPage(inquiryType);
    }

    @Then("I should see {string} as {string} on the details page")
    public void verifyFieldValueOnDetailsPage(String fieldName, String expectedValue) {
        getModulePage().verifyDetailFieldValueOnDetailsPage(fieldName, expectedValue);
    }

    @Then("I should see required CMCS inquiry detail fields:")
    @Then("I should see required field indicators for:")
    public void verifyRequiredCmcsInquiryDetailFields(List<String> fieldNames) {
        getModulePage().verifyRequiredDetailFields(fieldNames);
    }

    @Then("I verify Supporting Groups and Triager fields should {string}")
    public void verifySupportingGroupsAndTriagerFields(String isDisplay) {
        getModulePage().verifySupportingGroupsAndTriager_Section_Display(isDisplay);
    }

    @Then("I should see CMCS category options for group {string}:")
    public void verifyCmcsCategoryOptionsForGroup(String groupName, List<String> expectedOptions) {
        getModulePage().verifyCategoryOptionsForGroup(groupName, expectedOptions);
    }

    @When("I add a CMCS inquiry question with:")
    public void addCmcsInquiryQuestionWith(Map<String, String> questionData) {
        getModulePage().addInquiryQuestion(questionData);
    }

    @When("I remember the current CMCS inquiry title")
    public void rememberCurrentCmcsInquiryTitle() {
        getModulePage().rememberCurrentInquiryTitle();
    }

    @When("I create a related CMCS inquiry with:")
    public void createRelatedCmcsInquiryWith(Map<String, String> inquiryData) {
        getModulePage().createRelatedInquiry(inquiryData);
    }

    @Then("I should not see the add related inquiry action on the child inquiry")
    public void verifyAddRelatedInquiryActionNotVisibleOnChildInquiry() {
        getModulePage().verifyRelatedInquiryActionHidden();
    }

    @When("I open the parent inquiry from related inquiries")
    public void openParentInquiryFromRelatedInquiries() {
        getModulePage().openParentInquiryFromRelatedInquiries();
    }

    @Then("I should return to the remembered CMCS inquiry")
    public void verifyReturnedToRememberedCmcsInquiry() {
        getModulePage().verifyReturnedToRememberedInquiry();
    }

    @When("I update CMCS inquiry for field history with:")
    public void updateCmcsInquiryForFieldHistoryWith(Map<String, String> fields) {
        getModulePage().updateInquiryForFieldHistory(fields);
    }

    @Then("I should see CMCS inquiry field history entries:")
    public void verifyCmcsInquiryFieldHistoryEntries(List<String> fieldNames) {
        getModulePage().verifyInquiryFieldHistoryEntries(fieldNames);
        screenshot("Field History");
    }

    @Then("I should return to the inquiry details view")
    public void verifyReturnToInquiryDetailsView() {
        getModulePage().verifyReturnedToInquiryDetailsView();
    }

    @Then("I validate WFTCL details on the screen")
    public void validateWftclDetailsOnScreen() {
        getModulePage().validateWftclDetailsOnScreen();
    }

    @When("I update CMCS inquiry fields and verify stage {string}:")
    public void updateCmcsInquiryFieldsAndVerifyStage(String expectedStage, Map<String, String> fields) {
        getModulePage().updateFieldsAndVerifyStage(fields, expectedStage);
        screenshot("Stage: " + expectedStage);
    }

    @When("I update CMCS inquiry reviewers and verify stage {string}:")
    public void updateCmcsInquiryReviewersAndVerifyStage(String expectedStage, Map<String, String> reviewers) {
        if (reviewers == null || reviewers.isEmpty()) {
            throw new IllegalArgumentException(
                    "Reviewer field/value is missing for stage '" + expectedStage + "'.");
        }

        Map.Entry<String, String> reviewerEntry = reviewers.entrySet().iterator().next();
        String reviewerField = reviewerEntry.getKey();
        String reviewer = reviewerEntry.getValue();

        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException(
                    "Reviewer value is missing for field '" + reviewerField + "' at stage '" + expectedStage + "'.");
        }

        getModulePage().updateInquiryReviewersAndVerifyStage(reviewerField, reviewer);
    }

    @When("I update CMCS inquiry reviewers and verify stage on WFTCL {string}:")
    public void updateCmcsInquiryReviewersAndVerifyStage_on_WFTCL(String expectedStage, Map<String, String> reviewers) {
        if (reviewers == null || reviewers.isEmpty()) {
            throw new IllegalArgumentException(
                    "Reviewer field/value is missing for stage '" + expectedStage + "'.");
        }

        Map.Entry<String, String> reviewerEntry = reviewers.entrySet().iterator().next();
        String reviewerField = reviewerEntry.getKey();
        String reviewer = reviewerEntry.getValue();

        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException(
                    "Reviewer value is missing for field '" + reviewerField + "' at stage '" + expectedStage + "'.");
        }

        getModulePage().updateInquiryReviewersAndVerifyStage_WFTCL(reviewerField, reviewer);
    }

    @When("I clear CMCS senior reviewer and verify stage {string}")
    public void clearCmcsSeniorReviewerAndVerifyStage(String expectedStage) {
        getModulePage().clearSeniorReviewerAndVerifyStage(expectedStage);
    }

    @When("I escalate CMCS inquiry with:")
    public void escalateCmcsInquiryWith(Map<String, String> escalationData) {
        getModulePage().escalateInquiry(escalationData);
        screenshot("Escalation Submitted");
    }

    @Then("I should see CMCS inquiry stage {string}")
    public void verifyCmcsInquiryStage(String expectedStage) {
        getModulePage().verifyInquiryStage(expectedStage);
        screenshot("Inquiry Stage: " + expectedStage);
    }

    @Then("I should see escalation details on CMCS inquiry")
    public void verifyEscalationDetailsOnCmcsInquiry() {
        getModulePage().verifyEscalationDetails();
        screenshot("Escalation Details");
    }

    @When("I create CMCS inquiry response with:")
    public void createCmcsInquiryResponseWith(Map<String, String> responseData) {
        getModulePage().createInquiryResponse(responseData);
        screenshot("Response Form Saved");
    }

    @When("I read the default response email template")
    public void readDefaultResponseEmailTemplate() {
        String template = getModulePage().readDefaultEmailTemplate();
        logger.info("Default email template (first 100 chars): {}",
                template.length() > 100 ? template.substring(0, 100) + "..." : template);
    }

    @When("I send the CMCS inquiry response")
    public void sendCmcsInquiryResponse() {
        getModulePage().sendInquiryResponse();
        screenshot("Response Sent");
    }

    @Then("I should see CMCS inquiry response sent confirmation")
    public void verifyCmcsInquiryResponseSentConfirmation() {
        getModulePage().verifyResponseSentConfirmation();
        screenshot("Response Sent Confirmation");
    }

    @Then("I should see the response sent date is today")
    public void verifyResponseSentDateIsToday() {
        getModulePage().verifyResponseSentDateIsToday();
        screenshot("Response Sent Date");
    }

    @Then("I should see CMCS inquiry action buttons:")
    public void verifyCmcsInquiryActionButtons(List<String> actionButtons) {
        getModulePage().verifyInquiryActionButtons(actionButtons);
    }

    @When("I open CMCS TA dashboards")
    public void openCmcsTaDashboards() {
        getModulePage().openDashboards();
    }

    @When("I open CMCS TA reports")
    public void openCmcsTaReports() {
        getModulePage().openReports();
    }

    @When("I select CMCS TA list view {string}")
    public void selectCmcsTaListView(String listViewName) {
        getModulePage().selectCmcsTaListView(listViewName);
    }

    @When("I create a new CMCS TA list view with name {string}")
    public void createNewCmcsTaListView(String listViewName) {
        getModulePage().createNewCmcsTaListView(listViewName);
    }

    @When("I clone the current CMCS TA list view")
    public void cloneCurrentCmcsTaListView() {
        getModulePage().cloneCurrentCmcsTaListView();
    }

    @When("I delete the current CMCS TA list view")
    public void deleteCurrentCmcsTaListView() {
        getModulePage().deleteCurrentCmcsTaListView();
    }

    @When("I clone the current inquiry")
    public void cloneCurrentInquiry() {
        getModulePage().cloneCurrentInquiry();
    }

    @When("I update the inquiry title to {string}")
    public void updateInquiryTitle(String newTitle) {
        getModulePage().updateInquiryTitle(newTitle);
    }

    @When("I put the inquiry on hold with reason {string}")
    public void putInquiryOnHold(String holdReason) {
        getModulePage().putInquiryOnHold(holdReason);
    }

    @When("I release the inquiry from hold with reason {string}")
    public void releaseInquiryFromHold(String releaseReason) {
        getModulePage().releaseInquiryFromHold(releaseReason);
    }

    @Then("I should see {string} on hold message")
    public void verifyOnHoldMessage(String expectedText) {
        getModulePage().verifyMessageDisplayed(expectedText);
        screenshot("Inquiry On Hold");
    }

    // ========== Inquiry Status Management (Withdrawn/Close/Reopen) ==========

    @When("I edit inquiry status")
    public void editInquiryStatus() {
        getModulePage().editInquiryStatus();
    }

    @When("I select inquiry status {string}")
    public void selectInquiryStatus(String status) {
        getModulePage().selectInquiryStatus(status);
    }

    @When("I select withdrawal reason {string}")
    public void selectWithdrawalReason(String reason) {
        getModulePage().selectWithdrawalReason(reason);
    }

    @When("I fill other withdrawal reason with {string}")
    public void fillOtherWithdrawalReason(String reason) {
        getModulePage().fillOtherWithdrawalReason(reason);
    }

    @When("I fill reopen reason with {string}")
    public void fillReopenReason(String reason) {
        getModulePage().fillReopenReason(reason);
    }

    @Then("I should see inquiry status {string}")
    public void verifyInquiryStatus(String expectedStatus) {
        getModulePage().verifyInquiryStatus(expectedStatus);
        screenshot("Inquiry Status: " + expectedStatus);
    }

    @Then("the withdrawn transition to {string} should be blocked")
    public void verifyWithdrawnTransitionBlocked(String attemptedStatus) {
        getModulePage().verifyWithdrawnTransitionBlocked(attemptedStatus);
        screenshot("Withdrawn transition blocked: " + attemptedStatus);
    }

    @Then("I should see closed date is today")
    public void verifyClosedDateIsToday() {
        getModulePage().verifyClosedDateIsToday();
        screenshot("Closed Date Today");
    }

    @Then("I should see closed date is empty")
    public void verifyClosedDateIsEmpty() {
        getModulePage().verifyClosedDateIsEmpty();
        screenshot("Closed Date Empty");
    }

    // Generic verification steps - delegate to page methods
    @Then("I should see the CMCS inquiry form")
    public void verifyCmcsInquiryFormDisplayed() {
        getModulePage().verifyInquiryFormDisplayed();
    }

    @Then("I should see CMCS TA list view controls")
    public void verifyCmcsTaListViewControls() {
        getModulePage().verifyListViewControls();
    }

    @Then("I should see CMCS TA dashboard content")
    public void verifyCmcsTaDashboardContent() {
        getModulePage().verifyDashboardContent();
        screenshot("CMCS TA Dashboard");
    }

    @Then("I should see CMCS TA reports content")
    public void verifyCmcsTaReportsContent() {
        getModulePage().verifyReportsContent();
        screenshot("CMCS TA Reports");
    }

    @Then("I should see the CMCS TA list view {string} is active")
    public void verifyCmcsTaListViewIsActive(String listViewName) {
        getModulePage().verifyCmcsTaListViewActive(listViewName);
        screenshot("List View Active: " + listViewName);
    }

    @Then("I should see the cloned CMCS TA list view is active")
    public void verifyClonedCmcsTaListViewActive() {
        getModulePage().verifyClonedCmcsTaListViewActive();
        screenshot("Cloned List View Active");
    }

    @Then("I should see the cloned inquiry form is displayed")
    public void verifyClonedInquiryFormDisplayed() {
        getModulePage().verifyClonedInquiryFormDisplayed();
        screenshot("Cloned Inquiry Form");
    }

    @Then("I should see the inquiry stage returned to {string}")
    public void verifyInquiryStageReturned(String expectedStage) {
        getModulePage().verifyInquiryStageReturned(expectedStage);
        screenshot("Stage Returned: " + expectedStage);
    }

    // ========== CMCS TA Reports Validation Steps ==========

    @When("I wait for dashboard to load")
    public void waitForDashboardToLoad() {
        getModulePage().waitForDashboardWidgetsToLoad();
    }

    @When("I click on CMCS TA report {string}")
    public void clickOnCmcsTaReport(String reportName) {
        getModulePage().clickReportByName(reportName);
    }

    @Then("I should see report page with title {string}")
    public void verifyReportPageTitle(String expectedTitle) {
        getModulePage().verifyReportPageTitle(expectedTitle);
        screenshot("Report: " + expectedTitle);
    }

    @When("I capture the top record count")
    public void captureTopRecordCount() {
        getModulePage().captureTopRecordCount();
    }

    @When("I scroll to bottom of report")
    public void scrollToBottomOfReport() {
        getModulePage().scrollToBottomOfReport();
    }

    @When("I capture the bottom record count")
    public void captureBottomRecordCount() {
        getModulePage().captureBottomRecordCount();
    }

    @Then("the top and bottom record counts should match")
    public void verifyRecordCountsMatch() {
        getModulePage().verifyRecordCountsMatch();
        screenshot("Record Count Match");
    }

    @When("I close the current report tab")
    public void closeCurrentReportTab() {
        getModulePage().closeCurrentReportTab();
    }

    @When("I validate all CMCS TA reports with matching record counts:")
    public void validateAllCmcsTaReports(io.cucumber.datatable.DataTable dataTable) {
        List<String> reportNames = dataTable.asList(String.class);
        int totalReports = reportNames.size();
        int currentReport = 0;

        for (String reportName : reportNames) {
            currentReport++;
            logger.info("[{}/{}] Validating report: {}", currentReport, totalReports, reportName);

            try {
                getModulePage().clickReportByName(reportName);
                getModulePage().verifyReportPageTitle(reportName);
                screenshot(String.format("[%02d/%02d] Report: %s", currentReport, totalReports, reportName));
                getModulePage().captureTopRecordCount();
                screenshot(String.format("[%02d/%02d] %s - Top Count", currentReport, totalReports, reportName));
                getModulePage().scrollToBottomOfReport();
                getModulePage().captureBottomRecordCount();
                screenshot(String.format("[%02d/%02d] %s - Bottom Count", currentReport, totalReports, reportName));
                getModulePage().verifyRecordCountsMatch();
                getModulePage().closeCurrentReportTab();
                logger.info("[{}/{}] ✓ Report '{}' validated successfully", currentReport, totalReports, reportName);
            } catch (AssertionError | Exception e) {
                logger.error("[{}/{}] ✗ Report '{}' validation FAILED: {}", currentReport, totalReports, reportName,
                        e.getMessage());
                screenshot("FAILED: " + reportName);
                throw new AssertionError("Report validation failed for '" + reportName + "': " + e.getMessage(), e);
            }
        }
        logger.info("All {} reports validated successfully", totalReports);
    }

    // ========== E2E Full Flow — Stage and Date Steps ==========

    @Then("I should see {string} stage is selected by default")
    public void verifyStageSelectedByDefault(String stage) {
        getModulePage().verifyStageSelectedByDefault(stage);
        screenshot("Default Stage: " + stage);
    }

    @When("I change the submitted date to last year")
    public void changeSubmittedDateToLastYear() {
        getModulePage().changeSubmittedDateToLastYear();
    }

    @Then("the inquiry title year should reflect last year")
    public void verifyInquiryTitleYearReflectsLastYear() {
        getModulePage().verifyInquiryTitleContainsLastYear();
    }

    @When("I restore the submitted date to today")
    public void restoreSubmittedDateToToday() {
        getModulePage().restoreSubmittedDateToToday();
    }

    @When("I ask a question {string}, answer it, then delete it")
    public void askQuestionAnswerAndDelete(String question) {
        getModulePage().askQuestionAnswerAndDelete(question);
    }

    // ========== E2E Full Flow — Button Visibility ==========

    @Then("I should not see the {string} button")
    public void verifyButtonNotVisible(String buttonName) {
        getModulePage().verifyButtonVisibility(buttonName, false);
    }

    @Then("I should see the {string} button")
    public void verifyButtonVisible(String buttonName) {
        getModulePage().verifyButtonVisibility(buttonName, true);
    }

    @Then("I should see {string} button is visible")
    public void verifyNamedButtonIsVisible(String buttonName) {
        getModulePage().verifyButtonVisibility(buttonName, true);
    }

    @Then("I should see {string} button is not visible")
    public void verifyNamedButtonIsNotVisible(String buttonName) {
        getModulePage().verifyButtonVisibility(buttonName, false);
    }

    @Then("I should see field {string} with value {string}")
    public void verifyFieldWithValue(String fieldName, String expectedValue) {
        getModulePage().verifyFieldWithValue(fieldName, expectedValue);
    }

    // ========== E2E Full Flow — Notes ==========

    @When("I add a note {string} to the inquiry")
    public void addNoteToInquiry(String noteText) {
        getModulePage().addNoteToInquiry(noteText);
    }

    @Then("I should see the note {string} is displayed")
    public void verifyNoteDisplayed(String noteText) {
        getModulePage().verifyNoteDisplayed(noteText);
        screenshot("Note: " + noteText.substring(0, Math.min(30, noteText.length())));
    }

    @When("I edit the last note to {string}")
    public void editLastNote(String newText) {
        getModulePage().editLastNote(newText);
    }

    @When("I delete the last note")
    public void deleteLastNote() {
        getModulePage().deleteLastNote();
    }

    @Then("I should see note deleted confirmation")
    public void verifyNoteDeleted() {
        getModulePage().verifyNoteDeleted();
        screenshot("Note Deleted");
    }

    // ========== E2E Full Flow — Navigation and Escalation ==========

    @When("I navigate back to the inquiry")
    public void navigateBackToInquiry() {
        getModulePage().navigateBackToInquiry();
    }

    @When("I update the escalation resolution date to today")
    public void updateEscalationResolutionDateToToday() {
        getModulePage().updateEscalationResolutionDateToToday();
    }

    // ========== E2E Full Flow — OGC Fields ==========

    @When("I enable OGC Consultation on the inquiry")
    public void enableOgcConsultation() {
        getModulePage().enableOgcConsultation();
    }

    @When("I fill {string} with a future date")
    public void fillFieldWithFutureDate(String fieldLabel) {
        getModulePage().fillDateFieldWithFutureDate(fieldLabel);
    }

    @When("I fill {string} with {string}")
    public void fillFieldWith(String fieldLabel, String value) {
        getModulePage().fillDateFieldWithSpec(fieldLabel, value);
    }

    // ========== E2E Full Flow — Response Verification ==========

    @Then("I should see response code starts with {string}")
    public void verifyResponseCodeStartsWith(String prefix) {
        getModulePage().verifyResponseCodeStartsWith(prefix);
        screenshot("Response Code");
    }

    @Then("I should see the response email subject includes the inquiry title")
    public void verifyResponseEmailSubjectIncludesTitle() {
        getModulePage().verifyResponseEmailSubjectIncludesTitle();
        screenshot("Response Email Subject");
    }

    @Then("I should see the response email address matches the contact email")
    public void verifyResponseEmailAddressMatchesContact() {
        getModulePage().verifyResponseEmailAddressMatchesContact();
        screenshot("Response Email Address");
    }

    @When("I preview the response email")
    public void previewResponseEmail() {
        getModulePage().previewResponseEmail();
    }

    @Then("I should see preview email contact and subject details")
    public void verifyPreviewEmailBasicDetails() {
        getModulePage().verifyPreviewEmailBasicDetails();
        screenshot("Email Preview");
    }

    @Then("I should see preview email contact, subject, CC, BCC and sent date")
    public void verifyPreviewEmailFullDetails() {
        getModulePage().verifyPreviewEmailFullDetails();
        screenshot("Email Preview Full");
    }

    @When("I close the response email preview")
    public void closeResponseEmailPreview() {
        getModulePage().closeResponseEmailPreview();
    }

    @Then("I should see response status is {string}")
    public void verifyResponseStatus(String expectedStatus) {
        getModulePage().verifyResponseStatus(expectedStatus);
        screenshot("Response Status: " + expectedStatus);
    }

    @Then("I should see the attached file details are captured")
    public void verifyAttachedFileDetails() {
        getModulePage().verifyAttachedFileDetails();
        screenshot("Attached File Details");
    }

    // ========== E2E Full Flow — Edit Button Count ==========

    @Then("I should see edit buttons are reduced after inquiry close")
    public void verifyEditButtonsReduced() {
        getModulePage().verifyEditButtonsReduced();
        screenshot("Edit Buttons Reduced");
    }

    @Then("I should see edit buttons are restored after reopen")
    public void verifyEditButtonsRestored() {
        getModulePage().verifyEditButtonsRestored();
        screenshot("Edit Buttons Restored");
    }

    // ========== E2E Full Flow — Response Field History (TC_009) ==========

    @When("I open the last sent response from the responses tab")
    public void openLastSentResponse() {
        getModulePage().openLastSentResponse();
    }

    @When("I update the sent response fields with:")
    public void updateSentResponseFields(Map<String, String> fields) {
        getModulePage().updateSentResponseFields(fields);
    }

    @Then("I should see response field history entries:")
    public void verifyResponseFieldHistoryEntries(List<String> fieldNames) {
        getModulePage().verifyResponseFieldHistoryEntries(fieldNames);
        screenshot("Response Field History");
    }

    // ========== E2E Full Flow — Response Details Field (TC_010) ==========

    @Then("I should see response details field value is displayed")
    public void verifyResponseDetailsFieldDisplayed() {
        getModulePage().verifyResponseDetailsFieldDisplayed();
        screenshot("Response Details Field");
    }

    @Then("I should see response details field history entry")
    public void verifyResponseDetailsFieldHistoryEntry() {
        getModulePage().verifyResponseDetailsFieldHistoryEntry();
        screenshot("Response Details History");
    }

    @When("I edit Response Details to {string} with notes {string}")
    public void editResponseDetailsField(String picklistValue, String notes) {
        getModulePage().editResponseDetailsField(picklistValue, notes);
    }

    @When("I upload the file {string} to the inquiry")
    public void uploadInquiryFile(String fileName) {
        getModulePage().uploadInquiryFile(fileName);
        screenshot("Inquiry File Uploaded");
    }

    // ========== WFTCL Inquiry Creation Steps ==========

    @When("I select WFTCL inquiry type")
    public void selectWftclInquiryType() {
        getModulePage().selectWftclInquiryType();
    }

    @When("I fill the inquiry group with {string}")
    public void fillInquiryGroupWith(String group) {
        getModulePage().fillInquiryGroupStandalone(group);
    }

    @When("I fill the inquiry category with {string}")
    public void fillInquiryCategoryWith(String category) {
        getModulePage().fillFirstAvailableWftclCategory(category);
    }

    // @When("I fill the inquiry category with first available WFTCL category")
    // public void fillFirstAvailableWftclCategory() {
    // getModulePage().fillFirstAvailableWftclCategory();
    // }

    @When("I select Sub-Category {string}")
    public void selectSubCategory(String option) {
        getModulePage().selectSubCategoryOption(option);
    }

    @When("I check {string} checkbox")
    public void checkCheckbox(String label) {
        getModulePage().checkCheckboxByLabel(label);
    }

    @When("I select State_Territory as {string}")
    public void selectStateTerritoryAs(String option) {
        getModulePage().selectFieldOption("State/Territory/Tribal/Other", option);
    }

    @When("I select Provision {string}")
    public void selectProvision(String option) {
        getModulePage().selectProvisionOnNewForm(option);
    }

    @When("I close the inquiry form")
    public void closeInquiryForm() {
        getModulePage().closeInquiryForm();
    }

    // ========== WFTCL Details Page Steps ==========

    @Then("the inquiry title should start with {string}")
    public void verifyInquiryTitleStartsWithPrefix(String prefix) {
        getModulePage().verifyInquiryTitleStartsWithPrefix(prefix);
    }

    @When("I enter inline edit mode on the inquiry")
    public void enterInlineEditModeOnInquiry() {
        getModulePage().enterInlineEditModeOnInquiry();
    }

    @When("I cancel the inline edit")
    public void cancelInlineEdit() {
        getModulePage().cancelInlineEditOnInquiry();
    }

    @When("I set Provision to {string} on the inquiry details page")
    public void setProvisionOnDetailsPage(String option) {
        getModulePage().setProvisionOnDetailsPage(option);
    }

    @When("I fill {string} with {int} characters")
    public void fillFieldWithCharacters(String fieldLabel, int count) {
        getModulePage().fillFieldWithCharacters(fieldLabel, count);
        screenshot("Char Limit: " + fieldLabel);
    }

    @Then("I should see {string} field value is capped at {int} characters")
    public void verifyFieldValueCappedAt(String fieldLabel, int maxChars) {
        getModulePage().verifyFieldValueCappedAt(fieldLabel, maxChars);
        screenshot("Char Cap: " + fieldLabel);
    }

    // ========== Dashboard Navigation Steps ==========

    @When("I view all available CMCS TA dashboards")
    public void viewAllAvailableCmcsTaDashboards() {
        getModulePage().viewAllCmcsTaDashboards();
    }

    @When("I open the first available CMCS TA dashboard")
    public void openFirstAvailableCmcsTaDashboard() {
        getModulePage().openFirstAvailableCmcsTaDashboard();
    }

    @Then("the dashboard title should match the selected dashboard name")
    public void verifyDashboardTitleMatchesSelected() {
        getModulePage().verifyDashboardTitleMatchesSelected();
        screenshot("Dashboard Title Match");
    }

    @Then("I should see dashboard refresh date is today")
    public void verifyDashboardRefreshDateIsToday() {
        getModulePage().verifyDashboardRefreshDateIsToday();
        screenshot("Dashboard Refresh Date");
    }

    @Then("I select supporting group {string}")
    public void I_select_supporting_group(String s) {
        getModulePage().selectRandomOptionFromDropdown(s);
    }

    @Then("I save the changes")
    public void I_save_the_changes() {
        getModulePage().saveChanges();
    }

    @Then("I navigate to mailosure {string} environment")
    public void navigateToMailosure(String environment) {
        getModulePage().navigateToMailosure(environment);
    }

    @When("I select supporting group triager {string}:")
    public void selectSupportingGroupTriager(String triagerField, Map<String, String> triagerData) {
        if (triagerData == null || triagerData.isEmpty()) {
            throw new IllegalArgumentException(
                    "Triager field/value is missing for '" + triagerField + "'.");
        }

        Map.Entry<String, String> entry = triagerData.entrySet().iterator().next();
        String triagerName = entry.getValue();

        if (triagerName == null || triagerName.isBlank()) {
            throw new IllegalArgumentException(
                    "Triager value is missing for field '" + triagerField + "'.");
        }

        // getModulePage().fillField(triagerField, triagerName);
        getModulePage().updateDropdownOption(triagerField, triagerName);

    }

    @Then("I validate the Emails received on the account of supporting group updated")
    public void I_validate_the_Emails_received_on_the_account_of_supporting_group_updated() {
        getModulePage().verifySupportingGroup_Emails();
    }
}
