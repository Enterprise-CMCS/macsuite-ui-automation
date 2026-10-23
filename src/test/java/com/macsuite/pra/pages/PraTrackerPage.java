package com.macsuite.pra.pages;

import com.macsuite.common.pages.SfPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class PraTrackerPage extends SfPage {
    private String selectedRecordType = "Standard";

    public PraTrackerPage(Page page) {
        super(page);
    }

    public void verifyIcrPackageFormDisplayed() {
        Locator heading = waitForCreateFormLoaded(selectedRecordType, 12000);
        String text = heading.textContent().trim();
        String expectedHeader = "New CMCS ICR Package: " + selectedRecordType;
        if (!text.equalsIgnoreCase(expectedHeader)) {
            throw new AssertionError("Unexpected PRA form header: " + text);
        }
        logger.info("PRA ICR package form displayed");
    }

    public void fillIcrPackageDetails(Map<String, String> packageData) {
        clearFilledData();
        Map<String, String> data = new LinkedHashMap<>(packageData);
        populateStandaloneIdentifiersIfNeeded(data);
        fillForm(data);
        logger.info("PRA ICR package details filled");
    }

    public void selectRecordType(String recordType) {
        selectRecordTypeIfPrompted(recordType);
    }

    public void takeRequestAction(String requestType) {
        page.locator("//button[text()='Request Action']").click();
        page.waitForTimeout(5000);
        Locator umbrella = page.locator("//select[@name='Umbrella']");
        umbrella.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
        umbrella.selectOption(new SelectOption().setLabel(requestType));
        page.locator("//button[text()='Next']").click();
        page.waitForTimeout(2000);
        if (requestType.equalsIgnoreCase("Extension (WITHOUT change)")) {
            page.locator("//input[@name='Requested_OMB_Approval_Date']").fill(fetchWeekdayDate("MMM d, yyyy", 0));
        }
        page.locator("//label[text()='Note']/following::textarea").fill("Test");
        page.locator("//button[text()='Finish']").click();
        page.waitForTimeout(2000);
    }

    public void requestAction(String requestType) {
        String requestOption = normalizeRequestType(requestType);

        // Click Request Action button
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName("Request Action")).first().click();
        logger.info("Clicked Request Action button");

        // Find and select from the Request dropdown
        Locator requestDropdown = page.locator("flowruntime-picklist-input-lwc")
                .filter(new Locator.FilterOptions().setHasText("Request"))
                .locator("select").first();
        requestDropdown.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        requestDropdown.selectOption(new SelectOption().setLabel(requestOption));
        logger.info("Selected: {}", requestOption);

        // Click Next
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName("Next").setExact(true)).first().click();

        if ("Extension (WITHOUT change)".equalsIgnoreCase(requestOption)) {
            formUtils.fillField("Requested OMB Approval Date", fetchWeekdayDate("MMM d, yyyy", 0));
            formUtils.fillField("Note", "Requesting extension without change");
        }

        if ("Revision (WITHOUT extension)".equalsIgnoreCase(requestOption)) {
            try {
                if (page.locator("flowruntime-picklist-input-lwc")
                        .filter(new Locator.FilterOptions().setHasText("Request")).locator("select").first()
                        .isVisible()) {
                    formUtils.fillField("Note", "Requesting Revision (WITHOUT extension)");
                }
            } catch (Exception ignored) {
            }
        }

        // Click Finish if present
        try {
            Locator finishBtn = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Finish").setExact(true)).first();
            finishBtn.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            finishBtn.click();
            logger.info("Clicked Finish");
        } catch (Exception ignored) {
        }

        logger.info("PRA action requested: {}", requestOption);
    }

    public void verifyPackageClosedAndOpenChildIcr() {
        verifyStatusIsClosed();
        Locator childLink = waitForChildIcrLink();

        String href = childLink.getAttribute("href");
        String currentUrl = page.url();

        childLink.scrollIntoViewIfNeeded();
        childLink.click();

        // Wait for navigation by checking URL changed
        page.waitForURL(url -> !url.equals(currentUrl), new Page.WaitForURLOptions().setTimeout(10000));

        // Extract record ID from href and verify it's in the new URL
        if (href != null && !href.isBlank()) {
            // Extract the record ID (e.g., a0XSL00000DGPJA2A5) from href
            String recordId = href.replaceAll(".*/lightning/r/([a-zA-Z0-9]+).*", "$1");
            if (!page.url().contains(recordId)) {
                throw new AssertionError("Child ICR package URL mismatch. Expected record ID: " + recordId
                        + ", Actual URL: " + page.url());
            }
        }
        logger.info("PRA package closed and child ICR opened");
    }

    public void verifyGeneratedIdentifiersOnChildPackageIfRequired() {
        String packageType = getFilledData().get("Type");
        if (packageType == null || !packageType.equalsIgnoreCase("Standalone")) {
            logger.info("Skipping PRA identifier verification - not a Standalone package");
            return;
        }

        String expectedCmsId = getFilledData().get("CMS ID #");
        String expectedOmbNumber = getFilledData().get("OMB Number");
        if (expectedCmsId == null || expectedOmbNumber == null) {
            throw new AssertionError("Standalone PRA identifiers were not captured during package creation");
        }

        logger.info("Expected CMS ID: {}, Expected OMB Number: {}", expectedCmsId, expectedOmbNumber);

        // Wait for child package page to fully load by checking for the header
        try {
            page.locator("lightning-formatted-text[slot='primaryField']").first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (Exception e) {
            logger.warn("Page header not found, continuing anyway");
        }

        // Try to find the actual values with reduced timeouts
        String actualCmsId = getActualFieldValue("CMS ID #");
        String actualOmbNumber = getActualFieldValue("OMB Number");

        logger.info("Actual CMS ID: {}, Actual OMB Number: {}", actualCmsId, actualOmbNumber);

        boolean cmsOk = expectedCmsId.equals(actualCmsId);
        boolean ombOk = expectedOmbNumber.equals(actualOmbNumber);

        if (!cmsOk || !ombOk) {
            StringBuilder error = new StringBuilder("Generated PRA identifiers did not match child package.\n");
            error.append("Expected CMS ID: ").append(expectedCmsId).append(", Actual: ").append(actualCmsId)
                    .append("\n");
            error.append("Expected OMB Number: ").append(expectedOmbNumber).append(", Actual: ")
                    .append(actualOmbNumber);

            throw new AssertionError(error.toString());
        }
        logger.info("Standalone PRA identifiers verified");
    }

    private String getActualFieldValue(String fieldLabel) {
        try {
            // Find the parent container that has both the label and value
            Locator fieldContainer = page.locator("div.slds-form-element")
                    .filter(new Locator.FilterOptions().setHasText(fieldLabel)).first();

            // Wait for it to be in DOM - reduced timeout
            fieldContainer.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(3000));

            // Find the lightning-formatted-text value within this container
            Locator valueElement = fieldContainer
                    .locator("lightning-formatted-text[data-output-element-id='output-field']").first();

            // Just get the text content directly - no scrolling needed since element is in
            // DOM
            String value = valueElement.textContent().trim();
            logger.info("Found {} = {}", fieldLabel, value);
            return value;
        } catch (Exception e) {
            logger.warn("Could not find field value for '{}': {}", fieldLabel, e.getMessage());
            return "NOT FOUND";
        }
    }

    public void updateFieldsAndVerifyStage(Map<String, String> fields, String expectedStage) {
        page.waitForTimeout(5000);
        clickEditIfVisible();
        page.waitForTimeout(5000);
        page.getByLabel(fields.keySet().iterator().next(), new Page.GetByLabelOptions().setExact(false))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        fillForm(fields);
        clickSave("updating PRA package", false);
        verifyPraStageAfterSave(expectedStage);
        logger.info("PRA stage verified: {}", expectedStage);
    }

    private void verifyPraStageAfterSave(String expectedStage) {
        String stageSelector = String.format(
                "li.runtime_sales_pathassistantPathAssistantTab[data-name='%s']", expectedStage);
        Locator stageLocator = page.locator(stageSelector).first();
        Locator activeStageLocator = page.locator(
                stageSelector.replace("li.runtime_sales_pathassistantPathAssistantTab",
                        "li.runtime_sales_pathassistantPathAssistantTab.slds-is-active"))
                .first();

        try {
            activeStageLocator.waitFor(new Locator.WaitForOptions().setTimeout(20000));
        } catch (Exception e) {
            String classes = stageLocator.count() > 0 ? stageLocator.getAttribute("class") : null;
            throw new AssertionError(
                    "Stage '" + expectedStage + "' did not become active. Current classes: " + classes, e);
        }

        logger.info("Stage verified as active: {}", expectedStage);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void selectRecordTypeIfPrompted(String recordType) {
        String targetType = (recordType == null || recordType.isBlank()) ? "Standard" : recordType.trim();
        selectedRecordType = targetType;

        Locator modal = page.locator(".isModal.inlinePanel, .forceChangeRecordType").first();
        try {
            modal.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        } catch (Exception ignored) {
            return;
        }

        // Find and click the radio input directly based on the label text
        Locator radioInput = modal.locator("label.slds-radio")
                .filter(new Locator.FilterOptions().setHasText(targetType))
                .locator("input[type='radio']")
                .first();
        radioInput.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        radioInput.click(new Locator.ClickOptions().setForce(true));

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next").setExact(true))
                .first().click();
        waitForCreateFormLoaded(targetType, 12000);
        logger.info("Record type selected: {}", targetType);
    }

    private Locator waitForCreateFormLoaded(String recordType, int timeoutMs) {
        Locator heading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("New CMCS ICR Package: " + recordType).setExact(true))
                .first();
        heading.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
        return heading;
    }

    private void verifyStatusIsClosed() {
        page.waitForTimeout(3000); // Small pause to ensure the page is updated before checking the header
        // Check the header element which displays "(Closed)" prefix when status is
        // closed
        Locator headerText = page.locator("lightning-formatted-text[slot='primaryField']").first();
        headerText.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        String headerContent = headerText.textContent();
        if (headerContent != null && headerContent.contains("(Closed)")) {
            logger.info("PRA status verified as Closed from header: {}", headerContent.trim());
            return;
        }

        throw new AssertionError("PRA package is not closed. Header text: " + headerContent);
    }

    private Locator waitForChildIcrLink() {
        // Wait for the Child ICR Packages article to appear and contain a child record
        Locator article = page.locator("article[aria-label='Child ICR Packages']").first();
        article.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        // Find the link inside the article - the actual record link in the tile
        Locator childLink = article.locator("a[href*='/lightning/r/'][class*='truncate']").first();
        childLink.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        return childLink;
    }

    private void populateStandaloneIdentifiersIfNeeded(Map<String, String> data) {
        if (!"Standalone".equalsIgnoreCase(data.get("Type")))
            return;
        data.putIfAbsent("CMS ID #", generateDigits(5));
        data.putIfAbsent("OMB Number", generateDigits(4) + "-" + generateDigits(4));
    }

    private String generateDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(ThreadLocalRandom.current().nextInt(10));
        return sb.toString();
    }

    private String normalizeRequestType(String requestType) {
        if (requestType == null || requestType.isBlank())
            throw new AssertionError("PRA request type must be provided");
        return "Revision".equalsIgnoreCase(requestType) ? "Revision (WITHOUT extension)" : requestType.trim();
    }

    // ─── List view / dashboard / reports ─────────────────────────────────────
}
