package com.macsuite.common;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RichTextUtils {
    private static final Logger logger = LoggerFactory.getLogger(RichTextUtils.class);

    public RichTextUtils(Page page) {
    }

    public void fillRichTextField(String fieldLabel, String value, Locator context) {
        fillRichTextField(fieldLabel, null, value, context);
    }

    public void fillRichTextField(String fieldLabel, String dataId, String value, Locator context) {
        String resolvedValue = TemplateValueResolver.resolve(value);
        activateRichTextField(fieldLabel, context);
        Locator editor = findRichTextEditor(fieldLabel, dataId, context);

        editor.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.ATTACHED)
            .setTimeout(10000));
        editor.scrollIntoViewIfNeeded();

        try {
            if (editor.isVisible()) {
                editor.click(new Locator.ClickOptions().setTimeout(5000));
                editor.fill("");
                editor.pressSequentially(resolvedValue, new Locator.PressSequentiallyOptions().setDelay(5));
            } else {
                fillWithJavaScript(editor, resolvedValue);
            }
        } catch (Exception fillError) {
            logger.debug("Direct rich text type failed for '{}': {}", fieldLabel, fillError.getMessage());
            fillWithJavaScript(editor, resolvedValue);
        }

        if (!isValuePresent(editor, resolvedValue)) {
            fillWithJavaScript(editor, resolvedValue);
        }
        if (!isValuePresent(editor, resolvedValue)) {
            throw new RuntimeException("Failed to fill rich text field '" + fieldLabel + "'");
        }

        logger.debug("Successfully filled rich text field '{}'", fieldLabel);
    }

    private void activateRichTextField(String fieldLabel, Locator context) {
        Locator ariaContainer = getRichTextAriaContainer(fieldLabel, context);
        if (ariaContainer.count() == 0) {
            throw new RuntimeException("Rich text container not found for '" + fieldLabel + "'");
        }
        ariaContainer.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.ATTACHED)
            .setTimeout(8000));
        ariaContainer.scrollIntoViewIfNeeded();

        try {
            if (ariaContainer.isVisible()) {
                ariaContainer.click(new Locator.ClickOptions().setTimeout(5000));
            } else {
                ariaContainer.evaluate("node => node.click()");
            }
        } catch (Exception activationError) {
            logger.debug("Rich text container click fallback for '{}': {}", fieldLabel, activationError.getMessage());
            ariaContainer.evaluate("node => node.click()");
        }
    }

    public Locator getRichTextAriaContainer(String fieldLabel, Locator context) {
        String escapedFieldLabel = fieldLabel.replace("\\", "\\\\").replace("'", "\\'");
        return context.locator("div[aria-label='" + escapedFieldLabel + "']").first();
    }

    private Locator findRichTextEditor(String fieldLabel, String dataId, Locator context) {
        Locator editorFromAriaContainer = getRichTextAriaContainer(fieldLabel, context)
            .locator("div.ql-editor[contenteditable='true']");
        if (editorFromAriaContainer.count() > 0) {
            return editorFromAriaContainer.first();
        }

        if (dataId != null && !dataId.isBlank()) {
            String escapedDataId = dataId.replace("\\", "\\\\").replace("'", "\\'");
            Locator editorByDataId = context
                .locator("lightning-input-rich-text[data-id='" + escapedDataId + "'] div.ql-editor[contenteditable='true']");
            if (editorByDataId.count() > 0) {
                return editorByDataId.first();
            }
        }

        logger.debug("Using generic rich text editor fallback for '{}'", fieldLabel);
        return context.locator("div.ql-editor[contenteditable='true']").first();
    }

    private void fillWithJavaScript(Locator editor, String value) {
        editor.evaluate(
            "(node, text) => {"
                + "node.focus();"
                + "const quill = node.__quill || node.closest('.ql-container')?.__quill;"
                + "if (quill) {"
                + "  quill.setText(text);"
                + "} else {"
                + "  node.innerHTML = '';"
                + "  const paragraph = document.createElement('p');"
                + "  paragraph.textContent = text;"
                + "  node.appendChild(paragraph);"
                + "}"
                + "node.classList.remove('ql-blank');"
                + "node.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: text }));"
                + "node.dispatchEvent(new Event('change', { bubbles: true }));"
                + "node.blur();"
                + "}",
            value
        );
    }

    private boolean isValuePresent(Locator editor, String expectedValue) {
        try {
            editor.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(3000));
            String text = editor.textContent();
            return text != null && text.trim().contains(expectedValue.trim());
        } catch (Exception ignored) {
            return false;
        }
    }
}
