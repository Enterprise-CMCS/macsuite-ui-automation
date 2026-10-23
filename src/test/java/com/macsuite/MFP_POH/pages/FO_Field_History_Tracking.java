package com.macsuite.MFP_POH.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.pages.BasePage;
import com.macsuite.config.EnvLoader;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for MFP POH Field History Tracking management.
 * Handles MFP POH-specific field history tracking operations and interactions.
 * Inherits common form operations from BasePage.
 */
public class FO_Field_History_Tracking extends BasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(FO_Field_History_Tracking.class);

    // MFP POH Field History Tracking-specific locators (renamed to follow constant
    // naming
    // conventions)
    private static final String FIELD_HISTORY_TRACKING_TAB = "xpath=(//*[text()='Field History Tracking'])[last()]";
    private static final String VIEW_ALL_LABEL = "xpath=(//*[@class='view-all-label']/parent::a)[last()]";
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

    private static String EMAIL = "xpath=(//*[@aria-label='Email'])[last()]";
    private static String INSERT_TEMPLATE = "xpath=//*[@data-key='insert_template']";
    private static String INSERT_A_TEMPLATE = "xpath=//a[@title='Insert a template...']";
    private static String TEMPLATE_FOLDERS = "xpath=//*[text()='Template Folders']//following::select[@class='templateInputs select']";
    private static String TEMPLATE_NAME_NAMES = "xpath=//th[@class='datatableButtonLink']//button";
    private static String SEND_BUTTON = "xpath=//*[text()='Send']";
    private static String EMAIL_SENT_CONFIRMATION = "xpath=//*[text()='Email was sent.']";
    private static String EMAIL_SUBJECT_LINK = "xpath=//a[@class='subjectLink slds-truncate slds-col']";
    private static String EMAIL_SUBJECT = "xpath=//h1[contains(@class, 'slds-page-header__title')]";
    private static String EMAIL_PREVIEW_IFRAME = "xpath=//iframe[@title='Email Preview']";
    private static String EMAIL_PREVIEW_BODY = "xpath=//html[@title='Email Preview']";
    private static String EMAIL_INBOX_HEADER = "xpath=//li[1]/a[2]/div[@data-testid='email-summary']";
    private static String MAILOSSAUR_IFRAME = "xpath=(//iframe)[2]";
    private static String MAILOSSAUR_MAIL_BODY = "css=[title='Email content']";
    private static String ENTER_YOUR_EMAIL_ADDRESS = "xpath=//input[@placeholder='Enter your email address']";
    private static String CONTINUE_BUTTON = "xpath=//button[text()='Continue']";
    private static String ENTER_YOUR_PASSWORD = "xpath=//input[@placeholder='Enter your password']";
    private static String LOGIN_BUTTON = "xpath=//button[text()='Log in']";

    public static String EMAIL_SUBJECT_STRING = "";
    public static String EMAIL_BODY_STRING = "";
    public static String MAILOSSAUR_MAIL_BODY_STRING = "";

    public FO_Field_History_Tracking(Page page) {
        super(page);
    }

    public void sendAn_Email_With_Random_Picked_Template() {
        clickOn(FO_Nav_Link);
        waitFor(5);
        scrollToBottom();
        waitFor(5);
        clickOn(EMAIL);
        waitFor(2);
        clickOn(INSERT_TEMPLATE);
        waitFor(2);
        clickOn(INSERT_A_TEMPLATE);
        waitFor(2);
        String templateFolderList[] = {"MFP POH Funding Opportunity", "MFP POH Checklist"};
        int randomIndex = (int) (Math.random() * templateFolderList.length);
        String selectedTemplateFolder = templateFolderList[randomIndex];
        selectByVisibleText(TEMPLATE_FOLDERS, selectedTemplateFolder, selectedTemplateFolder);

        // TEMPLATE_NAME_NAMES locator refers to more than one element on the page, so we need to click randomly on one of the available template names.
        Locator templateNames = page.locator(TEMPLATE_NAME_NAMES);
        int templateCount = templateNames.count();
        if (templateCount > 0) {
            int randomTemplateIndex = (int) (Math.random() * templateCount);
            templateNames.nth(randomTemplateIndex).click();
        }

        clickOn(SEND_BUTTON);
        readAndVerifyText(EMAIL_SENT_CONFIRMATION, "Email was sent");
        LOGGER.info("Email sent successfully with a random template");
        waitFor(5);

        // Capture subject from the selected email row before opening preview.
        EMAIL_SUBJECT_STRING = normalizeText(getText(EMAIL_SUBJECT_LINK));
        clickOn(EMAIL_SUBJECT_LINK);

        switchToFrame(EMAIL_PREVIEW_IFRAME);

        FrameLocator emailPreviewFrame = page.frameLocator(EMAIL_PREVIEW_IFRAME);
        Locator previewBody = emailPreviewFrame.locator("body").first();
        previewBody.waitFor();
        EMAIL_BODY_STRING = normalizeText(previewBody.textContent());

        // If row-level subject is empty, resolve subject from the preview header.
        if (EMAIL_SUBJECT_STRING == null || EMAIL_SUBJECT_STRING.isBlank()) {
            Locator previewSubject = emailPreviewFrame.locator("h1.slds-page-header__title.slds-var-m-right_small")
                    .first();
            if (previewSubject.count() > 0) {
                EMAIL_SUBJECT_STRING = normalizeText(previewSubject.textContent());
            }
        }

        LOGGER.info("Email Subject: {}", EMAIL_SUBJECT_STRING);
        LOGGER.info("Email Body: {}", EMAIL_BODY_STRING);
    }

    public void navigateToMailosaur(String environment) {
        if (environment.equalsIgnoreCase("QA")) {
            String mailosaurUrl = "https://mailosaur.com/app/servers/5cbcluu9/messages/inbox";
            page.navigate(mailosaurUrl);
            LOGGER.info("Navigated to Mailosaur QA environment");
        } else if (environment.equalsIgnoreCase("UAT")) {
            String mailosaurUrl = "https://mailosaur.com/app/servers/hx4rvofy/messages/inbox";
            page.navigate(mailosaurUrl);
            LOGGER.info("Navigated to Mailosaur UAT environment");
        } else if (environment.equalsIgnoreCase("SEI")) {
            String mailosaurUrl = "https://mailosaur.com/app/servers/cxaqkvbr/messages/inbox";
            page.navigate(mailosaurUrl);
            LOGGER.info("Navigated to Mailosaur SEI environment");
        } else {
            throw new IllegalArgumentException("Invalid environment: " + environment);
        }

        String mailosaurUsername = resolveMailosaurCredential("MAILOSAUR_USERNAME", "mailosaur_username");
        String mailosaurPassword = resolveMailosaurCredential("MAILOSAUR_PASSWORD", "mailosaur_password");

        waitFor(5);
        type(ENTER_YOUR_EMAIL_ADDRESS, mailosaurUsername);
        clickOn(CONTINUE_BUTTON);
        type(ENTER_YOUR_PASSWORD, mailosaurPassword);
        clickOn(LOGIN_BUTTON);
        waitFor(5);

    }

    public void captureEmailDetails() {
        String emailHeader = normalizeText(getText(EMAIL_INBOX_HEADER));
        if (EMAIL_SUBJECT_STRING == null || EMAIL_SUBJECT_STRING.isBlank()) {
            throw new AssertionError("Email subject was not captured from Salesforce preview");
        }

        if (emailHeader != null && emailHeader.contains(EMAIL_SUBJECT_STRING)) {
            clickOn(EMAIL_INBOX_HEADER);
            switchToFrame(MAILOSSAUR_IFRAME);

            FrameLocator mailosaurFrame = page.frameLocator(MAILOSSAUR_IFRAME);
            Locator mailBody = mailosaurFrame.locator(MAILOSSAUR_MAIL_BODY).first();
            if (mailBody.count() == 0) {
                mailBody = mailosaurFrame.locator("body").first();
            }
            mailBody.waitFor();
            MAILOSSAUR_MAIL_BODY_STRING = normalizeText(mailBody.textContent());

            LOGGER.info("Captured email body from Mailosaur: {}", MAILOSSAUR_MAIL_BODY_STRING);
            page.mainFrame();
        } else {
            throw new AssertionError("Expected email subject not found in the inbox");
        }

        String expectedBody = normalizeText(EMAIL_BODY_STRING);
        String actualBody = normalizeText(MAILOSSAUR_MAIL_BODY_STRING);

        if (expectedBody.isBlank()) {
            throw new AssertionError("Expected email body captured from preview is empty");
        }

        if (actualBody.contains(expectedBody)) {
            LOGGER.info("Email body matches the expected content");
        } else {
            throw new AssertionError("Email body does not match the expected content");
        }
    }

    public void navigateToFieldHistoryTrackingTab(String instanceName) {
        waitFor(5);
        clickOn(FIELD_HISTORY_TRACKING_TAB);
        clickOn(VIEW_ALL_LABEL);
        LOGGER.info("Navigated to Field History Tracking tab for instance: {}", instanceName);
        waitFor(5);
        screenshot("Field_History_Tracking " + instanceName);
    }

    public String dateFieldValue(int dayCount) {
        return fetchWeekdayDate("M/d/yyyy", dayCount);
    }

    public void verifyFieldHistory_Display() {
        waitFor(5);
        getOriginal_New_Values_ForField("NOA Issued Date");
        compareValues(newValue, dateFieldValue(9));
        getOriginal_New_Values_ForField("Funding Certification Issued Date");
        compareValues(newValue, dateFieldValue(8));
        getOriginal_New_Values_ForField("Requested Funds Commit & Cert Date");
        compareValues(newValue, dateFieldValue(7));
        getOriginal_New_Values_ForField("Funding Memo Approved Date");
        compareValues(newValue, dateFieldValue(6));
        getOriginal_New_Values_ForField("Funding Memo Approval Requested Date");
        compareValues(newValue, dateFieldValue(5));
        getOriginal_New_Values_ForField("Concurrence Date");
        compareValues(newValue, dateFieldValue(4));
        getOriginal_New_Values_ForField("Funding Memo Sent Date");
        compareValues(newValue, dateFieldValue(3));
        getOriginal_New_Values_ForField("Approved Decision Memo Received Date");
        compareValues(newValue, dateFieldValue(2));
        getOriginal_New_Values_ForField("Decision Memo Sent Date");
        compareValues(newValue, dateFieldValue(1));
        getOriginal_New_Values_ForField("Funding Memo Requested Date");
        compareValues(newValue, dateFieldValue(0));

    }

    public void verifyFieldHistory_Display_After_Update() {
        waitFor(5);
        getOriginal_New_Values_ForField("Funding Certification Issued Date");
        compareValues(newValue, dateFieldValue(11));
        screenshot("Field_History_Tracking_After_Update");
    }

    public void verifyFieldHistory_Display_AfterClearingChecklist() {
        waitFor(5);
        getCheckBoxStatus_Checked_OR_Unchecked("Confirm Clear Dates");
        compareValues(originalValue, "Unchecked");
        compareValues(newValue, "Checked");
        screenshot("Field_History_Tracking_After_Clearing_Checklist");
    }

    public void getCheckBoxStatus_Checked_OR_Unchecked(String fieldName) {
        Locator tableRows = page.locator("//h1[text()='Funding Opportunity History']//following::table/tbody/tr");
        if (tableRows.count() == 0) {
            throw new AssertionError("Field History Tracking table is not loaded or contains no rows");
        }

        for (int index = 1; index <= tableRows.count(); index++) {
            try {
                Locator fieldCell = page
                        .locator("//h1[text()='Funding Opportunity History']//following::table/tbody/tr["
                                + index + "]/td[2]//span[text()='" + fieldName + "']");
                String fieldCellText = normalizeText(fieldCell.textContent());
                if (!fieldCellText.equals(fieldName)) {
                    continue;
                } else {

                    Locator originalValueCell = page.locator(
                            "//h1[text()='Funding Opportunity History']//following::table/tbody/tr[" + index
                                    + "]/td[4]//lightning-primitive-input-checkbox");

                    Locator newValueCell = page.locator(
                            "//h1[text()='Funding Opportunity History']//following::table/tbody/tr[" + index
                                    + "]/td[5]//lightning-primitive-input-checkbox");
                    try {
                        if (isCheckboxChecked(originalValueCell)) {
                            originalValue = "Checked";
                        } else {
                            originalValue = "Unchecked";
                        }
                    } catch (Exception e) {
                        originalValue = "Unchecked";
                        LOGGER.info("Original Value for field '{}' is Unchecked", fieldName);
                    }

                    try {
                        if (isCheckboxChecked(newValueCell)) {
                            newValue = "Checked";
                        } else {
                            newValue = "Unchecked";
                        }
                    } catch (Exception e) {
                        newValue = "Unchecked";
                        LOGGER.info("New Value for field '{}' is Unchecked", fieldName);
                    }

                    LOGGER.info("Found Field History row for '{}' with Original Value '{}' and New Value '{}'",
                            fieldName,
                            originalValue, newValue);
                    break; // Exit the loop once the field is found
                }
            } catch (Exception e) {
            }
        }

    }

    public String originalValue = "";
    public String newValue = "";

    public void getOriginal_New_Values_ForField(String fieldName) {
        Locator tableRows = page.locator("//h1[text()='Funding Opportunity History']//following::table/tbody/tr");
        if (tableRows.count() == 0) {
            throw new AssertionError("Field History Tracking table is not loaded or contains no rows");
        }

        for (int index = 1; index <= tableRows.count(); index++) {
            try {
                Locator fieldCell = page
                        .locator("//h1[text()='Funding Opportunity History']//following::table/tbody/tr["
                                + index + "]/td[2]//span[text()='" + fieldName + "']");
                if (fieldCell.count() == 0) {
                    continue;
                }
                String fieldCellText = normalizeText(fieldCell.textContent(
                        new Locator.TextContentOptions().setTimeout(1000)));
                if (!fieldCellText.equals(fieldName)) {
                    continue;
                } else {

                    Locator originalValueCell = page.locator(
                            "//h1[text()='Funding Opportunity History']//following::table/tbody/tr[" + index
                                    + "]/td[4]");

                    Locator newValueCell = page.locator(
                            "//h1[text()='Funding Opportunity History']//following::table/tbody/tr[" + index
                                    + "]/td[5]");

                    // originalValue = normalizeText(originalValueCell.textContent());
                    if (newValueCell.count() == 0) {
                        continue;
                    }
                    newValue = normalizeText(newValueCell.textContent(
                            new Locator.TextContentOptions().setTimeout(1000)));
                    LOGGER.info("Found Field History row for '{}' with Original Value '{}' and New Value '{}'",
                            fieldName,
                            originalValue, newValue);
                    break; // Exit the loop once the field is found
                }
            } catch (Exception e) {
            }
        }

    }

    private String normalizeText(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }

    private String resolveMailosaurCredential(String primaryKey, String fallbackKey) {
        String value = EnvLoader.get(primaryKey, null);
        if (value == null || value.isBlank()) {
            value = EnvLoader.get(fallbackKey, null);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing Mailosaur credential. Checked keys: " + primaryKey + " and " + fallbackKey);
        }

        return value;
    }

    private boolean isCheckboxChecked(Locator checkbox) {
        return checkbox.count() > 0 && checkbox.first().getAttribute("checked") != null;
    }

} // End of FO_Field_History_Tracking class
