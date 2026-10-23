package com.macsuite.hiring_metrics.steps;

import com.macsuite.hiring_metrics.pages.HiringMetricsPage;
import com.macsuite.common.ModuleSteps;
import io.cucumber.java.en.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Step definitions for CMCS Hiring Metrics Hub scenarios.
 * Handles hiring metrics creation, status transitions, notes, and field history.
 */
public class HiringMetricsSteps extends ModuleSteps<HiringMetricsPage> {
    private static final Logger logger = LoggerFactory.getLogger(HiringMetricsSteps.class);
    private String createdRecordNumber;

    @Override
    protected HiringMetricsPage createModulePage() {
        return new HiringMetricsPage(page());
    }

    /**
     * Verify hiring metrics form is displayed
     */
    @Then("I should see CMCS Hiring Metrics Hub home page header details")
    public void verifyHiringMetricsHubHomePageHeaderDetails() {
        logger.info("Verifying CMCS Hiring Metrics Hub home page header details");
        getModulePage().verifyHomePageHeaderDetails();
        screenshot("HMH Home Page Header");
    }

    @Then("I should see the hiring metrics form")
    public void verifyhiringMetricsFormDisplayed() {
        logger.info("Verifying hiring metrics form is displayed");
        getModulePage().verifyFormDisplayed();
    }

    /**
     * Fill hiring metrics form with provided data
     */
    @When("I fill in the hiring metrics details with:")
    public void fillHiringMetricsDetails(Map<String, String> formData) {
        logger.info("Filling hiring metrics form with data");
        getModulePage().fillForm(formData);
        logger.info("✓ Hiring metrics form filled");
        screenshot("Hiring Metrics Form Filled");
    }

    /**
     * Verify record number is displayed after creation
     */
    @Then("I should see the record number displayed")
    public void verifyRecordNumberDisplayed() throws Exception {
        logger.info("Verifying record number is displayed");

        createdRecordNumber = getModulePage().extractRecordNumber();

        if (createdRecordNumber != null && !createdRecordNumber.isEmpty()) {
            logger.info("✓ Record number displayed: {}", createdRecordNumber);
        } else {
            throw new AssertionError("Record number not displayed");
        }
        screenshot("Record Number");
    }

    /**
     * Update the status of hiring metrics record
     */
    @When("I update the status to {string}")
    public void updateStatus(String status) throws Exception {
        logger.info("Updating status to: {}", status);
        getModulePage().updateStatus(status);
    }

    /**
     * Add a note to the hiring metrics record
     */
    @When("I add a note {string}")
    public void addNote(String noteText) throws Exception {
        logger.info("Adding note: {}", noteText);
        getModulePage().addNote(noteText);
    }

    /**
     * Verify the status of the hiring metrics record
     */
    @Then("I should see the status as {string}")
    public void verifyStatus(String expectedStatus) throws Exception {
        logger.info("Verifying status is: {}", expectedStatus);
        getModulePage().verifyStatus(expectedStatus);
        screenshot("Status: " + expectedStatus);
    }


    /**
     * Verify status transitions in field history
     */
    @Then("I should see all status transitions in field history:")
    public void verifyStatusTransitionsInFieldHistory(List<Map<String, String>> transitions) {
        logger.info("Verifying status transitions in field history");
        getModulePage().verifyStatusTransitions(transitions);
        logger.info("✓ Status transitions verified in field history");
        screenshot("Field History Transitions");
    }

    @When("I clone a hiring metrics hub record")
    public void cloneHiringMetricsHubRecord() {
        logger.info("Cloning hiring metrics hub record");
        getModulePage().cloneHiringMetricsRecord();
    }

    @Then("I should see the cloned hiring metrics record")
    public void verifyClonedHiringMetricsRecord() {
        logger.info("Verifying cloned hiring metrics record");
        getModulePage().verifyClonedHiringMetricsRecord();
        screenshot("Cloned Record");
    }

    @Then("I should see {string} reports in the list")
    public void verifyReportsInList(String expectedCount) {
        logger.info("Verifying {} reports in the list", expectedCount);
        getModulePage().verifyReportsItemCount(expectedCount);
        screenshot(expectedCount + " Reports in List");
    }

}
