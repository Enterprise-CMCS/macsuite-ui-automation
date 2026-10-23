package com.macsuite.common;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Comprehensive Salesforce Lightning field interaction utilities.
 * 
 * PURPOSE:
 * --------
 * Consolidates all Salesforce field interaction patterns into reusable methods
 * to achieve 100% DRY compliance across PIER, Outreach, and other modules.
 * 
 * DESIGN PRINCIPLES:
 * ------------------
 * 1. Label-based semantic locators (P1 priority) - follows accessibility best practices
 * 2. Context-aware (handles modals/dialogs vs page body automatically)
 * 3. Handles all Salesforce Lightning quirks (inline-edit, force clicks, portal dropdowns)
 * 4. Built-in wait strategies for field dependencies and lazy loading
 * 5. Optional highlighting for debugging
 * 6. Graceful fallbacks (JS click when standard click fails)
 * 
 * LOCATOR PRIORITY (matches Copilot instructions):
 * ------------------------------------------------
 * P1: Semantic role + accessible name (getByRole/getByLabel)
 * P2: Label/placeholder
 * P3: Stable data attributes
 * P4: Contextual anchoring
 * 
 * COVERAGE:
 * ---------
 * ✅ Text/number inputs (Start Year, Description, etc.)
 * ✅ Date fields (Timeline Date, Closed Date, etc.)
 * ✅ Dropdowns/picklists (Start Month, Indicator Affected, etc.)
 * ✅ Standard checkboxes (Closed, Issue related to E&E?, etc.)
 * ✅ Activity section checkboxes (Fixed - Forward Data, etc.)
 * ✅ Dual-listbox fields (Sub Indicator Affected, Related Check)
 * ✅ Lookup fields (Account, Current Case Lead - inline only)
 * ✅ Rich text editors (Quill/Description with formatting)
 * ✅ Inline-edit detection and handling
 * ✅ Save/button clicks with toast verification
 * ✅ JavaScript click fallback for hidden elements
 * ✅ Context scoping (modal vs page body)
 * ✅ Wait strategies and error handling
 * 
 * USAGE EXAMPLE:
 * --------------
 * SalesforceFieldUtils sf = new SalesforceFieldUtils(page, true); // Enable highlighting
 * 
 * sf.selectLookupInline("Account", "Colorado");
 * sf.selectDropdown("Start Month", "January");
 * sf.selectDualListbox("Sub Indicator Affected", "1a, 1b");
 * sf.fillTextField("Start Year", "2026");
 * sf.clickActivityCheckbox("Fixed_Forward_Data__c");
 * sf.clickSave(true);
 * 
 * MIGRATION PLAN:
 * ---------------
 * [ ] Step 1: Keep existing methods in PierIssuePage, OutreachPage intact (DO NOT refactor yet)
 * [ ] Step 2: Add SalesforceFieldUtils instance to page objects (this.sfUtils = new SalesforceFieldUtils(page))
 * [ ] Step 3: Gradually replace individual methods with sfUtils calls in NEW scenarios only
 * [ ] Step 4: Once stable (after 5+ test runs), refactor existing methods
 * [ ] Step 5: Remove duplicate private methods from page objects
 * 
 * TODO / FUTURE ENHANCEMENTS:
 * ---------------------------
 * [ ] Add lookup modal handling (for "Show more results" advanced search)
 * [ ] Add selectDropdownByFieldId() fallback for problematic fields
 * [ ] Add verifyFieldValue() methods for assertions
 * [ ] Add waitForToastMessage() with specific text verification
 * [ ] Add selectFromRelatedList() for related list interactions
 * [ ] Add navigateToTab() for tab navigation
 * [ ] Add iframeContext() handling for dashboard/reports
 * [ ] Integration with TestConfig for highlight duration control
 * 
 * KNOWN LIMITATIONS:
 * ------------------
 * - Lookup fields: Currently only handles inline dropdowns, not modal "Show more results"
 * - Highlighting: Fixed 500ms duration, should read from TestConfig
 * - Context switching: No support for iframe-based dashboards yet
 * - Related lists: No helpers for clicking/editing related list records
 * 
 * DROPDOWN INTERFERENCE FIX (2026-06-08):
 * ----------------------------------------
 * Lightning components leave dropdown listboxes in the DOM (hidden) after selection.
 * Even worse: Salesforce creates DUPLICATE dropdown containers with the SAME aria-label.
 * 
 * Failed approaches:
 *   X .slds-dropdown[role='listbox'].first() - finds first in DOM (often hidden)
 *   X waitForSelector with VISIBLE - still picks first duplicate in DOM order
 *   X aria-label selector - duplicates have SAME aria-label!
 * 
 * Working solution:
 *   ✓ Skip dropdown container entirely
 *   ✓ Wait directly for the OPTION to appear (using getByRole(OPTION))
 *   ✓ If option is visible, dropdown is definitely open and populated
 *   ✓ No ambiguity, no duplicates, no stale elements
 * 
 * @author MACSuite Automation Team
 * @version 1.0.0
 * @since 2026-06-08
 */
