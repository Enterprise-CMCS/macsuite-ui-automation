package com.macsuite.MFP_POH.pages;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.FormUtils;
import com.macsuite.common.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Page Object for MFP POH Accounts management.
 * Handles MFP POH-specific account operations and interFons.
 * Inherits common form operations from BasePage.
 */
public class AccountsPage extends BasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountsPage.class);

    // MFP POH Account-specific locators (renamed to follow constant naming
    // conventions)
    private static final String MFP_POH_ACCOUNT_ROLE_BUTTON = "xpath=//label[text()='MFP POH Account Role']/following::button[2]";
    private static final String MFP_POH_RECIPIENT = "xpath=//*[@title='MFP POH Recipient']";
    private static final String GRANT_NUMBER = "xpath=//label[text()='Grant Number']/following::input[1]";
    private static final String BUDGET_AMOUNT = "xpath=//label[text()='Final Approved CY Budget Amount']/following::input[1]";
    private static final String BUDGET_REPORTING_YEAR = "xpath=//label[text()='Budget Reporting Year']/following::button[1]/span";
    private static final String BUDGET_REPORTING_YEAR_OPTION = "xpath=//*[@title='2025']";
    private static final String UEI_INPUT = "xpath=//label[text()='UEI']/following::input[1]";
    private static final String ASSIGNED_PO = "xpath=//label[text()='Assigned PO']/following::input[1]";
    private static final String ASSIGNED_GMS = "xpath=//label[text()='Assigned GMS']/following::input[1]";
    private static final String PARENT_ACCOUNT = "xpath=//label[text()='Parent Account']/following::input[1]";
    private static final String ASSIGNED_PO_OPTION = "xpath=//label[text()='Assigned PO']/following::lightning-base-combobox-item[1]";
    private static final String ASSIGNED_GMS_OPTION = "xpath=//label[text()='Assigned GMS']/following::lightning-base-combobox-item[1]";
    private static final String PARENT_ACCOUNT_OPTION = "xpath=//label[text()='Parent Account']/following::lightning-base-combobox-item[1]";
    public static final String SAVE_BUTTON = "xpath=//button[text()='Save']";
    private static final String ADD_CHECKLIST = "xpath=//button[text()='Add Checklist']";
    private static final String BUDGET_REVIEW_INPUT = "xpath=//span[text()='Budget Review']";
    private static final String NEXT_BUTTON = "xpath=//*[text()='Next']";
    private static final String MFP_PROJECT_DIRECTOR = "xpath=(//*[text()='MFP Project Director']/following::input[1])[last()]";
    private static final String MFP_PROJECT_DIRECTOR_OPTION = "xpath=(//*[text()='MFP Project Director']/following::input[1]/following::lightning-base-combobox-item[1])[last()]";
    private static final String SUBMISSION_DATE = "xpath=(//*[text()='Submission Date']/following::input[1])[last()]";
    private static final String REQUESTED_SUPPLEMENT_FUNDING_AMOUNT = "xpath=(//*[text()='Requested Supplemental Funding Amount']/following::input[1])[last()]";
    private static final String CREATE_CHECKLIST = "xpath=//*[text()='Create Checklist']";
    private static final String CHECKLIST_HEADER = "xpath=//h1//a";
    private static final String COMPLETE_THIS_FIELD = "xpath=//*[text()='Complete this field.']";
    private static final String CLOSE_ERROR_DIALOG = "xpath=//button[@title='Close error dialog']";
    private static final String FIELD_LEVEL_ERRORS = "xpath=//div[@class='fieldLevelErrors']";
    private static final String START_DATE = "xpath=//input[@name='Period_of_Performance_Start_Date']";
    private static final String END_DATE = "xpath=//input[@name='Period_of_Performance_End_Date']";
    private static final String ERROR_MESSAGE_STRING = "xpath=//*[text()='Performance Period Start Date cannot be after Performance Period End Date']";
    private static final String GRANTS_MANAGEMENT_SPECIALIST_INPUT = "xpath=//input[@aria-label='Grants Management Specialist']";
    private static final String GRANTS_MANAGEMENT_SPECIALIST_OPTION = "xpath=//input[@aria-label='Grants Management Specialist']/following::lightning-base-combobox-item[2]";
    private static final String APPLICATION_NUMBER_INPUT = "xpath=(//*[text()='Application Number']/following::input[1])[last()]";

    private static final String ACCOUNTS_SEARCH_INPUT = "xpath=//input[@name='Account-search-input']";
    private static final String CHILD_ACCOUNT_LINK = "xpath=//a[@title='Pankaj_Test_Child_Account_Automation']";
    public static final String SELECT_TYPE_BUTTON = "xpath=(//*[text()='Select Type']/following::button[1])[last()]";
    public static final String BUDGET_REVIEW_OPTION = "xpath=(//*[text()='Budget Review'])[last()]";

    public AccountsPage(Page page) {
        super(page);
    }

    public void verifyChecklistMandatoryFields() {
        logger.info("Verifying mandatory field errors on checklist item form");

        try {
            clickOn(ADD_CHECKLIST);
           
            clickOn(BUDGET_REVIEW_INPUT);
            clickOn(NEXT_BUTTON);
            type(START_DATE, fetchWeekdayDate("MM/dd/yyyy", 1));
            waitFor(1);
            type(END_DATE, fetchWeekdayDate("MM/dd/yyyy", -1));
            clickOn(CREATE_CHECKLIST);
            waitFor(2);
            readAndVerifyText(ERROR_MESSAGE_STRING,
                    "Performance Period Start Date cannot be after Performance Period End Date");
            logger.info("✓ Mandatory field error verified for checklist item");
            screenshot("MandatoryFieldError - Start date and End date");
            waitFor(1);

            type(START_DATE, fetchWeekdayDate("MM/dd/yyyy", -1));
            waitFor(1);
            type(END_DATE, fetchWeekdayDate("MM/dd/yyyy", 1));
        } catch (Exception e) {
            logger.warn("Error while verifying mandatory field errors on checklist item: {}", e.getMessage());
        }
    }

    /**
     * Fill checklist item form with default values
     * Customize based on your checklist item form structure
     */
    public void fillChecklistItemWithDefaultValues() throws Exception {
        logger.debug("Filling checklist item form with default values");

        try {
             clickOn(ADD_CHECKLIST);
             waitFor(1);
            clickOn(SELECT_TYPE_BUTTON);
            waitFor(1);
            clickOn(BUDGET_REVIEW_OPTION);
            waitFor(1);
            type(MFP_PROJECT_DIRECTOR, "Pankaj");
            clickOn(MFP_PROJECT_DIRECTOR);
            clickOn(MFP_PROJECT_DIRECTOR_OPTION);
            type(SUBMISSION_DATE, fetchWeekdayDate("MMM dd, yyyy", 0));
            // clickOn(GRANTS_MANAGEMENT_SPECIALIST_INPUT);
            // type(GRANTS_MANAGEMENT_SPECIALIST_INPUT, "Pankaj MFP App Ghosh");
            // clickOn(GRANTS_MANAGEMENT_SPECIALIST_OPTION);
            APPLICATION_NUMBER_STRING = randomNumberGenerator(13);
            type(APPLICATION_NUMBER_INPUT, APPLICATION_NUMBER_STRING);
            type(REQUESTED_SUPPLEMENT_FUNDING_AMOUNT, "1000");
            type(APPLICATION_NUMBER_INPUT, randomNumberGenerator(13));
            screenshot("Before Creating Checklist Item");
            clickOn(getText_Xpath("Save"));
            waitFor(5);
            String checklistHeader = page.locator(CHECKLIST_HEADER).first().textContent().trim();
            logger.info("After Checklist item created with header: {}", checklistHeader);
            logger.debug("Checklist item description filled");

        } catch (Exception e) {
            logger.warn("Could not fill checklist item: {}", e.getMessage());
        }

        // Add more fields as needed based on your form structure
    }

    /**
     * Fill account form with default required values
     * Customize field names and values based on your actual form
     */
    public void enterAccountName() throws Exception {
        logger.debug("Filling account form with default values");
        FormUtils utils = getFormUtils();

        try {
            RECEIPENT = "MFP POH Account - " + System.currentTimeMillis();
            utils.fillField("Account Name", RECEIPENT);
            logger.debug("Account Name filled");
        } catch (Exception e) {
            logger.warn("Could not fill Account Name: {}", e.getMessage());
        }

    }

    /**
     * Click the MFP POH Account Role button
     * Used for selecting or modifying account role settings
     */
    public void selectMFPPOHAccountRole() {
        logger.info("Clicking MFP POH Account Role button");

        try {
            Locator roleButton = page.locator(MFP_POH_ACCOUNT_ROLE_BUTTON).first();
            roleButton.waitFor();
            roleButton.click();
            Locator role_Recipient = page.locator(MFP_POH_RECIPIENT); // Wait for any post-click UI changes
            role_Recipient.waitFor();
            role_Recipient.click();
            logger.info("✓ MFP POH Account Role button clicked");
        } catch (Exception e) {
            logger.error("Failed to click MFP POH Account Role button: {}", e.getMessage());
            throw new RuntimeException("Could not click MFP POH Account Role button: " + e.getMessage(), e);
        }
    }

    public void verifyMandatoryFields_While_Filling_AccountDetails() {

        logger.info("Verifying mandatory field errors while filling account details");
        clickOn(SAVE_BUTTON);
        try {
            String errorText = page.locator(FIELD_LEVEL_ERRORS).first().textContent().trim();
            logger.info("Field level error text: " + errorText);
            if (errorText.contains("Grant Number")
                    && errorText.contains("Budget Amount")
                    && errorText.contains("Budget Reporting Year")
                    && errorText.contains("UEI")
                    && errorText.contains("Assigned PO")
                    && errorText.contains("Assigned GMS")
            // && errorText.contains("Parent Account")
            ) {
                logger.info("✓ Mandatory field error verified");
            } else {
                throw new AssertionError("Expected mandatory field errors not found. Actual error text: " + errorText);
            }

            screenshot("MandatoryFieldError - Account Details");
            clickOn(CLOSE_ERROR_DIALOG); // Close the error dialog to continue with form filling
        } catch (Exception e) {
            logger.warn("Error while verifying mandatory field errors: {}", e.getMessage());
        }
    }

    public void fillAccountFormWithDefaultValues() {
        logger.info("Filling account form with default values");
        try {

            clickOn(ASSIGNED_PO);
            clickOn(ASSIGNED_PO_OPTION);
            clickOn(ASSIGNED_GMS);
            clickOn(ASSIGNED_GMS_OPTION);
            clickOn(PARENT_ACCOUNT);
            type(PARENT_ACCOUNT, "t"); // Type something to trigger options
            clickOn(PARENT_ACCOUNT_OPTION);
            GRANT_NUMBER_STRING = randomNumberGenerator(6);
            type(GRANT_NUMBER, GRANT_NUMBER_STRING);
            type(BUDGET_AMOUNT, "100");
            Thread.sleep(2000); // Wait for Budget Reporting Year field to be interactable
            clickOn(BUDGET_REPORTING_YEAR);
            // Thread.sleep(2000);
            // clickOn(BUDGET_REPORTING_YEAR);
            Thread.sleep(2000);
            clickOn(BUDGET_REPORTING_YEAR_OPTION);
            type(UEI_INPUT, "ABCDE12345");
            clickOn(SAVE_BUTTON);
            logger.info("✓ Account form filled with default values");
        } catch (Exception e) {
            logger.error("Failed to fill account form with default values: {}", e.getMessage());
            throw new RuntimeException("Could not fill account form: " + e.getMessage(), e);
        }
    }

    /**
     * Verify that the MFP POH Account Role button is visible
     * 
     * @return true if button is visible, false otherwise
     */
    public boolean isMFPPOHAccountRoleButtonVisible() {
        logger.debug("Checking if MFP POH Account Role button is visible");

        try {
            Locator roleButton = page.locator(MFP_POH_ACCOUNT_ROLE_BUTTON);
            boolean isVisible = roleButton.isVisible();
            logger.debug("MFP POH Account Role button visible: {}", isVisible);
            return isVisible;
        } catch (Exception e) {
            logger.debug("Error checking MFP POH Account Role button visibility: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the text content of the MFP POH Account Role button
     * 
     * @return the button text, or null if not found
     */
    public String getMFPPOHAccountRoleButtonText() {
        logger.debug("Getting MFP POH Account Role button text");

        try {
            Locator roleButton = page.locator(MFP_POH_ACCOUNT_ROLE_BUTTON);
            if (roleButton.isVisible()) {
                String text = roleButton.textContent().trim();
                logger.debug("MFP POH Account Role button text: {}", text);
                return text;
            }
        } catch (Exception e) {
            logger.debug("Error getting MFP POH Account Role button text: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Navigate to Accounts list view
     * Assumes the user is in the MFP POH app
     */
    public void navigateToAccountsList() {
        logger.info("Navigating to Accounts list");

        try {
            // Try to find and click Accounts navigation link
            Locator accountsLink = page.getByRole(AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Accounts"));

            if (accountsLink.count() > 0) {
                accountsLink.first().click();
                page.waitForLoadState();
                logger.info("✓ Navigated to Accounts list");
            } else {
                logger.warn("Accounts navigation link not found");
            }
        } catch (Exception e) {
            logger.error("Failed to navigate to Accounts list: {}", e.getMessage());
            throw new RuntimeException("Could not navigate to Accounts list: " + e.getMessage(), e);
        }
    }

    /**
     * Click the New button to create a new account
     * Assumes the user is on the Accounts list view
     */
    public void clickNewAccountButton() {
        logger.info("Clicking New button to create account");

        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                    .setName("New")).click();

            // Wait for the account form modal to appear
            page.locator(".isModal.inlinePanel:visible").first().waitFor();

            logger.info("✓ New account form opened");
        } catch (Exception e) {
            logger.error("Failed to click New button: {}", e.getMessage());
            throw new RuntimeException("Could not open new account form: " + e.getMessage(), e);
        }
    }

    /**
     * Fill account form with provided values
     * Supports any account field that can be filled via FormUtils
     * 
     * @param formData Map of field labels to values
     */
    public void fillAccountForm(Map<String, String> formData) {
        logger.info("Filling account form with {} fields", formData.size());

        try {
            // Use inherited fillForm from BasePage which uses FormUtils smart field
            // detection
            fillForm(formData);
            logger.info("✓ Account form filled successfully");
        } catch (Exception e) {
            logger.error("Failed to fill account form: {}", e.getMessage());
            throw new RuntimeException("Could not fill account form: " + e.getMessage(), e);
        }
    }

    /**
     * Save the account form
     * Clicks the Save button to save account details
     */
    public void saveAccount() {
        logger.info("Saving account");

        try {
            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions()
                            .setName("Save")
                            .setExact(true))
                    .click();

            // Wait for post-save UI stabilization
            page.waitForLoadState();

            logger.info("✓ Account saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save account: {}", e.getMessage());
            throw new RuntimeException("Could not save account: " + e.getMessage(), e);
        }
    }

    /**
     * Verify account details are displayed on the detail page
     * 
     * @return true if account details are visible, false otherwise
     */
    public boolean verifyAccountDetailsDisplayed() {
        logger.info("Verifying account details are displayed");

        try {
            // Wait for detail page elements to load
            page.locator("records-highlights-details-item").first().waitFor();

            logger.info("✓ Account details verified as displayed");
            return true;
        } catch (Exception e) {
            logger.error("Failed to verify account details: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the account name from the detail page
     * 
     * @return the account name, or null if not found
     */
    public String getAccountName() {
        logger.debug("Getting account name from detail page");

        try {
            Locator accountNameField = page.locator(".slds-page-header__title").first();
            if (accountNameField.isVisible()) {
                String accountName = accountNameField.textContent().trim();
                logger.debug("Account name: {}", accountName);
                return accountName;
            }
        } catch (Exception e) {
            logger.debug("Error getting account name: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Navigate to the Checklist section of the account
     * Finds and scrolls to the checklist section
     * 
     * @return Locator for the checklist section, or null if not found
     */
    public Locator navigateToChecklistSection() {
        logger.info("Navigating to Checklist section");

        try {
            // Look for Checklist-related sections
            Locator checklistSection = page.locator("section[aria-label*='Checklist'], " +
                    "div[title*='Checklist'], " +
                    "[data-qa*='Checklist']").first();

            if (checklistSection.count() > 0) {
                checklistSection.scrollIntoViewIfNeeded();
                logger.info("✓ Navigated to Checklist section");
                return checklistSection;
            } else {
                logger.warn("Checklist section not found");
            }
        } catch (Exception e) {
            logger.error("Error navigating to Checklist section: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Click the Add button in a related list (e.g., Checklist)
     * 
     * @param relatedListSection The related list section locator
     */
    public void clickAddButtonInRelatedList(Locator relatedListSection) {
        logger.info("Clicking Add button in related list");

        try {
            Locator addButton = relatedListSection.locator("button:has-text('Add'), [title*='Add']").first();
            if (addButton.count() > 0) {
                addButton.click();
                page.waitForTimeout(500); // Wait for form to appear
                logger.info("✓ Add button clicked");
            } else {
                throw new RuntimeException("Add button not found in related list");
            }
        } catch (Exception e) {
            logger.error("Failed to click Add button: {}", e.getMessage());
            throw new RuntimeException("Could not click Add button: " + e.getMessage(), e);
        }
    }

    /**
     * Verify that a checklist item was added successfully
     * 
     * @return true if checklist item is visible, false otherwise
     */
    public boolean verifyChecklistItemAdded() {
        logger.info("Verifying checklist item was added");

        try {
            // Wait for the checklist item to appear in the list
            page.locator("[title*='Checklist']").first().waitFor();
            logger.info("✓ Checklist item verified as added");
            return true;
        } catch (Exception e) {
            logger.error("Failed to verify checklist item: {}", e.getMessage());
            return false;
        }
    }

    public void verifyMandatoryFieldErrors_While_AccountCreation() {
        logger.debug("Verifying mandatory field errors on account creation form");

        // Example: Verify "Complete this field." error appears for required fields
        try {
            clickOn(SAVE_BUTTON);
            readAndVerifyText(COMPLETE_THIS_FIELD, "Complete this field.");
            logger.info("✓ Mandatory field error verified");
            screenshot("MandatoryFieldError");
            clickOn(CLOSE_ERROR_DIALOG); // Close the error dialog to continue with form filling
        } catch (Exception e) {
            logger.warn("Error while verifying mandatory field errors: {}", e.getMessage());
        }
    }

    /**
     * Navigate to the Accounts view in the MFP POH app
     * Customize based on your app's actual navigation structure
     */
    public void navigateToAccountsView() {
        logger.debug("Navigating to Accounts view");

        // Try to find and click Accounts tab/menu item
        // Pattern: Look for navigation item with "Accounts" text
        try {
            Locator accountsNav = page().getByRole(AriaRole.LINK,
                    new com.microsoft.playwright.Page.GetByRoleOptions().setName("Accounts"));

            if (accountsNav.count() > 0) {
                accountsNav.first().click();
                page().waitForLoadState();
                logger.debug("Clicked Accounts navigation");
            } else {
                logger.warn("Accounts navigation item not found, may already be on page");
            }
        } catch (Exception e) {
            logger.debug("Navigation to Accounts view encountered: {}", e.getMessage());
        }
    }

    public void navigateToChildAccountDetailsPage(String accountName) {
        logger.debug("Navigating to child account details page");

        try {
            type(ACCOUNTS_SEARCH_INPUT, accountName);
            page().keyboard().press("Enter");
            page().waitForLoadState();
            Locator childAccountLink = page().locator(CHILD_ACCOUNT_LINK);
            if (childAccountLink.count() > 0) {
                childAccountLink.first().click();
                page().waitForLoadState();
                logger.debug("Clicked on child account link: {}", accountName);
            } else {
                logger.warn("Child account link not found for account name: {}", accountName);
            }
        } catch (Exception e) {
            logger.debug("Error navigating to child account details: {}", e.getMessage());
        }
       
    }

} // End of AccountsPage class
