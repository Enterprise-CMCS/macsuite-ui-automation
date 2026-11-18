package com.macsuite.RHTP.steps;

import com.macsuite.RHTP.pages.loginPage;
import com.macsuite.common.ModuleSteps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RHTP_login_Steps extends ModuleSteps<loginPage> {

    @Override
    protected loginPage createModulePage() {
        return new loginPage(page());
    }

    @When("I navigate to the Request Access page")
    public void i_navigate_to_the_request_access_page() {
        getModulePage().navigateToRequestAccessPage();
    }

    @Then("I validate all the mandatory fields on the Request Access page")
    public void i_validate_all_the_mandatory_fields_on_the_request_access_page() {
        getModulePage().verifyAllMandatoryFields();
    }

}
