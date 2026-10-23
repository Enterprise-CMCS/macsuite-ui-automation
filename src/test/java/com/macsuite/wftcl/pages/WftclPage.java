package com.macsuite.wftcl.pages;

import com.macsuite.common.DropdownUtils;
import com.macsuite.common.pages.SfPage;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class WftclPage extends SfPage {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final String NOTE_TEXT = "test";
    private static final String CHATTER_MSG = "Question Answered";

    private final DropdownUtils dropdownUtils;

    public WftclPage(Page page) {
        super(page);
        this.dropdownUtils = new DropdownUtils(page);
    }

    private String todayDate() {
        return LocalDate.now().format(DATE_FMT);
    }

    private String randomString(int len) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, Math.min(len, 32));
    }

    /**
     * Selects the first available option (after "--None--") from a Lightning combobox dropdown.
     * Uses the proven DropdownUtils approach for maximum reliability.
     */
    private void selectFirstAvailableOption(String fieldLabel) {
        logger.debug("Selecting first available option for field: {}", fieldLabel);
        
        // Find the combobox
        Locator combobox = page.getByRole(AriaRole.COMBOBOX,
            new Page.GetByRoleOptions().setName(fieldLabel).setExact(true)).first();
        
        if (combobox.count() == 0) {
            combobox = page.getByLabel(fieldLabel, new Page.GetByLabelOptions().setExact(true)).first();
        }
        
        combobox.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        
        // Check if already has a value
        String currentValue = combobox.getAttribute("data-value");
        if (currentValue != null && !currentValue.equals("--None--") && !currentValue.isBlank()) {
            logger.debug("Dropdown '{}' already has value '{}'; skipping", fieldLabel, currentValue);
            return;
        }
        
        // Click to open dropdown
        combobox.scrollIntoViewIfNeeded();
        combobox.click();
        
        // Wait for dropdown to fully render
        page.waitForTimeout(500);
        
        // Get the listbox
        String controlsId = combobox.getAttribute("aria-controls");
        Locator listbox = null;
        
        if (controlsId != null && !controlsId.isBlank()) {
            listbox = page.locator("#" + controlsId).first();
        }
        
        // If listbox not found or not visible, try ArrowDown to open it
        if (listbox == null || listbox.count() == 0 || !listbox.isVisible()) {
            logger.debug("Listbox not visible, pressing ArrowDown for field: {}", fieldLabel);
            combobox.press("ArrowDown");
            page.waitForTimeout(500);
            
            if (listbox == null || listbox.count() == 0) {
                // Fallback: find any visible listbox
                listbox = page.locator("[role='listbox']:visible").first();
            }
        }
        
        // Wait for listbox to be visible
        if (listbox != null && listbox.count() > 0) {
            try {
                listbox.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5000));
            } catch (Exception e) {
                logger.error("Listbox still not visible after retries for field: {}", fieldLabel);
                throw e;
            }
        } else {
            throw new AssertionError("Cannot find listbox for field: " + fieldLabel);
        }
        
        // Select the first option that is not "--None--"
        Locator firstOption = listbox.locator("[role='option']")
            .filter(new Locator.FilterOptions().setHasNotText("--None--"))
            .first();
        
        firstOption.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(3000));
        firstOption.scrollIntoViewIfNeeded();
        firstOption.click();
        
        logger.debug("Selected first available option for field: {}", fieldLabel);
    }

    private void enterValue(String label, String value) {
        page.getByLabel(label, new Page.GetByLabelOptions().setExact(true)).first().fill(value);
    }

    private void enterTextarea(String label, String value) {
        page.getByLabel(label, new Page.GetByLabelOptions().setExact(true)).first().fill(value);
    }

    // ─── TC_002: Provisions lifecycle ─────────────────────────────────────────

    public void navigateToProvisions() {
        page.getByTitle("Provisions").first().click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        logger.info("Navigated to Provisions tab");
    }

    public void clickNewProvision() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New").setExact(true))
                .first().click();
        } catch (Exception e) {
            page.getByTitle("New").first().click();
        }
        logger.info("Clicked New Provision");
    }

    public void verifyProvisionFormDisplayed() {
        // Verify form is displayed by checking for the first combobox field
        page.getByRole(AriaRole.COMBOBOX,
            new Page.GetByRoleOptions().setName("Provision Number and Name").setExact(true))
            .first()
            .waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("WFTCL Provision form displayed");
    }

    public void enterProvisionOverviewDetails() {
        // Select first available option for all provision overview dropdowns
        selectFirstAvailableOption("Provision Number and Name");
        selectFirstAvailableOption("DSG Division");
        selectFirstAvailableOption("MACPro System Impacted");
        selectFirstAvailableOption("OneMAC System Impacted");
        selectFirstAvailableOption("WMS System Impacted");
        selectFirstAvailableOption("Other System Impacted");
        logger.info("Provision overview details entered");
    }

    public void submitProvisionDates() {
        clickEditIfVisible();
        enterValue("Draft Ready for PRA", todayDate());
        enterValue("System Ready", todayDate());
        enterValue("Provision Effective Date", todayDate());
        enterValue("Projected Submission Window", randomString(10));
        enterTextarea("Provision Date Notes", randomString(10));
        clickSave("submitting provision dates", true);
        logger.info("Provision dates submitted");
    }

    public void verifyProvisionDatesSaved() {
        // PREDICTED (not runtime-verified): assert the inline save actually completed. clickSave()
        // swallows a validation-blocked save (Save stays visible), so the wait is now mandatory (no
        // catch) — the step fails if the Provision Dates section never left edit mode.
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true))
            .first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN).setTimeout(8000));
        logger.info("Provision dates saved");
    }

    public void submitMeetingsDates() {
        clickEditIfVisible();
        enterValue("Date of Last Meeting", todayDate());
        enterValue("Date of Next Meeting", todayDate());
        clickSave("submitting meetings dates", true);
        logger.info("Meetings dates submitted");
    }

    public void verifyMeetingDatesSaved() {
        // PREDICTED (not runtime-verified): mandatory save-completed wait (see verifyProvisionDatesSaved).
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true))
            .first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN).setTimeout(8000));
        logger.info("Meetings dates saved");
    }

    public void submitDocumentTracking() {
        clickEditIfVisible();
        enterValue("Template Submitted to Business", todayDate());
        enterValue("PDF Created for PRA", todayDate());
        enterValue("Template Received from Business", todayDate());
        enterValue("Final Version Received Post PRA", todayDate());
        // Salesforce sometimes requires a second save for multi-field inline edits
        Locator saveBtn = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Save").setExact(true)).first();
        saveBtn.click();
        try {
            saveBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignored) {}
        try {
            if (saveBtn.isVisible()) {
                saveBtn.click();
                saveBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
            }
        } catch (Exception ignored) {}
        logger.info("Document tracking submitted");
    }

    public void verifyDocumentTrackingSaved() {
        // PREDICTED (not runtime-verified): mandatory save-completed wait (see verifyProvisionDatesSaved).
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true))
            .first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN).setTimeout(8000));
        logger.info("Document tracking saved");
    }

    public void uploadFileOnProvision() {
        String filePath = System.getProperty("user.dir") + "/src/test/resources/test-data/outreach-emergency.pdf";
        page.onFileChooser(chooser -> chooser.setFiles(Paths.get(filePath)));
        page.locator("xpath=(//*[text()='Upload Files']/lightning-primitive-icon)[last()]").click();
        try {
            Locator doneBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Done").setExact(true)).first();
            doneBtn.waitFor(new Locator.WaitForOptions().setTimeout(10000));
            doneBtn.click();
            doneBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignored) {}
        logger.info("File uploaded to Provision record");
    }

    public void verifyFileUploaded() {
        // Wait for upload dialog to close
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Done").setExact(true))
            .first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN).setTimeout(8000));
        
        // Look for the uploaded file as a link in the Files section
        // Try multiple strategies to find the visible file name
        Locator fileLink = null;
        
        // Strategy 1: Look for link with file name
        fileLink = page.getByRole(AriaRole.LINK)
            .filter(new Locator.FilterOptions().setHasText("outreach-emergency"))
            .first();
        
        if (fileLink.count() == 0) {
            // Strategy 2: Look within Files related list for any element with the filename
            Locator filesSection = page.locator("article[aria-label='Files']").first();
            if (filesSection.count() > 0) {
                fileLink = filesSection.locator("a, span").filter(new Locator.FilterOptions()
                    .setHasText("outreach-emergency")).first();
            }
        }
        
        if (fileLink.count() == 0) {
            // Strategy 3: Generic search for visible element with filename (not assistive text)
            fileLink = page.locator("a, span:not(.slds-assistive-text)")
                .filter(new Locator.FilterOptions().setHasText("outreach-emergency"))
                .first();
        }
        
        fileLink.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(15000));
        
        logger.info("File upload verified");
    }

    public void addNoteOnProvision() {
        page.getByText("Notes", new Page.GetByTextOptions().setExact(true)).first().click();
        Locator noteArea = page.getByLabel("Add Note", new Page.GetByLabelOptions().setExact(false)).first();
        noteArea.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        noteArea.fill(NOTE_TEXT);
        clickNoteSaveButton(noteArea);
        logger.info("Note added to Provision record");
    }

    private void clickNoteSaveButton(Locator noteArea) {
        java.util.regex.Pattern saveText = java.util.regex.Pattern.compile("^\\s*Save\\s*$", java.util.regex.Pattern.CASE_INSENSITIVE);
        Locator noteSaveButton = page.locator("button[title*='save new note' i]:not([disabled]):not([aria-disabled='true'])")
            .or(page.locator("button[title*='save updated note' i]:not([disabled]):not([aria-disabled='true'])"))
            .or(page.locator("button[title*='save note' i]:not([disabled]):not([aria-disabled='true'])"))
            .or(noteArea.locator("xpath=ancestor::*[contains(@class,'slds-card') or contains(@class,'slds-modal') or @role='dialog'][1]")
                .locator("button:not([disabled]):not([aria-disabled='true'])")
                .filter(new Locator.FilterOptions().setHasText(saveText)))
            .first();
        noteSaveButton.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        noteSaveButton.scrollIntoViewIfNeeded();
        noteSaveButton.click();
    }

    public void verifyNoteAdded() {
        page.getByText(NOTE_TEXT, new Page.GetByTextOptions().setExact(true))
            .first().waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("Note verified on Provision record");
    }

    public void postChatterMessage() {
        page.getByText("Chatter", new Page.GetByTextOptions().setExact(true)).first().click();
        Locator askBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
            .setName(java.util.regex.Pattern.compile("Ask", java.util.regex.Pattern.CASE_INSENSITIVE))).first();
        askBtn.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        askBtn.click();
        
        // Fill Question field (required)
        Locator questionArea = page.getByLabel(
            java.util.regex.Pattern.compile("Question.*255", java.util.regex.Pattern.CASE_INSENSITIVE)).first();
        questionArea.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        questionArea.fill(CHATTER_MSG);
        
        // Fill Details field (rich text editor) - Quill editor
        Locator detailsEditor = page.locator("div.ql-editor[contenteditable='true'][data-placeholder*='add details']").first();
        detailsEditor.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        detailsEditor.click();
        detailsEditor.fill("Additional details for the question");
        
        // Use stable class-based selector for submit button
        Locator submitBtn = page.locator("button.cuf-publisherShareButton").first();
        
        // Wait for button to be visible and attached
        submitBtn.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.ATTACHED)
            .setTimeout(10000));
        
        // Give Lightning time to validate and enable the button (simple and reliable)
        page.waitForTimeout(1500);
        
        // Click using force to bypass any overlay issues
        submitBtn.click(new Locator.ClickOptions().setForce(true));
        
        // Wait for button to disappear after successful post
        try {
            submitBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(3000));
        } catch (Exception ignored) {
            // Button might not hide in all cases, continue
        }
        logger.info("Chatter message posted");
    }

    public void verifyChatterMessagePosted() {
        page.getByText(CHATTER_MSG, new Page.GetByTextOptions().setExact(false))
            .first().waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("Chatter message verified");
    }

    public void openFieldHistoryTracking() {
        page.getByText("Field History Tracking", new Page.GetByTextOptions().setExact(true)).first().click();
        
        // Wait for the "View All" link to appear (indicates field history section loaded)
        Locator viewAllLink = page.locator("span.view-all-label").first();
        viewAllLink.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        viewAllLink.click();
        
        // Wait for table to be attached (exists in DOM) - rows may be virtualized/hidden
        page.locator("table tbody tr").first()
            .waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(10000));
        
        // Give time for modal/table to render fully
        page.waitForTimeout(1000);
        
        logger.info("Field History Tracking opened");
    }

    public void verifyFieldHistoryEntries() {
        // Wait for the field history table to be attached (rows may be virtualized/hidden)
        page.locator("table tbody tr").first()
            .waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(15000));
        
        // Verify at least one history entry exists (count works even for hidden elements)
        int rowCount = page.locator("table tbody tr").count();
        if (rowCount == 0) {
            throw new RuntimeException("No field history entries found");
        }
        
        logger.info("Field history entries verified: {} rows found", rowCount);
    }

}
