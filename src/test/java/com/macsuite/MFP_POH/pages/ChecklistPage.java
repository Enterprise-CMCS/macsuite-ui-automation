package com.macsuite.MFP_POH.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.pages.BasePage;
import com.microsoft.playwright.Page;

public class ChecklistPage extends BasePage {
    public ChecklistPage(Page page) {
        super(page);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountsPage.class);

    public static final String GENERATE_COMMENTS_PACKAGE = "xpath=//button[text()='Generate Comments Package']";
    private static final String CURRENT_STAGE = "xpath=//*[@data-aura-class='runtime_sales_pathassistantPathAssistantHeader']";

    private static final String COMMENTS_UNDER_REVIEW_SUCCESS_MSG = "xpath=//*[contains(text(), 'The file has been attached successfully.')]";
    private static final String RECOMMENDATION_BUTTON = "xpath=//*[@title='Recommendation']/following::button[2]";
    private static final String PROJECT_OFFICER_OPTION = "xpath=//*[text()='Recommendation Project Officer']/following::lightning-base-combobox-item";
    private static final String RECOMENDATION_SUBMIT_SUCCESS_MSG = "xpath=//*[contains(text(), 'Record updated successfully')]";

    private static final String MAKE_RECOMMENDATION = "xpath=//*[text()='Make Recommendation']";
    private static final String COMPLETE_THIS_FIELD = "xpath=//*[text()='Complete this field.']";
    private static final String SAVE_BUTTON = "xpath=(//*[text()='Save'])[last()]";
    private static final String FUND_OPTION = "xpath=//*[@title='Recommendation']/following::button[2]/following::lightning-base-combobox-item//span[@title='Fund']";
    private static final String FUND_WITH_RESTRICTIONS_OPTION = "xpath=//*[@title='Recommendation']/following::button[2]/following::lightning-base-combobox-item//span[@title='Fund with Restrictions']";
    private static final String DO_NOT_FUND_OPTION = "xpath=//*[@title='Recommendation']/following::button[2]/following::lightning-base-combobox-item//span[@title='Do not fund']";

    private static final String RESTRICTION_AMOUNT = "xpath=//*[text()='Restriction Amount']/following-sibling::div/input";
    private static final String RESTRICTION_AMOUNT_ERROR_MSG = "xpath=//*[contains(text(), 'Restriction Amount cannot exceed the Requested Supplemental Funding Amount')]";
    private static final String HOLD_REASON = "xpath=//*[text()='Hold Reason']/following::input";

    public void verify_funding_Memo_On_Hold_Stage() {
        LOGGER.info("Verifying Funding Memo On Hold stage");
        clickOn(getButtonText_Xpath("Place Review On Hold"));
        waitFor(2);
        type(HOLD_REASON, generateRandomString(252));
        clickOn(SAVE_BUTTON);
        readAndVerifyText(getText_Xpath("Budget Review record is put on hold"), "Budget Review record is put on hold");
        waitFor(2);
        readAndVerifyText(getText_Xpath("This record is on hold."), "This record is on hold.");
        readAndVerifyText(getText_Xpath(" To remove the hold, select the Remove Hold button."),
                " To remove the hold, select the Remove Hold button.");
        clickOn(getButtonText_Xpath("Remove Hold"));
        waitFor(1);
        clickOn(SAVE_BUTTON);
        readAndVerifyText(getText_Xpath("Hold is removed from Budget Review"), "Hold is removed from Budget Review");
        waitFor(2);

        if (!isElementVisible(getText_Xpath("This record is on hold."))) {
            LOGGER.info("Hold status removed successfully, as expected.");
        } else {
            throw new RuntimeException("Hold status is still present after removing hold, which is unexpected.");
        }

    }

    public void navigateToCommentsUnderReview() {
        LOGGER.info("Navigating to comments under review");
        clickOn(GENERATE_COMMENTS_PACKAGE);
        readAndVerifyText(COMMENTS_UNDER_REVIEW_SUCCESS_MSG, "The file has been attached successfully.");
        waitFor(5);
        screenshot("Comments Under Review page details");
    }

    public void verify_Details_In_Comments_Under_Review() {
        LOGGER.info("Verifying details in comments under review");
        readAndVerifyText(getButtonText_Xpath("Make Recommendation"), "Make Recommendation");
        readAndVerifyText(getButtonText_Xpath("Place Review On Hold"), "Place Review On Hold");
        readAndVerifyText(getButtonText_Xpath("Generate Comments Package"), "Generate Comments Package");
        readAndVerifyText(CURRENT_STAGE, "Comments under Review");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        readAndVerifyText(
                getText_Xpath("You have sent embedded comments for this Budget Review to OAGM via email."),
                "You have sent embedded comments for this Budget Review to OAGM via email.");
        waitFor(5);
        screenshot("Comments Under Review details verified");
    }

    public void verify_Details_In_Recommendation() {
        LOGGER.info("Verifying details in recommendation stage");
        readAndVerifyText(getButtonText_Xpath("Request Funding Memo"), "Request Funding Memo");
        readAndVerifyText(getButtonText_Xpath("Place Review On Hold"), "Place Review On Hold");
        readAndVerifyText(getButtonText_Xpath("Update Recommendation"), "Update Recommendation");
        readAndVerifyText(CURRENT_STAGE, "Recommendation Sent");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        readAndVerifyText(getText_Xpath(
                "You have completed this review and sent final comments and recommendation via email to OAGM. Don't forget to upload all documents to GrantSolutions."),
                "You have completed this review and sent final comments and recommendation via email to OAGM. Don't forget to upload all documents to GrantSolutions.");
        screenshot("Recommendation stage details verified");
    }

