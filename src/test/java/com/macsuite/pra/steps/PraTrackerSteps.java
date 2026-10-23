package com.macsuite.pra.steps;

import com.macsuite.common.ModuleSteps;
import com.macsuite.pra.pages.PraTrackerPage;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Step definitions for CMCS PRA Tracker workflows.
 */
public class PraTrackerSteps extends ModuleSteps<PraTrackerPage> {
    private static final Logger logger = LoggerFactory.getLogger(PraTrackerSteps.class);

    @Override
    protected PraTrackerPage createModulePage() {
        return new PraTrackerPage(page());
    }

    @Then("I should see the PRA ICR package form")
    public void verifyPraIcrPackageForm() {
        logger.info("Verifying PRA ICR package form");
        getModulePage().verifyIcrPackageFormDisplayed();
    }

    @When("I select PRA record type {string}")
    public void selectPraRecordType(String recordType) {
        logger.info("Selecting PRA record type: {}", recordType);
        getModulePage().selectRecordType(recordType);
    }

    @When("I fill PRA ICR package details with:")
    public void fillPraIcrPackageDetails(Map<String, String> packageData) {
        logger.info("Filling PRA ICR package details");
        getModulePage().fillIcrPackageDetails(packageData);
        screenshot("PRA Package Form Filled");
    }

    @When("I update PRA package fields and verify stage {string}:")
    public void updatePraFieldsAndVerifyStage(String expectedStage, Map<String, String> fields) {
        logger.info("Updating PRA package and verifying stage: {}", expectedStage);
        getModulePage().updateFieldsAndVerifyStage(fields, expectedStage);
        screenshot("PRA Stage: " + expectedStage);
    }

    @And("I request PRA action with {string}")
    public void andRequestPraActionWith(String requestType) {
        logger.info("Requesting PRA action with: {}", requestType);
        getModulePage().takeRequestAction(requestType);
    }

    @When("I request PRA action {string}")
    public void requestPraAction(String requestType) {
        logger.info("Requesting PRA action: {}", requestType);
        getModulePage().requestAction(requestType);
    }

    @Then("the PRA package should be closed and the child ICR package should open")
    public void verifyPraPackageClosedAndChildOpened() {
        logger.info("Verifying PRA package is closed and child ICR package opens");
        getModulePage().verifyPackageClosedAndOpenChildIcr();
        screenshot("PRA Package Closed");
    }

    @Then("I should see generated PRA identifiers when required")
    public void verifyGeneratedPraIdentifiersWhenRequired() {
        logger.info("Verifying generated PRA identifiers when required");
        getModulePage().verifyGeneratedIdentifiersOnChildPackageIfRequired();
        screenshot("PRA Identifiers");
    }

    @Then("I should see entity {string} label displayed")
    public void verifyEntityLabelDisplayed(String entityName) {
        logger.info("Verifying {} entity label displayed", entityName);
        page().locator("span.entityName.uiOutputText")
            .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText(entityName))
            .first()
            .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
        screenshot(entityName + " Label Displayed");
    }
}