public class SalesforceFieldUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesforceFieldUtils.class);
    private final Page page;
    private final boolean highlightEnabled;
    
    /**
     * Constructor with highlighting disabled (production default).
     * @param page Active Playwright page instance
     */
    public SalesforceFieldUtils(Page page) {
        this(page, false);
    }
    
    /**
     * Constructor with optional highlighting for debugging.
     * @param page Active Playwright page instance
     * @param highlightEnabled If true, highlights elements before interaction
     */
    public SalesforceFieldUtils(Page page, boolean highlightEnabled) {
        this.page = page;
        this.highlightEnabled = highlightEnabled;
    }
    
    // ==========================================
    // CONTEXT RESOLUTION (Modal vs Page Body)
    // ==========================================
    
    /**
     * Returns the current context - either the active modal/dialog or page body.
     * Salesforce opens modals for "New Record" forms, lookups, etc.
     * 
     * WHY: Lightning dropdowns and options render at PAGE level (portal pattern),
     * but form fields are scoped to the modal or page body.
     */
    private Locator getContext() {
        Locator dialogs = page.getByRole(AriaRole.DIALOG);
        return dialogs.count() > 0 ? dialogs.last() : page.locator("body");
    }
    
    // ==========================================
    // 1. SIMPLE INPUT FIELDS (Text, Number, Year)
    // ==========================================
    
    /**
     * Fills a standard text/number input field by its label.
     * Handles: Start Year, End Year, Description, Subject, etc.
     * 
     * @param label Human-readable field label (e.g., "Start Year", "Description")
     * @param value Text to fill
     */
    public void fillTextField(String label, String value) {
        Locator ctx = getContext();
        Locator field = ctx.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
        
        field.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        highlightIfEnabled(field, label);
        
        // Skip if it's a table header (common Salesforce issue in field history pages)
        if ("TH".equals(field.evaluate("el => el.tagName"))) {
            logger.warn("Skipping table header for label: {}", label);
            return;
        }
        
        field.scrollIntoViewIfNeeded();
        field.fill(value);
        logger.info("Filled text field '{}' with: {}", label, value);
    }
    
    // ==========================================
    // 2. DATE FIELDS
    // ==========================================
    
    /**
     * Fills a Salesforce date field by its label.
     * Handles: Timeline Date - Forward Data, Closed Date, Follow-up Date, etc.
     * 
     * @param label Field label (e.g., "Timeline Date - Forward Data")
     * @param dateValue Date in M/d/yyyy format (e.g., "6/8/2026")
     */
    public void fillDateField(String label, String dateValue) {
        Locator ctx = getContext();
        Locator dateInput = ctx.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
        
        dateInput.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        highlightIfEnabled(dateInput, label);
        
        dateInput.scrollIntoViewIfNeeded();
        dateInput.click();
        dateInput.fill(dateValue);
        logger.info("Filled date field '{}' with: {}", label, dateValue);
    }
    
    // ==========================================
    // 3. DROPDOWN/PICKLIST FIELDS
    // ==========================================
    
    /**
     * Selects a dropdown/picklist option by label and value.
     * Handles: Start Month, Indicator Affected, Report Type, Issue Type, Status, etc.
     * 
     * IMPORTANT: Lightning dropdowns render at PAGE LEVEL (not in context),
     * so we search for options in entire page. This is a known Lightning portal pattern.
     * 
     * @param label Field label (e.g., "Start Month", "Indicator Affected")
     * @param optionValue Option to select (e.g., "January", "1. Call Center Volume")
     */
    public void selectDropdown(String label, String optionValue) {
        Locator ctx = getContext();
        Locator combobox = ctx.getByRole(AriaRole.COMBOBOX, 
            new Locator.GetByRoleOptions().setName(label)).first();
        
        combobox.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        
        // Wait for field to be enabled (not disabled) - important for dependent fields
        String ariaDisabled = combobox.getAttribute("aria-disabled");
        if ("true".equals(ariaDisabled)) {
            logger.info("Field '{}' is disabled, waiting for it to be enabled...", label);
            page.waitForTimeout(2000);
        }
        
        // Check if already selected (optimization to avoid unnecessary clicks)
        String currentValue = combobox.getAttribute("data-value");
        if (optionValue.equals(currentValue)) {
            logger.info("'{}' already selected for '{}', skipping", optionValue, label);
            return;
        }
        
        highlightIfEnabled(combobox, label);
        combobox.scrollIntoViewIfNeeded();
        combobox.click();
        
        // Wait for dropdown to actually open and populate (allows time for dependent fields or Lightning rendering)
        page.waitForTimeout(1000);
        
        // Wait DIRECTLY for the option to appear (Lightning portal pattern - renders at document root)
        // Don't wait for dropdown container - Salesforce creates duplicates with same aria-label
        Locator option = page.getByRole(AriaRole.OPTION, 
            new Page.GetByRoleOptions().setName(optionValue).setExact(true)).first();
        
        try {
            option.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(3000));
        } catch (Exception e) {
            // Dropdown didn't open, try clicking again
            logger.warn("Dropdown '{}' didn't open on first click, trying again", label);
            combobox.click();
            page.waitForTimeout(1000);
            option.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(8000));
        }
        
        option.click();
        
        logger.info("Selected '{}' for '{}'", optionValue, label);
    }
    
    // ==========================================
    // 4. CHECKBOX FIELDS (Standard)
    // ==========================================
    
    /**
     * Sets a checkbox to a specific state (checked or unchecked).
     * Uses semantic label-based approach (P1 priority).
     * 
     * Best for: Fields visible on screen with accessible labels
     * Handles: Closed, Issue related to E&E?, QA, IsEscalated, etc.
     * 
     * @param label Field label (e.g., "Closed", "Issue related to E&E?")
     * @param shouldBeChecked Target state (true = checked, false = unchecked)
     */
    public void setCheckbox(String label, boolean shouldBeChecked) {
        Locator ctx = getContext();
        Locator checkbox = ctx.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
        
        checkbox.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        boolean isCurrentlyChecked = checkbox.isChecked();
        
        if (isCurrentlyChecked != shouldBeChecked) {
            highlightIfEnabled(checkbox, label);
            checkbox.scrollIntoViewIfNeeded();
            checkbox.click();
            logger.info("Set checkbox '{}' to: {}", label, shouldBeChecked);
        } else {
            logger.info("Checkbox '{}' already in target state: {}", label, shouldBeChecked);
        }
    }
    
    /**
     * Sets a checkbox by field API name (fallback for hidden/dynamic fields).
     * Uses native hidden checkbox input with JS click.
     * 
     * Best for: Fields that don't have accessible labels or are in complex forms
     * Handles: Closed__c, QA__c, IsEscalated, etc.
     * 
     * @param fieldName Salesforce field API name (e.g., "Closed__c", "QA__c")
     * @param shouldBeChecked Target state
     */
    public void setCheckboxByFieldName(String fieldName, boolean shouldBeChecked) {
        try {
            Locator nativeCheckbox = page.locator("input[name='" + fieldName + "']").first();
            nativeCheckbox.waitFor(new Locator.WaitForOptions().setTimeout(4000));
            
            boolean isCurrentlyChecked = nativeCheckbox.isChecked();
            if (isCurrentlyChecked != shouldBeChecked) {
                // Use JS click for hidden native inputs
                page.locator("xpath=//input[@name='" + fieldName + "']").first()
                    .evaluate("el => el.click()");
                logger.info("Set checkbox by field name '{}' to: {}", fieldName, shouldBeChecked);
            }
        } catch (Exception e) {
            logger.warn("Checkbox field '{}' not available: {}", fieldName, e.getMessage());
        }
    }
    
    // ==========================================
    // 5. ACTIVITY SECTION CHECKBOXES (Force Click)
    // ==========================================
    
    /**
     * Clicks an Activity section checkbox using force click.
     * Activity sections use custom Lightning checkbox wrappers that require force clicks
     * to bypass floating panels and LWC structural layers.
     * 
     * WHY FORCE CLICK: Activity section checkboxes are wrapped in complex Lightning
     * web components with overlapping elements that intercept standard clicks.
     * 
     * Handles: Fixed - Forward Data, Fixed - Historical Data, Affecting Historical Data,
     *          State will not fix - Forward Data, State will not fix - Historical Data
     * 
     * @param fieldApiName Field API name (e.g., "Fixed_Forward_Data__c", "Affecting_Historical_Data__c")
     */
    public void clickActivityCheckbox(String fieldApiName) {
        Locator checkboxWrapper = page.locator(
            "div[data-target-selection-name*='" + fieldApiName + "'] span.slds-checkbox_faux");
        
        checkboxWrapper.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        checkboxWrapper.scrollIntoViewIfNeeded();
        checkboxWrapper.click(new Locator.ClickOptions().setForce(true));
        
        logger.info("Clicked Activity section checkbox: {}", fieldApiName);
    }
    
    // ==========================================
    // 6. DUAL-LISTBOX FIELDS
    // ==========================================
    
    /**
     * Selects items in a Salesforce dual-listbox field.
     * Handles: Sub Indicator Affected, Related Check, etc.
     * 
     * Process:
     * 1. Find field by label
     * 2. For each comma-separated value:
     *    a. Check if already in Chosen list (skip if present)
     *    b. Select from Available list
     *    c. Click "Move to Chosen" button
     *    d. Verify transferred to Chosen list
     * 
     * @param fieldLabel Field label (e.g., "Sub Indicator Affected", "Related Check")
     * @param commaSeparatedValues Values to select (e.g., "1a, 1b" or "10.DL.01")
     */
    public void selectDualListbox(String fieldLabel, String commaSeparatedValues) {
        Locator ctx = getContext();
        Locator fieldGroup = ctx.locator("div[role='group']:has-text('" + fieldLabel + "')").first();
        
        Locator availableList = fieldGroup.locator("ul[data-source-list]").first();
        availableList.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        
        availableList.getByRole(AriaRole.OPTION).first()
            .waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        
        Locator chosenList = fieldGroup.locator("ul[data-selected-list], ul[data-destination-list]").first();
        
        for (String rawValue : commaSeparatedValues.split(",")) {
            String value = rawValue.trim();
            if (value.isEmpty()) continue;
            
            // Skip if already in Chosen list
            boolean alreadyChosen = chosenList.getByRole(AriaRole.OPTION)
                .filter(new Locator.FilterOptions().setHasText(value)).count() > 0;
            
            if (alreadyChosen) {
                logger.info("'{}' already selected in '{}', skipping", value, fieldLabel);
                continue;
            }
            
            // Select from Available list
            Locator optionToSelect = availableList.getByRole(AriaRole.OPTION)
                .filter(new Locator.FilterOptions().setHasText(value)).first();
            
            optionToSelect.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            optionToSelect.click();
            
            // Click "Move to Chosen" button
            fieldGroup.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
                .setName(Pattern.compile("Move selection to Chosen", Pattern.CASE_INSENSITIVE)))
                .first().click();
            
            // Verify transferred
            chosenList.getByRole(AriaRole.OPTION)
                .filter(new Locator.FilterOptions().setHasText(value)).first()
                .waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            
            logger.info("'{}' moved to Chosen for '{}'", value, fieldLabel);
        }
    }
    
    // ==========================================
    // 7. LOOKUP FIELDS (Simple Inline)
    // ==========================================
    
    /**
     * Selects a lookup field value (inline dropdown).
     * Handles: Account, Current Case Lead (simple cases where results appear immediately)
     * 
     * LIMITATION: Only handles inline dropdown lookups. Does NOT handle:
     * - "Show more results" modal lookups
     * - Advanced search in lookup modals
     * 
     * TODO: Add selectLookupModal() for advanced search scenarios
     * 
     * @param label Field label (e.g., "Account", "Current Case Lead")
     * @param lookupValue Value to search and select (e.g., "Colorado", "Shital")
     */
    public void selectLookupInline(String label, String lookupValue) {
        Locator ctx = getContext();
        Locator input = ctx.getByRole(AriaRole.COMBOBOX, 
            new Locator.GetByRoleOptions().setName(label)).first();
        
        input.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        highlightIfEnabled(input, label);
        
        input.scrollIntoViewIfNeeded();
        input.click();
        input.fill(lookupValue);
        
        page.waitForTimeout(1500); // Wait for dropdown to populate
        
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions()
            .setName(lookupValue).setExact(false))
            .first().click();
        
        // Brief wait for dropdown to close naturally
        page.waitForTimeout(300);
        
        logger.info("Selected lookup '{}' for '{}'", lookupValue, label);
    }
    
    // ==========================================
    // 8. RICH TEXT / QUILL EDITOR
    // ==========================================
    
    /**
     * Fills a Salesforce rich text editor (Quill).
     * Handles: Description fields with formatting toolbar, Notes fields, etc.
     * 
     * Process:
     * 1. Click on the field container to activate editor
     * 2. Wait for Quill editor to appear
     * 3. Fill content
     * 4. Fallback to JS innerHTML if standard fill fails
     * 
     * @param label Field label (e.g., "Description", "Notes")
     * @param richTextValue Text content to fill (can include HTML tags)
     */
    public void fillRichText(String label, String richTextValue) {
        Locator ctx = getContext();
        Locator container = ctx.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
        
        container.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        highlightIfEnabled(container, label);
        
        container.scrollIntoViewIfNeeded();
        container.click();
        
        Locator editor = page.locator(".ql-editor[contenteditable='true']").first();
        editor.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        editor.fill(richTextValue);
        
        // Fallback: use JS if standard fill fails
        if (!editor.textContent().contains(richTextValue)) {
            editor.evaluate("(el, value) => el.innerHTML = value", richTextValue);
        }
        
        logger.info("Filled rich text field '{}' with: {}", label, richTextValue);
    }
    
    // ==========================================
    // 9. INLINE-EDIT DETECTION & HANDLING
    // ==========================================
    
    /**
     * Clicks an inline-edit button if present (Salesforce detail pages).
     * Call this before updating fields on detail/view pages.
     * 
     * Handles multiple button title patterns:
     * - "Edit [Field Label]" (e.g., "Edit Affecting Historical Data")
     * - "Edit [Field_API_Name]" (e.g., "Edit Fixed_Forward_Data__c")
     * 
     * Use case: Activity section fields, detail page fields with inline-edit pencil icons
     * 
     * @param fieldLabel Field label to edit (e.g., "Affecting Historical Data", "Current Case Lead")
     */
    public void clickInlineEditIfVisible(String fieldLabel) {
        try {
            Locator editButton = page.locator(
                "button[title*='Edit " + fieldLabel + "']," +
                "button[title*='Edit " + fieldLabel.replace(" ", "_") + "']"
            ).first();
            
            if (editButton.count() > 0 && editButton.isVisible()) {
                editButton.scrollIntoViewIfNeeded();
                editButton.click(new Locator.ClickOptions().setTimeout(3000));
                page.waitForTimeout(1500); // Wait for edit mode to activate
                logger.info("Clicked inline edit button for: {}", fieldLabel);
            }
        } catch (Exception e) {
            logger.debug("No inline edit button for '{}': {}", fieldLabel, e.getMessage());
        }
    }
    
    /**
     * Clicks the main record Edit button if visible (page-level edit mode).
     * Use case: Detail pages where entire form needs to be in edit mode
     * (vs inline-edit which edits individual fields)
     */
    public void clickEditIfVisible() {
        try {
            Locator editBtn = page.getByRole(AriaRole.BUTTON, 
                new Page.GetByRoleOptions().setName("Edit").setExact(true)).first();
            
            if (editBtn.count() > 0 && editBtn.isVisible(new Locator.IsVisibleOptions().setTimeout(2000))) {
                editBtn.scrollIntoViewIfNeeded();
                editBtn.click();
                page.waitForTimeout(1000); // Wait for form to enter edit mode
                logger.info("Clicked main Edit button");
            }
        } catch (Exception e) {
            logger.debug("No Edit button visible");
        }
    }
    
    // ==========================================
    // 10. SAVE & BUTTON CLICKS
    // ==========================================
    
    /**
     * Clicks the Save button and optionally waits for success toast.
     * 
     * @param waitForToast If true, waits for success toast message (recommended for create operations)
     *                     If false, just waits 2s for page transition (recommended for updates)
     */
    public void clickSave(boolean waitForToast) {
        Locator saveBtn = page.getByRole(AriaRole.BUTTON, 
            new Page.GetByRoleOptions().setName("Save").setExact(true)).first();
        
        saveBtn.scrollIntoViewIfNeeded();
        saveBtn.click();
        
        if (waitForToast) {
            page.locator(".slds-theme--success, .slds-notify--toast").first()
                .waitFor(new Locator.WaitForOptions().setTimeout(10000));
            logger.info("Clicked Save button - success toast verified");
        } else {
            page.waitForTimeout(2000); // Allow page transition
            logger.info("Clicked Save button");
        }
    }
    
    /**
     * Clicks any button by its name.
     * 
     * @param buttonName Button text (e.g., "Cancel", "Next", "Submit", "New")
     */
    public void clickButton(String buttonName) {
        Locator button = page.getByRole(AriaRole.BUTTON, 
            new Page.GetByRoleOptions().setName(buttonName).setExact(true)).first();
        
        button.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        button.scrollIntoViewIfNeeded();
        button.click();
        
        logger.info("Clicked button: {}", buttonName);
    }
    
    // ==========================================
    // 11. JAVASCRIPT CLICK FALLBACK
    // ==========================================
    
    /**
     * Clicks an element via JavaScript (for hidden/stubborn elements).
     * Use as last resort when standard clicks fail.
     * 
     * Common use cases:
     * - Hidden checkboxes in complex forms
     * - Elements behind floating panels
     * - Navigation elements that trigger route changes
     * 
     * @param xpath XPath to element (e.g., "//input[@name='Field__c']", "//*[text()='Tab Name']")
     */
    public void jsClick(String xpath) {
        page.locator("xpath=" + xpath).first().evaluate("el => el.click()");
        logger.info("JS clicked element: {}", xpath);
    }
    
    // ==========================================
    // 12. WAIT & VERIFICATION HELPERS
    // ==========================================
    
    /**
     * Waits for a field label to appear (for dependent fields).
     * Use when field visibility depends on another field's value.
     * 
     * Example: Category/Sub Category dropdowns where Sub Category only appears
     * after selecting a Category value.
     * 
     * @param label Field label to wait for (e.g., "Sub Category", "End Month")
     * @param timeoutMs Timeout in milliseconds (e.g., 15000 for 15 seconds)
     */
    public void waitForFieldLabel(String label, int timeoutMs) {
        Locator ctx = getContext();
        ctx.getByText(label, new Locator.GetByTextOptions().setExact(false)).first()
            .waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
        logger.info("Field label '{}' appeared", label);
    }
    
    /**
     * Dismisses error banner if present (Salesforce validation errors).
     * Call this before verifying error counts to clear page-level error banners
     * that might interfere with field-level error detection.
     */
    public void dismissErrorBannerIfPresent() {
        try {
            Locator closeBtn = page.locator("button.slds-notify__close").first();
            if (closeBtn.count() > 0 && closeBtn.isVisible(new Locator.IsVisibleOptions().setTimeout(1000))) {
                closeBtn.click();
                logger.info("Dismissed error banner");
            }
        } catch (Exception ignored) {
            // Silently ignore if banner not present
        }
    }
    
    // ==========================================
    // PRIVATE HELPERS
    // ==========================================
    
    /**
     * Highlights an element if highlighting is enabled.
     * Used for debugging and visual verification during test development.
     * 
     * TODO: Read highlight duration from TestConfig.HIGHLIGHT_DURATION
     */
    private void highlightIfEnabled(Locator locator, String label) {
        if (!highlightEnabled) return;
        
        try {
            locator.scrollIntoViewIfNeeded();
            String originalStyle = (String) locator.evaluate("el => el.getAttribute('style')");
            
            locator.evaluate("el => {" +
                "el.style.border = '3px solid #4ECDC4';" +
                "el.style.backgroundColor = '#FFE66D';" +
                "el.style.boxShadow = '0 0 10px 2px #4ECDC4';" +
            "}");
            
            logger.debug("→ Highlighted: {}", label);
            Thread.sleep(500); // TODO: Use TestConfig.HIGHLIGHT_DURATION
            
            if (originalStyle != null) {
                locator.evaluate("el => el.setAttribute('style', '" + originalStyle + "')");
            } else {
                locator.evaluate("el => el.removeAttribute('style')");
            }
        } catch (Exception e) {
            logger.debug("Could not highlight element: {}", e.getMessage());
        }
    }
}
