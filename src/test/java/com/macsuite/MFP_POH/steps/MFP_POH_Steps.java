package com.macsuite.MFP_POH.steps;

import com.macsuite.MFP_POH.pages.AccountsPage;
import com.macsuite.MFP_POH.pages.ChecklistPage;
import com.macsuite.MFP_POH.pages.Document_Review;
import com.macsuite.MFP_POH.pages.FO_Field_History_Tracking;
import com.macsuite.MFP_POH.pages.Funding_Opportunity_Page;
import com.macsuite.MFP_POH.pages.Workbook_UnderReviewPage;
import com.macsuite.common.PdfValidator;
import com.macsuite.common.StepBase;
import com.macsuite.common.pages.SalesforceBasePage;
import com.macsuite.core.PlaywrightManager;
import com.microsoft.playwright.options.AriaRole;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * MFP POH-specific step definitions for Account Creation and Checklist
 * scenarios.
 * Common steps (navigation, buttons, success verification) are reused from
 * CommonSteps.
 * MFP POH-specific actions are defined here.
 */
public class MFP_POH_Steps extends StepBase {

    private SalesforceBasePage salesforceBasePage;
    private String createdAccountId;
    private AccountsPage accountsPage;
    private Workbook_UnderReviewPage workbookUnderReviewPage;
    private ChecklistPage checklistPage;
    private PdfValidator pdfValidator;
    private Funding_Opportunity_Page fundingOpportunityPage;
    private FO_Field_History_Tracking fieldHistoryTrackingPage;
    private Document_Review documentReviewPage;

    private AccountsPage getAccountsPage() {
        if (accountsPage == null) {
            accountsPage = new AccountsPage(page());

        }
        return accountsPage;
    }

    private Workbook_UnderReviewPage getWorkbookUnderReviewPage() {
        if (workbookUnderReviewPage == null) {
            workbookUnderReviewPage = new Workbook_UnderReviewPage(page());
        }
        return workbookUnderReviewPage;
    }

    private ChecklistPage getChecklistPage() {
        if (checklistPage == null) {
            checklistPage = new ChecklistPage(page());
        }
        return checklistPage;
    }

    private PdfValidator getPdfValidator() {
        if (pdfValidator == null) {
            pdfValidator = new PdfValidator();
        }
        return pdfValidator;
    }

    private Funding_Opportunity_Page getFundingOpportunityPage() {
        if (fundingOpportunityPage == null) {
            fundingOpportunityPage = new Funding_Opportunity_Page(page());

        }
        return fundingOpportunityPage;
    }

    private FO_Field_History_Tracking getFieldHistoryTrackingPage() {
        if (fieldHistoryTrackingPage == null) {
            fieldHistoryTrackingPage = new FO_Field_History_Tracking(page());
        }
        return fieldHistoryTrackingPage;
    }

    private Document_Review getDocumentReviewPage() {
        if (documentReviewPage == null) {
            documentReviewPage = new Document_Review(page());
        }
        return documentReviewPage;
    }

    @Given("I kill the previous browser session and launch new browser instance")
    public void restartBrowserSession() {
        logger.info("Restarting Playwright browser session for current scenario");

        if (PlaywrightManager.isInitialized()) {
            PlaywrightManager.close(false, null);
        }

        PlaywrightManager.init("browser-restart-" + System.currentTimeMillis());
        resetCachedPages();
    }

