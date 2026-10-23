package com.macsuite.MFP_POH.pages;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.pages.BasePage;
import com.microsoft.playwright.Page;

/**
 * Page Object for MFP POH Funding Opportunity management.
 * Handles MFP POH-specific funding opportunity operations and interactions.
 * Inherits common form operations from BasePage.
 */
public class Funding_Opportunity_Page extends BasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(Funding_Opportunity_Page.class);

    // MFP POH Funding Opportunity-specific locators (renamed to follow constant
    // naming
    // conventions)
    private static final String SEARCH_INPUT = "xpath=//input[@placeholder='Search this list...']";
    private static final String FUNDING_NAME = "xpath=(//th[@data-label='Funding Number']//a)[1]";
    private static final String GRANT_NUMBER = "xpath=//label[text()='Grant Number']/following::input[1]";
    private static final String STAGE_DESCRIPTION = "xpath=//*[@class='slds-text-longform slds-path__guidance-content']/div";
    private static final String FUNDING_MEMO_REVIEW = "xpath=(//*[text()='Funding Memo Review'])[last()]";
    private static final String FUNDING_MEMO_EDIT_BUTTON = "xpath=//button[@title='Edit, Funding Memo Requested']";
    private static final String CONFIRM_CLEAR_DATES = "xpath=(//input[@name='Confirm_Clear_Dates__c']/../label/span[1])[last()]";
    private static final String CANNOT_ADD_FILE = "xpath=//*[contains(text(), 'add 1 file to Funding Opportunity.')]";
    private static final String RELATED_RECORDS = "xpath=//a[text()='Related Records']";
    private static String FUNDING_NUMBER = "xpath=//th[@data-label='Funding Number']//a";
    private static final String CHECKLIST_TAB = "xpath=(//*[text()='Checklists'])[1]";
    private static final String SEARCH_CHECKLIST_INPUT = "xpath=//input[@placeholder='Search this list...']";
    private static final String CHECKLIST_LINK = "xpath=(//*[@data-label='Budget Review Name']//a)[1]";
    private static final String STATUS = "xpath=(//label[text()='Status']/../div//div/lightning-formatted-text)[last()]";
    private static final String STAGE_NAME = "xpath=(//p[@class='stepLabel slds-path__stage-name'])[last()]";
    private static final String EMPTY_DATE_FIELD = "xpath=(//label[text()='NOA Issued']/../../following::label[1]/following::lightning-formatted-text[1])[last()]";
    private static final String FO_Nav_Link = "xpath=(//h1[text()='Funding Opportunity History']/parent::div/preceding-sibling::lst-breadcrumbs//a)[last()]";

    public Funding_Opportunity_Page(Page page) {
        super(page);
    }

    public void navigateToFundingOpportunityPage() {
          waitFor(2);
        clickOn(CHECKLIST_TAB);
        waitFor(2);
        type(SEARCH_CHECKLIST_INPUT, CHECKLIST_RECORD_ID);
        // type(SEARCH_CHECKLIST_INPUT, "-BR-26-0211");
        page.locator(SEARCH_CHECKLIST_INPUT).press("Enter");
        waitFor(5);
        clickOn(CHECKLIST_LINK);
        waitFor(2);
        clickOn(RELATED_RECORDS);
        waitFor(2);
        FUNDING_NUMBER = getText(FUNDING_NUMBER);
        LOGGER.info("Captured Funding Number: " + FUNDING_NUMBER);
        LOGGER.info("Navigating to Funding Opportunity page");
        clickOn(getText_Xpath("Funding Opportunities"));
        waitFor(2);
        clickOn(getText_Xpath("Recently Viewed"));
         waitFor(2);
         clickOn(getText_Xpath("All"));
         waitFor(5);
        type(SEARCH_INPUT, FUNDING_NUMBER);
        // type(SEARCH_INPUT, "FO-26-0053");
        page.locator(SEARCH_INPUT).press("Enter");
        waitFor(3);
        clickOn(FUNDING_NAME);
        waitFor(2);
        screenshot("Funding Opportunity page details");
    }

    public void verifyFundingOpportunityDetails() {
        LOGGER.info("Verifying Funding Opportunity details");

        readAndVerifyText(FUNDING_MEMO_REVIEW, "Funding Memo Review");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        String stage_Description_Text = getText(STAGE_DESCRIPTION);
        LOGGER.info("Funding Memo Review Text: " + stage_Description_Text);
        if (stage_Description_Text.contains("If the status is")
                && stage_Description_Text.contains(
                        ", you have received a request for a funding memo. you must create a decision memo for AO signature.")
                && stage_Description_Text.contains("Decision Memo Sent")
                && stage_Description_Text.contains(
                        ", you have sent the decision memo to the AO for signature. you are waiting for the AO to sign the DM.")
                && stage_Description_Text.contains("Decision Memo Received")
                && stage_Description_Text.contains(
                        ", you have received a signed decision memo from the AO. Upload the signed decision memo to GrantSolutions and create the funding memo in GrantSolutions.")) {
            LOGGER.info("Funding Memo Review section is displayed correctly.");
        } else {
            LOGGER.error("Funding Memo Review section is NOT displayed correctly.");
        }
        screenshot("Funding Opportunity details verification");
    }

    public void verifyFundingMemoSent_Details_on_the_screen() {
        LOGGER.info("Verifying Funding Memo Sent status in checklist");
        readAndVerifyText(STAGE_NAME, "Funding Memo Processing");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        String stage_Description_Text = getText(STAGE_DESCRIPTION);
        LOGGER.info("stage_Description_Text: " + stage_Description_Text);
        if (stage_Description_Text.contains("If the status is")
                && stage_Description_Text.contains(
                        ", you have sent the funding memo for concurrence. You are now waiting for the GMO to concur.")
                && stage_Description_Text.contains("Funding Memo Sent")
                && stage_Description_Text.contains(
                        "Concurrence")
                && stage_Description_Text.contains(
                        ", GMO has concurred with the funding memo. You must send an approval request to the AO.")) {
            LOGGER.info("stage_Description_Text is displayed correctly.");
        } else {
            LOGGER.error("stage_Description_Text is NOT displayed correctly.");
        }
        screenshot("Funding Memo Sent details verification");
    }

    public void verifyFundingMemo_Approval_Requested_Details_on_the_screen() {
        LOGGER.info("Verifying Funding Memo Approval Requested status in checklist");
        readAndVerifyText(STAGE_NAME, "Funding Under Approval");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        String stage_Description_Text = getText(STAGE_DESCRIPTION);
        LOGGER.info("stage_Description_Text: " + stage_Description_Text);
        if (stage_Description_Text
                .contains("You have send the AO a request for approval. You are waiting for approval from the AO.")) {
            LOGGER.info("stage_Description_Text is displayed correctly.");
        } else {
            LOGGER.error("stage_Description_Text is NOT displayed correctly.");
        }
        screenshot("Funding Memo Approval Requested details verification");
    }

    public void verifyFundingMemo_Processing_Details_on_the_screen() {
        LOGGER.info("Verifying Funding Memo Processing status in checklist");
        readAndVerifyText(STAGE_NAME, "Funding Memo Processing");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        String stage_Description_Text = getText(STAGE_DESCRIPTION);
        LOGGER.info("stage_Description_Text: " + stage_Description_Text);
        if (stage_Description_Text
                .contains("If the status is")
                && stage_Description_Text
                        .contains(
                                ", GMO has concurred with the funding memo. You must send an approval request to the AO.")
                && stage_Description_Text
                        .contains(
                                ", you have sent the funding memo for concurrence. You are now waiting for the GMO to concur.")
                && stage_Description_Text.contains("Concurrence")
                && stage_Description_Text.contains("Funding Memo Sent")) {
            LOGGER.info("stage_Description_Text is displayed correctly.");
        } else {
            LOGGER.error("stage_Description_Text is NOT displayed correctly.");
        }
        screenshot("Funding Memo Processing details verification");
    }

    public void verifyFundingMemo_Approved_Details_on_the_screen() {
        LOGGER.info("Verifying Funding Memo Approved status in checklist");
        readAndVerifyText(STAGE_NAME, "Funding Approved");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        String stage_Description_Text = getText(STAGE_DESCRIPTION);
        LOGGER.info("stage_Description_Text: " + stage_Description_Text);
        if (stage_Description_Text.contains(
                "You have received approval from the AO. You must send an email to the FMO requesting funds commitment and certification.")) {
            LOGGER.info("stage_Description_Text is displayed correctly.");
        } else {
            LOGGER.error("stage_Description_Text is NOT displayed correctly.");
        }
        screenshot("Funding Memo Approved details verification");
    }

    public void verifyFunding_Insurance_Details_on_the_screen() {
        LOGGER.info("Verifying Funding Insurance status in checklist");
        readAndVerifyText(STAGE_NAME, "Funding Insurance");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        String stage_Description_Text = getText(STAGE_DESCRIPTION);
        LOGGER.info("stage_Description_Text: " + stage_Description_Text);
        if (stage_Description_Text.contains("If the status is")
                && stage_Description_Text.contains(
                        "Requested Funds Commitment and Certification")
                && stage_Description_Text.contains(
                        ", you have sent an email to the FMO requesting funds commitment and certification. You are waiting for the FMO to notify you.")
                && stage_Description_Text.contains(
                        "Funding Certification Issued")
                && stage_Description_Text
                        .contains(", the FMO has informed you that funds commitment has been certified.")) {
            LOGGER.info("stage_Description_Text is displayed correctly.");
        } else {
            LOGGER.error("stage_Description_Text is NOT displayed correctly.");
        }
        screenshot("Funding Insurance details verification");
    }

    public void verifyFunding_Opportunity_Closed_Details_on_the_screen() {
        LOGGER.info("Verifying Funding Opportunity Closed status in checklist");
        readAndVerifyText(STAGE_NAME, "Closed");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        String stage_Description_Text = getText(STAGE_DESCRIPTION);
        LOGGER.info("stage_Description_Text: " + stage_Description_Text);
        if (stage_Description_Text.contains("Notice of Award has been issued and you've saved your NOA.")) {
            LOGGER.info("stage_Description_Text is displayed correctly.");
        } else {
            LOGGER.error("stage_Description_Text is NOT displayed correctly.");
        }
        screenshot("Funding Opportunity Closed details verification");
    }

    public void verifyFileUploadFunctionality() {
        LOGGER.info("Verifying file upload functionality for Funding Opportunity");
        waitFor(2);

        String filePath = System.getProperty("user.dir") +
                "\\src\\test\\resources\\data\\2-Proposed Process JIRA SOP.pdf";
        page.setInputFiles("(//input[@type='file'])[last()]", Paths.get(filePath));
        waitFor(10);
        clickOn(DONE_BUTTON);
        waitFor(5);
        readAndVerifyText(CANNOT_ADD_FILE, "add 1 file to Funding Opportunity");
        // Verify that the file is uploaded successfully (replace with actual
        // verification logic)
        boolean isFileUploaded = page.isVisible(getText_Xpath("Uploaded File Name"));
        if (isFileUploaded) {
            LOGGER.info("File uploaded successfully.");
        } else {
            LOGGER.error("File upload failed.");
        }
        screenshot("File upload verification");
    }

    public String checkList_Xpath(String label, String value) {
        return "xpath=//*[text()='Status Checklist']//following::*[text()='" + label + "']/following::input[" + value
                + "]";
    }

    public void verify_Checklist_Skip_Error_Message() {
        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        type(checkList_Xpath("Decision Memo Sent", "1"), "Decision Memo Sent Test");
        type(checkList_Xpath("Decision Memo Sent", "2"), fetchWeekdayDate("MMM d, yyyy", 1));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(1);
        readAndVerifyText(getText_Xpath("You must complete previous checklist steps before updating this step."),
                "You must complete previous checklist steps before updating this step.");
        screenshot("Checklist Skip Error Message verification");
    }

    public void editChecklist() {
        waitFor(5);
        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        waitFor(2);
        scrollToBottom();
    }

    public void fill_Section_Checklist_Data() {
        // clickOn(FUNDING_MEMO_EDIT_BUTTON);
        waitFor(2);
        type(checkList_Xpath("Funding Memo Requested", "1"), "Funding Memo Requested Test");
        type(checkList_Xpath("Funding Memo Requested", "2"), fetchWeekdayDate("MMM d, yyyy", 0));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Funding Memo Requested");

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Decision Memo Sent", "1"), "Decision Memo Sent Test");
        type(checkList_Xpath("Decision Memo Sent", "2"), fetchWeekdayDate("MMM d, yyyy", 1));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Decision Memo Sent");

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Approved Decision Memo Received", "1"), "Approved Decision Memo Received Test");
        type(checkList_Xpath("Approved Decision Memo Received", "2"), fetchWeekdayDate("MMM d, yyyy", 2));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Approved Decision Memo Received");

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Memo Sent", "1"), "Funding Memo Sent Test");
        type(checkList_Xpath("Funding Memo Sent", "2"), fetchWeekdayDate("MMM d, yyyy", 3));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Funding Memo Sent");

        verifyFundingMemoSent_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Concurrence", "1"), "Concurrence Test");
        type(checkList_Xpath("Concurrence", "2"), fetchWeekdayDate("MMM d, yyyy", 4));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Concurrence");

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Memo Approval Requested", "1"), "Funding Memo Approval Requested Test");
        type(checkList_Xpath("Funding Memo Approval Requested", "2"), fetchWeekdayDate("MMM d, yyyy", 5));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Funding Memo Approval Requested");

        verifyFundingMemo_Approval_Requested_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Memo Approved", "1"), "Funding Memo Approved Test");
        type(checkList_Xpath("Funding Memo Approved", "2"), fetchWeekdayDate("MMM d, yyyy", 6));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Funding Memo Approved");

        verifyFundingMemo_Approved_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Requested Funds Commitment", "1"), "Requested Funds Commitment Test");
        type(checkList_Xpath("Requested Funds Commitment", "2"), fetchWeekdayDate("MMM d, yyyy", 7));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Requested Funds Commitment and Certification");

        verifyFunding_Insurance_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Certification Issued", "1"), "Funding Certification Issued Test");
        type(checkList_Xpath("Funding Certification Issued", "2"), fetchWeekdayDate("MMM d, yyyy", 8));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Funding Certification Issued");

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("NOA Issued", "1"), "NOA Issued Test");
        type(checkList_Xpath("NOA Issued", "2"), fetchWeekdayDate("MMM d, yyyy", 9));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(4);

        readAndVerifyText(getText_Xpath("Record updated successfully"), "Record updated successfully");
        readAndVerifyText(STATUS, "NOA Issued");
        verifyFunding_Opportunity_Closed_Details_on_the_screen();
        screenshot("Filled all checklist details verification");
    }

    public void fill_Section_Checklist_Data_For_History_Tracking_Validation() {
        waitFor(5);
        screenshot("Before checklist update");
        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        waitFor(2);
        type(checkList_Xpath("Funding Memo Requested", "1"), "Funding Memo Requested Test");
        type(checkList_Xpath("Funding Memo Requested", "2"), fetchWeekdayDate("MMM d, yyyy", 0));

        type(checkList_Xpath("Decision Memo Sent", "1"), "Decision Memo Sent Test");
        type(checkList_Xpath("Decision Memo Sent", "2"), fetchWeekdayDate("MMM d, yyyy", 1));

        type(checkList_Xpath("Approved Decision Memo Received", "1"), "Approved Decision Memo Received Test");
        type(checkList_Xpath("Approved Decision Memo Received", "2"), fetchWeekdayDate("MMM d, yyyy", 2));

        scrollToBottom();
        type(checkList_Xpath("Funding Memo Sent", "1"), "Funding Memo Sent Test");
        type(checkList_Xpath("Funding Memo Sent", "2"), fetchWeekdayDate("MMM d, yyyy", 3));

        scrollToBottom();
        type(checkList_Xpath("Concurrence", "1"), "Concurrence Test");
        type(checkList_Xpath("Concurrence", "2"), fetchWeekdayDate("MMM d, yyyy", 4));

        scrollToBottom();
        type(checkList_Xpath("Funding Memo Approval Requested", "1"), "Funding Memo Approval Requested Test");
        type(checkList_Xpath("Funding Memo Approval Requested", "2"), fetchWeekdayDate("MMM d, yyyy", 5));

        scrollToBottom();
        type(checkList_Xpath("Funding Memo Approved", "1"), "Funding Memo Approved Test");
        type(checkList_Xpath("Funding Memo Approved", "2"), fetchWeekdayDate("MMM d, yyyy", 6));

        scrollToBottom();
        type(checkList_Xpath("Requested Funds Commitment", "1"), "Requested Funds Commitment Test");
        type(checkList_Xpath("Requested Funds Commitment", "2"), fetchWeekdayDate("MMM d, yyyy", 7));

        scrollToBottom();
        type(checkList_Xpath("Funding Certification Issued", "1"), "Funding Certification Issued Test");
        type(checkList_Xpath("Funding Certification Issued", "2"), fetchWeekdayDate("MMM d, yyyy", 8));

        scrollToBottom();
        type(checkList_Xpath("NOA Issued", "1"), "NOA Issued Test");
        type(checkList_Xpath("NOA Issued", "2"), fetchWeekdayDate("MMM d, yyyy", 9));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(4);
        screenshot("Filled all checklist details verification to track the history of changes");
    }

    public void updateDateFields_Verify_In_Field_History_Tracking() {
        screenshot("before navigating to FO to update Funding Certification date");
        clickOn(FO_Nav_Link);
        waitFor(5);
        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        screenshot("After navigated to Funding Opportunity page");
        scrollToBottom();
        // type(checkList_Xpath("Funding Certification Issued", "1"), "");
        type(checkList_Xpath("Funding Certification Issued", "2"), fetchWeekdayDate("MMM d, yyyy", 11));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(5);
        screenshot("Checklist Updated for verification of Field History Tracking");

    }

    public void updateDateFields_VErify_Clearence_Checklist_Warning_Alert(String condtion) {
        if (condtion.equalsIgnoreCase("Yes")) {
            screenshot("Before navigating to Funding Opportunity page");
            clickOn(FO_Nav_Link);
            waitFor(5);
            screenshot("After navigated to Funding Opportunity page");
        }

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        // type(checkList_Xpath("Funding Certification Issued", "1"), "");
        type(checkList_Xpath("Funding Certification Issued", "2"), fetchWeekdayDate("MMM d, yyyy", 10));
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(getText_Xpath(
                "Changing this date will clear dates from all subsequent checklist items. If you want to continue click checkbox to Confirm Clear Dates."),
                "Changing this date will clear dates from all subsequent checklist items. If you want to continue click checkbox to Confirm Clear Dates.");
        scrollToBottom();
        scrollToBottom();
        waitFor(2);
        clickOn(CONFIRM_CLEAR_DATES);
        clickOn(getButtonText_Xpath("Save"));
        waitFor(5);
        readAndVerifyText(EMPTY_DATE_FIELD, "");
        screenshot("Checklist Clear Dates Warning Alert verification");
    }

    public void verifyFunding_Memo_Navigation_Back_After_Clearing_Checklist() {
        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        // type(checkList_Xpath("NOA Issued", "1"), "");
        type(checkList_Xpath("NOA Issued", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(getText_Xpath("Record updated successfully"), "Record updated successfully");
        readAndVerifyText(STATUS, "Funding Certification Issued");
        verifyFunding_Insurance_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Certification Issued", "1"), "");
        type(checkList_Xpath("Funding Certification Issued", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Requested Funds Commitment and Certification");

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Requested Funds Commitment", "1"), "");
        type(checkList_Xpath("Requested Funds Commitment", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);

        verifyFundingMemo_Approved_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Memo Approved", "1"), "");
        type(checkList_Xpath("Funding Memo Approved", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Approval Requested");

        verifyFundingMemo_Approval_Requested_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Memo Approval Requested", "1"), "");
        type(checkList_Xpath("Funding Memo Approval Requested", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Concurrence");
        verifyFundingMemo_Processing_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Concurrence", "1"), "");
        type(checkList_Xpath("Concurrence", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);

        verifyFundingMemoSent_Details_on_the_screen();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Memo Sent", "1"), "");
        type(checkList_Xpath("Funding Memo Sent", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Decision Memo Received");

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Approved Decision Memo Received", "1"), "");
        type(checkList_Xpath("Approved Decision Memo Received", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Decision Memo Sent");

        verifyFundingOpportunityDetails();

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Decision Memo Sent", "1"), "");
        type(checkList_Xpath("Decision Memo Sent", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        readAndVerifyText(STATUS, "Funding Memo Requested");

        clickOn(FUNDING_MEMO_EDIT_BUTTON);
        scrollToBottom();
        type(checkList_Xpath("Funding Memo Requested", "1"), "");
        type(checkList_Xpath("Funding Memo Requested", "2"), "");
        clickOn(getButtonText_Xpath("Save"));
        waitFor(2);
        screenshot("Funding Memo Requested details verification");

    }

} // End of Funding_Opportunity_Page class
