package com.macsuite.MFP_POH.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class Document_Review extends BasePage {
    public Document_Review(Page page) {
        super(page);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Document_Review.class);

    public static final String ADD_DOCUMENT_REVIEW_BUTTON = "xpath=//button[text()='Add Document Review']";
    public static final String FINISH_BUTTON = "xpath=(//button[text()='Finish'])[last()]";
    public static final String DOCUMENT_TYPE_ERROR = "xpath=//div[@id='wrapper-body']//span[text()='Document Type']/..";
    public static final String REVIEW_DUE_DATE_ERROR = "xpath=//div[@data-name='DR_ReviewDueDate__c']/span/..";
    public static final String DOCUMENT_RECEIVED_DATE_ERROR = "xpath=//div[@data-name='DR_DocumentReceivedDate__c']/span/..";
    public static final String DOCUMENT_REPOSITORY_ERROR = "xpath=(//*[text()='Document Repository'])[last()]/..";
    public static final String DOCUMENT_APPROVER_ERROR = "xpath=(//*[text()='A value is required.'])[last()]";
    public static final String DOCUMENT_TYPE_BUTTON = "xpath=//button[@aria-label='Document Type']";
    public static final String DOCUMENT_TYPE_OTHER_OPTION = "xpath=//*[@title='Other']";
    public static final String OTHER_DOCUMENT_TYPE_ERROR = "xpath=//div[@data-name='Other_Document_Type_Input']/following::lightning-formatted-rich-text";
    public static final String OTHER_DOCUMENT_TYPE_INPUT = "xpath=//input[@name='Other_Document_Type_Input']";
    public static final String REVIEW_DUE_DATE_INPUT = "xpath=//input[@name='DR_ReviewDueDate__c']";
    public static final String DOCUMENT_REPOSITORY_INPUT = "xpath=//*[text()='Document Repository']/following::input[1]";
    public static final String DOCUMENT_REPOSITORY_INPUT_LAST = "xpath=(//*[text()='Document Repository']/following::input[1])[last()]";
    public static final String DOCUMENT_RECEIVED_DATE_INPUT = "xpath=//input[@name='DR_DocumentReceivedDate__c']";
    public static final String DOCUMENT_APPROVER_INPUT = "xpath=//input[@aria-label='Document Approver']";
    public static final String DOCUMENT_APPROVER_INPUT_LAST = "xpath=(//input[@aria-label='Document Approver'])[last()]";
    public static final String DOCUMENT_APPROVER_OPTION = "xpath=//*[@title='Pankaj MFP App Ghosh']";
    public static final String DOCUMENT_APPROVER_OPTION_LAST = "xpath=(//*[@title='Pankaj MFP App Ghosh'])[last()]";
    public static final String REVIEW_STATUS_NOTES = "xpath=//textarea";
    public static final String REVIEW_STATUS_NOTES_LAST = "xpath=(//textarea)[last()]";

    public static final String DOCUMENT_REVIEW_HEADER = "xpath=//*[text()='Document Review']/following::h1//lightning-formatted-text";
    public static final String CREATED_BY_LINK = "xpath=//*[text()='Created By']//following::records-hoverable-link//a//span//span//span";
    public static final String LAST_MODIFIED_BY_LINK = "xpath=//*[text()='Last Modified By']//following::records-hoverable-link//a//span//span//span";
    public static final String SHARE_AN_UPDATE_BUTTON = "xpath=(//button[@title='Share an update...'])[last()]";
    public static final String SHARE_AN_UPDATE_TEXTAREA = "xpath=(//div[@data-placeholder='Share an update...'])[last()]";
    public static final String SHARE_BUTTON = "xpath=(//button[@title='Click, or press Ctrl+Enter'])[last()]";
    public static final String UPDATE_SHARED_SUCCESS_MSG = "xpath=//*[text()='Your update was shared.']";
    public static final String DOCUMENT_REVIEW_TABLE_LINK = "xpath=(//a[contains(@href, '/lightning/r/DocumentReview/')])[1]";
    public static final String REVIEW_STATUS = "xpath=(//div/span[text()='Review Status']/../following-sibling::div//lightning-formatted-text)[last()]";
    public static final String EDIT_REVIEW_STATUS = "xpath=(//button[@title='Edit Review Status'])[last()]";
    public static final String REVIEW_STATUS_BUTTON = "xpath=//label[text()='Review Status']/following::button[1]";
    public static final String REVIEW_STATUS_ACCEPTED = "xpath=(//span[@title='Accepted'])[last()]";
    public static final String REVIEW_STATUS_COMPLETE = "xpath=(//span[@title='Complete'])[last()]";
    public static final String SAVE_BUTTON = "xpath=(//button[text()='Save'])[last()]";
    public static final String CANCEL_BUTTON = "xpath=(//button[text()='Cancel'])[last()]";
    public static final String ACCEPTANCE_TASK_REQUIRED_MSG = "xpath=//*[text()='Acceptance Task is required when the Review Status is Accepted.']";
    public static final String CANCEL_EDIT = "xpath=//button[@name='CancelEdit']";
    public static final String ACCEPT_DOCUMENT_BUTTON = "xpath=(//button[text()='Accept Document'])[last()]";
    public static final String CANNOT_ACCEPT_DOCUMENT_MSG = "xpath=//*[text()='Cannot Accept Document']";
    public static final String CHILD_ACCOUNT_LINK = "xpath=//*[text()='Recipient Account']/following::a[1]";
    public static final String COMPLETE_REVIEW_BUTTON = "xpath=//button[text()='Complete Review']";
    public static final String ADD_TASK_BUTTON = "xpath=//button[text()='Add Task']";
    public static final String DUE_DATE_ERROR_MSG = "xpath=//*[text()='Due Date']/following::*[contains(text(), 'Complete this field with format')]";
    public static final String REVIEWER_ROLE_ERROR_MSG = "xpath=//*[text()='Reviewer Role']/following::*[contains(text(), 'Complete this field')]";
    public static final String DOCUMENT_REPOSITORY_ERROR_MSG = "xpath=//*[contains(text(), 'Document Repository must be a valid URL with a domain')]";
    public static final String DOCUMENT_VERSION_ERROR_MSG = "xpath=//*[text()='Document Version #']/following::*[contains(text(), 'Complete this field')]";
    public static final String DUE_DATE_INPUT = "xpath=//*[text()='Due Date']/following::input[1]";
    public static final String ADD_TASK_DOCUMENT_REPOSITORY_INPUT = "xpath=//input[@placeholder='https://']";
    public static final String DOCUMENT_VERSION_INPUT = "xpath=//*[text()='Document Version #']/following::input";
    public static final String COMMENTS_TEXTAREA = "xpath=//*[text()='Comments']/following::textarea";
    public static final String REVIEW_GUIDANCE_TEXTAREA = "xpath=//*[text()='Review Guidance']/following::textarea";
    public static final String ASSIGNED_USER_INPUT = "xpath=//*[text()='Assigned User']/following::input";
    public static final String ASSIGNED_USER_CLEAR_BUTTON = "xpath=//button[@title='Clear Assigned User Selection']";
    public static final String ASSIGNED_USER_AUTOCOMPLETE_OPTION = "xpath=//*[@title='Pankaj TA App Ghosh']";
    public static final String REVIEWER_ROLE_BUTTON = "xpath=//button[@aria-label='Reviewer Role']";
    public static final String REVIEWER_ROLE_OPTION = "xpath=//*[@title='Team Lead (TL)']";
    public static final String TASK_CREATED_SUCCESSFULLY_MSG = "xpath=//*[text()='Task created successfully.']";
    public static final String DOCUMENT_REPOSITORY_INVALID_ERROR_MSG = "xpath=//label[text()='Document Repository']/../following-sibling::div";

    public static final String OPEN_TASKS_LINK = "xpath=//h3[text()='Open Tasks']/following::tbody//a";
    public static final String COMPLETED_TASKS_LINK = "xpath=//h3[text()='Completed Tasks']/following::tbody//a";
    public static final String STATUS_IN_PROGRESS = "xpath=//*[text()='Status']/following::*[text()='In Progress']";
    public static final String TASK_TITLE = "xpath=(//h1/div//span)[last()]";
    public static final String MARK_COMPLETE_BUTTON = "xpath=//*[text()='Mark Complete']";
    public static final String STATUS_COMPLETED = "xpath=//*[text()='Status']/following::*[text()='Completed']";
    public static final String STATUS_CANCELLED = "xpath=//*[text()='Status']/following::*[text()='Cancelation']";
    public static final String EDIT_STATUS_BUTTON = "xpath=(//button[@title='Edit Status'])[last()]";
    public static final String STATUS_DR = "xpath=//button[@name='Status']";
    public static final String STATUS_COMPLETED_OPTION = "xpath=//*[@title='Completed']";
    public static final String STATUS_CANCELLED_OPTION = "xpath=//*[@title='Cancelation']";
    // public static final String SAVE_BUTTON = "xpath=//button[text()='Save']";
    public static final String TASK_UPDATE_SUCCESS_MSG = "xpath=//*[text()='The Task was updated successfully.']";
    public static final String CANNOT_CHANGE_STATUS_MSG = "xpath=//*[text()='Task status cannot be changed once it is Complete or Canceled.']";
    public static final String RELATED_DOCUMENT_LINK = "xpath=(//div[@title='Related To']/following-sibling::div//a)[last()]";
    public static final String NO_OPEN_TASKS_MSG = "xpath=//*[text()='No open tasks.']";
    
     public static final String DUE_DATE_INPUT_LAST_STRING = "xpath=(//*[text()='Due Date']/following::input[1])[last()]";
    public static final String ADD_TASK_DOCUMENT_REPOSITORY_INPUT_LAST_STRING = "xpath=(//input[@placeholder='https://'])[last()]";
     public static final String DOCUMENT_VERSION_INPUT_LAST_STRING = "xpath=(//*[text()='Document Version #']/following::input[1])[last()]";
    public static final String COMMENTS_TEXTAREA_LAST_STRING = "xpath=(//*[text()='Comments']/following::textarea)[last()]";
    public static final String REVIEW_GUIDANCE_TEXTAREA_LAST_STRING = "xpath=(//*[text()='Review Guidance']/following::textarea)[last()]";
    public static final String ASSIGNED_USER_INPUT_LAST_STRING = "xpath=(//*[text()='Assigned User']/following::input)[last()]";
    public static final String ASSIGNED_USER_CLEAR_BUTTON_LAST_STRING = "xpath=(//button[@title='Clear Assigned User Selection'])[last()]";
    public static final String ASSIGNED_USER_AUTOCOMPLETE_OPTION_LAST_STRING = "xpath=//*[@title='Pankaj TA App Ghosh']";
    public static final String REVIEWER_ROLE_BUTTON_LAST_STRING = "xpath=(//button[@aria-label='Reviewer Role'])[last()]";
    public static final String REVIEWER_ROLE_OPTION_LAST_STRING = "xpath=//*[@title='Team Lead (TL)']";

    public void verifyOpen_And_Completeted_Tasks() {
        clickOn(RELATED_DOCUMENT_LINK);
        waitFor(5);
        int completedTasksCount = this.page.locator(COMPLETED_TASKS_LINK).count();
        if (completedTasksCount == 3) {
            LOGGER.info("There are 3 completed tasks as expected");
        } else {
            LOGGER.error("Expected 3 completed tasks, but found " + completedTasksCount);
        }

    }

    public void navigateToTaskAndVerifyDetails() {
        clickOn(OPEN_TASKS_LINK);
        waitFor(5);
        readAndVerifyText(STATUS_IN_PROGRESS, "In Progress");
        String taskTitle = this.page.locator(TASK_TITLE).textContent();
        if (taskTitle.length() == 13) {
            LOGGER.info("Task Title: " + taskTitle + " is of expected length 13 characters");
        } else {
            LOGGER.error("Task Title: " + taskTitle + " is of NOT expected length 13 characters");
        }
        screenshotPage(page,"Task title is 13 characters long");
    }

    public void completeTaskAndVerifyStatus() {
        clickOn(EDIT_STATUS_BUTTON);
        waitFor(2);
        clickOn(STATUS_DR);
        waitFor(2);
        clickOn(STATUS_COMPLETED_OPTION);
        clickOn(SAVE_BUTTON);
        waitFor(1);
        readAndVerifyText(STATUS_COMPLETED, "Completed");
        screenshotPage(page,"Task completed and status verified");
    }

    public void verifyCancellation_AfterCompletion() {
        clickOn(EDIT_STATUS_BUTTON);
          waitFor(1);
        clickOn(STATUS_DR);
        waitFor(1);
        clickOn(STATUS_CANCELLED_OPTION);
        clickOn(SAVE_BUTTON);
        waitFor(1);
        readAndVerifyText(CANNOT_CHANGE_STATUS_MSG, "Task status cannot be changed once it is Complete or Canceled.");

        screenshotPage(page,"Cancellation after completion verified");
        clickOn(CANCEL_BUTTON);
    }

    public void verifyTaskCancellation() {
        waitFor(5);
        clickOn(EDIT_STATUS_BUTTON);
        clickOn(STATUS_DR);
        waitFor(1);
        clickOn(STATUS_CANCELLED_OPTION);
        clickOn(SAVE_BUTTON);
        waitFor(1);
        readAndVerifyText(STATUS_CANCELLED, "Cancelation");
        screenshotPage(page,"Task cancellation verified");
    }

    public void verifyTaskComplete_After_Cancellation() {
        clickOn(EDIT_STATUS_BUTTON);
        waitFor(1);
        clickOn(STATUS_DR);
        waitFor(1);
        clickOn(STATUS_COMPLETED_OPTION);
        clickOn(SAVE_BUTTON);
        waitFor(1);
        readAndVerifyText(CANNOT_CHANGE_STATUS_MSG, "Task status cannot be changed once it is Complete or Canceled.");
        screenshotPage(page,"Completion after cancellation verified");
        clickOn(CANCEL_BUTTON);
    }

    public void navigateToRelatedDocumentAndAddTask() {
        clickOn(RELATED_DOCUMENT_LINK);
        waitFor(5);
        clickOn(ADD_TASK_BUTTON);
        waitFor(2);
        type(DUE_DATE_INPUT_LAST_STRING, fetchWeekdayDate("MMM d, yyyy", 1));
        type(ADD_TASK_DOCUMENT_REPOSITORY_INPUT_LAST_STRING, "https://example.com");
        type(DOCUMENT_VERSION_INPUT_LAST_STRING, "1.0");
        type(COMMENTS_TEXTAREA_LAST_STRING, "This is a comment.");
        type(REVIEW_GUIDANCE_TEXTAREA_LAST_STRING, "This is review guidance.");
        type(ASSIGNED_USER_INPUT_LAST_STRING, "Pankaj TA App Ghosh");
        waitFor(1);
        clickOn(ASSIGNED_USER_AUTOCOMPLETE_OPTION_LAST_STRING);
        clickOn(REVIEWER_ROLE_BUTTON_LAST_STRING);
        waitFor(1);
        clickOn(REVIEWER_ROLE_OPTION_LAST_STRING);
        waitFor(1);
        try {
            if (page.locator(ASSIGNED_USER_CLEAR_BUTTON_LAST_STRING).isVisible()) {
                clickOn(ASSIGNED_USER_CLEAR_BUTTON_LAST_STRING);
            }
        } catch (Exception e) {
            LOGGER.info("Assigned User clear button not found");
        }

        type(ASSIGNED_USER_INPUT_LAST_STRING, "Pankaj TA App Ghosh");
        waitFor(1);
        clickOn(ASSIGNED_USER_AUTOCOMPLETE_OPTION_LAST_STRING);
        screenshotPage(page, "Filled ADD Task form");

        clickOn(SAVE_BUTTON);
        waitFor(2);
        readAndVerifyText(TASK_CREATED_SUCCESSFULLY_MSG, "Task created successfully.");
       
        screenshotPage(page,"Related document task added additionally to verify cancellation");
    }

    public void addTaskInDocumentReviewModalPopup() {

        clickOn(ADD_TASK_BUTTON);
        waitFor(1);
        type(ADD_TASK_DOCUMENT_REPOSITORY_INPUT, "https://test");
        clickOn(SAVE_BUTTON);
        waitFor(1);
        readAndVerifyText(DUE_DATE_ERROR_MSG, "Complete this field with format");
        readAndVerifyText(REVIEWER_ROLE_ERROR_MSG, "Complete this field");
        readAndVerifyText(DOCUMENT_REPOSITORY_ERROR_MSG, "Document Repository must be a valid URL with a domain");
        readAndVerifyText(DOCUMENT_VERSION_ERROR_MSG, "Complete this field");
        // //readAndVerifyText(DOCUMENT_REPOSITORY_INVALID_ERROR_MSG, "Document
        // Repository must be a valid URL with a domain");
        screenshotPage(page, "Mandatory fields verification for ADD Task");
        type(DUE_DATE_INPUT, fetchWeekdayDate("MMM d, yyyy", 0));
        type(ADD_TASK_DOCUMENT_REPOSITORY_INPUT, "https://example.com");
        type(DOCUMENT_VERSION_INPUT, "1.0");
        type(COMMENTS_TEXTAREA, "This is a comment.");
        type(REVIEW_GUIDANCE_TEXTAREA, "This is review guidance.");
        type(ASSIGNED_USER_INPUT, "Pankaj TA App Ghosh");
        waitFor(1);
        clickOn(ASSIGNED_USER_AUTOCOMPLETE_OPTION);
        clickOn(REVIEWER_ROLE_BUTTON);
        waitFor(1);
        clickOn(REVIEWER_ROLE_OPTION);
        waitFor(1);
        try {
            if (page.locator(ASSIGNED_USER_CLEAR_BUTTON).isVisible()) {
                clickOn(ASSIGNED_USER_CLEAR_BUTTON);
            }
        } catch (Exception e) {
            LOGGER.info("Assigned User clear button not found");
        }

        type(ASSIGNED_USER_INPUT, "Pankaj TA App Ghosh");
        waitFor(1);
        clickOn(ASSIGNED_USER_AUTOCOMPLETE_OPTION);
        screenshotPage(page, "Filled ADD Task form");

        clickOn(SAVE_BUTTON);
        waitFor(2);
        readAndVerifyText(TASK_CREATED_SUCCESSFULLY_MSG, "Task created successfully.");
       
    }

    public void verify_Review_Status_Accepted() {
        clickOn(EDIT_REVIEW_STATUS);
        clickOn(REVIEW_STATUS_BUTTON);
        clickOn(REVIEW_STATUS_ACCEPTED);
        LOGGER.info("Verifying Review Status Accepted");
        clickOn(SAVE_BUTTON);
        waitFor(2);
        scrollToBottomOfThePage();
        scrollToBottomUsingKeyboard();
        waitFor(2);
        readAndVerifyText(ACCEPTANCE_TASK_REQUIRED_MSG,
                "Acceptance Task is required when the Review Status is Accepted.");
        screenshotPage(page, "ACCEPTANCE_TASK_REQUIRED_MSG verification");
        clickOn(CANCEL_EDIT);
        waitFor(4);
    }

    public void verify_Review_Status_Complete() {
        clickOn(EDIT_REVIEW_STATUS);
        clickOn(REVIEW_STATUS_BUTTON);
        clickOn(REVIEW_STATUS_COMPLETE);
        clickOn(SAVE_BUTTON);
        waitFor(2);
        LOGGER.info("Verifying Review Status Complete");
        readAndVerifyText(REVIEW_STATUS, "Complete");
        screenshotPage(page, "Verifying Review Status Complete");
        clickOn(ACCEPT_DOCUMENT_BUTTON);
        waitFor(2);
        readAndVerifyText(CANNOT_ACCEPT_DOCUMENT_MSG, "Cannot Accept Document");
        screenshotPage(page, "Cannot Accept Document verification");
        clickOn(FINISH_BUTTON);
        waitFor(5);
    }

    public void verify_Document_Review_Header() {
        LOGGER.info("Verifying Document Review header");
        waitFor(5);

        page = switchToWindow(1);

        // page.reload();
        waitFor(5);
        screenshotPage(page, "Verifying Document Review header");
        String str = page.locator(DOCUMENT_REVIEW_HEADER).textContent();
        LOGGER.info("Document Review header text: {}", str);

        readAndVerifyText(DOCUMENT_REVIEW_HEADER, "Document Review");
        readAndVerifyText(REVIEW_STATUS, "In Progress");
        screenshotPage(page, "Document_Review_Header_Verification");
        page.reload();
        waitFor(5);
        scrollToBottomOfThePage();
        scrollToBottomUsingKeyboard();
        waitFor(2);
        Locator createdByLink = page.locator(CREATED_BY_LINK).first();
        createdByLink.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        waitFor(2);
        readAndVerifyText(CREATED_BY_LINK, "Pankaj MFP App Ghosh");
        readAndVerifyText(LAST_MODIFIED_BY_LINK, "Pankaj MFP App Ghosh");
        screenshotPage(page, "Created and modified user verification");
    }

    public void verify_Chatter_Functionality() {
        LOGGER.info("Verifying Chatter functionality");
        // Ensure we're on the correct window (the Document Review details page)
        waitFor(1);

        clickOn(SHARE_AN_UPDATE_BUTTON);
        type(SHARE_AN_UPDATE_TEXTAREA, "This is a test update for Chatter functionality.");
        clickOn(SHARE_BUTTON);
        waitFor(2);
        readAndVerifyText(UPDATE_SHARED_SUCCESS_MSG, "Your update was shared.");
    }

    public void verifyDocumentReviewMandatoryFields() {
        LOGGER.info("Verifying mandatory fields in Document Review");
        clickOn(ADD_DOCUMENT_REVIEW_BUTTON);
        waitFor(2);
        clickOn(FINISH_BUTTON);
        waitFor(2);
        screenshot("Document_Review_Mandatory_Fields");
        readAndVerifyText(DOCUMENT_TYPE_ERROR, "Please enter some valid input.");
        readAndVerifyText(REVIEW_DUE_DATE_ERROR, "Please enter some valid input.");
        readAndVerifyText(DOCUMENT_RECEIVED_DATE_ERROR, "Please enter some valid input.");
        readAndVerifyText(DOCUMENT_REPOSITORY_ERROR, "You have entered an invalid format.");
        readAndVerifyText(DOCUMENT_APPROVER_ERROR, "A value is required");
        clickOn(DOCUMENT_TYPE_BUTTON);
        waitFor(2);
        clickOn(DOCUMENT_TYPE_OTHER_OPTION);
        waitFor(2);
        clickOn(FINISH_BUTTON);
        waitFor(2);
        readAndVerifyText(OTHER_DOCUMENT_TYPE_ERROR, "Please enter some valid input.");
        screenshot("Other option mandatory field error message");
    }

    public void fillDocumentReviewDetails() {
        LOGGER.info("Filling Document Review details");

        type(OTHER_DOCUMENT_TYPE_INPUT, generateRandomString(10));
        type(REVIEW_DUE_DATE_INPUT, fetchWeekdayDate("M/d/yyyy", 0));
        type(DOCUMENT_RECEIVED_DATE_INPUT, fetchWeekdayDate("M/d/yyyy", 0));
        type(DOCUMENT_REPOSITORY_INPUT, "https://www.example.com");
        type(DOCUMENT_APPROVER_INPUT, "Pankaj MFP App Ghosh");
        clickOn(DOCUMENT_APPROVER_OPTION);
        type(REVIEW_STATUS_NOTES, "This is a test note for Document Review.");
        screenshot("Before Document_Review_Submission");
        clickOn(FINISH_BUTTON);
        waitFor(6);
    }

    public void navigateToChildAccount() {
        LOGGER.info("Navigating to child account");
        clickOn(CHILD_ACCOUNT_LINK);
        waitFor(2);
    }

    public void createDocumentReviewInChildAccount() {
        LOGGER.info("Creating another document review in child account");
        clickOn(ADD_DOCUMENT_REVIEW_BUTTON);
        waitFor(3);
        LOGGER.info("Filling Document Review details");
        clickOn(DOCUMENT_TYPE_BUTTON);
        waitFor(2);
        // above locator returns 5 elements we have to pick random one from 2 to 5
        int randomIndex = 2 + (int) (Math.random() * 4); // generates a random number between 2 and 5
        String locator = "xpath=(//*[text()='Document Type']/following::lightning-base-combobox-item/span/span)["
                + randomIndex + "]";

        clickOn(locator);
        waitFor(2);
        if (randomIndex == 5) {
            type(OTHER_DOCUMENT_TYPE_INPUT, generateRandomString(10));
            waitFor(2);
        }

        type(REVIEW_DUE_DATE_INPUT, fetchWeekdayDate("M/d/yyyy", 0));
        type(DOCUMENT_RECEIVED_DATE_INPUT, fetchWeekdayDate("M/d/yyyy", 0));
        waitFor(2);
        type(DOCUMENT_REPOSITORY_INPUT_LAST, "https://www.example.com");
        type(DOCUMENT_APPROVER_INPUT_LAST, "Pankaj MFP App Ghosh");
        clickOn(DOCUMENT_APPROVER_OPTION_LAST);
        type(REVIEW_STATUS_NOTES, "This is a test note for Document Review.");
        screenshot("Before Document_Review_Submission");
        clickOn(FINISH_BUTTON);
        waitFor(6);
    }

    public void verifyReviewStatusComplete_From_CompleteReview() {
        LOGGER.info("Verifying Review Status Complete from Complete Review");
        waitFor(2);
        page = switchToWindow(2);
        waitFor(2);
        LOGGER.info("Verifying Review Status Complete");
        readAndVerifyText(REVIEW_STATUS, "In Progress");
        screenshotPage(page, "Review Status Complete verification");
        clickOn(COMPLETE_REVIEW_BUTTON);
        waitFor(6);
        readAndVerifyText(REVIEW_STATUS, "Complete");
        screenshotPage(page, "Review Status Complete after clicking Complete Review");
        clickOn(ACCEPT_DOCUMENT_BUTTON);
        waitFor(2);
        readAndVerifyText(CANNOT_ACCEPT_DOCUMENT_MSG, "Cannot Accept Document");
        screenshotPage(page, "Cannot Accept Document verification");
        clickOn(FINISH_BUTTON);
        waitFor(5);

    }

}