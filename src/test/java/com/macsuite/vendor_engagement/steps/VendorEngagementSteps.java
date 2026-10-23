package com.macsuite.vendor_engagement.steps;

import com.macsuite.common.ModuleSteps;
import com.macsuite.vendor_engagement.pages.VendorEngagementMeetingPage;
import io.cucumber.java.en.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.Map;

/**
 * VE-specific step definitions for Vendor Engagement Meeting scenarios.
 * Common steps (navigation, buttons, success verification) are in CommonSteps.
 */
public class VendorEngagementSteps extends ModuleSteps<VendorEngagementMeetingPage> {
    private static final Logger logger = LoggerFactory.getLogger(VendorEngagementSteps.class);

    @Override
    protected VendorEngagementMeetingPage createModulePage() {
        return new VendorEngagementMeetingPage(page());
    }

    // ==================== VE-SPECIFIC STEPS ====================

    @When("I fill the meeting details with:")
    public void fillMeetingDetails(Map<String, String> meetingData) {
        logger.info("Filling meeting details with {} fields", meetingData.size());
        getModulePage().fillForm(meetingData);
        logger.info("✓ Meeting details filled successfully");
        screenshot("Meeting Form Filled");
    }

    @Then("I should see the meeting details are correct")
    public void verifyMeetingDetailsDisplayed() throws Exception {
        logger.info("Verifying meeting details display");
        boolean detailsVerified = getModulePage().verifyMeetingDetails();
        Assert.assertTrue(detailsVerified, "Meeting details should be displayed correctly");
        logger.info("✓ Meeting details verified successfully");
        screenshot("Meeting Details");
    }

    @When("I open the first vendor engagement record")
    public void openFirstVendorEngagementRecord() {
        getModulePage().openFirstVendorEngagementRecord();
        screenshot("Opened VE Record");
    }

    @When("I edit the meeting with:")
    public void editMeetingWith(Map<String, String> updates) {
        logger.info("Editing vendor engagement meeting with {} field(s)", updates.size());
        getModulePage().editMeetingFields(updates);
    }
}
