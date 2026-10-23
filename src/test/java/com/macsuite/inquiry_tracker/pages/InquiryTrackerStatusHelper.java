package com.macsuite.inquiry_tracker.pages;

import com.macsuite.common.pages.BasePage;
import com.macsuite.common.pages.SfNavigator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class InquiryTrackerStatusHelper extends BasePage{
    private final Page page;

    InquiryTrackerStatusHelper(Page page) {
        super(page);
        this.page = page;
    }

    void putInquiryOnHold(String holdReason) {
        // Click the Put On Hold button
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Put On Hold").setExact(true)).click();
        
        // Wait for modal and find the reason input field by its stable name attribute
        Locator reasonInput = page.locator("input[name='scr_PutonHold']").first();
        reasonInput.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        reasonInput.fill(holdReason);
        
        clickSave();
    }

    void releaseInquiryFromHold(String releaseReason) {
        // Wait for page to stabilize after put on hold action (page may have refreshed)
        // page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE, 
        //     new Page.WaitForLoadStateOptions().setTimeout(10000));
        page.waitForTimeout(5000);
        // 1. Locate the section header and scroll it into view
        Locator sectionHeader = page.getByRole(
            AriaRole.BUTTON, 
            new Page.GetByRoleOptions().setName("On Hold Details")
        );
        sectionHeader.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        
        // This ensures the section is visible and scrolled into view
        sectionHeader.scrollIntoViewIfNeeded();
        
        // 2. Locate the specific edit button
        Locator editButton = page.getByRole(
            AriaRole.BUTTON, 
            new Page.GetByRoleOptions().setName("Edit Is On Hold?")
        );
        editButton.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        
        // 3. Click the edit button
        editButton.click();
        
        // Uncheck the "Is On Hold?" checkbox
        page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Is On Hold?")).uncheck();
        
        // Fill the release reason (click first to activate, then fill)
        Locator releaseReasonField = page.getByRole(AriaRole.TEXTBOX, 
            new Page.GetByRoleOptions().setName("Released from On Hold Reason"));
        releaseReasonField.click();
        releaseReasonField.fill(releaseReason);
        
        clickSave();
    }

    void editInquiryStatus() {
        // Wait for page stability before attempting to edit
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                new Page.WaitForLoadStateOptions().setTimeout(10000));
            Thread.sleep(1000);
        } catch (Exception ignored) {
        }

        Locator editButton = page.locator("button[title='Edit Inquiry Status']:visible").first();
        for (int attempt = 0; attempt < 20; attempt++) {
            if (editButton.count() > 0) {
                try {
                    editButton.scrollIntoViewIfNeeded();
                    editButton.click();
                    // Wait briefly for edit mode to activate
                    page.waitForTimeout(500);
                    return;
                } catch (Exception ignored) {
                }
            }
            page.mouse().wheel(0, 700);
        }

        throw new AssertionError("Edit Inquiry Status button was not found after scrolling the record page");
    }

    void selectInquiryStatus(String status) {
        Locator statusCombobox = inquiryStatusCombobox();
        if (!isVisible(statusCombobox, 2000)) {
            editInquiryStatus();
            statusCombobox = inquiryStatusCombobox();
            statusCombobox.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        }
        
        // Log the current value before changing
        String currentValue = statusCombobox.getAttribute("data-value");
        System.out.println("Current Inquiry Status (data-value): " + currentValue);
        System.out.println("Selecting Inquiry Status: " + status);
        
        // Wait briefly to ensure any previous error state has settled
        page.waitForTimeout(500);
        

        // Special case: "None" for validation testing - try to select "--None--" or just close dropdown
        if ("None".equals(status)) {
            Locator noneOption = openComboboxAndFindOption(statusCombobox, "--None--");
            if (noneOption != null) {
                noneOption.click();
                System.out.println("✓ Selected: --None--");
            } else {
                page.keyboard().press("Escape");
                System.out.println("✓ Kept status unchanged (no --None-- option available)");
            }
            return;
        }

        Locator statusOption = openComboboxAndFindOption(statusCombobox, status);
        if (statusOption == null) {
            throw new AssertionError("Inquiry Status option not found: " + status);
        }

        statusOption.click();
        page.keyboard().press("Tab");
        waitForStatusComboboxValue(statusCombobox, status);
        System.out.println("✓ Selected Inquiry Status: " + status);
    }

    void verifyWithdrawnTransitionBlocked(String attemptedStatus) {
        String expectedError = "You cannot move from Withdrawn to Open or Closed.";
        Locator errorMessage = page.getByText(expectedError, new Page.GetByTextOptions().setExact(false)).first();
        boolean sawError = false;
        try {
            errorMessage.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(3000));
            System.out.println("✓ Verified Withdrawn → " + attemptedStatus + " was blocked with error");
            sawError = true;
        } catch (Exception ignored) {
        }

        discardStatusEditIfOpen();

        String actualStatus = currentInquiryStatus();
        if (!"Withdrawn".equals(actualStatus)) {
            throw new AssertionError("Expected Withdrawn → " + attemptedStatus
                + " to be blocked by an error or by keeping status as Withdrawn, but current status is '"
                + actualStatus + "'");
        }
        if (!sawError) {
            System.out.println("✓ Verified Withdrawn → " + attemptedStatus
                + " was blocked; status remains Withdrawn");
        }
    }

    void selectWithdrawalReason(String reason) {
        Locator withdrawalCombobox = comboboxByLabel("Withdrawal Reason", "Reason for Withdrawal");
        withdrawalCombobox.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(15000));

        Locator reasonOption = openComboboxAndFindOption(withdrawalCombobox, reason);
        if (reasonOption == null) {
            throw new AssertionError("Withdrawal Reason option not found: " + reason);
        }

        reasonOption.click();
    }

    private Locator comboboxByLabel(String primaryLabel, String alternateLabel) {
        return page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName(primaryLabel).setExact(true)).first()
            .or(page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName(alternateLabel).setExact(true)).first())
            .or(page.locator("button[role='combobox'][aria-label='" + primaryLabel + "']").first())
            .or(page.locator("button[role='combobox'][aria-label='" + alternateLabel + "']").first())
            .or(page.locator("lightning-combobox:has-text('" + primaryLabel + "') button[role='combobox']").first())
            .or(page.locator("lightning-combobox:has-text('" + alternateLabel + "') button[role='combobox']").first());
    }

    private Locator openComboboxAndFindOption(Locator combobox, String optionText) {
        for (int attempt = 0; attempt < 2; attempt++) {
            combobox.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(attempt == 0 ? 5000 : 10000));
            combobox.scrollIntoViewIfNeeded();
            combobox.click();

            Locator option = activeComboboxOption(combobox, optionText);
            try {
                option.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(attempt == 0 ? 3000 : 8000));
                return option;
            } catch (Exception ignored) {
                Locator valueOption = visibleListbox().locator(
                    "[role='option'][data-value='" + statusEscape(optionText) + "']:visible").first();
                if (valueOption.count() > 0) {
                    return valueOption;
                }
            }
        }

        return null;
    }

    private Locator activeComboboxOption(Locator combobox, String optionText) {
        Locator listbox = listboxForCombobox(combobox);

        Locator valueOption = listbox.locator(
            "[role='option'][data-value='" + statusEscape(optionText) + "']:visible").first();
        if (valueOption.count() > 0) {
            return valueOption;
        }

        return listbox.getByRole(AriaRole.OPTION,
            new Locator.GetByRoleOptions().setName(optionText).setExact(true)).first();
    }

    private Locator listboxForCombobox(Locator combobox) {
        String controlsId = combobox.getAttribute("aria-controls");
        if (controlsId != null && !controlsId.isBlank()) {
            Locator controlledListbox = page.locator(
                "[id='" + statusEscape(controlsId) + "'][role='listbox']:visible").first();
            if (controlledListbox.count() > 0) {
                return controlledListbox;
            }
        }

        return visibleListbox();
    }

    private Locator visibleListbox() {
        return page.locator("[role='listbox']:visible").last();
    }

    private Locator inquiryStatusCombobox() {
        return page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Inquiry Status")).first();
    }

    private void waitForStatusComboboxValue(Locator combobox, String expectedStatus) {
        long deadline = System.currentTimeMillis() + 3000;
        String actualValue = "";
        String actualText = "";

        while (System.currentTimeMillis() < deadline) {
            actualValue = normalize(combobox.getAttribute("data-value"));
            actualText = normalize(combobox.textContent());
            if (expectedStatus.equals(actualValue) || expectedStatus.equals(actualText)) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new AssertionError("Inquiry Status did not change to '" + expectedStatus
            + "' before Save. Current data-value='" + actualValue + "', text='" + actualText + "'");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isVisible(Locator locator, int timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String currentInquiryStatus() {
        Locator combobox = inquiryStatusCombobox();
        if (isVisible(combobox, 1000)) {
            String dataValue = combobox.getAttribute("data-value");
            if (dataValue != null && !dataValue.isBlank()) {
                return dataValue.trim();
            }
        }

        Locator statusValue = detailsFieldValue("Inquiry Status")
            .locator("lightning-formatted-text, span.slds-truncate")
            .first();
        statusValue.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        String text = statusValue.innerText();
        return text == null ? "" : text.trim();
    }

    private void discardStatusEditIfOpen() {
        Locator cancelButton = page.locator(
            "button[name='CancelEdit']:visible:not([disabled]):not([aria-disabled='true'])").last();
        if (!isVisible(cancelButton, 1000)) {
            return;
        }

        cancelButton.click();
        try {
            inquiryStatusCombobox().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(5000));
        } catch (Exception ignored) {
        }
    }

    void fillOtherWithdrawalReason(String reason) {
        page.getByLabel("Other Withdrawal Reason").first()
            .or(page.getByLabel("Other Reason for Withdrawal").first())
            .fill(reason);
    }

    void fillReopenReason(String reason) {
        page.getByLabel("Reopen Reason").first().fill(reason);
    }

    void verifyInquiryStatus(String expectedStatus) {
        // Wait for page to stabilize after status change (may involve navigation/refresh)
        // try {
        //     page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE, 
        //         new Page.WaitForLoadStateOptions().setTimeout(10000));
        // } catch (Exception ignored) {
        // }
        
        // Locator statusField = detailsFieldValue("Inquiry Status");
        // Locator statusValue = statusField.locator("lightning-formatted-text, span.slds-truncate").first();
        
        // // Wait for the status element to appear and contain the expected text
        // try {
        //     statusValue.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        // } catch (Exception e) {
        //     throw new AssertionError("Inquiry Status field did not appear within 10 seconds");
        // }
    
        scrollToBottomOfThePage();
        scrollToBottomUsingKeyboard();
        page.waitForTimeout(2000);
        String actualStatus = page.locator("//*[text()='Inquiry Status']/../following-sibling::div//lightning-formatted-text").textContent();
        // if (actualStatus == null) {
        //     actualStatus = "";
        // }
        System.out.println("Actual Inquiry Status: " + actualStatus);
        actualStatus = actualStatus.trim();
        
        if (!actualStatus.equals(expectedStatus)) {
            throw new AssertionError("Inquiry Status mismatch: expected '" + expectedStatus + 
                "' but found '" + actualStatus + "'");
        }
        
        // Additional verification: log the status to console for debugging
        System.out.println("✓ Verified Inquiry Status: " + actualStatus);
    }

    void verifyClosedDateIsToday() {
        LocalDate today = LocalDate.now();
        Locator closedDateField = detailsFieldValue("Closed Date");
        
        // Target the lightning-formatted-text element specifically (as shown in the HTML)
        Locator closedDateValue = closedDateField.locator("lightning-formatted-text").first();
        
        // Wait for the element to be visible and have content (it updates after save)
        try {
            closedDateValue.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        } catch (Exception e) {
            throw new AssertionError("Closed Date field did not appear within 10 seconds");
        }
        
        // Get the actual text using innerText (more reliable for visible text than textContent)
        String actualText = closedDateValue.innerText();
        if (actualText == null) {
            actualText = "";
        }
        actualText = actualText.trim();
        
        // Try multiple date format patterns that Salesforce might use
        String[] formatPatterns = {
            "M/d/yyyy",      // 5/27/2026
            "MM/dd/yyyy",    // 05/27/2026
            "M/dd/yyyy",     // 5/27/2026
            "MM/d/yyyy",     // 05/27/2026
            "MMM d, yyyy",   // May 27, 2026
            "MMMM d, yyyy"   // May 27, 2026
        };
        
        boolean foundMatch = false;
        for (String pattern : formatPatterns) {
            String expectedDate = today.format(DateTimeFormatter.ofPattern(pattern));
            if (actualText.equals(expectedDate)) {
                foundMatch = true;
                break;
            }
        }
        
        if (!foundMatch) {
            throw new AssertionError("Closed Date did not match today (expected " + 
                today.format(DateTimeFormatter.ofPattern("M/d/yyyy")) + 
                " or similar format), but found: '" + actualText + "'");
        }
    }

    void verifyClosedDateIsEmpty() {
        Locator closedDateValue = detailsFieldValue("Closed Date")
            .locator("lightning-formatted-text, lightning-formatted-date-time, span.slds-truncate")
            .first();
        String text = closedDateValue.textContent();
        if (text != null && !text.trim().isEmpty()) {
            throw new AssertionError("Expected Closed Date to be empty, but found: " + text);
        }
    }

    private void clickSave() {
        System.out.println("Clicking Save button...");
        new SfNavigator(page).clickSaveButton();
    }

    private Locator detailsFieldValue(String fieldLabel) {
        return page.locator("records-record-layout-item, div.slds-form-element, lightning-output-field")
            .filter(new Locator.FilterOptions().setHas(
                page.locator(".test-id__field-label, .slds-form-element__label, .slds-form-element__label span")
                    .filter(new Locator.FilterOptions().setHasText(fieldLabel))))
            .first()
            .locator(".slds-form-element__control, .test-id__field-value, lightning-formatted-text, lightning-formatted-date-time")
            .first();
    }

    private String statusEscape(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
