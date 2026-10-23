package com.macsuite.bct.pages;

import com.macsuite.common.pages.SfPage;
import com.macsuite.core.PlaywrightManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.options.AriaRole;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ContractDetailPage extends SfPage {

    public ContractDetailPage(Page page) {
        super(page);
    }

    public ContractDetailPage clickFirstContractOrderNumberLink() {
        logger.info("Opening the first Contract / Order Number link");
        Locator contractGrid = page.getByRole(AriaRole.GRID)
                .filter(new Locator.FilterOptions().setHasText("Contract / Order Number"))
                .first();
        Locator firstLink = contractGrid.locator("tbody tr").first().getByRole(AriaRole.LINK).first();
        firstLink.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        String contractNumber = firstLink.textContent().trim();
        String href = firstLink.getAttribute("href");
        Page contractPage = page.waitForPopup(firstLink::click);
        contractPage.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        PlaywrightManager.setActivePage(contractPage);
        logger.info("Clicked contract link '{}' (href={}, page={}, openPages={})",
                contractNumber, href, contractPage.url(),
                contractPage.context().pages().stream().map(Page::url).toList());
        return new ContractDetailPage(contractPage);
    }

    public void editContractFields(Map<String, String> updates) {
        Locator saveButton = page.locator("button[name='SaveEdit']:visible").first();
        if (!saveButton.isVisible()) {
            clickRecordEditButton();
        }
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();
            logger.info("Editing field: {}", label);
            fillField(label, value);
            waitForDependentField(label, value);
        }
        logger.info("All fields updated");
    }

    public void clickRecordEditButton() {
        Locator editButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Edit").setExact(true)).first();
        editButton.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));
        editButton.click();
        logger.info("Clicked contract record Edit button");
    }

    private void waitForDependentField(String fieldLabel, String value) {
        if (!"Component".equalsIgnoreCase(fieldLabel) || !"Other".equalsIgnoreCase(value)) {
            return;
        }

        Locator otherComponentField = page.getByLabel("Other Component",
                new Page.GetByLabelOptions().setExact(false)).first();
        otherComponentField.waitFor(new Locator.WaitForOptions().setTimeout(10000));
    }

    public void verifyUpdateSuccess() {
        helper.waitForToast("success");
    }

    public void verifyCreateSuccess() {
        helper.waitForToast("created");
    }

    public void selectCheckboxInActiveDialog(String label) {
        Locator dialog = page.getByRole(AriaRole.DIALOG).last();
        dialog.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        helper.setCheckbox(dialog, label, true);
        logger.info("Selected checkbox '{}' in the active dialog", label);
    }

    public void navigateToTab(String tabName) {
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(tabName)).first().click();
        logger.info("Navigated to tab: {}", tabName);
    }

    public void navigateBackInBrowser() {
        page.goBack();
        logger.info("Navigated back in the browser");
    }

    public ContractDetailPage navigateBackToRecordLink() {
        Locator recordLinks = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Contract-.*")));
        Locator recordLink = recordLinks.last();
        if (recordLinks.count() == 0) {
            Locator breadcrumbs = page.locator("[aria-label='Breadcrumbs'], .slds-breadcrumb");
            recordLink = breadcrumbs.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions()
                            .setName(Pattern.compile("^(?:C-|CA-|CMCS-).*")))
                    .last();
        }
        recordLink.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        String target = recordLink.getAttribute("target");
        Page contractPage;
        if (target != null && !target.isBlank() && !"_self".equalsIgnoreCase(target)) {
            contractPage = page.waitForPopup(recordLink::click);
            contractPage.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
            PlaywrightManager.setActivePage(contractPage);
        } else {
            recordLink.click();
            contractPage = page;
        }
        contractPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Details").setExact(true).setSelected(true))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        logger.info("Navigated back to the CMCS Contract record using its record link");
        return new ContractDetailPage(contractPage);
    }

    public void addNote(String noteText) {
        Locator noteField = page.getByRole(AriaRole.TEXTBOX,
            new Page.GetByRoleOptions().setName("Add Note").setExact(true));
        noteField.waitFor(new Locator.WaitForOptions()
            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(8000));
        noteField.fill(noteText);
        clickLastVisibleSaveButton();
        logger.info("Note added: {}", noteText);
    }

    private void clickLastVisibleSaveButton() {
        Locator saveButtons = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Save").setExact(true));
        int count = saveButtons.count();
        for (int index = count - 1; index >= 0; index--) {
            Locator saveButton = saveButtons.nth(index);
            try {
                if (saveButton.isVisible()) {
                    saveButton.scrollIntoViewIfNeeded();
                    saveButton.click(new Locator.ClickOptions().setTimeout(5000));
                    return;
                }
            } catch (Exception e) {
                logger.debug("Could not click visible note Save button at index {}: {}", index, e.getMessage());
            }
        }
        throw new AssertionError("Visible Save button was not found for adding a note");
    }

    public void verifyNoteExists(String noteText) {
        Locator notes = page.locator("table[aria-label='Notes'] tbody");
        Locator note = notes.getByText(noteText, new Locator.GetByTextOptions().setExact(false)).first();
        note.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        highlightElement(note);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(notes).containsText(noteText);
        logger.info("Note verified: {}", noteText);
    }

    public void verifyFieldHistoryTracking(List<String> expectedFields) {
        Locator viewAllLink = page
                .locator("xpath=//*[contains(text(), 'Contract History')]/following::*[text()='View All']/parent::a")
                .first();
        if (viewAllLink.count() > 0 && viewAllLink.isVisible()) {
            viewAllLink.click();
        } else {
            openFieldHistorySurface();
        }

        Locator historyTable = visibleHistoryTable();
        for (String fieldName : expectedFields) {
            if (!hasVisibleHistoryField(historyTable, fieldName)) {
                throw new AssertionError("Contract field history entry was not found for: " + fieldName);
            }
            logger.debug("Verified field '{}' in contract history", fieldName);
        }
        logger.info("Field history verified: {}", expectedFields);
    }

    private void openFieldHistorySurface() {
        Locator fieldHistoryTab = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName(Pattern.compile("Field History|History", Pattern.CASE_INSENSITIVE)))
                .first();
        if (fieldHistoryTab.count() > 0 && fieldHistoryTab.isVisible()) {
            fieldHistoryTab.click();
        }

        Locator viewAllLink = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("View All", Pattern.CASE_INSENSITIVE))).first();
        if (viewAllLink.count() > 0 && viewAllLink.isVisible()) {
            viewAllLink.click();
        }

        Locator historyTable = visibleHistoryTable();
        try {
            historyTable.scrollIntoViewIfNeeded();
        } catch (Exception e) {
            logger.debug("Could not scroll contract history table into view: {}", e.getMessage());
        }
    }

    private Locator visibleHistoryTable() {
        Locator historyTable = page.locator("table[role='grid'][data-num-rows]:visible").last();
        try {
            historyTable.waitFor(new Locator.WaitForOptions().setTimeout(10000));
            return historyTable;
        } catch (Exception ignored) {
        }

        historyTable = page.locator("table:visible")
                .filter(new Locator.FilterOptions().setHas(page.locator("tbody tr"))).last();
        historyTable.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        return historyTable;
    }

    private boolean hasVisibleHistoryField(Locator historyTable, String fieldName) {
        Locator fieldCells = historyTable.locator("td[data-label='Field']");
        int count = fieldCells.count();
        for (int index = 0; index < Math.min(count, 50); index++) {
            try {
                Locator fieldCell = fieldCells.nth(index);
                if (fieldCell.isVisible()
                        && normalizeHistoryText(fieldCell.textContent()).equalsIgnoreCase(normalizeHistoryText(fieldName))) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private String normalizeHistoryText(String value) {
        return value == null ? "" : value.replace('\u00a0', ' ').replaceAll("\\s+", " ").trim();
    }

    public void verifyContractHeaderDetails(Map<String, String> expectedFields) {
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE, 
                new Page.WaitForLoadStateOptions().setTimeout(15000));
        } catch (Exception e) {
            logger.debug("Network idle timeout, continuing anyway");
        }
        
        for (Map.Entry<String, String> entry : expectedFields.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();
            
            // Find field by label, then verify value
            Locator fieldRow = page.locator("[data-target-selection-name*='" + label.replace(" ", "_") + "']:visible," +
                "div:has(span.test-id__field-label:text-is('" + label + "')):visible").first();
            fieldRow.waitFor(new Locator.WaitForOptions().setTimeout(15000));
            fieldRow.locator(".test-id__field-value:visible, lightning-formatted-text:visible")
                .getByText(value).first()
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));
            
            logger.info("✓ Contract header shows {} = {}", label, value);
        }
    }

    public void verifyContractNumberAndOrderNumberInHeader() {
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout(5000));
        } catch (Exception e) {
            logger.debug("Network idle timeout, continuing anyway");
        }
        // PREDICTED de-hollow (not runtime-verified): the prior version only waited for the static
        // "Contract Number"/"Order Number" field LABEL (page chrome present regardless of data). Assert
        // the entered values actually rendered on the saved record. The scenario enters
        // "AutoContract${timestamp}" / "ORD-${timestamp}", so assert the stable prefixes are present
        // (timestamp-tolerant). Update these if the feature's contract-number convention changes.
        page.getByText("AutoContract", new Page.GetByTextOptions().setExact(false))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(8000));
        page.getByText("ORD-", new Page.GetByTextOptions().setExact(false))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(8000));
        logger.info("✓ Contract number (AutoContract*) and order number (ORD-*) verified on the saved record");
    }

    public void openContractActionsMenu() {
        // Navigate to Contract Actions tab
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions()
            .setName("Contract Actions")).first().click();
        logger.info("Navigated to Contract Actions tab");
    }

    public void verifyContractActionsMenuIsDisplayed() {
        // Verify the Contract Actions section/related list is displayed on the tab
        Locator section = page.locator("h2.slds-card__header-title")
            .filter(new Locator.FilterOptions().setHasText("Contract Actions"))
            .first();
        section.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        highlightElement(section);
        logger.info("✓ Contract Actions section is displayed");
    }

    public void clickContractActionNumberLink() {
        Locator actionNumberLink = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("^CA-\\d+$")))
            .first();
        actionNumberLink.waitFor(new Locator.WaitForOptions()
            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));
        actionNumberLink.click();
        logger.info("Clicked Contract Action Number link in Contract Actions table");
    }

    public void verifyButtonVisibility(String buttonName, boolean shouldBeVisible) {
        Pattern buttonNamePattern = Pattern.compile(Pattern.quote(buttonName), Pattern.CASE_INSENSITIVE);
        Locator buttons = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(buttonNamePattern));
        Locator visibleButton = firstVisibleMatch(buttons);
        Locator titledButtons = page.locator("[role='button'][title]")
                .filter(new Locator.FilterOptions().setHasText(buttonNamePattern));
        Locator buttonText = page.getByText(buttonName,
                new Page.GetByTextOptions().setExact(true));
        if (visibleButton == null) {
            visibleButton = firstVisibleMatch(titledButtons);
        }
        if (visibleButton == null) {
            visibleButton = firstVisibleMatch(buttonText);
        }
        if (visibleButton == null) {
            Locator dashboardIframe = page.locator("iframe[title='dashboard']:visible").last();
            if (dashboardIframe.count() > 0) {
                FrameLocator dashboard = dashboardIframe.contentFrame();
                Locator dashboardButtons = dashboard.getByRole(AriaRole.BUTTON)
                        .filter(new Locator.FilterOptions().setHasText(buttonNamePattern));
                visibleButton = firstVisibleMatch(dashboardButtons);
                if (visibleButton == null) {
                    visibleButton = firstVisibleMatch(dashboard.getByText(buttonName,
                            new FrameLocator.GetByTextOptions().setExact(true)));
                }
            }
        }

        if (shouldBeVisible && visibleButton == null) {
            Locator.WaitForOptions visibleWait = new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(10000);
            try {
                buttons.first().waitFor(visibleWait);
            } catch (com.microsoft.playwright.TimeoutError roleTimeout) {
                Locator titleMatch = page.getByTitle(buttonName, new Page.GetByTitleOptions().setExact(true)).first();
                titleMatch.waitFor(visibleWait);
            }
            visibleButton = firstVisibleMatch(buttons);
            if (visibleButton == null) {
                visibleButton = firstVisibleMatch(titledButtons);
            }
            if (visibleButton == null) {
                visibleButton = firstVisibleMatch(
                        page.getByTitle(buttonName, new Page.GetByTitleOptions().setExact(true)));
            }
        }

        if (shouldBeVisible && visibleButton == null) {
            throw new AssertionError("Expected button '" + buttonName + "' to be visible but it was not found");
        }
        if (!shouldBeVisible && visibleButton != null) {
            throw new AssertionError("Expected button '" + buttonName + "' to not be visible but it was visible");
        }
        if (shouldBeVisible) {
            highlightElement(visibleButton);
        }
        logger.info("Button visibility checked for '{}' -> {}", buttonName, shouldBeVisible);
    }

    private Locator firstVisibleMatch(Locator candidates) {
        for (int i = 0; i < candidates.count(); i++) {
            if (candidates.nth(i).isVisible()) {
                return candidates.nth(i);
            }
        }
        return null;
    }

    public void verifySummaryValueDisplayed(String summaryName) {
        Locator summary = visibleSummaryLabel(summaryName);
        summary.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        highlightElement(summary);
        logger.info("Summary value '{}' is displayed", summaryName);
    }

    public void verifyTableDisplayed(String tableName) {
        Locator table = visibleTextMatch(tableName, false);
        table.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        highlightElement(table);
        logger.info("Table '{}' is displayed", tableName);
    }

    public void verifyTableColumns(String tableName, List<String> expectedColumns) {
        if (expectedColumns.isEmpty()) {
            throw new IllegalArgumentException("At least one expected table column is required");
        }

        Locator table = contractCostDetailsGrid(tableName);
        table.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));

        for (String expected : expectedColumns) {
            if (table.getByRole(AriaRole.COLUMNHEADER,
                    new Locator.GetByRoleOptions().setName(expected.trim()).setExact(true)).count() == 0) {
                throw new AssertionError("Expected table column '" + expected + "' was not found");
            }
        }
        logger.info("Verified columns in table '{}': {}", tableName, expectedColumns);
    }

    private Locator contractCostDetailsGrid(String tableName) {
        Locator namedGrid = page.getByRole(AriaRole.GRID,
                new Page.GetByRoleOptions().setName(tableName).setExact(true));
        if (namedGrid.count() > 0) {
            return namedGrid;
        }

        Locator heading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(tableName).setExact(true)).first();
        heading.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        Locator grid = heading.locator("..").getByRole(AriaRole.GRID);
        if (grid.count() == 0) {
            throw new AssertionError("No table grid found in the section '" + tableName + "'");
        }
        return grid;
    }

    private int columnHeaderIndex(Locator table, String columnName, String tableName) {
        Locator headers = table.getByRole(AriaRole.COLUMNHEADER);
        for (int i = 0; i < headers.count(); i++) {
            String accessibleName = headers.nth(i).getAttribute("aria-label");
            if (accessibleName != null && accessibleName.trim().equalsIgnoreCase(columnName.trim())) {
                return i;
            }
        }
        throw new AssertionError("Column '" + columnName + "' was not found in table '" + tableName + "'");
    }

    public void hoverDropdownIcon() {
        Locator dropdown = page.locator(
                "button[aria-label*='Show More'], button[title*='Show More'], .slds-dropdown-trigger button, [data-label='Actions']").first();
        if (dropdown.count() == 0) {
            dropdown = page.locator("button").filter(new Locator.FilterOptions().setHasText("Show More")).first();
        }
        dropdown.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        dropdown.hover();
        logger.info("Hovered over the dropdown icon");
    }

    public void clickViewAll() {
        Locator viewAll = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("View All", Pattern.CASE_INSENSITIVE))).first();
        if (viewAll.count() == 0) {
            viewAll = page.locator("a:has-text('View All')").first();
        }
        viewAll.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        viewAll.click();
        logger.info("Clicked View All");
    }

    private BigDecimal currentColumnSum = BigDecimal.ZERO;

    public void sumColumnInTable(String columnName, String tableName) {
        Locator table = contractCostDetailsGrid(tableName);
        table.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        int targetIndex = columnHeaderIndex(table, columnName, tableName);

        BigDecimal sum = BigDecimal.ZERO;
        Locator rows = table.locator("tbody tr");
        for (int i = 0; i < rows.count(); i++) {
            Locator row = rows.nth(i);
            Locator labeledCell = row.locator("[data-label]").filter(
                    new Locator.FilterOptions().setHasText(columnName));
            String cellText;
            if (labeledCell.count() > 0) {
                cellText = labeledCell.first().textContent();
            } else {
                Locator cells = row.getByRole(AriaRole.GRIDCELL);
                int cellIndex = targetIndex;
                if (row.getByRole(AriaRole.ROWHEADER).count() > 0) {
                    cellIndex--;
                }
                if (cellIndex < 0 || cells.count() <= cellIndex) {
                    continue;
                }
                cellText = cells.nth(cellIndex).textContent();
            }
            BigDecimal value = parseCurrency(cellText);
            if (value != null) {
                sum = sum.add(value);
            }
        }
        currentColumnSum = sum;
        logger.info("Sum for column '{}' in table '{}' = {}", columnName, tableName, sum);
    }

    public void verifyTotalMatchesDisplayed(String totalLabel) {
        Locator summaryLabel = visibleSummaryLabel(totalLabel);
        summaryLabel.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        highlightElement(summaryLabel);

        String displayedText = (String) summaryLabel.evaluate(
                "label => { " +
                        "let element = label; " +
                        "for (let depth = 0; element && depth < 6; depth++, element = element.parentElement) { " +
                        "const text = (element.innerText || '').trim(); " +
                        "const values = text.match(/-?\\$?\\s*\\d[\\d,]*(?:\\.\\d+)?/g) || []; " +
                        "if (values.length === 1) return values[0]; " +
                        "} " +
                        "return ''; " +
                        "}");
        BigDecimal displayedValue = parseCurrency(displayedText);
        if (displayedValue == null) {
            throw new AssertionError("Could not parse displayed total for '" + totalLabel + "': " + displayedText);
        }
        if (displayedValue.compareTo(currentColumnSum) != 0) {
            throw new AssertionError("Displayed total '" + displayedValue + "' does not match sum '" + currentColumnSum + "' for '" + totalLabel + "'");
        }
        logger.info("Total '{}' matches computed table sum {}", totalLabel, currentColumnSum);
    }

    private Locator visibleSummaryLabel(String summaryName) {
        return visibleTextMatch(summaryName, true);
    }

    private Locator visibleTextMatch(String text, boolean exact) {
        Locator matches = page.getByText(text,
                new Page.GetByTextOptions().setExact(exact));
        for (int i = 0; i < matches.count(); i++) {
            Locator match = matches.nth(i);
            if (match.isVisible()) {
                return match;
            }
        }
        throw new AssertionError("Visible text was not found: " + text);
    }

    private BigDecimal parseCurrency(String rawText) {
        if (rawText == null) {
            return null;
        }
        String sanitized = rawText.replaceAll("[,$\s]", "");
        if (sanitized.isEmpty() || sanitized.equals("-") || sanitized.equals("--")) {
            return null;
        }
        try {
            return new BigDecimal(sanitized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void verifyMessageDisplayed(String message) {
        page.getByText(message, new Page.GetByTextOptions().setExact(true))
            .first().waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("Message verified: {}", message);
    }

    public void updateSection(String sectionName, Map<String, String> data) {
        editContractFields(data);
        logger.info("Updated '{}' section with data: {}", sectionName, data);
    }
}
