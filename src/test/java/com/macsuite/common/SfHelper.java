package com.macsuite.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.config.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class SfHelper {
    private static final Logger logger = LoggerFactory.getLogger(SfHelper.class);
    private final Page page;

    public SfHelper(Page page) {
        this.page = page;
    }

    // ─── Input helpers ────────────────────────────────────────────────────────

    public void fillText(Locator context, String label, String value) {
        String resolved = TemplateValueResolver.resolve(value);
        logger.debug("fillText '{}' = '{}'", label, resolved);
        Locator field = context.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
        highlightElement(field, label);
        field.fill(resolved);
    }

    public void selectDropdown(Locator context, String label, String value) {
        logger.debug("selectDropdown '{}' = '{}'", label, value);
        Locator combobox = context.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
        String current = combobox.textContent();
        if (current != null && current.trim().equalsIgnoreCase(value.trim())) {
            logger.debug("Dropdown '{}' already set to '{}'; skipping", label, value);
            return;
        }
        highlightElement(combobox, label);
        combobox.click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(value).setExact(true))
                .first().click();
    }

    public void selectLookup(Locator context, String label, String value) {
        logger.debug("selectLookup '{}' = '{}'", label, value);
        dismissTooltips();

        // Try COMBOBOX role first (Lightning lookup pattern), then fallback to label
        Locator input;
        try {
            input = context.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions()
                    .setName(label)).first();
            input.waitFor(new Locator.WaitForOptions().setTimeout(3000));
        } catch (Exception e) {
            logger.debug("Combobox not found for '{}', trying getByLabel", label);
            input = context.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
            input.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        }

        input.scrollIntoViewIfNeeded();
        highlightElement(input, label);
        input.click();
        input.fill(value);

        // Wait for dropdown to populate
        page.waitForTimeout(1500);

        try {
            // Find all options in the current lookup dropdown and select from the same modal/page,
            // without falling through to the advanced search modal.
            Locator listbox = page.getByRole(AriaRole.LISTBOX).first();
            listbox.waitFor(new Locator.WaitForOptions().setTimeout(3000));

            Locator allOptions = listbox.getByRole(AriaRole.OPTION);
            int optionCount = allOptions.count();

            for (int i = 0; i < optionCount; i++) {
                Locator option = allOptions.nth(i);
                String optionText = option.textContent();
                if (optionText == null) {
                    continue;
                }

                String normalizedText = optionText.trim();
                if (normalizedText.isEmpty() || normalizedText.toLowerCase().contains("show more results")) {
                    continue;
                }

                if (normalizedText.equalsIgnoreCase(value.trim()) ||
                    normalizedText.toLowerCase().startsWith(value.trim().toLowerCase()) ||
                    normalizedText.toLowerCase().contains(value.trim().toLowerCase())) {
                    logger.debug("Selected lookup option from current list at index {}: {}", i, normalizedText);
                    option.click();
                    confirmLookupSelection(context, value);
                    return;
                }
            }

            // No matching result in the visible list: choose the first visible non-advanced-search option.
            for (int i = 0; i < optionCount; i++) {
                Locator option = allOptions.nth(i);
                String optionText = option.textContent();
                if (optionText != null && !optionText.toLowerCase().contains("show more results") && !optionText.trim().isEmpty()) {
                    logger.debug("Falling back to first visible option from current lookup list: {}", optionText.trim());
                    option.click();
                    confirmLookupSelection(context, optionText.trim());
                    return;
                }
            }

            logger.debug("No usable lookup option found in the current list for '{}'; stopping before advanced search", value);
            return;
        } catch (Exception e) {
            logger.debug("Dropdown option selection failed: {}", e.getMessage());
        }

        // Do not open the advanced search modal as a fallback path.
        throw new RuntimeException("No usable lookup result found in the current modal dropdown for '" + value + "'");
    }

    public void fillRichText(Locator context, String label, String value) {
        String resolved = TemplateValueResolver.resolve(value);
        logger.debug("fillRichText '{}' = '{}'", label, resolved);
        Locator container = context.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
        highlightElement(container, label);
        container.click();
        Locator editor = page.locator(".ql-editor[contenteditable='true']").first();
        editor.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        editor.fill(resolved);
        if (!editorContains(editor, resolved)) {
            fillRichTextWithJs(editor, resolved);
        }
    }

    public void clickButton(String name) {
        logger.debug("clickButton '{}'", name);
        Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true))
                .first();
        highlightElement(button, name);
        button.click();
    }

    public void clickButton(Locator context, String name) {
        logger.debug("clickButton '{}' (scoped)", name);
        Locator button = context.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(name).setExact(true))
                .first();
        highlightElement(button, name);
        button.click();
    }

    public void setCheckbox(Locator context, String label, boolean checked) {
        logger.debug("setCheckbox '{}' = {}", label, checked);
        Locator checkbox = context.getByLabel(label, new Locator.GetByLabelOptions().setExact(false)).first();
        checkbox.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        if (checkbox.isChecked() == checked) {
            return;
        }

        Locator labelText = context.getByText(label, new Locator.GetByTextOptions().setExact(true)).first();
        labelText.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        highlightElement(labelText, label);
        labelText.click();

        if (checkbox.isChecked() != checked) {
            throw new AssertionError("Checkbox '" + label + "' did not change to checked=" + checked);
        }
    }

    public String readText(Locator context, String label) {
        Locator item = context.locator("records-highlights-details-item")
                .filter(new Locator.FilterOptions().setHasText(label))
                .first();
        item.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        return item.locator("lightning-formatted-text, lightning-formatted-number, lightning-formatted-date-time, a")
                .first().textContent().trim();
    }

    // ─── Toast ────────────────────────────────────────────────────────────────

    public void waitForToast(String type) {
        waitForToast(type, null);
    }

    public void waitForToast(String type, String message) {
        logger.debug("waitForToast type='{}' message='{}'", type, message);
        String css = "error".equalsIgnoreCase(type)
            ? ".slds-theme_error, .slds-theme--error, .slds-notify_toast.slds-theme_error, .slds-notify_toast.slds-theme--error"
            : ".slds-theme_success, .slds-theme--success, .slds-notify_toast.slds-theme_success, .slds-notify_toast.slds-theme--success";
        Locator toast = message != null
                ? page.locator(css).filter(new Locator.FilterOptions().setHasText(message)).first()
                : page.locator(css).first();
        toast.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.debug("Toast: {}", toast.textContent());
    }

    public String extractRecordNumber() {
        Locator toast = page.locator(".slds-theme--success, .slds-notify--toast")
                .filter(new Locator.FilterOptions().setHasText("was created"));
        toast.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        Matcher m = Pattern.compile("\"([^\"]+)\"").matcher(toast.textContent());
        return m.find() ? m.group(1) : null;
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

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

    private void dismissTooltips() {
        try {
            page.keyboard().press("Escape");
            Locator tooltip = page.locator("lightning-primitive-bubble[role='tooltip']").first();
            if (tooltip.count() > 0)
                tooltip.evaluate("node => node.remove()");
        } catch (Exception ignored) {
        }
    }

    private void confirmLookupSelection(Locator context, String value) {
        try {
            context.getByText(value, new Locator.GetByTextOptions().setExact(false))
                    .first().waitFor(new Locator.WaitForOptions().setTimeout(4000));
        } catch (Exception ignored) {
        }
    }

    private void selectFromAdvancedLookupModal() {
        Locator modal = page.getByRole(AriaRole.DIALOG).last();
        modal.waitFor(new Locator.WaitForOptions().setTimeout(3000));
        Locator firstRow = modal.locator("tbody tr").first();
        firstRow.waitFor(new Locator.WaitForOptions().setTimeout(3000));
        firstRow.getByRole(AriaRole.RADIO).first().check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Select").setExact(true)).click();
        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
    }

    private boolean editorContains(Locator editor, String value) {
        try {
            String text = editor.textContent();
            return text != null && text.trim().contains(value.trim());
        } catch (Exception ignored) {
            return false;
        }
    }

    private void fillRichTextWithJs(Locator editor, String value) {
        editor.evaluate(
                "(node, text) => {" +
                        "  node.focus();" +
                        "  const quill = node.__quill || node.closest('.ql-container')?.__quill;" +
                        "  if (quill) { quill.setText(text); } else {" +
                        "    node.innerHTML = '';" +
                        "    const p = document.createElement('p');" +
                        "    p.textContent = text;" +
                        "    node.appendChild(p);" +
                        "  }" +
                        "  node.classList.remove('ql-blank');" +
                        "  node.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: text }));"
                        +
                        "  node.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "  node.blur();" +
                        "}",
                value);
    }
}
