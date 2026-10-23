package com.macsuite.sdp.pages;

import com.macsuite.common.pages.SfPage;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SdpPage extends SfPage {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_FMT_BAD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private String pdfFileName = "";

    public SdpPage(Page page) {
        super(page);
    }

    private String date(int offsetDays) {
        return LocalDate.now().plusDays(offsetDays).format(DATE_FMT);
    }

    private Locator.WaitForOptions timeout(int ms) {
        return new Locator.WaitForOptions().setTimeout(ms);
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    public void navigateToSdpRecordForm() {
        page.locator("xpath=//a/span[contains(text(),'SDP Records')]/../following-sibling::one-app-nav-bar-item-dropdown").first().click();
        page.locator("xpath=//span[contains(text(),'New SDP Record')]").first().waitFor(timeout(5000));
        page.locator("xpath=//span[contains(text(),'New SDP Record')]").first().click();
        logger.info("Navigated to SDP record creation form");
    }

    // ─── TC_001: Mandatory field validation ──────────────────────────────────

    public void verifyMandatoryFieldErrors() {
        page.getByText("We hit a snag.").first().waitFor(timeout(10000));
        page.getByText("Review the following fields").first().waitFor(timeout(5000));
        // PREDICTED de-hollow (not runtime-verified): legacy asserted each mandatory field by label.
        // As a real (lower-risk) improvement over the summary-banner-only check, require at least one
        // field-level validation error to be highlighted on the form itself.
        page.locator(".slds-form-element__help, [class*='errorMessage'], .slds-has-error")
            .first().waitFor(timeout(8000));
        logger.info("Mandatory field errors verified on blank SDP record form");
    }

    public void verifyInputFieldFunctionality(String fieldName) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMddyyHHmmss"));
        page.getByLabel(fieldName, new Page.GetByLabelOptions().setExact(false)).first()
            .fill("Test_" + timestamp);
        clickSave("verifying input field " + fieldName, false);
        handleErrorSnag();
        page.locator("button[title='Undo " + fieldName + "']").first().click();
        page.getByText("Complete this field").first().waitFor(timeout(5000));
        logger.info("Input field '{}' validation verified", fieldName);
    }

    public void verifyDropdownFieldFunctionality(String fieldName, String option) {
        page.locator("xpath=//label[contains(text(),'" + fieldName + "')]/following-sibling::div//button").first().click();
        page.locator("span[title='" + option + "']").first().waitFor(timeout(5000));
        page.locator("span[title='" + option + "']").first().click();
        clickSave("verifying dropdown field " + fieldName, false);
        handleErrorSnag();
        page.locator("button[title='Undo " + fieldName + "']").first().click();
        page.getByText("Complete this field").first().waitFor(timeout(5000));
        logger.info("Dropdown field '{}' with option '{}' verified", fieldName, option);
    }

    public void verifySearchFieldFunctionality(String fieldName) {
        page.getByLabel(fieldName, new Page.GetByLabelOptions().setExact(false)).first().click();
        page.locator("lightning-base-combobox-item").first().waitFor(timeout(5000));
        page.locator("lightning-base-combobox-item").first().click();
        clickSave("verifying search field " + fieldName, false);
        handleErrorSnag();
        page.locator("button[title='Undo " + fieldName + "']").first().click();
        page.getByText("Complete this field").first().waitFor(timeout(5000));
        logger.info("Search field '{}' validation verified", fieldName);
    }

    public void verifyDateFieldFunctionality(String fieldName, int offsetDays) {
        Locator dateInput = page.getByLabel(fieldName, new Page.GetByLabelOptions().setExact(false)).first();
        dateInput.click();
        dateInput.fill(date(offsetDays));
        clickSave("verifying date field " + fieldName, false);
        page.locator("button[title='Undo " + fieldName + "']").first().click();
        page.getByText("Complete this field").first().waitFor(timeout(5000));
        logger.info("Date field '{}' with offset {} verified", fieldName, offsetDays);
    }

    public void handleErrorSnag() {
        try {
            Locator closeBtn = page.locator("button[title='Close error dialog']").first();
            closeBtn.waitFor(timeout(2000));
            if (closeBtn.isVisible()) {
                closeBtn.click();
            }
        } catch (Exception ignored) {}
        logger.info("Error snag handled");
    }

    public void dragOptionToSelected(String optionName) {
        page.locator("xpath=(//*[text()='" + optionName + "'])[last()]").click();
        page.locator("xpath=(//*[text()='" + optionName + "']//following::button[1])[last()]").click();
        clickSave("dragging option " + optionName, false);
        handleErrorSnag();
        logger.info("Option '{}' dragged to selected", optionName);
    }

    public void verifyDragOptionField(String fieldName) {
        page.locator("button[title='Undo " + fieldName + "']").first().click();
        page.locator("xpath=//*[contains(text(),'An option must be selected')]/../div[contains(text(),'" + fieldName + "')]")
            .first().waitFor(timeout(5000));
        logger.info("Drag option field '{}' validation verified", fieldName);
    }

    public void closeSdpRecordFormWithoutSaving() {
        page.locator("button[title='Cancel and close']").first().click();
        logger.info("SDP record form closed without saving");
    }

    public void verifySdpRecordFormClosed() {
        // PREDICTED de-hollow (not runtime-verified): the Save-hidden wait was wrapped in a swallow-all
        // catch (false-green if the form stayed open). Make it mandatory so the step fails if the SDP
        // record form did not actually close.
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true))
            .first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN).setTimeout(8000));
        logger.info("SDP record form closed");
    }

    // ─── TC_002–TC_004: Fill SDP record form ─────────────────────────────────

    public void fillSdpRecordDetails(String status, boolean requireParentRecord) {
        String sdpStatus = (status == null || status.isEmpty()) ? "Submitted but not determined complete" : status;

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMddyyHHmmss"));
        page.getByLabel("Control Name", new Page.GetByLabelOptions().setExact(false)).first()
            .fill("CT-" + timestamp);

        // State
        page.locator("xpath=//label[contains(text(),'State')]/following-sibling::div//button").first().click();
        page.locator("span[title='AL']").first().waitFor(timeout(5000));
        page.locator("span[title='AL']").first().click();

        // Review Type
        page.locator("xpath=//label[contains(text(),'Review Type')]/following-sibling::div//button").first().click();
        page.locator("span[title='New']").first().waitFor(timeout(5000));
        page.locator("span[title='New']").first().click();

        // State Submitted to CMS
        page.getByLabel("State Submitted to CMS", new Page.GetByLabelOptions().setExact(false)).first().fill(date(0));

        // DMCP Lead - click to show options, pick first
        page.getByLabel("DMCP Lead", new Page.GetByLabelOptions().setExact(false)).first().click();
        page.locator("lightning-base-combobox-item").first().waitFor(timeout(5000));
        page.locator("lightning-base-combobox-item").first().click();

        // Parent SDP Record (optional)
        if (requireParentRecord) {
            page.locator("xpath=//label[contains(text(),'Parent SDP Record')]/following-sibling::div//input").first().fill("SDP");
            page.locator("xpath=//label[contains(text(),'Parent SDP Record')]/following-sibling::div//input/../../../following-sibling::div//ul//lightning-base-combobox-item")
                .first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
            page.locator("xpath=//label[contains(text(),'Parent SDP Record')]/following-sibling::div//input/../../../following-sibling::div//ul//lightning-base-combobox-item")
                .first().click();
        }

        // SDP Status
        page.locator("xpath=//label[contains(text(),'SDP Status')]/following-sibling::div//button").first().click();
        page.locator("xpath=//label[text()='SDP Status']/..//span[@title='" + sdpStatus + "']").first()
            .waitFor(timeout(5000));
        page.locator("xpath=//label[text()='SDP Status']/..//span[@title='" + sdpStatus + "']").first().click();

        // Date Determined Complete — only when Under Review
        if ("Under Review".equals(status)) {
            page.getByLabel("Date Determined Complete", new Page.GetByLabelOptions().setExact(false)).first().fill(date(3));
        }

        // Rating period dates
        page.getByLabel("Rating Period Begin Date", new Page.GetByLabelOptions().setExact(false)).first().fill(date(-10));
        page.getByLabel("Rating Period End Date", new Page.GetByLabelOptions().setExact(false)).first().fill(date(10));
        page.getByLabel("Payment Arrangement Start Date", new Page.GetByLabelOptions().setExact(false)).first().fill(date(5));

        // Period
        page.getByLabel("Period", new Page.GetByLabelOptions().setExact(false)).first().fill("3");

        // Separate Payment Term shuttle
        page.locator("xpath=(//*[text()='Separate Payment Term'])[last()]").click();
        page.locator("xpath=(//*[text()='Separate Payment Term']//following::button[1])[last()]").click();

        // Inpatient Hospital Service shuttle
        page.locator("xpath=(//*[text()='Inpatient Hospital Service'])[last()]").click();
        page.locator("xpath=(//*[text()='Inpatient Hospital Service']//following::button[1])[last()]").click();

        // State Medicaid Director
        page.getByLabel("State Medicaid Director", new Page.GetByLabelOptions().setExact(false)).first().click();
        page.locator("lightning-base-combobox-item").first().waitFor(timeout(5000));
        page.locator("lightning-base-combobox-item").first().click();

        logger.info("SDP record details filled — status='{}', requireParentRecord={}", sdpStatus, requireParentRecord);
    }

    // ─── TC_002: Payment date boundary value validation ───────────────────────

    public void validatePaymentArrangementStartDateBoundary() {
        // Set valid rating period
        page.getByLabel("Rating Period Begin Date", new Page.GetByLabelOptions().setExact(false)).first().fill(date(0));
        page.getByLabel("Rating Period End Date", new Page.GetByLabelOptions().setExact(false)).first().fill(date(10));

        Locator paymentField = page.getByLabel("Payment Arrangement Start Date",
            new Page.GetByLabelOptions().setExact(false)).first();

        // Date after end date — triggers boundary error
        paymentField.fill(date(11));
        clickSave("testing payment date beyond end", false);
        clickSave("testing payment date beyond end (retry)", false);
        page.getByText("Payment Arrangement Start Date must occur on or between the rating period start or end dates.")
            .first().waitFor(timeout(10000));

        // Date before begin date — same boundary error shown inline
        paymentField.fill(date(-11));
        page.getByText("Payment Arrangement Start Date must occur on or between the rating period start or end dates.")
            .first().waitFor(timeout(5000));

        // Wrong date format — triggers format error
        paymentField.fill(LocalDate.now().plusDays(11).format(DATE_FMT_BAD));
        clickSave("testing payment date wrong format", false);
        page.getByText("Your entry does not match the allowed format")
            .first().waitFor(timeout(10000));

        logger.info("Payment Arrangement Start Date boundary values validated");
    }

    // ─── TC_003: Key Contacts + Provider Type ────────────────────────────────

    public void selectKeyContactsForSdpRecord() {
        page.locator("button[title='Edit State Medicaid Director']").first().click();
        try {
            Locator clearBtn = page.locator("button[title='Clear Selection']").first();
            clearBtn.waitFor(timeout(2000));
            clearBtn.click();
        } catch (Exception ignored) {}

        page.locator("xpath=//label[text()='State Medicaid Director']/..//input").first().fill("sa");
        page.locator("span[title*='Show more results for']").last().waitFor(timeout(5000));
        page.locator("span[title*='Show more results for']").last().click();

        page.locator("xpath=(//label[contains(@id,'radio-button-label')]/span)[last()-1]")
            .waitFor(timeout(5000));
        page.locator("xpath=(//label[contains(@id,'radio-button-label')]/span)[last()-1]").click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Select").setExact(true)).first().click();
        try {
            page.getByRole(AriaRole.DIALOG).last()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignored) {}

        Locator saInput = page.locator("xpath=//label[text()='Signing Authority (Contact)']/..//input").first();
        saInput.click();
        saInput.fill("la");
        page.locator("span[title*='Show more results for']").last().waitFor(timeout(5000));
        page.locator("span[title*='Show more results for']").last().click();
        page.locator("xpath=(//label[contains(@id,'radio-button-label')]/span)[last()-1]")
            .waitFor(new Locator.WaitForOptions().setTimeout(5000));
        page.locator("xpath=(//label[contains(@id,'radio-button-label')]/span)[last()-1]").click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Select").setExact(true)).first().click();

        logger.info("Key contacts selected for SDP record");
    }

    public void selectProviderTypeForSdpRecord() {
        page.locator("*[title='Outpatient Hospital Service']").first().click();
        page.locator("xpath=//div[contains(text(),'Provider Type')]/..//button[@title='Move to Chosen']").first().click();
        page.locator("*[title='Professional Services at an Academic Med']").first().click();
        page.locator("xpath=//div[contains(text(),'Provider Type')]/..//button[@title='Move to Chosen']").first().click();
        clickSave("selecting provider type", true);
        logger.info("Provider type selected for SDP record");
    }

    public void verifySdpRecordUpdatedWithKeyContactsAndProviderType() {
        page.locator("button[title='Edit Provider Type']").first().waitFor(timeout(10000));
        logger.info("SDP record updated with Key Contacts and Provider Type");
    }

    // ─── TC_005: Control Name duplicate + Naming Convention ──────────────────

    public void verifyControlNameDuplicateError() {
        page.locator("xpath=(//*[text()='Naming Convention']//following::lightning-formatted-text[1])[last()]")
            .waitFor(new Locator.WaitForOptions().setTimeout(10000));
        String suggestedName = page.locator(
            "xpath=(//*[text()='Naming Convention']//following::lightning-formatted-text[1])[last()]")
            .textContent().trim();

        page.locator("xpath=(//*[text()='Edit Control Name']/parent::button)[last()]").click();
        page.locator("xpath=//*[text()='Control Name']/following-sibling::div/input").first().fill(suggestedName);
        clickSave("verifying control name duplicate", true);

        Locator errorItem = page.locator(
            "xpath=//*[contains(text(),'Review the errors on this page.')]/../following-sibling::ul/li").first();
        errorItem.waitFor(timeout(10000));
        String errorText = errorItem.textContent();
        if (!errorText.contains("duplicate value found: Control_Name__c")) {
            throw new AssertionError("Duplicate control name error not shown. Got: " + errorText);
        }

        page.locator("button[title='Close error dialog']").first().click();
        page.locator("button[name='CancelEdit']").first().click();
        logger.info("Control Name duplicate error verified");
    }

    public void verifyNamingConventionAfterRatingPeriodEndDateChange() {
        page.locator("xpath=(//button[@title='Edit Rating Period End Date'])[last()]").click();
        page.locator("xpath=(//input[contains(@name,'Rating_Period_End_Date')])[last()]")
            .waitFor(new Locator.WaitForOptions().setTimeout(5000));
        page.locator("xpath=(//input[contains(@name,'Rating_Period_End_Date')])[last()]").fill(date(31));
        clickSave("updating rating period end date", false);
        clickSave("updating rating period end date (retry)", false);
        applyNamingConventionSuggestion();
    }

    private void applyNamingConventionSuggestion() {
        page.locator("xpath=(//*[text()='Naming Convention']//following::lightning-formatted-text[1])[last()]")
            .waitFor(new Locator.WaitForOptions().setTimeout(10000));
        String suggestedName = page.locator(
            "xpath=(//*[text()='Naming Convention']//following::lightning-formatted-text[1])[last()]")
            .textContent().trim();
        page.locator("xpath=(//*[text()='Edit Control Name']/parent::button)[last()]").click();
        page.locator("xpath=//*[text()='Control Name']/following-sibling::div/input").first().fill(suggestedName);
        clickSave("applying naming convention", false);
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true))
                .first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignored) {}

        String changedName = page.locator(
            "xpath=(//*[text()='Control Name']/parent::div/parent::dt/following-sibling::dd//lightning-formatted-text)[last()]")
            .textContent().trim();
        if (!changedName.equals(suggestedName)) {
            throw new AssertionError("Naming convention mismatch. Expected: '" + suggestedName + "' got: '" + changedName + "'");
        }
        logger.info("Naming convention applied: {}", suggestedName);
    }

    // ─── TC_006: Review Type transitions + CMS Clock + Most Recent Action ────

    public void changeReviewType(String reviewType) {
        page.locator("xpath=(//button[@title='Edit Review Type'])[last()]").click();
        page.locator("button[aria-label='Review Type']").first().click();
        page.locator("lightning-base-combobox-item[data-value='" + reviewType + "']").first().click();
        logger.info("Review Type changed to: {}", reviewType);
    }

    public void verifyParentRecordAlertForAmendment() {
        verifyParentRecordRequiredError();
    }

    public void verifyParentRecordErrorForRenewal() {
        verifyParentRecordRequiredError();
    }

    private void verifyParentRecordRequiredError() {
        Locator errorLocator = page.locator(
            "xpath=//div[contains(@id,'help-message')]/span[text()='Parent SDP Record']/..").first();
        errorLocator.waitFor(timeout(10000));
        String errorHtml = errorLocator.getAttribute("innerHTML");
        if (errorHtml == null || !errorHtml.contains(
                "If Review Type = \"Renewal\" or \"Amendment\", then Parent SDP Record is required")) {
            throw new AssertionError("Parent record required error not found. innerHTML: " + errorHtml);
        }
        page.locator("button[name='CancelEdit']").first().click();
        logger.info("Parent record required error verified");
    }

    public void linkParentRecordAndVerifyChildRecordUpdates() {
        page.locator("xpath=(//button[@title='Edit Review Type'])[last()]").click();
        page.locator("button[aria-label='Review Type']").first().click();
        page.locator("lightning-base-combobox-item[data-value='Renewal']").first().click();

        Locator parentInput = page.locator("xpath=//label[text()='Parent SDP Record']/..//input").first();
        parentInput.fill("SDP");
        page.locator("xpath=//label[text()='Parent SDP Record']/..//input//following::lightning-base-combobox-item[last()-1]")
            .first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
        parentInput.click();
        page.locator("xpath=//label[text()='Parent SDP Record']/..//input//following::lightning-base-combobox-item[last()-1]")
            .first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
        page.locator("xpath=//label[text()='Parent SDP Record']/..//input//following::lightning-base-combobox-item[last()-1]")
            .first().click();

        clickSave("linking parent record", false);
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true))
                .first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignored) {}

        String reviewType = page.locator(
            "xpath=(//*[text()='Review Type']/parent::div/parent::dt/following-sibling::dd//lightning-formatted-text)[last()]")
            .textContent().trim();
        if (!reviewType.contains("Renewal")) {
            throw new AssertionError("Review Type not updated to Renewal. Got: " + reviewType);
        }
        logger.info("Parent record linked, child record shows Review Type: {}", reviewType);
    }

    public void verifyCmsClockFieldDisplayed() {
        page.locator("xpath=(//button[@title='Edit Date Determined Complete'])[last()]").click();
        page.locator("xpath=(//input[contains(@name,'Date_Determined_Complete')])[last()]").fill(date(2));

        page.locator("xpath=(//button[@aria-label='SDP Status'])[last()]").click();
        page.locator("lightning-base-combobox-item[data-value='Under Review']").first().click();

        clickSave("setting CMS clock", false);

        Locator cmsClockLocator = page.locator(
            "xpath=(//*[text()='CMS Clock']/../../following-sibling::dd//lightning-formatted-number)[last()]");
        cmsClockLocator.waitFor(timeout(10000));
        String cmsClockValue = cmsClockLocator.textContent().trim();
        if (!cmsClockValue.contains("-2")) {
            throw new AssertionError("CMS Clock expected to contain '-2', got: " + cmsClockValue);
        }
        logger.info("CMS Clock field displayed: {}", cmsClockValue);
    }

    public void verifyMostRecentActionActivity() {
        page.locator("xpath=(//button[@title='Edit CMS Finalized'])[last()]").click();
        page.locator("xpath=(//input[contains(@name,'Date_Determined_Complete')])[last()]").fill(date(0));
        page.locator("xpath=(//label[text()='CMS Finalized']/../div/input)[last()]").fill(date(2));

        page.locator("xpath=(//button[@aria-label='SDP Status'])[last()]").click();
        page.locator("lightning-base-combobox-item[data-value='Approved']").first().click();

        clickSave("verifying most recent action", false);

        page.locator("xpath=(//*[text()='Most Recent Action']/../../following-sibling::dd//lightning-formatted-text)[last()]")
            .waitFor(timeout(10000));
        logger.info("Most Recent Action activity verified");
    }

    // ─── TC_007: PDF generation ───────────────────────────────────────────────

    public void generatePdf() {
        page.locator("xpath=(//button[contains(text(),'Generate PDF')])[last()]").click();

        page.locator("xpath=(//span[text()='Select Item 1']/parent::label)[last()]").waitFor(timeout(5000));
        page.locator("xpath=(//span[text()='Select Item 1']/parent::label)[last()]").click();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Select").setExact(true)).first().click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Finish").setExact(true)).first().click();

        Locator fileNameLocator = page.locator(
            "xpath=//div[contains(@data-divid,'AttachedContent')]/following-sibling::div/span").first();
        fileNameLocator.waitFor(timeout(10000));
        pdfFileName = fileNameLocator.textContent().trim();

        page.locator("xpath=//div/div[2]/*[contains(text(),'State Directed Payments')]").first().click();
        logger.info("PDF generated: {}", pdfFileName);
    }

    public void verifyPdfCreated() {
        Locator pdfImage = page.locator("xpath=//img[contains(@alt,'State Directed Payments')]").first();
        pdfImage.waitFor(timeout(15000));
        String altText = pdfImage.getAttribute("alt");
        if (altText == null || !altText.contains(pdfFileName)) {
            throw new AssertionError("PDF file name mismatch. Expected '" + pdfFileName + "' in alt='" + altText + "'");
        }
        logger.info("PDF created successfully: {}", pdfFileName);
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    public void navigateToDashboardPage() {
        page.locator("xpath=//a/span[text()='Dashboards']").first().click();
        page.locator("xpath=//a[text()='All Dashboards']").first().waitFor(timeout(5000));
        page.locator("xpath=//a[text()='All Dashboards']").first().click();
        page.locator("xpath=//a[text()='SDP Dashboard']").first().waitFor(timeout(5000));
        page.locator("xpath=//a[text()='SDP Dashboard']").first().click();
        logger.info("Navigated to SDP dashboard page");
    }

    public void verifyAllSdpDashboardsDisplayed() {
        FrameLocator frame = page.locator("iframe[title='dashboard']").last().contentFrame();

        try {
            frame.locator("button").filter(new Locator.FilterOptions().setHasText("Refresh")).last().click();
        } catch (Exception ignored) {}

        frame.getByText("SDP Review Status by Submission Date").first().waitFor(timeout(15000));
        frame.getByText("CMS Clock for Outstanding SDPs").first().waitFor(timeout(5000));
        frame.getByText("# SDPs with CMS Clock>90 Days/Year").first().waitFor(timeout(5000));
        frame.getByText("SDPs by State").first().waitFor(timeout(5000));
        logger.info("All SDP dashboards displayed");
    }

    public void verifyReportCountWidget(String widgetName) {
        FrameLocator frame = page.locator("iframe[title='dashboard']").last().contentFrame();
        frame.getByText(widgetName).first().waitFor(timeout(10000));
        logger.info("Report count widget displayed: {}", widgetName);
    }

    // ─── Rating Period ────────────────────────────────────────────────────────

    public void navigateToSdpRatingPeriodScreen() {
        page.locator("xpath=//a/span[text()='Dashboards']").first().click();
        page.locator("xpath=//a[text()='All Dashboards']").first().waitFor(timeout(5000));
        page.locator("xpath=//a[text()='All Dashboards']").first().click();
        page.locator("xpath=//a[text()='SDP Rating Period']").first().waitFor(timeout(5000));
        page.locator("xpath=//a[text()='SDP Rating Period']").first().click();
        logger.info("Navigated to SDP Rating Period screen");
    }

    public void switchToSdpRatingPeriodIframe() {
        FrameLocator frame = page.locator("iframe[title='dashboard']").last().contentFrame();
        frame.locator("xpath=//label[text()='SDP Status']/..//button").waitFor(timeout(15000));
        logger.info("SDP Rating Period iframe loaded");
    }

    public void verifyStatusCountDisplayed(String status) {
        FrameLocator frame = page.locator("iframe[title='dashboard']").last().contentFrame();
        String ariaLabel = singleStatusAriaLabel(status);

        frame.locator("xpath=//label[text()='SDP Status']/..//button").first().click();
        frame.locator("xpath=//span[@aria-label='" + ariaLabel + "']").first().waitFor(timeout(5000));
        frame.locator("xpath=//span[@aria-label='" + ariaLabel + "']").first().click();
        // Wait for the data grid to refresh after applying the status filter
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout(8000));
        } catch (Exception ignored) {}

        String expectedText = statusTableText(status);
        List<Locator> rows = frame.locator(
            "xpath=//tr[contains(@class,'data-grid-table-row')]/td[4]/div/div").all();
        for (Locator row : rows) {
            String rowText = row.textContent().trim();
            if (!rowText.isEmpty() && !rowText.equals(expectedText)) {
                throw new AssertionError("Row status mismatch. Expected '" + expectedText + "' but found '" + rowText + "'");
            }
        }
        logger.info("Status count verified for: {}", status);
    }

    public void verifyCombinedStatusCountDisplayed(String combinedStatus) {
        FrameLocator frame = page.locator("iframe[title='dashboard']").last().contentFrame();
        String ariaLabel = combinedStatusAriaLabel(combinedStatus);

        frame.locator("xpath=//label[text()='SDP Status']/..//button").first().click();
        frame.locator("xpath=//span[@aria-label='" + ariaLabel + "']").first().waitFor(timeout(5000));
        frame.locator("xpath=//span[@aria-label='" + ariaLabel + "']").first().click();
        // Wait for the data grid to refresh after applying the status filter
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout(8000));
        } catch (Exception ignored) {}

        List<String> allowed = allowedStatuses(combinedStatus);
        List<Locator> rows = frame.locator(
            "xpath=//tr[contains(@class,'data-grid-table-row')]/td[4]/div/div").all();
        for (Locator row : rows) {
            String rowText = row.textContent().trim();
            if (!rowText.isEmpty() && !allowed.contains(rowText)) {
                throw new AssertionError("Row status '" + rowText + "' not in expected statuses: " + allowed);
            }
        }
        logger.info("Combined status count verified for: {}", combinedStatus);
    }

    private String singleStatusAriaLabel(String status) {
        switch (status) {
            case "Approved":     return "equals Approved";
            case "Under Review": return "equals Under Review";
            case "Submitted":    return "equals Submitted but not determined complete";
            case "Withdrawn":    return "equals Withdrawn";
            default:             return "equals " + status;
        }
    }

    private String statusTableText(String status) {
        return "Submitted".equals(status) ? "Submitted but not determined complete" : status;
    }

    private String combinedStatusAriaLabel(String combinedStatus) {
        switch (combinedStatus) {
            case "Under Review + Submitted":
                return "equals Under Review, Submitted but not determined complete";
            case "Approved = Withdrawn":
                return "equals Approved, Withdrawn";
            default:
                return "equals " + combinedStatus;
        }
    }

    private List<String> allowedStatuses(String combinedStatus) {
        List<String> list = new ArrayList<>();
        switch (combinedStatus) {
            case "Under Review + Submitted":
                list.add("Under Review");
                list.add("Submitted but not determined complete");
                break;
            case "Approved = Withdrawn":
                list.add("Approved");
                list.add("Withdrawn");
                break;
        }
        return list;
    }
}