    /**
     * Navigate to Account Creation page in the specified MFP POH app
     * Handles: Opening the app and navigating to the Account Creation page/list
     * view
     * 
     * @param appName The app name (e.g., "MFP POH")
     */
    @Given("I navigate to Account Creation page in {string} app")
    public void navigateToAccountCreationPage(String appName) {
        logger.info("Navigating to Account Creation page in {} app", appName);
       
        try {
            // Open the app (reuses CommonSteps pattern)
            getSalesforceBasePage().selectSalesforceApp(appName);
            logger.debug("Opened {} app", appName);

            // Wait for app to fully load
            page().waitForLoadState();

            getAccountsPage().navigateToAccountsView();
            screenshot("Navigated to Accounts View page");
            logger.info("✓ Successfully navigated to Account Creation page in {} app", appName);
        } catch (Exception e) {
            logger.error("Failed to navigate to Account Creation page: {}", e.getMessage());
            throw new RuntimeException("Could not navigate to Account Creation page: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new account with basic required information
     * Assumes the user is on the Accounts list view
     */
    @When("I create a new account")
    public void createNewAccount() {
        logger.info("Creating a new account");

        try {
            // Click the New button to open account creation form
            page().getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Page.GetByRoleOptions()
                    .setName("New")).click();

            page().locator(".isModal.inlinePanel:visible").first().waitFor();

            logger.info("Account creation form opened");
            screenshot("Account creation form opened");
            getAccountsPage().verifyMandatoryFieldErrors_While_AccountCreation();
            getAccountsPage().enterAccountName();
            getAccountsPage().selectMFPPOHAccountRole();
            getAccountsPage().verifyMandatoryFields_While_Filling_AccountDetails();
            getAccountsPage().fillAccountFormWithDefaultValues();

            logger.info("✓ Account form filled with default values");
        } catch (Exception e) {
            logger.error("Failed to create account: {}", e.getMessage());
            throw new RuntimeException("Could not create new account: " + e.getMessage(), e);
        }
    }

    /**
     * Verify that the account was created successfully
     * Validates: Success message appears and account details are saved
     */
    @Then("the account should be created successfully")
    public void verifyAccountCreatedSuccessfully() {
        logger.info("Verifying account creation success");

        try {
            // Click Save button to save the account
            page().getByRole(AriaRole.BUTTON,
                    new com.microsoft.playwright.Page.GetByRoleOptions()
                            .setName("Save")
                            .setExact(true))
                    .click();

            // Wait for post-save UI stabilization
            page().waitForLoadState();

            // Verify success message appears
            // getFormUtils().verifySuccessMessage();

            logger.info("✓ Account created successfully. Account ID: {}", createdAccountId);
        } catch (Exception e) {
            logger.error("Account creation verification failed: {}", e.getMessage());
            throw new AssertionError("Account was not created successfully: " + e.getMessage(), e);
        }
    }

    /**
     * Add a checklist item to the created account
     * Assumes the user is on the account detail page
     */
    @When("I add a checklist item to the created account")
    public void addChecklistItemToAccount() {
        logger.info("Adding a checklist item to the account");

        try {
            // getAccountsPage().verifyChecklistMandatoryFields();
            getAccountsPage().fillChecklistItemWithDefaultValues();

            logger.info("✓ Checklist item form filled");
        } catch (Exception e) {
            logger.error("Failed to add checklist item: {}", e.getMessage());
            throw new RuntimeException("Could not add checklist item: " + e.getMessage(), e);
        }
    }

    @When("I verify details under workbook under review stage")
    public void verifyWorkbookUnderReviewDetails() {
        logger.info("Verifying details under workbook under review stage");

        try {

            getWorkbookUnderReviewPage().verify_workbook_under_review_details();
            getWorkbookUnderReviewPage().verify_WFPB_Section_Displayed();

            logger.info("✓ Workbook under review details verified");
        } catch (Exception e) {
            logger.error("Failed to verify workbook under review details: {}", e.getMessage());
            throw new RuntimeException("Could not verify workbook under review details: " + e.getMessage(), e);
        }
    }

    @When("I navigate to field history tracking tab and verify the history of changes")
    public void verifyFieldHistory_Tracking() {
        logger.info("Verifying field history tracking");

        try {

            getWorkbookUnderReviewPage().VerifyFieldHistory_Tracking();
            getWorkbookUnderReviewPage().navigateToChecklistAndVerifyDetails();

            logger.info("✓ Field history details verified");
        } catch (Exception e) {
            logger.error("Failed to verify field history details: {}", e.getMessage());
            throw new RuntimeException("Could not verify field history details: " + e.getMessage(), e);
        }
    }

    @Then("I validate the checklist details in main window and sub window")
    public void I_validate_the_checklist_details_in_main_window_and_sub_window() {
        logger.info("Validating checklist details in main window and sub window");

        try {

            getWorkbookUnderReviewPage().verify_Checkist_Details_in_main_window_And_Rightside_details_section();

            logger.info("✓ Checklist details validated in main window and sub window");
        } catch (Exception e) {
            logger.error("Failed to validate checklist details: {}", e.getMessage());
            throw new RuntimeException("Could not validate checklist details: " + e.getMessage(), e);
        }
    }

    @When("I navigate to comments under review")
    public void I_navigate_to_comments_under_review() {
        getChecklistPage().navigateToCommentsUnderReview();
    }

    @Then("I verify the details of current stage comments under review stage")
    public void I_verify_the_details_of_current_stage_comments_under_review_stage() {
        getChecklistPage().verify_Details_In_Comments_Under_Review();
        getWorkbookUnderReviewPage().verifyFieldHistoryUpdate("Comments Under Review");
    }

    @When("I navigate to Recommendation stage")
    public void I_navigate_to_Recommendation_stage() {
        getChecklistPage().submit_Recommendation_Form();
    }

    @Then("I verify the details of current stage Recommendation stage")
    public void I_verify_the_details_of_current_stage_Recommendation_stage() {
        getChecklistPage().verify_Details_In_Recommendation();
        getWorkbookUnderReviewPage().verifyFieldHistoryUpdate("Recommendation Sent");
        // String pdfContent = getPdfValidator().downloadPdfAndExtractText(page(),
        // "xpath=//*[@class='filerow']/parent::a");
        // getPdfValidator().validatePdfContent(pdfContent, "Budget Review –
        // Recommendation Report");
        // getPdfValidator().validatePdfContent(pdfContent, "Pankaj MFP App Ghosh");
        // getPdfValidator().validatePdfContent(pdfContent, "Budget Review Summary");
        // getPdfValidator().validatePdfContent(pdfContent, BasePage.RECEIPENT);
        // getPdfValidator().validatePdfContent(pdfContent, "Liane Grayson");
        // getPdfValidator().validatePdfContent(pdfContent, "Pankaj MFP App Ghosh");
        // getPdfValidator().validatePdfContent(pdfContent,
        // BasePage.GRANT_NUMBER_STRING);
        // getPdfValidator().validatePdfContent(pdfContent,
        // BasePage.CHECKLIST_RECORD_ID);
        // getPdfValidator().validatePdfContent(pdfContent, "Application Number");
        // getPdfValidator().validatePdfContent(pdfContent, "Recommendation Details");
        // getPdfValidator().validatePdfContent(pdfContent, "Fund with Restrictions");
        // getPdfValidator().validatePdfContent(pdfContent, "Restriction Amount");
        // getPdfValidator().validatePdfContent(pdfContent, "$900.00");
        // getPdfValidator().validatePdfContent(pdfContent, "Final Approved CY Budget
        // Amount");
        // getPdfValidator().validatePdfContent(pdfContent, "$90,000.00");
        // getPdfValidator().validatePdfContent(pdfContent, "Previous CY Unobligated
        // Balance");
        // getPdfValidator().validatePdfContent(pdfContent, "$100.00");
        // getPdfValidator().validatePdfContent(pdfContent, "Final Supplemental Funding
        // Amount");
        // getPdfValidator().validatePdfContent(pdfContent, "$500.00");
        // getPdfValidator().validatePdfContent(pdfContent, "Additional Details");
        // getPdfValidator().validatePdfContent(pdfContent, "Recommendation Project
        // Officer");
        // getPdfValidator().validatePdfContent(pdfContent, "Recommendation Date");
        // getPdfValidator().validatePdfContent(pdfContent,
        // getWorkbookUnderReviewPage().fetchWeekdayDate("MM/dd/yyyy", 0));

        // page().locator("xpath=(//button[@title='Close'])[last()]").first().click();

    }

    @Then("I navigate to Request Funding Memo stage")
    public void I_verify_the_details_of_current_stage_Request_Funding_Memo_stage() {
        getChecklistPage().navigateToRequestFundingMemoStage();

    }

    @When("I verify the details of current stage Request Funding Memo stage")
    public void I_navigate_to_Request_Funding_Memo_stage() {
        getChecklistPage().verify_Details_In_Request_Funding_Memo();
        getChecklistPage().verify_funding_Memo_On_Hold_Stage();
        getWorkbookUnderReviewPage().verifyFieldHistoryUpdate("Funding Memo Requested");
    }

    @When("I navigated to Converted To Funding stage")
    public void I_navigated_to_Converted_To_Funding_stage() {
        getChecklistPage().navigatedToConvertedToFundingStage();
    }

    @Then("I verify the details of current stage Converted To Funding stage")
    public void I_verify_the_details_of_current_stage_Converted_To_Funding_stage() {
        getChecklistPage().verify_Details_In_Converted_To_Funding();

    }

    @When("I navigte to already created account details page")
    public void I_navigte_to_already_created_account_details_page() {
        getAccountsPage().navigateToChildAccountDetailsPage("Pankaj_Test_Child_Account_Automation");
    }

    @Given("I navigate to Funding Opportunity page")
    public void I_navigate_to_Funding_Opportunity_page() {
        getFundingOpportunityPage().navigateToFundingOpportunityPage();
        getFundingOpportunityPage().verifyFundingOpportunityDetails();
    }

    @Then("I open document review modal popup and verify mandatory fields")
    public void I_open_document_review_modal_popup_and_verify_mandatory_fields() {
        getDocumentReviewPage().verifyDocumentReviewMandatoryFields();
    }

    @Then("I submit document review form and verify success message")
    public void I_submit_document_review_form_and_verify_success_message() {
        getDocumentReviewPage().fillDocumentReviewDetails();
    }

    @Then("I verify the Document Review header details")
    public void I_verify_the_Document_Review_header_details() {
        getDocumentReviewPage().verify_Document_Review_Header();
    }

    @Then("I add a task in document review modal popup and verify the task details")
    public void I_add_a_task_in_document_review_modal_popup_and_verify_the_task_details() {
        getDocumentReviewPage().addTaskInDocumentReviewModalPopup();
    }

    @When("I navigate to the task then I verify the task details")
    public void I_navigate_to_the_task_then_I_verify_the_task_details() {
        getDocumentReviewPage().navigateToTaskAndVerifyDetails();
    }

    @And("I complete the task and verify its status")
    public void I_complete_the_task_and_verify_its_status() {
        getDocumentReviewPage().completeTaskAndVerifyStatus();
        getDocumentReviewPage().verifyCancellation_AfterCompletion();
    }

    @When("I navigate to the related document and I add another task")
    public void I_navigate_to_the_related_document_and_I_add_another_task() {
        getDocumentReviewPage().navigateToRelatedDocumentAndAddTask();
    }

    @Then("I verify that the task is canceled")
    public void I_verify_that_the_task_is_canceled() {
        getDocumentReviewPage().navigateToTaskAndVerifyDetails();
        getDocumentReviewPage().verifyTaskCancellation();
        getDocumentReviewPage().verifyTaskComplete_After_Cancellation();
       
    }
    @Then("I verify the open and completed tasks in the related document")
    public void I_verify_the_open_and_completed_tasks_in_the_related_document() {
        getDocumentReviewPage().verifyOpen_And_Completeted_Tasks();
    }

    @Then("I verify Review Status Accepted in document review")
    public void I_verify_Review_Status_Accepted_in_document_review() {
        getDocumentReviewPage().verify_Review_Status_Accepted();
    }

    @Then("I verify Review Status Complete in document review")
    public void I_verify_Review_Status_Complete_in_document_review() {
        getDocumentReviewPage().verify_Review_Status_Complete();
    }

    @Then("I verify the Chatter functionality in document review modal popup")
    public void I_verify_the_Chatter_functionality_in_document_review_modal_popup() {
        getDocumentReviewPage().verify_Chatter_Functionality();
    }

    @Given("I navigate to Funding Opportunity page to verify the details of Field History Tracking")
    public void I_navigate_to_Funding_Opportunity_page_to_verify_the_details_of_Field_History_Tracking() {
        getFundingOpportunityPage().navigateToFundingOpportunityPage();
    }

    @Then("I navigate to child Account")
    public void I_navigate_to_child_Account() {
        getDocumentReviewPage().navigateToChildAccount();
    }

    @Then("I verify the Complete Review by clicking on Complete Review button and checking the review status")
    public void I_verify_the_Complete_Review_by_clicking_on_Complete_Review_button_and_checking_the_review_status() {
        getDocumentReviewPage().verifyReviewStatusComplete_From_CompleteReview();
    }
    

    @Then("I create another document review in child account")
    public void I_create_another_document_review_in_child_account() {
        getDocumentReviewPage().createDocumentReviewInChildAccount();
    }

    @When("I upload the funding memo document")
    public void I_upload_the_funding_memo_document() {
        getFundingOpportunityPage().verifyFileUploadFunctionality();
    }

    @Then("I verify the uploaded document details and validate the document content")
    public void I_verify_the_uploaded_document_details_and_validate_the_document_content() {

    }

    @When("I submit the Section checklist details and verify the details in checklist tab")
    public void I_submit_the_Section_checklist_details_and_verify_the_details_in_checklist_tab() {
        getFundingOpportunityPage().fill_Section_Checklist_Data();
    }

    @When("I skip previous checklist then i can validate error message for skipped checklist")
    public void I_skip_previous_checklist_then_i_can_validate_error_message_for_skipped_checklist() {
        getFundingOpportunityPage().verify_Checklist_Skip_Error_Message();
    }

    @When("I clear the Section checklist one by one and verify the Funding memo navigation back")
    public void I_clear_the_Section_checklist_one_by_one_and_verify_the_Funding_memo_navigation_back() {
        getFundingOpportunityPage().verifyFunding_Memo_Navigation_Back_After_Clearing_Checklist();
    }

    @Then("I verify the details in checklist tab after clearing the checklist details")
    public void I_verify_the_details_in_checklist_tab_after_clearing_the_checklist_details() {
        getFundingOpportunityPage().editChecklist();
        getFundingOpportunityPage().fill_Section_Checklist_Data();
        getFundingOpportunityPage().updateDateFields_VErify_Clearence_Checklist_Warning_Alert("No");

    }

    @When("I navigte to field history tracking tab")
    public void I_navigte_to_field_history_tracking_tab() {
        logger.info("Navigating to Field History Tracking tab");
        getFieldHistoryTrackingPage().navigateToFieldHistoryTrackingTab("After FO Checklist update");
    }

    @Then("I verify the history of changes for different fields in field history tracking tab")
    public void I_verify_the_history_of_changes_for_different_fields_in_field_history_tracking_tab() {
        logger.info("Verifying Field History Tracking display");
        getFieldHistoryTrackingPage().verifyFieldHistory_Display();
    }

    @When("I edit the Section checklist details")
    public void I_edit_the_Section_checklist_details() {
        getFundingOpportunityPage().editChecklist();
    }

    @Then("I verify the history of changes for different fields in field history tracking tab after clearing the checklist details")
    public void I_verify_the_history_of_changes_for_different_fields_in_field_history_tracking_tab_after_clearing_the_checklist_details() {
        getFieldHistoryTrackingPage().navigateToFieldHistoryTrackingTab("After Clearance Checklist update");
        getFieldHistoryTrackingPage().verifyFieldHistory_Display_AfterClearingChecklist();
    }

    @When("I clear the Section checklist to validate error message")
    public void I_clear_the_Section_checklist_to_validate_error_message() {

        getFundingOpportunityPage().updateDateFields_VErify_Clearence_Checklist_Warning_Alert("Yes");
    }

    @When("I update the checklist status")
    public void I_update_the_checklist_status() {
        getFundingOpportunityPage().fill_Section_Checklist_Data_For_History_Tracking_Validation();
    }

    @When("I update cleared checklist details and verify the history of changes in field history tracking tab")
    public void I_update_cleared_checklist_details_and_verify_the_history_of_changes_in_field_history_tracking_tab() {
        getFundingOpportunityPage().updateDateFields_Verify_In_Field_History_Tracking();
        getFieldHistoryTrackingPage().navigateToFieldHistoryTrackingTab("After Funding Certificate Date field update");
        getFieldHistoryTrackingPage().verifyFieldHistory_Display_After_Update();
    }

    @When("I send the email template to the user")
    public void I_send_the_email_template_to_the_user() {
        getFieldHistoryTrackingPage().sendAn_Email_With_Random_Picked_Template();
    }

    @Then("I validate the email template details in mailbox {string} and verify the email content")
    public void I_validate_the_email_template_details_in_mailbox_and_verify_the_email_content(String s) {
        getFieldHistoryTrackingPage().navigateToMailosaur(s);
        getFieldHistoryTrackingPage().captureEmailDetails();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get SalesforceBasePage instance, creating it if needed
     */
    private SalesforceBasePage getSalesforceBasePage() {
        if (salesforceBasePage == null) {
            salesforceBasePage = new SalesforceBasePage(page());
        }
        return salesforceBasePage;
    }

    private void resetCachedPages() {
        salesforceBasePage = null;
        accountsPage = null;
        workbookUnderReviewPage = null;
        checklistPage = null;
        fundingOpportunityPage = null;
        fieldHistoryTrackingPage = null;
    }

}
