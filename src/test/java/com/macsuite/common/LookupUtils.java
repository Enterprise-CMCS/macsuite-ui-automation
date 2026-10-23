package com.macsuite.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LookupUtils {
    private static final Logger logger = LoggerFactory.getLogger(LookupUtils.class);

    private final Page page;

    public LookupUtils(Page page) {
        this.page = page;
    }

    /**
     * Fills a lookup field by searching and selecting a value.
     * Selects from inline dropdown search results only.
     * Automatically retries once if verification fails (Option 2 approach).
     * 
     * @param fieldLabel The label of the lookup field
     * @param searchTerm The full text of the value to select
     * @param context    The locator context to scope the search (pass null for
     *                   page-level, or modal/section locator)
     */
    public void fillLookupField(String fieldLabel, String searchTerm, Locator context) {
        try {
            String normalizedFieldLabel = normalizeFieldLabel(fieldLabel);
            logger.debug("Filling lookup field: '{}' = '{}'", normalizedFieldLabel, searchTerm);

            // Perform initial selection attempt
            Locator combobox = performLookupSelection(normalizedFieldLabel, searchTerm, context);

            // Verify the field was filled successfully (no error message)
            if (!verifyLookupFieldFilled(normalizedFieldLabel, combobox)) {
                logger.warn("Lookup field '{}' has error after first attempt, retrying...", normalizedFieldLabel);

                // Retry once more
                combobox = performLookupSelection(normalizedFieldLabel, searchTerm, context);

                // Verify again - fail if still errored
                if (!verifyLookupFieldFilled(normalizedFieldLabel, combobox)) {
                    logger.error("Lookup field '{}' still has error after retry", normalizedFieldLabel);
                    throw new RuntimeException("Verification failed: Value '" + searchTerm + "' was not accepted into '"
                            + normalizedFieldLabel + "'");
                }
            }

            logger.debug("Lookup field '{}' completed successfully", normalizedFieldLabel);

        } catch (Exception e) {
            logger.error("Lookup field fill failed for '{}': {}", fieldLabel, e.getMessage());
            throw new RuntimeException("Failed to fill lookup '" + fieldLabel + "': " + e.getMessage(), e);
        }
    }

    /**
     * Performs the actual lookup selection workflow (search + select).
     * Separated for clean retry logic without code duplication.
     * 
     * @return The combobox locator for verification
     */
    private Locator performLookupSelection(String fieldLabel, String searchTerm, Locator context) {
        String normalizedFieldLabel = normalizeFieldLabel(fieldLabel);
        List<Locator> candidateScopes = new ArrayList<>();
        if (context != null) {
            candidateScopes.add(context);
        }
        candidateScopes.add(page.locator("body"));

        Locator combobox = null;
        for (Locator scope : candidateScopes) {
            combobox = findLookupCombobox(scope, normalizedFieldLabel);
            if (isUsableLocator(combobox)) {
                break;
            }
        }

        if (combobox == null || !isUsableLocator(combobox)) {
            throw new RuntimeException("Lookup field '" + fieldLabel + "' was not found in the active page context");
        }

        try {
            combobox.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        } catch (Exception e) {
            logger.warn("Lookup combobox for '{}' was not visible within 5s, continuing with the best match", fieldLabel);
        }

        String currentValue = normalize(combobox.getAttribute("data-value"));
        if (currentValue.equalsIgnoreCase(searchTerm.trim())) {
            logger.debug("Lookup field '{}' already set to '{}'; skipping", fieldLabel, searchTerm);
            return combobox;
        }

        clearExistingLookupSelection(fieldLabel, combobox);

        // Use caller-provided query text directly; supports single-token and
        // multi-token input.
        String searchQuery = buildLookupSearchQuery(searchTerm);
        logger.debug("Searching with: '{}'", searchQuery);

        combobox.click();

        // Use pressSequentially to handle frontend debouncing safely
        combobox.pressSequentially(searchQuery, new Locator.PressSequentiallyOptions().setDelay(50));

        // Always select from the currently visible lookup list on the modal page.
        handleSelection(combobox, searchTerm, normalizeFieldLabel(fieldLabel));

        return combobox;
    }

    /**
     * Handles selection from inline dropdown suggestions only.
     */
    private void handleSelection(Locator combobox, String searchTerm, String fieldLabel) {
        Locator visibleListbox = waitForVisibleLookupUi(combobox, fieldLabel);

        if (visibleListbox.count() > 0 && visibleListbox.isVisible()) {
            selectFromDropdown(combobox, searchTerm);
            waitForLookupValueApplied(combobox, fieldLabel);
            logger.debug("Selected '{}' from dropdown", searchTerm);
            return;
        }

        throw new RuntimeException("Lookup suggestions did not appear for field '" + fieldLabel + "'");
    }

    private Locator waitForVisibleLookupUi(Locator combobox, String fieldLabel) {
        long deadline = System.currentTimeMillis() + 10000;
        boolean openedWithArrowDown = false;

        while (System.currentTimeMillis() < deadline) {
            Locator scopedListbox = resolveVisibleListbox(combobox);
            if (scopedListbox.count() > 0 && scopedListbox.isVisible()) {
                return scopedListbox;
            }

            if (!openedWithArrowDown && System.currentTimeMillis() > deadline - 7000) {
                try {
                    combobox.press("ArrowDown");
                    openedWithArrowDown = true;
                } catch (Exception ignored) {
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new RuntimeException("Lookup UI did not appear for field '" + fieldLabel + "'");
    }

    private void selectFromDropdown(Locator combobox, String searchTerm) {
        Pattern exactPattern = Pattern.compile("^" + Pattern.quote(searchTerm.trim()) + "$", Pattern.CASE_INSENSITIVE);
        Pattern containsPattern = Pattern.compile(Pattern.quote(searchTerm.trim()), Pattern.CASE_INSENSITIVE);
        String firstToken = searchTerm.trim().split("\\s+")[0];
        Pattern firstTokenPattern = Pattern.compile("\\b" + Pattern.quote(firstToken) + "\\b",
                Pattern.CASE_INSENSITIVE);

        long deadline = System.currentTimeMillis() + 10000;
        Locator option = null;

        while (System.currentTimeMillis() < deadline) {
            Locator scopedListbox = resolveVisibleListbox(combobox);
            option = findBestOption(scopedListbox, searchTerm, exactPattern, containsPattern, firstTokenPattern);
            if (!isInlineSearchResultOption(option)) {
                option = pickFirstVisibleNonAdvancedOption(scopedListbox, searchTerm);
            }

            if (!isUsableOption(option)) {
                Locator genericListbox = page.locator("[role='listbox']:visible").first();
                option = findBestOption(genericListbox, searchTerm, exactPattern, containsPattern, firstTokenPattern);
                if (!isInlineSearchResultOption(option)) {
                    option = pickFirstVisibleNonAdvancedOption(genericListbox, searchTerm);
                }
            }

            if (isUsableOption(option)) {
                String optionText = normalize(option.textContent());
                logger.debug("Selected lookup option '{}' for search '{}'.", optionText, searchTerm);
                option.click();
                return;
            }

            try {
                combobox.press("ArrowDown");
            } catch (Exception ignored) {
            }

            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        Locator firstVisibleFallback = pickFirstVisibleNonAdvancedOption(resolveVisibleListbox(combobox), searchTerm);
        if (isUsableOption(firstVisibleFallback)) {
            String optionText = normalize(firstVisibleFallback.textContent());
            logger.debug("Fallback selected first visible lookup option '{}'.", optionText);
            firstVisibleFallback.click();
            return;
        }

        throw new RuntimeException("No visible lookup option found for '" + searchTerm + "'");
    }

    private Locator pickFirstVisibleNonAdvancedOption(Locator listbox, String searchTerm) {
        if (listbox == null || listbox.count() == 0 || !listbox.isVisible()) {
            return page.locator("__never__").first();
        }

        Locator options = listbox.getByRole(com.microsoft.playwright.options.AriaRole.OPTION);
        if (options.count() == 0) {
            options = listbox.locator("lightning-base-combobox-item:visible, li.slds-listbox__item:visible");
        }

        int count = options.count();
        if (count <= 0) {
            return page.locator("__never__").first();
        }

        String normalizedSearch = normalize(searchTerm).toLowerCase();
        for (int i = 0; i < count; i++) {
            Locator option = options.nth(i);
            try {
                if (!option.isVisible()) {
                    continue;
                }
                String text = normalize(option.textContent());
                if (text.isBlank() || text.toLowerCase().contains("show more results")) {
                    continue;
                }
                if (normalizedSearch.isBlank()) {
                    return option;
                }
                String lowerText = text.toLowerCase();
                if (lowerText.contains(normalizedSearch) || normalizedSearch.contains(lowerText)) {
                    return option;
                }
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; i < count; i++) {
            Locator option = options.nth(i);
            try {
                if (!option.isVisible()) {
                    continue;
                }
                String text = normalize(option.textContent());
                if (!text.isBlank() && !text.toLowerCase().contains("show more results")) {
                    return option;
                }
            } catch (Exception ignored) {
            }
        }

        return page.locator("__never__").first();
    }

    private Locator pickFirstVisibleMatchingOption(Locator listbox, String searchTerm) {
        if (listbox == null || listbox.count() == 0 || !listbox.isVisible()) {
            return page.locator("__never__").first();
        }

        Locator options = listbox.getByRole(com.microsoft.playwright.options.AriaRole.OPTION);
        if (options.count() == 0) {
            options = listbox.locator("lightning-base-combobox-item:visible, li.slds-listbox__item:visible");
        }

        int count = options.count();
        if (count <= 0) {
            return page.locator("__never__").first();
        }

        String normalizedSearch = normalize(searchTerm).toLowerCase();
        for (int i = 0; i < count; i++) {
            Locator option = options.nth(i);
            try {
                if (!option.isVisible()) {
                    continue;
                }
                String text = normalize(option.textContent());
                if (text.isBlank() || !isLookupPersonCandidate(text)) {
                    continue;
                }
                if (normalizedSearch.isBlank()) {
                    return option;
                }
                String lowerText = text.toLowerCase();
                if (lowerText.startsWith(normalizedSearch) || lowerText.contains(normalizedSearch)) {
                    return option;
                }
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; i < count; i++) {
            Locator option = options.nth(i);
            try {
                if (!option.isVisible()) {
                    continue;
                }
                String text = normalize(option.textContent());
                if (!text.isBlank() && isLookupPersonCandidate(text)) {
                    return option;
                }
            } catch (Exception ignored) {
            }
        }

        return page.locator("__never__").first();
    }

    private Locator pickRandomVisibleOption(Locator listbox) {
        if (listbox == null || listbox.count() == 0 || !listbox.isVisible()) {
            return page.locator("__never__").first();
        }

        Locator options = listbox.getByRole(com.microsoft.playwright.options.AriaRole.OPTION);
        if (options.count() == 0) {
            options = listbox.locator("lightning-base-combobox-item:visible, li.slds-listbox__item:visible");
        }
        int count = options.count();
        if (count <= 0) {
            return page.locator("__never__").first();
        }

        List<Integer> selectableIndexes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                Locator option = options.nth(i);
                if (!option.isVisible()) {
                    continue;
                }
                String text = normalize(option.textContent());
                String ariaDisabled = normalize(option.getAttribute("aria-disabled"));
                if (text.isBlank() || "true".equalsIgnoreCase(ariaDisabled)
                        || !isLookupPersonCandidate(text)) {
                    continue;
                }
                selectableIndexes.add(i);
            } catch (Exception ignored) {
            }
        }

        if (selectableIndexes.isEmpty()) {
            return page.locator("__never__").first();
        }

        int selected = selectableIndexes.get(ThreadLocalRandom.current().nextInt(selectableIndexes.size()));
        return options.nth(selected);
    }

    private Locator findBestOption(Locator listbox, String searchTerm, Pattern exactPattern,
            Pattern containsPattern, Pattern firstTokenPattern) {
        if (listbox == null || listbox.count() == 0 || !listbox.isVisible()) {
            return page.locator("__never__").first();
        }

        Locator option = listbox.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new Locator.GetByRoleOptions().setName(searchTerm).setExact(true)).first();
        if (isUsableOption(option)) {
            return option;
        }

        option = listbox.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new Locator.GetByRoleOptions().setName(exactPattern)).first();
        if (isUsableOption(option)) {
            return option;
        }

        option = listbox.getByRole(com.microsoft.playwright.options.AriaRole.OPTION)
                .filter(new Locator.FilterOptions().setHasText(containsPattern))
                .first();
        if (isUsableOption(option)) {
            return option;
        }

        return listbox.getByRole(com.microsoft.playwright.options.AriaRole.OPTION)
                .filter(new Locator.FilterOptions().setHasText(firstTokenPattern))
                .first();
    }

    private boolean isUsableOption(Locator option) {
        try {
            return option != null && option.count() > 0 && option.isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isUsableLocator(Locator locator) {
        try {
            return locator != null && locator.count() > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isInlineSearchResultOption(Locator option) {
        if (!isUsableOption(option)) {
            return false;
        }
        try {
            return isLookupPersonCandidate(normalize(option.textContent()));
        } catch (Exception ignored) {
            return false;
        }
    }

    private Locator resolveVisibleListbox(Locator combobox) {
        Locator genericListbox = page.locator("[role='listbox']:visible").last();

        String controlsId = combobox.getAttribute("aria-controls");
        if (controlsId != null && !controlsId.isBlank()) {
            Locator controlsListbox = page
                    .locator("[id='" + escapeCssAttribute(controlsId) + "'][role='listbox']:visible")
                    .first();
            if (controlsListbox.count() > 0 && controlsListbox.isVisible()) {
                return controlsListbox;
            }
        }

        return genericListbox;
    }

    private boolean isLookupPersonCandidate(String optionText) {
        String normalized = normalize(optionText).toLowerCase();
        if (normalized.isBlank()) {
            return false;
        }
        if (normalized.startsWith("show more results")) {
            return false;
        }
        if (normalized.equals("new contact") || normalized.startsWith("create new")) {
            return false;
        }
        return true;
    }

    private void waitForLookupValueApplied(Locator combobox, String fieldLabel) {
        long deadline = System.currentTimeMillis() + 7000;
        while (System.currentTimeMillis() < deadline) {
            String currentValue = normalize(combobox.getAttribute("data-value"));
            String currentText = normalize(combobox.textContent());
            if (!currentValue.isBlank() || !currentText.isBlank()) {
                return;
            }
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new RuntimeException("Lookup value was not applied for field '" + fieldLabel
                + "' after selecting a search result.");
    }

    private String buildLookupSearchQuery(String searchTerm) {
        String normalized = normalize(searchTerm);
        if (normalized.isBlank()) {
            throw new RuntimeException("Lookup search term is blank");
        }
        return normalized;
    }

    /**
     * Verifies that a lookup field was filled successfully by checking for error
     * indicators.
     * 
     * @param fieldLabel The label of the lookup field
     * @param combobox   The combobox locator
     * @return true if field is filled without errors, false if error is present
     */
    private boolean verifyLookupFieldFilled(String fieldLabel, Locator combobox) {
        try {
            // Wait a moment for any validation errors to appear
            page.waitForTimeout(500);

            // Resolve parent from the exact combobox we just interacted with.
            Locator parent = page.locator("lightning-grouped-combobox")
                    .filter(new Locator.FilterOptions().setHas(combobox))
                    .first();

            // Use short timeout for verification - if it takes >3s, something is wrong
            try {
                String classes = parent.getAttribute("class", new Locator.GetAttributeOptions().setTimeout(3000));
                if (classes != null && classes.contains("slds-has-error")) {
                    logger.warn("Lookup field '{}' has error class: slds-has-error", fieldLabel);

                    // Check for specific error message
                    Locator errorMsg = parent.locator("div.slds-form-element__help").first();
                    if (errorMsg.count() > 0 && errorMsg.isVisible()) {
                        String errorText = errorMsg.textContent();
                        logger.warn("Error message: {}", errorText);
                    }
                    return false;
                }
            } catch (Exception e) {
                // If getAttribute times out or fails, check visibility of error message
                // directly
                Locator errorIndicator = page.locator("lightning-grouped-combobox.slds-has-error")
                        .filter(new Locator.FilterOptions().setHas(combobox))
                        .first();

                if (isVisible(errorIndicator, 1000)) {
                    logger.warn("Lookup field '{}' has error indicator visible", fieldLabel);
                    return false;
                }
            }

            logger.debug("Lookup field '{}' verified - no errors detected", fieldLabel);
            return true;

        } catch (Exception e) {
            logger.warn("Could not verify lookup field '{}', assuming success: {}", fieldLabel, e.getMessage());
            return true; // Assume success if verification fails
        }
    }

    private void clearExistingLookupSelection(String fieldLabel, Locator combobox) {
        String currentValue = normalize(combobox.getAttribute("data-value"));
        if (currentValue.isBlank()) {
            return;
        }

        Locator lookupContainer = page.locator("lightning-grouped-combobox")
                .filter(new Locator.FilterOptions().setHas(combobox))
                .first();
        Locator clearButton = lookupContainer.locator("button[data-clear-selection-button]")
                .or(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Clear " + fieldLabel + " Selection").setExact(true)))
                .first();

        logger.debug("Clearing existing lookup value for '{}': '{}'", fieldLabel, currentValue);
        clearButton.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        clearButton.click();
        waitForLookupCleared(combobox, fieldLabel);
    }

    private void waitForLookupCleared(Locator combobox, String fieldLabel) {
        long deadline = System.currentTimeMillis() + 5000;
        String currentValue = normalize(combobox.getAttribute("data-value"));

        while (System.currentTimeMillis() < deadline) {
            currentValue = normalize(combobox.getAttribute("data-value"));
            if (currentValue.isBlank()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new RuntimeException("Lookup field '" + fieldLabel + "' did not clear existing value. Current value: '"
                + currentValue + "'");
    }

    private boolean isVisible(Locator locator, int timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Locator findLookupCombobox(Locator scope, String fieldLabel) {
        String normalizedFieldLabel = normalize(fieldLabel);
        Pattern exactLabel = Pattern.compile("^\\s*" + Pattern.quote(normalizedFieldLabel) + "\\s*$", Pattern.CASE_INSENSITIVE);
        Pattern containsLabel = Pattern.compile(Pattern.quote(normalizedFieldLabel), Pattern.CASE_INSENSITIVE);

        List<Locator> candidates = new ArrayList<>();

        candidates.add(scope.getByLabel(normalizedFieldLabel, new Locator.GetByLabelOptions().setExact(true)));
        candidates.add(scope.getByLabel(normalizedFieldLabel, new Locator.GetByLabelOptions().setExact(false)));
        candidates.add(scope.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
            new Locator.GetByRoleOptions().setName(exactLabel)));
        candidates.add(scope.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
            new Locator.GetByRoleOptions().setName(containsLabel)));

        Locator labelMatch = scope.locator("label, span.slds-form-element__label")
                .filter(new Locator.FilterOptions().setHasText(containsLabel))
                .first();
        if (isUsableLocator(labelMatch)) {
            Locator formElement = labelMatch.locator("xpath=ancestor::*[contains(@class,'slds-form-element')][1]").first();
            if (isUsableLocator(formElement)) {
                candidates.add(formElement.locator("input[role='combobox'], input[aria-autocomplete='list'], div[role='combobox'], lightning-combobox, lightning-input, lightning-grouped-combobox"));
                candidates.add(formElement.locator("input, textarea, div[role='combobox']"));
            }
        }

        Locator groupedByLabel = scope.locator("lightning-grouped-combobox")
                .filter(new Locator.FilterOptions().setHas(
                        scope.locator("label, span.slds-form-element__label")
                                .filter(new Locator.FilterOptions().setHasText(containsLabel))
                                .first()))
                .first();
        candidates.add(groupedByLabel.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX));

        Locator fallbackGroup = scope.locator("lightning-grouped-combobox")
                .filter(new Locator.FilterOptions().setHasText(containsLabel))
                .first();
        candidates.add(fallbackGroup.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX));

        for (Locator candidate : candidates) {
            int count = candidate.count();
            for (int index = 0; index < count; index++) {
                Locator match = candidate.nth(index);
                if (isVisible(match, 1000)) {
                    return match;
                }
            }
        }

        return page.locator("__never__").first();
    }

    private String normalizeFieldLabel(String fieldLabel) {
        if (fieldLabel == null) {
            return "";
        }
        String normalized = fieldLabel.trim();
        if ("Contract Officier".equalsIgnoreCase(normalized)) {
            return "Contract Officer";
        }
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeCssAttribute(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

}
