package com.macsuite.inquiry_tracker.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

class InquiryTrackerNotesHelper {
    private static final Logger logger = LoggerFactory.getLogger(InquiryTrackerNotesHelper.class);

    private final Page page;

    InquiryTrackerNotesHelper(Page page) {
        this.page = page;
    }

    void addNoteToInquiry(String noteText) {
        Locator notesTextarea = page.getByLabel("Add Note", new Page.GetByLabelOptions().setExact(false)).first();
        notesTextarea.waitFor(new Locator.WaitForOptions().setTimeout(8000));
        notesTextarea.fill(noteText);

        clickNoteSaveButton(notesTextarea);
        logger.info("✓ Added note: {}", noteText);
    }

    private void clickNoteSaveButton(Locator notesTextarea) {
        Pattern saveText = Pattern.compile("^\\s*Save\\s*$", Pattern.CASE_INSENSITIVE);
        Locator noteSaveButton = page.locator("button[title*='save new note' i]:not([disabled]):not([aria-disabled='true'])")
            .or(page.locator("button[title*='save updated note' i]:not([disabled]):not([aria-disabled='true'])"))
            .or(page.locator("button[title*='save note' i]:not([disabled]):not([aria-disabled='true'])"))
            .or(page.locator("button[title*='reload list' i]:not([disabled]):not([aria-disabled='true'])")
                .filter(new Locator.FilterOptions().setHasText(saveText)))
            .or(notesTextarea.locator("xpath=ancestor::*[contains(@class,'slds-card') or contains(@class,'slds-modal') or @role='dialog'][1]")
                        .locator("button:not([disabled]):not([aria-disabled='true'])")
                .filter(new Locator.FilterOptions().setHasText(saveText)))
                .first();
        noteSaveButton.waitFor(new Locator.WaitForOptions().setTimeout(8000));
        noteSaveButton.scrollIntoViewIfNeeded();
        noteSaveButton.click();
    }

    void verifyNoteDisplayed(String noteText) {
        String displayedNoteText = page.locator("(//lightning-base-formatted-text[text()='" + noteText + "'])[last()]").textContent();
        if (displayedNoteText == null || displayedNoteText.isEmpty()) {
            throw new AssertionError("Expected note '" + noteText + "' to be visible in Notes table but it was not found");
        }
        logger.info("✓ Note '{}' is displayed", displayedNoteText);
    }

    void editLastNote(String newText) {
        Locator notesTab = page.getByRole(AriaRole.TAB,
            new Page.GetByRoleOptions().setName("Notes").setExact(true)).first();
        notesTab.waitFor(new Locator.WaitForOptions().setState(
            com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(8000));
        if (!"true".equals(notesTab.getAttribute("aria-selected"))) {
            notesTab.click();
        }
      
        Locator notesTable = page.locator("table[aria-label='Notes']").last();

        page.locator("(//table[@aria-label='Notes']//tbody/tr/td//label)[last()]").click();
       
        
        Locator editNoteButton = page.locator(
            "button[title=\"Click 'Edit/Save' to save existing note and reload list.\"]:visible:not([disabled]):not([aria-disabled='true'])")
            .first();
        editNoteButton.waitFor(new Locator.WaitForOptions().setTimeout(8000));
        editNoteButton.click();
        
        Locator noteInput = page.getByLabel("Add Note", new Page.GetByLabelOptions().setExact(false)).last();
        noteInput.waitFor(new Locator.WaitForOptions().setState(
            com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(8000));
        noteInput.fill(newText);
        
        clickNoteSaveButton(noteInput);
        notesTable.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        logger.info("✓ Edited last note to: {}", newText);
    }

    void deleteLastNote() {
        Locator notesTable = page.locator("table[aria-label='Notes']").last();
        notesTable.waitFor(new Locator.WaitForOptions().setTimeout(8000));
        
        Locator noteRadio = notesTable.locator("tbody input[type='radio']").last();
        // Wait for table to be stable after any previous operations (like edit)
        noteRadio.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        noteRadio.scrollIntoViewIfNeeded();
        noteRadio.click(new Locator.ClickOptions().setForce(true));
        
        // Find Delete button and wait for it to be clickable
        Locator deleteBtn = page.locator("button[title*='Delete']").last();
        deleteBtn.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        
        // Small wait to ensure radio selection is processed
        page.waitForTimeout(500);
        
        deleteBtn.click();
        logger.info("✓ Deleted last note");
    }

    void verifyNoteDeleted() {
        page.getByText(Pattern.compile("Note Deleted", Pattern.CASE_INSENSITIVE)).first()
            .waitFor(new Locator.WaitForOptions().setTimeout(5000));
        logger.info("✓ Note Deleted confirmation visible");
    }
}
