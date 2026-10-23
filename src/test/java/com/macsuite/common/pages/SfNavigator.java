package com.macsuite.common.pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class SfNavigator extends SfPage {
    public SfNavigator(Page page) {
        super(page);
    }

    public void validateTableColumns(List<String> columnNames) {
        if (columnNames.isEmpty()) {
            throw new IllegalArgumentException("Provide at least one table column under 'Column Name'.");
        }
        for (String columnName : columnNames) {
            if (columnName == null || columnName.isBlank()) {
                throw new IllegalArgumentException("Each table row must contain a nonblank 'Column Name'.");
            }
                Locator columnHeader = page.getByRole(AriaRole.COLUMNHEADER,
                        new Page.GetByRoleOptions().setName(columnName).setExact(true))
                        .locator(":visible").first();
            try {
                assertThat(columnHeader)
                        .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(10000));
            } catch (AssertionError failure) {
                throw new AssertionError("Expected table column is not visible: " + columnName, failure);
            }
            logger.info("Verified table column: {}", columnName);
        }
    }

    public void clickSaveButton() {
        // Locator recordSaveButton = page.locator(
        // "button[name='SaveEdit']:visible:not([disabled]):not([aria-disabled='true'])").last();
        // if (clickIfVisible(recordSaveButton, 3000)) {
        // return;
        // }

        // Locator saveButton = enabledSaveButton().last();
        // saveButton.waitFor(new Locator.WaitForOptions()
        // .setState(WaitForSelectorState.VISIBLE)
        // .setTimeout(10000));
        // saveButton.click();

        try {
            Locator SaveButton = page.locator("(//*[text()='Save'])[last()]");
            if (SaveButton.isVisible()) {
                SaveButton.click();
            }
        } catch (Exception ignored) {
        }
        page.waitForTimeout(2000);
    }

    private boolean clickIfVisible(Locator button, int timeoutMs) {
        try {
            button.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
            button.click();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