    public void submit_Recommendation_Form() {
        clickOn(MAKE_RECOMMENDATION);
        waitFor(1);
        clickOn(SAVE_BUTTON);
        int size = getElementsCount(COMPLETE_THIS_FIELD);
        if (size == 5) {
            LOGGER.info("Recommendation form mandatory fields are verified.");
        } else {
            throw new RuntimeException(
                    "Recommendation form mandatory fields are not verified. Expected 5 mandatory fields, but found "
                            + size);
        }
        waitFor(5);
        screenshot("Recommendation form details before filling mandatory fields");

        clickOn(RECOMMENDATION_BUTTON);
        waitFor(5);
        screenshot("Recommendation form details before selecting fund option");
        clickOn(FUND_OPTION);
        if (!isElementVisible(RESTRICTION_AMOUNT)) {
            LOGGER.info("Restriction Amount field is not visible for fund option, as expected.");
        } else {
            throw new RuntimeException("Restriction Amount field is visible for fund option, which is unexpected.");
        }
        clickOn(RECOMMENDATION_BUTTON);
        clickOn(DO_NOT_FUND_OPTION);
        if (!isElementVisible(RESTRICTION_AMOUNT)) {
            LOGGER.info("Restriction Amount field is not visible for Do not fund option, as expected.");
        } else {
            throw new RuntimeException(
                    "Restriction Amount field is visible for Do not fund option, which is unexpected.");
        }
        waitFor(5);
        screenshot("Recommendation form details after selecting Do not fund option");

        clickOn(RECOMMENDATION_BUTTON);
        clickOn(FUND_WITH_RESTRICTIONS_OPTION);
        if (isElementVisible(RESTRICTION_AMOUNT)) {
            LOGGER.info("Restriction Amount field is visible for Fund with Restrictions option, as expected.");
        } else {
            throw new RuntimeException(
                    "Restriction Amount field is not visible for Fund with Restrictions option, which is unexpected.");
        }
        type(RESTRICTION_AMOUNT, "1100");
        clickOn(SAVE_BUTTON);
        waitFor(1);
        if (isElementVisible(RESTRICTION_AMOUNT_ERROR_MSG)) {
            LOGGER.info(
                    "Restriction Amount error message is displayed when entered amount exceeds the limit, as expected.");
        } else {
            throw new RuntimeException(
                    "Restriction Amount error message is not displayed when entered amount exceeds the limit, which is unexpected.");
        }
        waitFor(1);
        screenshot("Recommendation form details after entering invalid Restriction Amount");
        type(RESTRICTION_AMOUNT, "900");
        type(get_Text_Input_Xpath("Final Approved CY Budget Amount"), "1000");
        type(get_Text_Input_Xpath("Previous CY Unobligated Balance"), "100");
        type(get_Text_Input_Xpath("Final Supplemental Funding Amount"), "500");
        type(get_Text_Input_Xpath("Recommendation Project Officer"), "");
        clickOn(PROJECT_OFFICER_OPTION);
        clickOn(getButtonText_Xpath("Save"));
        readAndVerifyText(RECOMENDATION_SUBMIT_SUCCESS_MSG, "Record updated successfully");
        waitFor(5);
        screenshot("Recommendation form details after submitting recommendation");
    }

    public void navigateToRequestFundingMemoStage() {
        LOGGER.info("Navigating to Request Funding Memo stage");
        waitFor(3);
        clickOn(getButtonText_Xpath("Request Funding Memo"));
        waitFor(2);
        clickOn(SAVE_BUTTON);
        waitFor(5);
        screenshot("Request Funding Memo stage details");
    }

    public void verify_Details_In_Request_Funding_Memo() {
        LOGGER.info("Verifying details in Request Funding Memo stage");
        readAndVerifyText(getButtonText_Xpath("Convert to Funding"), "Convert to Funding");
        readAndVerifyText(getButtonText_Xpath("Place Review On Hold"), "Place Review On Hold");
        readAndVerifyText(CURRENT_STAGE, "Funding Memo Requested");
        readAndVerifyText(getText_Xpath("Guidance for Success"), "Guidance for Success");
        readAndVerifyText(
                getText_Xpath(
                        "OAGM has requested a funding memo. You can convert this Budget Review to a Funding Opportunity."),
                "OAGM has requested a funding memo. You can convert this Budget Review to a Funding Opportunity.");
        screenshot("Request Funding Memo stage details verified");
    }

    public void navigatedToConvertedToFundingStage() {

        LOGGER.info("Navigating to Converted To Funding stage");
        waitFor(3);
        clickOn(getButtonText_Xpath("Convert to Funding"));
        waitFor(2);
        clickOn(getButtonText_Xpath("Convert"));
        waitFor(2);
        screenshot("Converted To Funding stage details");
    }

    public void verify_Details_In_Converted_To_Funding() {
        LOGGER.info("Verifying details in Converted To Funding stage");

    }

} // End of ChecklistPage class
