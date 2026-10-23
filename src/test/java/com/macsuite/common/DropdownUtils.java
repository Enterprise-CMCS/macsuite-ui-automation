package com.macsuite.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.config.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

public class DropdownUtils {
    private static final Logger logger = LoggerFactory.getLogger(DropdownUtils.class);

    private final Page page;

    public DropdownUtils(Page page) {
        this.page = page;
    }

    public void selectDropdown(String fieldName, String optionText) {
        selectDropdown(fieldName, optionText, page.locator("body"));
    }

    public void selectDropdown(String fieldName, String optionText, Locator context) {
        logger.debug("Selecting '{}' from dropdown '{}'", optionText, fieldName);
        try {
            // P1: Try COMBOBOX role with exact name in context (most specific)
            Locator combobox = context.getByRole(AriaRole.COMBOBOX,
                    new Locator.GetByRoleOptions().setName(fieldName).setExact(true)).first();

            // P2: Try label with exact match in context
            if (combobox.count() == 0) {
                combobox = context.getByLabel(fieldName, new Locator.GetByLabelOptions().setExact(true))
                        .first();
            }

            // P3: Try button with role=combobox for Lightning dropdowns
            if (combobox.count() == 0) {
                combobox = context.locator(
                        "button[role='combobox'][aria-label*='" + fieldName + "']").first();
            }

            // P4: Try input elements with partial aria-label match (exclude table headers)
            if (combobox.count() == 0) {
                combobox = context.locator(
                        "input[aria-label*='" + fieldName + "'], " +
                                "button[aria-label*='" + fieldName + "'][role='combobox']")
                        .first();
            }

            // P5: Fallback to page-level COMBOBOX role search
            if (combobox.count() == 0) {
                combobox = page.getByRole(AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName(fieldName).setExact(true)).first();
            }

            // P6: Final fallback to page-level exact label
            if (combobox.count() == 0) {
                combobox = page.getByLabel(fieldName, new Page.GetByLabelOptions().setExact(true)).first();
            }

            selectOptionFromCombobox(combobox, fieldName, optionText);
        } catch (Exception e) {
            logger.error("Dropdown selection failed for '{}': {}", fieldName, e.getMessage());
            throw new RuntimeException("Failed to select '" + fieldName + "': " + e.getMessage(), e);
        }
    }

    public void selectDropdown(Locator combobox, String fieldName, String optionText) {
        logger.debug("Selecting '{}' from dropdown '{}'", optionText, fieldName);
        try {
            selectOptionFromCombobox(combobox, fieldName, optionText);
        } catch (Exception e) {
            logger.error("Dropdown selection failed for '{}': {}", fieldName, e.getMessage());
            throw new RuntimeException("Failed to select '" + fieldName + "': " + e.getMessage(), e);
        }
    }

    private void selectOptionFromCombobox(Locator combobox, String fieldName, String optionText) {
        combobox.waitFor(new Locator.WaitForOptions().setTimeout(5000));

        String tagName = String.valueOf(combobox.evaluate("element => element.tagName"));
        if ("SELECT".equalsIgnoreCase(tagName)) {
            combobox.scrollIntoViewIfNeeded();
            highlightElement(combobox, fieldName);
            combobox.selectOption(new SelectOption().setLabel(optionText));
            logger.debug("Successfully selected '{}' from native dropdown '{}'", optionText, fieldName);
            return;
        }

        if (alreadySelected(combobox, optionText)) {
            logger.debug("Dropdown '{}' already set to '{}'; skipping", fieldName, optionText);
            return;
        }

        combobox.scrollIntoViewIfNeeded();
        highlightElement(combobox, fieldName);
        combobox.click(new Locator.ClickOptions().setTimeout(3000));
        findDropdownOption(optionText, combobox)
                .click(new Locator.ClickOptions().setTimeout(5000));

        logger.debug("Successfully selected: {}", optionText);
    }

    private boolean alreadySelected(Locator combobox, String optionText) {
        String currentValue = combobox.getAttribute("data-value");
        String currentText = combobox.textContent();
        return equalsIgnoreCase(currentValue, optionText) || equalsIgnoreCase(currentText, optionText);
    }

    private Locator findDropdownOption(String optionText, Locator combobox) {
        Locator listbox = resolveListbox(combobox);
        Locator option = listbox.getByRole(AriaRole.OPTION,
                new Locator.GetByRoleOptions().setName(optionText).setExact(true)).first();
        if (option.count() == 0) {
            option = listbox.getByRole(AriaRole.OPTION,
                    new Locator.GetByRoleOptions().setName(optionText)).first();
        }
        if (option.count() == 0) {
            option = listbox.locator("lightning-base-combobox-item:visible, li.slds-listbox__item:visible")
                .filter(new Locator.FilterOptions().setHasText(optionText))
                .first();
        }
        if (option.count() == 0) {
            option = page.locator("[role='option']:visible")
                    .filter(new Locator.FilterOptions().setHasText(optionText))
                    .first();
        }

        option.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        return option;
    }

    private Locator resolveListbox(Locator combobox) {
        String controlsId = combobox.getAttribute("aria-controls");
        Locator listbox;
        if (controlsId != null && !controlsId.isBlank()) {
            listbox = page.locator("[id='" + escapeCssAttribute(controlsId) + "'][role='listbox']:visible").first();
            if (listbox.count() > 0) {
                return waitForListbox(listbox, combobox);
            }
        }

        listbox = page.locator("[role='listbox']:visible").last();
        return waitForListbox(listbox, combobox);
    }

    private Locator waitForListbox(Locator listbox, Locator combobox) {
        try {
            listbox.waitFor(new Locator.WaitForOptions().setTimeout(4000));
        } catch (Exception firstOpenError) {
            combobox.press("ArrowDown");
            listbox.waitFor(new Locator.WaitForOptions().setTimeout(4000));
        }
        return listbox;
    }

    private String escapeCssAttribute(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private boolean equalsIgnoreCase(String actual, String expected) {
        return actual != null && actual.trim().equalsIgnoreCase(expected.trim());
    }

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

}
