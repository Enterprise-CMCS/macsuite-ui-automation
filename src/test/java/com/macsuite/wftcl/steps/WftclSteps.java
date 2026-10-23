package com.macsuite.wftcl.steps;

import com.macsuite.common.ModuleSteps;
import com.macsuite.wftcl.pages.WftclPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step definitions for WFTCL Implementation scenarios.
 * Covers home page header, Provisions creation lifecycle (overview, dates, meetings,
 * document tracking, file upload, notes, chatter, field history), list view management,
 * dashboard navigation, and report navigation.
 */
public class WftclSteps extends ModuleSteps<WftclPage> {
    private static final Logger logger = LoggerFactory.getLogger(WftclSteps.class);

    @Override
    protected WftclPage createModulePage() {
        return new WftclPage(page());
    }

    // ─── TC_002: Provisions lifecycle navigation ──────────────────────────────

    @When("I navigate to WFTCL Provisions")
    public void navigateToProvisions() {
        logger.info("Navigating to WFTCL Provisions");
        getModulePage().navigateToProvisions();
    }

    @When("I click New to create a Provision record")
    public void clickNewProvision() {
        logger.info("Clicking New to create a Provision record");
        getModulePage().clickNewProvision();
    }

    @Then("I should see the WFTCL Provision form")
    public void verifyProvisionFormDisplayed() {
        logger.info("Verifying WFTCL Provision form displayed");
        getModulePage().verifyProvisionFormDisplayed();
        screenshot("WFTCL Provision Form");
    }

    @When("I enter the Provision overview details")
    public void enterProvisionOverviewDetails() {
        logger.info("Entering Provision overview details");
        getModulePage().enterProvisionOverviewDetails();
        screenshot("WFTCL Provision Overview");
    }

    // ─── TC_002: Provision dates ──────────────────────────────────────────────

    @When("I submit the Provision dates section")
    public void submitProvisionDates() {
        logger.info("Submitting Provision dates section");
        getModulePage().submitProvisionDates();
    }

    @Then("I should see the Provision dates saved successfully")
    public void verifyProvisionDatesSaved() {
        logger.info("Verifying Provision dates saved");
        getModulePage().verifyProvisionDatesSaved();
        screenshot("WFTCL Provision Dates Saved");
    }

    // ─── TC_002: Meetings dates ───────────────────────────────────────────────

    @When("I submit the Meetings dates section")
    public void submitMeetingsDates() {
        logger.info("Submitting Meetings dates section");
        getModulePage().submitMeetingsDates();
    }

    @Then("I should see the Meetings dates saved successfully")
    public void verifyMeetingDatesSaved() {
        logger.info("Verifying Meetings dates saved");
        getModulePage().verifyMeetingDatesSaved();
        screenshot("WFTCL Meetings Dates Saved");
    }

    // ─── TC_002: Document tracking ────────────────────────────────────────────

    @When("I submit the Document Tracking section")
    public void submitDocumentTracking() {
        logger.info("Submitting Document Tracking section");
        getModulePage().submitDocumentTracking();
    }

    @Then("I should see the Document Tracking entry saved")
    public void verifyDocumentTrackingSaved() {
        logger.info("Verifying Document Tracking saved");
        getModulePage().verifyDocumentTrackingSaved();
        screenshot("WFTCL Document Tracking Saved");
    }

    // ─── TC_002: File upload ──────────────────────────────────────────────────

    @When("I upload a file on the Provision record")
    public void uploadFileOnProvision() {
        logger.info("Uploading file on Provision record");
        getModulePage().uploadFileOnProvision();
    }

    @Then("I should see the file uploaded successfully")
    public void verifyFileUploaded() {
        logger.info("Verifying file uploaded");
        getModulePage().verifyFileUploaded();
        screenshot("WFTCL File Uploaded");
    }

    // ─── TC_002: Notes ────────────────────────────────────────────────────────

    @When("I add a note on the Provision record")
    public void addNoteOnProvision() {
        logger.info("Adding note on Provision record");
        getModulePage().addNoteOnProvision();
    }

    @Then("I should see the note added successfully")
    public void verifyNoteAdded() {
        logger.info("Verifying note added");
        getModulePage().verifyNoteAdded();
        screenshot("WFTCL Note Added");
    }

    // ─── TC_002: Chatter ─────────────────────────────────────────────────────

    @When("I post a Chatter message on the Provision record")
    public void postChatterMessage() {
        logger.info("Posting Chatter message on Provision record");
        getModulePage().postChatterMessage();
    }

    @Then("I should see the Chatter message posted")
    public void verifyChatterMessagePosted() {
        logger.info("Verifying Chatter message posted");
        getModulePage().verifyChatterMessagePosted();
        screenshot("WFTCL Chatter Posted");
    }

    // ─── TC_002: Field history ────────────────────────────────────────────────

    @When("I open the Field History Tracking on the Provision record")
    public void openFieldHistoryTracking() {
        logger.info("Opening Field History Tracking on Provision record");
        getModulePage().openFieldHistoryTracking();
    }

    @Then("I should see field history entries for the Provision")
    public void verifyFieldHistoryEntries() {
        logger.info("Verifying field history entries for Provision");
        getModulePage().verifyFieldHistoryEntries();
        screenshot("WFTCL Field History");
    }

}
