package com.macsuite.MFP_POH.pages;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.pages.BasePage;
import com.microsoft.playwright.Page;

/**
 * Page Object for MFP POH Accounts management.
 * Handles MFP POH-specific account operations and interactions.
 * Inherits common form operations from BasePage.
 */
public class Workbook_UnderReviewPage extends BasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(Workbook_UnderReviewPage.class);

    // MFP POH Account-specific locators (renamed to follow constant naming
    // conventions)

    private static final String GUIDENCE_FOR_SUCCES = "xpath=(//*[text()='Guidance for Success'])[last()]";
    private static final String DESCRIPTION_MESSAGE = "xpath=//*[contains(text(), 'You have created')]";
    private static final String GRANT_NUMBER_TEXT = "xpath=(//label[text()='Grant Number']/../div//lightning-formatted-text)[last()]";
    private static final String APPLICATION_NUMBER_TEXT = "xpath=(//label[text()='Application Number']/../div//lightning-formatted-text)[last()]";
    private static final String FUNDING_AMOUNT_TEXT = "xpath=(//label[text()='Requested Supplemental Funding Amount']/../div//lightning-formatted-text)[last()]";
    private static final String START_DATE_TEXT = "xpath=(//label[text()='Period of Performance Start Date']/../div//lightning-formatted-text)[last()]";
    private static final String END_DATE_TEXT = "xpath=(//label[text()='Period of Performance End Date']/../div//lightning-formatted-text)[last()]";
    private static final String SUBMISSION_DATE_TEXT = "xpath=(//label[text()='Submission Date']/../div//lightning-formatted-text)[last()]";
    private static final String GRANT_MANAGEMENT_SPECIALIST_TEXT = "xpath=(//label[text()='Grants Management Specialist']/../div//a)[last()]";
    private static final String PROJECT_OFFICER_SUBMITTING_CHECKLIST = "xpath=(//label[text()='Project Officer Submitting Checklist']/../div//a)[last()]";
    private static final String CREATED_BY_TEXT = "xpath=(//*[text()='Created By']/../following-sibling::div//a)[last()]";
    private static final String EDIT_SUBMIT_BY = "xpath=(//*[@data-field='BR_ProjectOfficerSubmitting__c'])[last()]";
    public static final String SAVE_BUTTON = "xpath=//button[text()='Save']";
    private static final String CLEAR_PROJECT_OFFICER = "xpath=//*[@title='Clear Project Officer Submitting Checklist Selection']";
    private static final String SEARCH_PEOPLE_INPUT = "xpath=(//input[@placeholder='Search People...'])[last()]";
    private static final String SUBMITTED_BY_OPTION = "xpath=//input[@placeholder='Search People...']/following::lightning-base-combobox-item[2]";
    private static final String MFP_RECIPIENT_PREPARER = "xpath=//*[text()='MFP Recipient Preparer']/following::input[1]";
    private static final String MFP_RECIPIENT_OPTION = "xpath=//*[text()='MFP Recipient Preparer']/following::input[1]/following::lightning-base-combobox-item[1]";
    private static final String SUBMISSION_DATE = "xpath=//*[text()='Submission Date']/following::input[1]";
    private static final String REQUESTED_SUPPLEMENT_FUNDING_AMOUNT = "xpath=//*[text()='Requested Supplemental Funding Amount']/following::input[1]";
    private static final String CREATE_CHECKLIST = "xpath=//*[text()='Create Checklist']";
    private static final String CHECKLIST_HEADER = "xpath=(//h1//a)[last()]";
    private static final String COMPLETE_THIS_FIELD = "xpath=//*[text()='Complete this field.']";
    private static final String CLOSE_ERROR_DIALOG = "xpath=//button[@title='Close error dialog']";
    private static final String FIELD_LEVEL_ERRORS = "xpath=//div[@class='fieldLevelErrors']";
    public static final String UPLOAD_FILES_ICON = "xpath=(//*[text()='Upload Files']/lightning-primitive-icon)[last()]";
    public static final String FIELD_HISTORY_TRACKING_TAB = "xpath=//*[text()='Field History Tracking']";
    public static final String VIEW_ALL_LINK = "xpath=//*[contains(text(), 'Checklist History')]/following::*[text()='View All']/parent::a";

    public static final String DONE_BUTTON = "xpath=//*[text()='Done']";
    public static final String CHECKLIST_NAV_LINK = "xpath=//*[@title='Checklists']/../following-sibling::li/a";
    public static final String CHECKLIST_NAV_LINKS = "xpath=";
    public static final String PLACE_REVIEW_ON_HOLD = "xpath=(//button[text()='Place Review On Hold'])[last()]";
    public static final String GENERATE_COMMENTS_PACKAGE_BUTTON = "xpath=(//button[text()='Generate Comments Package'])[last()]";

    public static final String EDIT_COMMENTS_BUTTON = "xpath=(//button[@title='Edit, Comments'])[last()]";
    public static final String BR_RecPassDataValidationChcks__c = "xpath=//button[@name='BR_RecPassDataValidationChcks__c']";
    public static final String BR_RecPassDataValidationChcks__c_NO = "xpath=//button[@name='BR_RecPassDataValidationChcks__c']/following::lightning-base-combobox-item/span[2]/span[@title='No']";
    public static final String BR_RecPassDataValidationChcks__c_YES = "xpath=//button[@name='BR_RecPassDataValidationChcks__c']/following::lightning-base-combobox-item/span[2]/span[@title='Yes']";
    public static final String BR_RecPassDataValidationChcks__c_UNCLEAR = "xpath=//button[@name='BR_RecPassDataValidationChcks__c']/following::lightning-base-combobox-item/span[2]/span[@title='Unclear']";
    public static final String text_AreaOne = "xpath=(//textarea)[1]";

    public static final String BR_RecCompleteAllYellowReqFields__c = "xpath=//button[@name='BR_RecCompleteAllYellowReqFields__c']";
    public static final String BR_RecCompleteAllYellowReqFields__c_NO = "xpath=//button[@name='BR_RecCompleteAllYellowReqFields__c']/following::lightning-base-combobox-item/span[2]/span[@title='No']";
    public static final String BR_RecCompleteAllYellowReqFields__c_YES = "xpath=//button[@name='BR_RecCompleteAllYellowReqFields__c']/following::lightning-base-combobox-item/span[2]/span[@title='Yes']";
    public static final String BR_RecCompleteAllYellowReqFields__c_UNCLEAR = "xpath=//button[@name='BR_RecCompleteAllYellowReqFields__c']/following::lightning-base-combobox-item/span[2]/span[@title='Unclear']";
    public static final String text_AreaTwo = "xpath=(//textarea)[2]";

    public static final String BR_FundingSupportQltyImprovement__c = "xpath=//button[@name='BR_FundingSupportQltyImprovement__c']";
    public static final String BR_FundingSupportQltyImprovement__c_NO = "xpath=//button[@name='BR_FundingSupportQltyImprovement__c']/following::lightning-base-combobox-item/span[2]/span[@title='No']";
    public static final String BR_FundingSupportQltyImprovement__c_YES = "xpath=//button[@name='BR_FundingSupportQltyImprovement__c']/following::lightning-base-combobox-item/span[2]/span[@title='Yes']";
    public static final String BR_FundingSupportQltyImprovement__c_UNCLEAR = "xpath=//button[@name='BR_FundingSupportQltyImprovement__c']/following::lightning-base-combobox-item/span[2]/span[@title='Unclear']";
    public static final String text_AreaThree = "xpath=(//textarea)[3]";

    public Workbook_UnderReviewPage(Page page) {
        super(page);
    }

    public void verify_WFPB_Section_Displayed() {
        waitFor(5);
        clickOn(EDIT_COMMENTS_BUTTON);
        waitFor(2);
        clickOn(BR_RecPassDataValidationChcks__c);
        waitFor(2);
        readAndVerifyText(BR_RecPassDataValidationChcks__c_NO, "No");
        readAndVerifyText(BR_RecPassDataValidationChcks__c_YES, "Yes");
        readAndVerifyText(BR_RecPassDataValidationChcks__c_UNCLEAR, "Unclear");
        clickOn(BR_RecPassDataValidationChcks__c_NO);
        clickOn(BR_RecCompleteAllYellowReqFields__c);
        waitFor(2);
        readAndVerifyText(BR_RecCompleteAllYellowReqFields__c_NO, "No");
        readAndVerifyText(BR_RecCompleteAllYellowReqFields__c_YES, "Yes");
        readAndVerifyText(BR_RecCompleteAllYellowReqFields__c_UNCLEAR, "Unclear");
        clickOn(BR_RecCompleteAllYellowReqFields__c_NO);
        clickOn(BR_FundingSupportQltyImprovement__c);
        waitFor(2);
        readAndVerifyText(BR_FundingSupportQltyImprovement__c_NO, "No");
        readAndVerifyText(BR_FundingSupportQltyImprovement__c_YES, "Yes");
        readAndVerifyText(BR_FundingSupportQltyImprovement__c_UNCLEAR, "Unclear");
        clickOn(BR_FundingSupportQltyImprovement__c_NO);
        type(text_AreaOne, "Test Comment for BR_RecPassDataValidationChcks__c");
        type(text_AreaTwo, "Test Comment for BR_RecCompleteAllYellowReqFields__c");
        type(text_AreaThree, "Test Comment for BR_FundingSupportQltyImprovement__c");
        clickOn(SAVE_BUTTON);
    }

    public void verify_workbook_under_review_details() {
        page().waitForLoadState();
        readAndVerifyText(CURRENT_STAGE, "Workbook under Review");
        readAndVerifyText(GUIDENCE_FOR_SUCCES, "Guidance for Success");
        readAndVerifyText(DESCRIPTION_MESSAGE,
                "You have created a checklist and started a review OR you have received new recipient-provided information and are continuing your review.");
        readAndVerifyText(getText_Xpath("ays remaining until a submission response is due."),
                "ays remaining until a submission response is due.");
        readAndVerifyText(GRANT_NUMBER_TEXT, GRANT_NUMBER_STRING);
        // waitFor(300);
        // readAndVerifyText(APPLICATION_NUMBER_TEXT, APPLICATION_NUMBER_STRING);
        readAndVerifyText(FUNDING_AMOUNT_TEXT, "$1,000.00");
        readAndVerifyText(START_DATE_TEXT, fetchWeekdayDate("M/d/yyyy", -1));
        readAndVerifyText(END_DATE_TEXT, fetchWeekdayDate("\"M/d/yyyy", 1));
        readAndVerifyText(SUBMISSION_DATE_TEXT, fetchWeekdayDate("\"M/d/yyyy", 0));

        readAndVerifyText(GRANT_MANAGEMENT_SPECIALIST_TEXT, "Pankaj MFP App Ghosh");
        readAndVerifyText(PROJECT_OFFICER_SUBMITTING_CHECKLIST, "Pankaj MFP App Ghosh");
        readAndVerifyText(CREATED_BY_TEXT, "Pankaj MFP App Ghosh");

        screenshot("Workbook under Review page details");

        clickOn(EDIT_SUBMIT_BY);
        clickOn(CLEAR_PROJECT_OFFICER);
        type(SEARCH_PEOPLE_INPUT, "lia");
        waitFor(1);
        clickOn(SEARCH_PEOPLE_INPUT);
        clickOn(SUBMITTED_BY_OPTION);
        waitFor(5);
        readAndVerifyText(CREATED_BY_TEXT, "Pankaj MFP App Ghosh");
        String filePath = System.getProperty("user.dir") +
                "\\src\\test\\resources\\data\\Approval Letter.pdf";
        page.setInputFiles("(//input[@type='file'])[last()]", Paths.get(filePath));
        waitFor(2);
        clickOn(DONE_BUTTON);
        waitFor(5);
        clickOn(SAVE_BUTTON);
        waitFor(5);
        readAndVerifyText(PLACE_REVIEW_ON_HOLD, "Place Review On Hold");
        readAndVerifyText(GENERATE_COMMENTS_PACKAGE_BUTTON, "Generate Comments Package");
        verifyCurrentTab_Selection("Workbook under Review");
        screenshot("After file upload while Workbook under Review page details");
    }

    public void VerifyFieldHistory_Tracking() {
        clickOn(FIELD_HISTORY_TRACKING_TAB);
        waitFor(5);

        clickOn(VIEW_ALL_LINK);
        waitFor(2);

        List<String> fieldValues = page.locator(
                "//*[contains(text(),'Checklist History')]/following::table/tbody/tr/td[1]").allInnerTexts();

        System.out.println("fieldValues: " + fieldValues.size());

        Map<String, Integer> stringCounts = new HashMap<>();

        for (String str : fieldValues) {
            // Get the current count of the string, or 0 if it's the first time
            Integer count = stringCounts.getOrDefault(str, 0);
            // Increment the count and put it back in the map
            stringCounts.put(str, count + 1);
        }
        System.out.println("stringCounts: " + stringCounts);

        try {
            if (stringCounts.get("Created.").toString().equals("1")) {
                logger.info("Created. updated as expected");
            }
        } catch (Exception e) {
            logger.error("Created. NOT updated as expected");
        }

        try {
            if (stringCounts.get("Project Officer Submitting Checklis").toString().equals("1")) {
                logger.info("Project Officer Submitting Checklis updated as expected");
            }
        } catch (Exception e) {
            logger.error("Project Officer Submitting Checklis NOT updated as expected");
        }
        screenshot("Field History Tracking");
        System.out.println("stringCounts: " + stringCounts);
    }

    public void verifyFieldHistoryUpdate(String textToValidate) {
        clickOn(FIELD_HISTORY_TRACKING_TAB);
        waitFor(5);
        clickOn(VIEW_ALL_LINK);
        waitFor(2);
        List<String> fieldValues = page.locator(
                "//*[contains(text(),'Checklist History')]/following::table/tbody/tr/td[5]").allInnerTexts();

        System.out.println("fieldValues: " + fieldValues.size());

        Map<String, Integer> stringCounts = new HashMap<>();

        for (String str : fieldValues) {
            // Get the current count of the string, or 0 if it's the first time
            Integer count = stringCounts.getOrDefault(str, 0);
            // Increment the count and put it back in the map
            stringCounts.put(str, count + 1);
        }
        System.out.println("stringCounts: " + stringCounts);

        try {
            if (stringCounts.get(textToValidate).toString().equals("1")) {
                logger.info("{} updated as expected", textToValidate);
            }
        } catch (Exception e) {
            logger.error("{} NOT updated as expected", textToValidate);
        }
        clickOn(CHECKLIST_NAV_LINK);
        waitFor(5);
    }

    public void navigateToChecklistAndVerifyDetails() {
        clickOn(CHECKLIST_NAV_LINK);
        waitFor(5);
    }

    public void verify_Checkist_Details_in_main_window_And_Rightside_details_section() {
        waitFor(5);
        CHECKLIST_RECORD_ID = page().locator(CHECKLIST_HEADER).innerText();
        logger.info("Checklist Record ID: {}", CHECKLIST_RECORD_ID);
        compare_MainWindow_and_RightSideDetails("Recipient State/Territory");
        compare_MainWindow_and_RightSideDetails("Requested Supplemental Funding Amount");
        compare_MainWindow_and_RightSideDetails("Grant Number");
        compare_MainWindow_and_RightSideDetails("Record Type");
        compare_MainWindow_and_RightSideDetails("Assigned PO");
        compare_MainWindow_and_RightSideDetails("Grants Management Specialist");
        compare_MainWindow_and_RightSideDetails("Submission Date");
        compare_MainWindow_and_RightSideDetails("Created By");
        screenshot("Checklist Details in Main Window and Right Side Details Section");
    }

    public void compare_MainWindow_and_RightSideDetails(String selectorText) {
        String xpath = "(//*[text()='" + selectorText + "']/..)[last()]";
        String xpath2 = "(//*[text()='" + selectorText + "']/..)[last()-1]";

        try {
            if (page().locator(xpath).isVisible()) {
                logger.info("Main Window Element is visible: {}", selectorText);
            } else {
                logger.error("Main Window Element is not visible: {}", selectorText);
            }
            if (page().locator(xpath2).isVisible()) {
                logger.info("Right Side Details Element is visible: {}", selectorText);

            } else {
                logger.error("Right Side Details Element is not visible: {}", selectorText);
            }
        } catch (Exception e) {
            logger.error("Error checking visibility of element {}: {}", selectorText, e.getMessage());
            throw new RuntimeException("Could not check visibility of element: " + selectorText, e);
        }
    }

} // End of AccountsPage class
