package com.macsuite.hiring_metrics.pages;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.macsuite.common.pages.SfPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class HiringMetricsPage extends SfPage {

    private String clonedListViewName = "";

    public HiringMetricsPage(Page page) {
        super(page);
    }

    public void verifyHomePageHeaderDetails() {
        Locator.WaitForOptions timeout = new Locator.WaitForOptions().setTimeout(10000);
        page.getByText(Pattern.compile("CMCS Hiring Metrics Hub", Pattern.CASE_INSENSITIVE)).first().waitFor(timeout);
        page.getByText(Pattern.compile("Production Release Announcements", Pattern.CASE_INSENSITIVE)).first()
                .waitFor(timeout);
        String todayMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM"));
        page.getByText(Pattern.compile(todayMonth, Pattern.CASE_INSENSITIVE)).first().waitFor(timeout);
        logger.info("CMCS Hiring Metrics Hub home page header details verified");
    }

    public void verifyFormDisplayed() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save")).first()
                .waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("Hiring metrics form displayed");
    }

    public void updateStatus(String status) {
        clickEditIfVisible();

        // fillField("Status", status);

        page.locator("//label[text()='Status']//following::button[1]").click();
        waitFor(1);
        page.locator("//label[text()='Status']//following::button[1]/following::span[@title='" + status + "']").click();
       
        clickSave("updating hiring metrics status", false);
        logger.info("Status updated to: {}", status);
    }

    public void addNote(String noteText) {
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions()
                        .setName(java.util.regex.Pattern.compile("Notes", java.util.regex.Pattern.CASE_INSENSITIVE)))
                .first().click();
        Locator noteArea = page.getByLabel("Add Note").first();
        noteArea.fill(noteText);
        clickNoteSaveButton(noteArea);
        logger.info("Note added: {}", noteText);
    }

    private void clickNoteSaveButton(Locator noteArea) {
        java.util.regex.Pattern saveText = java.util.regex.Pattern.compile("^\\s*Save\\s*$", java.util.regex.Pattern.CASE_INSENSITIVE);
        Locator noteSaveButton = page.locator("button[title*='save new note' i]:not([disabled]):not([aria-disabled='true'])")
            .or(page.locator("button[title*='save updated note' i]:not([disabled]):not([aria-disabled='true'])"))
            .or(page.locator("button[title*='save note' i]:not([disabled]):not([aria-disabled='true'])"))
            .or(noteArea.locator("xpath=ancestor::*[contains(@class,'slds-card') or contains(@class,'slds-modal') or @role='dialog'][1]")
                .locator("button:not([disabled]):not([aria-disabled='true'])")
                .filter(new Locator.FilterOptions().setHasText(saveText)))
            .first();
        noteSaveButton.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        noteSaveButton.scrollIntoViewIfNeeded();
        noteSaveButton.click();
    }

    public void verifyStatus(String expectedStatus) {
        // Find the Status field container via the Edit Status button, then get the
        // value
        Locator statusContainer = page.locator("div.slds-form-element__control")
                .filter(new Locator.FilterOptions().setHas(page.locator("button[title='Edit Status']")))
                .locator("lightning-formatted-text, .test-id__field-value").first();

        Locator statusValue = statusContainer.locator("lightning-formatted-text").first();
        statusValue.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        String actualStatus = statusValue.textContent().trim();
        if (!actualStatus.equalsIgnoreCase(expectedStatus)) {
            throw new AssertionError("Expected status '" + expectedStatus + "' but found: " + actualStatus);
        }
        logger.info("Status verified: {}", expectedStatus);
    }

    public void verifyStatusTransitions(List<Map<String, String>> transitions) {
        // 1. Wait for field history table to load on the tab
        Locator historyTable = page.locator("table[role='grid']").last();
        historyTable.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        logger.info("Field history table visible.");

        // 2. Click "View All" if available to expose complete history
        Locator viewAllLink = page.locator("a.slds-card__footer:has-text('View All')").first();
        if (viewAllLink.count() > 0) {
            logger.info("Clicking 'View All' to load complete field history");
            viewAllLink.click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            logger.info("Navigated to full field history view");
        } else {
            logger.info("'View All' link not found or already on full view");
        }

        // 3. Re-identify the data rows inside tbody after view adjustment
        historyTable = page.locator("table[role='grid']").last();
        Locator allRows = historyTable.locator("tbody tr[role='row']");
        int totalRows = allRows.count();
        logger.info("Total history rows loaded for parsing: {}", totalRows);

        // 4. Validate each transition rule passed by the step definition
        for (Map<String, String> transition : transitions) {
            String expectedFrom = transition.get("From Status").trim();
            String expectedTo = transition.get("To Status").trim();

            logger.info("Validating transition: {} → {}", expectedFrom, expectedTo);
            boolean matched = false;

            for (int i = 0; i < totalRows; i++) {
                Locator currentRow = allRows.nth(i);

                // Locate target elements explicitly via cell tags combined with attributes
                Locator fieldCell = currentRow.locator("th[data-label='Field'], td[data-label='Field']");
                Locator originalCell = currentRow
                        .locator("th[data-label='Original Value'], td[data-label='Original Value']");
                Locator newCell = currentRow.locator("th[data-label='New Value'], td[data-label='New Value']");
                // Safeguard if row does not follow the standard schema
                if (fieldCell.count() == 0 || originalCell.count() == 0 || newCell.count() == 0) {
                    continue;
                }

                // Extract text metrics and drop extra whitespace gaps safely
                String fieldName = fieldCell.textContent().trim();
                if (!"Status".equalsIgnoreCase(fieldName)) {
                    continue; // Not a Status alteration row, move down
                }

                String actualFrom = originalCell.textContent().replaceAll("\\s+", " ").trim();
                String actualTo = newCell.textContent().replaceAll("\\s+", " ").trim();

                if (actualFrom.equalsIgnoreCase(expectedFrom) && actualTo.equalsIgnoreCase(expectedTo)) {
                    matched = true;
                    logger.info("✓ Found: {} → {} (row {})", expectedFrom, expectedTo, i + 1);
                    break;
                }
            }

            // 5. Assert row match state
            if (!matched) {
                throw new AssertionError(String.format(
                        "Field history validation failed! Could not find a row capturing transition status from '%s' to '%s'.",
                        expectedFrom, expectedTo));
            }
        }
    }

    // ─── List view / dashboard / reports ─────────────────────────────────────

    public void cloneHiringMetricsRecord() {
        // Switch to "All" view to ensure Clone button is enabled
        selectListView("All");
        // Wait for the list view to finish loading before accessing its controls menu
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout(3000));
        } catch (Exception ignored) {}

        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("List View Controls", Pattern.CASE_INSENSITIVE)))
                .first().click();
        page.getByRole(AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Clone$", Pattern.CASE_INSENSITIVE))).first()
                .click();
        // Generate readable timestamp: MMDDYYHHMMSS
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMddyyHHmmss"));
        clonedListViewName = "HMH Clone " + timestamp;
        Locator nameInput = page.getByLabel("List Name", new Page.GetByLabelOptions().setExact(false)).first();
        nameInput.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        nameInput.fill(clonedListViewName);
        helper.clickButton("Save");
        logger.info("Hiring metrics list view cloned as: {}", clonedListViewName);
    }

    @Override
    public void selectListView(String listViewName) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
            .setName(Pattern.compile("Select a List View", Pattern.CASE_INSENSITIVE))).first().click();
        page.getByText(listViewName, new Page.GetByTextOptions().setExact(true)).first().click();
        logger.info("Selected list view: {}", listViewName);
    }

    public void verifyClonedHiringMetricsRecord() {
        // After cloning a list view the page stays on the list; verify the new name is
        // active
        page.getByText(clonedListViewName, new Page.GetByTextOptions().setExact(false)).first()
                .waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("Cloned list view verified: {}", clonedListViewName);
    }

    public void openDashboardFromAllDashboards(String dashboardName) {
        // Click "All Dashboards" tab
        page.getByRole(AriaRole.TAB, 
            new Page.GetByRoleOptions().setName("All Dashboards")).first().click();
        logger.info("Clicked 'All Dashboards' tab");

        // Wait for the dashboard list to load and click the specific dashboard
        Locator dashboardList = page.getByLabel("All Dashboards", 
            new Page.GetByLabelOptions().setExact(true)).first();
        dashboardList.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        
        Locator dashboardLink = dashboardList.getByRole(AriaRole.LINK, 
            new Locator.GetByRoleOptions().setName(dashboardName).setExact(true)).first();
        dashboardLink.click();
        logger.info("Opened dashboard: {}", dashboardName);
    }

    public void verifyReportsItemCount(String expectedCount) {
        // Verify the "X items" text is visible
        String expectedText = expectedCount + " item" + (expectedCount.equals("1") ? "" : "s");
        Locator countElement = page.getByText(expectedText, new Page.GetByTextOptions().setExact(true)).first();
        countElement.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        if (!countElement.isVisible()) {
            throw new AssertionError("Expected reports count '" + expectedText + "' not visible");
        }
        logger.info("✓ Reports item count verified: {}", expectedText);
    }

}
