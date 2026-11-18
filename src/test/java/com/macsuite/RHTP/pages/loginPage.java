package com.macsuite.RHTP.pages;

import com.macsuite.common.pages.BasePage;
import com.microsoft.playwright.Page;

public class loginPage extends BasePage {

    public static final String RequestAccessButton = "xpath=//a[text()='Request access']";
    public static final String submitButton = "xpath=//button[text()='Submit access request']";

    public loginPage(Page page) {
        super(page);
    }

    public void navigateToRequestAccessPage() {
        page.click(RequestAccessButton);
    }

    public void verifyAllMandatoryFields() {
        page.click(submitButton);
        waitFor(1);
        readAndVerifyText(getText_Xpath("Enter your organization."), "Enter your organization.");
        readAndVerifyText(getText_Xpath("Select your affiliation."), "Select your affiliation.");
        readAndVerifyText(getText_Xpath("Select your state."), "Select your state.");
        readAndVerifyText(getText_Xpath("Select your affiliation."), "Select your affiliation.");
        readAndVerifyText(getText_Xpath("Select your state."), "Select your state.");
        readAndVerifyText(getText_Xpath("Enter your first name."), "Enter your first name.");
        readAndVerifyText(getText_Xpath("Enter your last name."), "Enter your last name.");
        readAndVerifyText(getText_Xpath("Enter your work email."), "Enter your work email.");
        readAndVerifyText(getText_Xpath("Select your role."), "Select your role.");
        readAndVerifyText(getText_Xpath("Describe your areas of expertise."), "Describe your areas of expertise.");
        readAndVerifyText(getText_Xpath("Answer the security question."), "Answer the security question.");
        screenshot("MandatoryFieldsVerification");
    }

}
