package com.macsuite.common;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.config.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class FormUtils {
    private static final Logger logger = LoggerFactory.getLogger(FormUtils.class);

    private final Page page;
    private final DropdownUtils dropdownUtils;
    private final LookupUtils lookupUtils;
    private final RichTextUtils richTextUtils;

    public FormUtils(Page page) {
        this.page = page;
        this.dropdownUtils = new DropdownUtils(page);
        this.lookupUtils = new LookupUtils(page);
        this.richTextUtils = new RichTextUtils(page);
    }

    public boolean verifySuccessMessage() {
        logger.info("Verifying success message for record creation");
        try {
            // Wait for success toast notification (Salesforce uses slds-theme--success)
            Locator toast = page.locator(".slds-theme--success, .slds-notify--toast")
                    .filter(new Locator.FilterOptions().setHasText("was created"));
            toast.waitFor(new Locator.WaitForOptions().setTimeout(10000));

            String messageText = toast.textContent();
            logger.info("✓ Success message displayed: {}", messageText);
            return true;
        } catch (Exception e) {
            logger.error("Success message verification failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractRecordNumberFromSuccessMessage() {
        logger.info("Extracting record number from success message");
        try {
            // Wait for success toast notification
            Locator toast = page.locator(".slds-theme--success, .slds-notify--toast")
                    .filter(new Locator.FilterOptions().setHasText("was created"));
            toast.waitFor(new Locator.WaitForOptions().setTimeout(10000));

            String messageText = toast.textContent();
            logger.info("Success message: {}", messageText);

            // Generic pattern to extract any text between quotes
            // Matches: 'Something "RECORD-ID" was created'
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"([^\"]+)\"")
                    .matcher(messageText);

            if (matcher.find()) {
                String recordNumber = matcher.group(1);
                logger.info("✓ Extracted record number: {}", recordNumber);
                return recordNumber;
            }
        } catch (Exception e) {
            logger.error("Record number extraction failed: {}", e.getMessage());
        }
        return null;
    }

    // ─── Context resolution ───────────────────────────────────────────────────

    private Locator getContext() {
        // Check for active modals, but filter out lookup modals which can linger
        // Includes role=dialog and quick-action wrappers so Lightning quick actions
        // (e.g. "Related Inquiry") resolve to their own modal instead of falling back to body.
        Locator activeModals = page.locator(
                "[role='dialog']:visible, .slds-modal:visible, .isModal.inlinePanel:visible, "
                        + ".runtime_platform_actionsQuickActionWrapper:visible");

        for (int i = activeModals.count() - 1; i >= 0; i--) {
            Locator modal = activeModals.nth(i);
            try {
                // Skip if this is a lookup modal (has Select button)
                Locator selectButton = modal.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Select").setExact(true)).first();
                if (selectButton.count() > 0) {
                    continue; // Skip lookup modals
                }

                // Use this modal as context if it's truly visible
                if (modal.isVisible()) {
                    return modal;
                }
            } catch (Exception ignored) {
            }
        }

        return page.locator("body");
    }

    // ─── Section-scoped filling ───────────────────────────────────────────────

    public void fillFieldsInSection(String sectionName, Map<String, String> formData) {
        
        logger.debug("Filling section '{}' with {} fields", sectionName, formData.size());
        Locator sectionContext = resolveSectionContext(sectionName);
        formData.forEach((fieldLabel, value) -> fillField(fieldLabel, value, sectionContext));
    }

    private Locator resolveSectionContext(String sectionName) {
        String escapedSectionName = escapeForCssAttribute(sectionName);
        Locator sectionHeader = page.locator("h3.slds-section__title span[title='" + escapedSectionName + "']").first();
        sectionHeader.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        sectionHeader.scrollIntoViewIfNeeded();

        Locator sectionContainer = sectionHeader.locator("xpath=ancestor::div[contains(@class,'slds-section')][1]")
                .first();
        sectionContainer.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        Locator visibleContent = sectionContainer.locator(".section-content[aria-hidden='false']").first();
        return visibleContent.count() > 0 ? visibleContent : sectionContainer;
    }

    // ─── Public field API ─────────────────────────────────────────────────────

    public void fillField(String fieldLabel, String value) {
        fillField(fieldLabel, value, getContext());
    }

    private void fillField(String fieldLabel, String value, Locator context) {
        String resolvedFieldLabel = normalizeFieldLabel(fieldLabel);
        String resolvedValue = TemplateValueResolver.resolve(value);

        if (tryFillForcedField(resolvedFieldLabel, resolvedValue, context))
            return;
        if (tryFillTimePickerField(resolvedFieldLabel, resolvedValue, context))
            return;
        if (tryFillResolvedLabeledField(resolvedFieldLabel, resolvedValue, context))
            return;
        if (tryFillLightningFallbackField(resolvedFieldLabel, resolvedValue, context))
            return;

        String errorMsg = "Field '" + fieldLabel + "' not found or not visible. " +
                "Please check if the label matches exactly what's shown in the UI.";
        logger.error(errorMsg);
        throw new IllegalArgumentException(errorMsg);
    }

    public void fillRichTextField(String fieldLabel, String value) {
        richTextUtils.fillRichTextField(fieldLabel, value, getContext());
    }

    public void fillRichTextField(String fieldLabel, String dataId, String value) {
        richTextUtils.fillRichTextField(fieldLabel, dataId, value, getContext());
    }

    // ─── Field fill strategies ────────────────────────────────────────────────

    private boolean tryFillForcedField(String fieldLabel, String value, Locator context) {
        if (isForcedLookupField(fieldLabel)) {
            lookupUtils.fillLookupField(fieldLabel, value, context);
            return true;
        }
        if (isForcedDropdownField(fieldLabel)) {
            dropdownUtils.selectDropdown(fieldLabel, value, context);
            return true;
        }
        return false;
    }

    private boolean tryFillTimePickerField(String fieldLabel, String value, Locator context) {
        try {
            String escapedLabel = escapeForCssAttribute(fieldLabel);
            Locator timeInput = context
                    .locator("input[aria-label='" + escapedLabel + "'][role='combobox'][aria-autocomplete='list']")
                    .first();
            if (timeInput.count() == 0 || !timeInput.isVisible())
                return false;

            timeInput.scrollIntoViewIfNeeded();
            highlightElement(timeInput, fieldLabel);
            timeInput.fill(value);
            timeInput.press("Tab");
            waitForInputValue("input[aria-label='" + escapedLabel + "'][role='combobox']", value, 5000);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean tryFillResolvedLabeledField(String fieldLabel, String value, Locator context) {
        try {
            Locator field = context.getByLabel(fieldLabel).first();

            // Use shorter timeout for context check - avoid wasting time on stale context
            try {
                field.waitFor(new Locator.WaitForOptions().setTimeout(1000));
            } catch (Exception e) {
                // Field not found in context - might be in wrong context, let specific handlers
                // try
                return false;
            }

            if (field.count() == 0 || !field.isVisible())
                return false;

            String role = attributeOrEmpty(field, "role");
            String tagName = ((String) field.evaluate("node => node.tagName")).toLowerCase();

            if ("combobox".equals(role)) {
                if (isLookupLikeField(field)) {
                    lookupUtils.fillLookupField(fieldLabel, value, context);
                } else {
                    dropdownUtils.selectDropdown(fieldLabel, value, context);
                }
                return true;
            }

            field.scrollIntoViewIfNeeded();
            highlightElement(field, fieldLabel);
            field.fill(value);
            waitForFieldValue(fieldLabel, value, tagName);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean tryFillLightningFallbackField(String fieldLabel, String value, Locator context) {
        try {
            Locator inputField = context
                    .locator(
                            ".slds-form-element, lightning-input, lightning-textarea, lightning-input-rich-text, lightning-textarea-rich-text, lightning-datepicker, records-record-layout-base-input")
                    .filter(new Locator.FilterOptions().setHasText(fieldLabel))
                    .locator("input, textarea, div[contenteditable='true']")
                    .first();

            // Use shorter timeout to avoid wasting time on stale context
            try {
                inputField.waitFor(new Locator.WaitForOptions().setTimeout(1000));
            } catch (Exception e) {
                return false;
            }

            if (inputField.count() == 0 || !inputField.isVisible())
                return false;

            inputField.scrollIntoViewIfNeeded();
            highlightElement(inputField, fieldLabel);
            String tagName = ((String) inputField.evaluate("node => node.tagName")).toLowerCase();
            if ("div".equals(tagName)) {
                inputField.click();
                inputField.fill("");
                inputField.pressSequentially(value, new Locator.PressSequentiallyOptions().setDelay(5));
            } else {
                inputField.fill(value);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void highlightElement(Locator locator, String label) {
        if (!TestConfig.isHighlightEnabled())
            return;
        try {
            locator.scrollIntoViewIfNeeded();
            String originalStyle = (String) locator.evaluate("el => el.getAttribute('style')");
            locator.evaluate("el => {" +
                    "el.style.border = '3px solid #4ECDC4';" +
                    "el.style.backgroundColor = '#FFE66D';" +
                    "el.style.boxShadow = '0 0 10px 2px #4ECDC4';" +
                    "}");
            if (label != null)
                logger.debug("→ Highlighted: {}", label);
            try {
                Thread.sleep(TestConfig.HIGHLIGHT_DURATION);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            if (originalStyle != null) {
                locator.evaluate("el => el.setAttribute('style', '" + originalStyle + "')");
            } else {
                locator.evaluate("el => el.removeAttribute('style')");
            }
        } catch (Exception e) {
            logger.debug("Could not highlight element: {}", e.getMessage());
        }
    }

    private void waitForInputValue(String cssSelector, String expectedValue, int timeoutMs) {
        page.waitForFunction(
                "([selector, expected]) => { const el = document.querySelector(selector); return !!el && (el.value || '') === expected; }",
                new Object[] { cssSelector, expectedValue },
                new Page.WaitForFunctionOptions().setTimeout(timeoutMs));
    }

    private void waitForFieldValue(String fieldLabel, String expectedValue, String tagName) {
        String escapedLabel = escapeForCssAttribute(fieldLabel);
        if ("textarea".equals(tagName)) {
            waitForInputValue("textarea[aria-label='" + escapedLabel + "']", expectedValue, 5000);
        } else {
            waitForInputValue("input[aria-label='" + escapedLabel + "']", expectedValue, 5000);
        }
    }

    private String escapeForCssAttribute(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private String attributeOrEmpty(Locator locator, String attributeName) {
        String value = locator.getAttribute(attributeName);
        return value == null ? "" : value;
    }

    private String normalizeFieldLabel(String fieldLabel) {
        if ("Vendor Size".equalsIgnoreCase(fieldLabel)) {
            return "Business Classification";
        }
        if ("Contract Officier".equalsIgnoreCase(fieldLabel)) {
            return "Contract Officer";
        }
        return fieldLabel;
    }

    private boolean isForcedLookupField(String resolvedFieldLabel) {
        return "Contract Officer".equalsIgnoreCase(resolvedFieldLabel)
                || "Contract Specialist".equalsIgnoreCase(resolvedFieldLabel)
                || "Vendor".equalsIgnoreCase(resolvedFieldLabel)
                || "Vendor Point of Contact".equalsIgnoreCase(resolvedFieldLabel)
                || "Contract Officer Representative".equalsIgnoreCase(resolvedFieldLabel)
                || "Triager".equalsIgnoreCase(resolvedFieldLabel)
                || "Triager (Additional)".equalsIgnoreCase(resolvedFieldLabel)
                || "SME".equalsIgnoreCase(resolvedFieldLabel)
                || "Senior Reviewer / Technical Director".equalsIgnoreCase(resolvedFieldLabel);
    }

    private boolean isForcedDropdownField(String resolvedFieldLabel) {
        return "Business Classification".equalsIgnoreCase(resolvedFieldLabel)
                || "Vendor Vehicle".equalsIgnoreCase(resolvedFieldLabel);
    }

    private boolean isLookupLikeField(Locator field) {
        String ariaAutocomplete = attributeOrEmpty(field, "aria-autocomplete");
        String placeholder = attributeOrEmpty(field, "placeholder").toLowerCase();
        if (!"list".equalsIgnoreCase(ariaAutocomplete)) {
            return false;
        }
        if (placeholder.contains("search")) {
            return true;
        }
        try {
            Object result = field.evaluate("node => !!node.closest('[data-lookup]')");
            return Boolean.TRUE.equals(result);
        } catch (Exception ignored) {
            return false;
        }
    }

}
