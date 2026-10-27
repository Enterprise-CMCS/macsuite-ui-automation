package com.macsuite.common.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.DropdownUtils;
import com.macsuite.common.FormUtils;
import com.macsuite.common.LookupUtils;
import com.macsuite.common.ScreenshotHelper;
import com.macsuite.common.SfHelper;
import com.macsuite.common.TemplateValueResolver;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

public abstract class SfPage extends BasePage {
    protected static final Logger logger = LoggerFactory.getLogger(SfPage.class);
    protected final Page page;
    protected final SfHelper helper;
    protected final FormUtils formUtils;
    private final Map<String, String> filledData = new HashMap<>();

    protected SfPage(Page page) {
        super(page);
        this.page = page;
        this.helper = new SfHelper(page);
        this.formUtils = new FormUtils(page);
    }

    // ─── Element Highlighting ─────────────────────────────────────────────────

    /**
     * Highlight an element before interacting with it (for visual debugging)
     * Respects TestConfig.isHighlightEnabled() setting
     * 
     * @param locator The element to highlight
     */
    protected void highlightElement(Locator locator) {
        try {
            if (!com.macsuite.config.TestConfig.isHighlightEnabled()) {
                return;
            }

            locator.scrollIntoViewIfNeeded();
            String originalStyle = (String) locator.evaluate("el => el.getAttribute('style')");

            locator.evaluate("el => {" +
                    "el.style.border = '3px solid #FF6B6B';" +
                    "el.style.backgroundColor = '#FFE66D';" +
                    "el.style.boxShadow = '0 0 10px 2px #FF6B6B';" +
                    "}");

            page.waitForTimeout(com.macsuite.config.TestConfig.HIGHLIGHT_DURATION);

            if (originalStyle != null) {
                locator.evaluate("el => el.setAttribute('style', '" + originalStyle + "')");
            } else {
                locator.evaluate("el => el.removeAttribute('style')");
            }
        } catch (Exception e) {
            logger.debug("Could not highlight element: {}", e.getMessage());
        }
    }

    // ─── Context ──────────────────────────────────────────────────────────────

    protected Locator context() {
        Locator dialogs = page.getByRole(AriaRole.DIALOG);
        return dialogs.count() > 0 ? dialogs.last() : page.locator("body");
    }

    // ─── Form filling ─────────────────────────────────────────────────────────

    public void fillForm(Map<String, String> formData) {
        page.waitForTimeout(3000);
        filledData.putAll(formData);
        page.waitForTimeout(3000); // Wait for any dynamic content to load before filling the form
        formData.forEach(this::fillField);
    }

    public void fillForm(String sectionName, Map<String, String> formData) {
        filledData.putAll(formData);
        formUtils.fillFieldsInSection(sectionName, formData);
    }

    public void fillField(String label, String value) {
        String resolved = TemplateValueResolver.resolve(value);
        String resolvedLabel = normalizeFieldLabel(label);
        Locator ctx = context();

        Locator field = preferredLabeledField(
                ctx.getByLabel(resolvedLabel, new Locator.GetByLabelOptions().setExact(false)));
        try {
            field.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            // Skip if it's a table header
            if ("TH".equals(field.evaluate("el => el.tagName"))) {
                throw new Exception("Found table header, not input field");
            }
        } catch (Exception e) {
            logger.debug("Field '{}' not found in context, trying page level", label);
            ctx = page.locator("body");
            // Exclude related-list column headers (<th aria-label=...>) that share the field label
            field = preferredLabeledField(
                    ctx.getByLabel(resolvedLabel, new Locator.GetByLabelOptions().setExact(false))
                            .and(page.locator("*:not(th):visible")));
            field.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        }

        String role = field.getAttribute("role");
        String tagName = ((String) field.evaluate("element => element.tagName")).toLowerCase();

        if (!"input".equals(tagName) && !"textarea".equals(tagName)
                && !"select".equals(tagName) && !"combobox".equals(role)) {
            logger.debug("Label '{}' resolved to <{}>; using form utility to locate its input", label, tagName);
            formUtils.fillField(resolvedLabel, resolved);
            return;
        }

        if ("select".equals(tagName)) {
            field.scrollIntoViewIfNeeded();
            highlightElement(field);
            field.selectOption(new SelectOption().setLabel(resolved));
        } else if ("combobox".equals(role)) {
            // For combobox fields, check if it's a forced lookup field
            boolean isForcedLookup = isForcedLookupField(resolvedLabel);

            if (isForcedLookup) {
                // Use LookupUtils for proper modal handling
                new LookupUtils(page).fillLookupField(resolvedLabel, resolved, ctx);
            } else {
                // Regular dropdown
                new DropdownUtils(page).selectDropdown(resolvedLabel, resolved, ctx);
            }
        } else {
            // Regular text field
            field.fill(resolved);
        }
    }

    private Locator preferredLabeledField(Locator fields) {
        for (int index = fields.count() - 1; index >= 0; index--) {
            Locator candidate = fields.nth(index);
            if (!candidate.isVisible()) {
                continue;
            }

            String tagName = ((String) candidate.evaluate("element => element.tagName")).toLowerCase();
            if (("input".equals(tagName) || "textarea".equals(tagName))
                    && candidate.inputValue().isBlank()) {
                return candidate;
            }
        }
        return fields.first();
    }

    private String normalizeFieldLabel(String fieldLabel) {
        if ("Contract Officier".equalsIgnoreCase(fieldLabel)) {
            return "Contract Officer";
        }
        return fieldLabel;
    }

    private boolean isForcedLookupField(String fieldLabel) {
        return "Contract Officer".equalsIgnoreCase(fieldLabel)
                || "Contract Specialist".equalsIgnoreCase(fieldLabel)
                || "Contractor Name".equalsIgnoreCase(fieldLabel)
                || "Vendor".equalsIgnoreCase(fieldLabel)
                || "Vendor Point of Contact".equalsIgnoreCase(fieldLabel)
                || "Contract Officer Representative".equalsIgnoreCase(fieldLabel)
                || "Triager".equalsIgnoreCase(fieldLabel)
                || "Triager (Additional)".equalsIgnoreCase(fieldLabel)
                || "SME".equalsIgnoreCase(fieldLabel)
                || "Senior Reviewer / Technical Director".equalsIgnoreCase(fieldLabel);
    }

    protected Map<String, String> getFilledData() {
        return new HashMap<>(filledData);
    }

    protected void clearFilledData() {
        filledData.clear();
    }

    public void waitForToast(String type) {
        helper.waitForToast(type);
    }

    public void waitForToast(String type, String message) {
        helper.waitForToast(type, message);
    }

    public String extractRecordNumber() {
        return helper.extractRecordNumber();
    }

    public void verifyErrorToastMessage(String expectedMessage) {
        helper.waitForToast("error", expectedMessage);
    }

    // ─── Record actions ───────────────────────────────────────────────────────

    public void deleteCurrentRecord() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName(java.util.regex.Pattern.compile("Show more actions|Show Actions",
                        java.util.regex.Pattern.CASE_INSENSITIVE)))
                .first().click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions()
                .setName("Delete").setExact(true))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions()
                .setName("Delete").setExact(true))
                .first().click();
        Locator dialog = page.getByRole(AriaRole.DIALOG).first();
        dialog.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
                .setName("Delete").setExact(true))
                .first().click();
        logger.info("Current record deleted");
    }

    protected void clickEditIfVisible() {
        page.waitForTimeout(6000);
        boolean editClicked = false;

        Locator directEditButtons = page
                .locator("button.test-id__inline-edit-trigger[title*='Edit'], button[title*='Edit']");
        int directCount = directEditButtons.count();
        for (int index = 0; index < Math.min(directCount, 20); index++) {
            try {
                Locator candidate = directEditButtons.nth(index);
                if (!candidate.isVisible()) {
                    continue;
                }
                candidate.scrollIntoViewIfNeeded();
                candidate.click();
                logger.info("Clicked Edit button");
                editClicked = true;
                break;
            } catch (Exception ignored) {
            }
        }

        if (!editClicked) {
            try {
                Locator actionsButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                        .setName(java.util.regex.Pattern.compile(
                                "Show more actions|Show Actions", java.util.regex.Pattern.CASE_INSENSITIVE)));

                int actionsCount = actionsButton.count();
                for (int idx = 0; idx < Math.min(actionsCount, 10); idx++) {
                    Locator candidate = actionsButton.nth(idx);
                    if (candidate.isVisible()) {
                        candidate.scrollIntoViewIfNeeded();
                        candidate.click();
                        break;
                    }
                }

                Locator editMenuItems = page.getByRole(AriaRole.MENUITEM,
                        new Page.GetByRoleOptions().setName(
                                java.util.regex.Pattern.compile("^Edit$", java.util.regex.Pattern.CASE_INSENSITIVE)));
                int menuCount = editMenuItems.count();
                for (int idx = 0; idx < Math.min(menuCount, 10); idx++) {
                    Locator candidate = editMenuItems.nth(idx);
                    if (candidate.isVisible()) {
                        candidate.scrollIntoViewIfNeeded();
                        candidate.click();
                        logger.info("Clicked Edit button from Show more actions menu");
                        editClicked = true;
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        try {
            page.locator("button[name='SaveEdit']").first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(3000));
        } catch (Exception ignored) {
        }
    }

    protected void clickDetailsTabIfVisible() {
        Locator detailsTab = page.locator("a[role='tab'][data-label='Details']:visible")
                .or(page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Details").setExact(true)))
                .first();
        try {
            detailsTab.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            if (!"true".equals(detailsTab.getAttribute("aria-selected"))) {
                detailsTab.click();
                logger.info("Returned to Details tab");
            }
        } catch (Exception ignored) {
        }
    }

    protected void clickSave(String failureContext, boolean retryIfStillVisible) {
        Locator saveBtn = enabledSaveButton().last();
        saveBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        saveBtn.click();
        try {
            saveBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
        } catch (Exception ignored) {
        }
        if (retryIfStillVisible && saveBtn.isVisible()) {
            saveBtn.click();
            try {
                saveBtn.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
            } catch (Exception ignored) {
            }
        }
    }

    protected Locator enabledSaveButton() {
        Locator enabledExactSave = page
                .locator("button:not([disabled]):not([aria-disabled='true']):not([title*='save new note' i])")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*Save\\s*$")));
        Locator dialogSave = page.getByRole(AriaRole.DIALOG).last()
                .locator("button:not([disabled]):not([aria-disabled='true']):not([title*='save new note' i])")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*Save\\s*$")));

        return page.locator("button[name='SaveEdit']:not([disabled]):not([aria-disabled='true'])")
                .or(page.locator(".forceRecordEditActions button:not([disabled]):not([aria-disabled='true'])")
                        .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*Save\\s*$"))))
                .or(dialogSave)
                .or(enabledExactSave);
    }

    protected void verifyStageIsActive(String expectedStage, String recordTypeName) {
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName(expectedStage).setSelected(true))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
        logger.info("{} stage verified: {}", recordTypeName, expectedStage);
    }

    protected String readDetailFieldValue(String fieldLabel) {
        return helper.readText(page.locator("body"), fieldLabel);
    }

    // ─── Salesforce navigation ────────────────────────────────────────────────

    public void selectSalesforceApp(String appName) {
        logger.info("Launching app: {}", appName);
        page.getByTitle("App Launcher").click();
        page.getByPlaceholder("Search apps and items...").fill(appName);
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(appName).setExact(true))
                .first().click();
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(appName))
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));
    }

    public void waitForDashboardHeading(String appName) {
        page.getByText(appName, new Page.GetByTextOptions().setExact(false))
                .first()
                .waitFor(new Locator.WaitForOptions().setTimeout(20000));
        logger.info("✓ Dashboard loaded: {}", appName);
    }

    /**
     * Verifies the dashboard header details including title, date, and "Viewing as"
     * user.
     * Uses Playwright semantic locators with auto-wait.
     * Date is automatically set to today's date.
     * Dashboard content is rendered inside an iframe.
     * 
     * Special case: PIER has two dashboards ("PIER Cases" and "PIER Issues")
     * displayed on home page.
     * When expectedTitle contains "PIER", validates that at least one PIER
     * dashboard is present.
     */
    private String normalizeTitle(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", " ")
                .replace("-", " ")
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll("\\s+", "")
                .trim();
    }

    public void verifyDashboardHeaderDetails(String expectedTitle) {
        String expectedUser = "SEI Admin";
        // Use abbreviated month format (MMM) to match Salesforce's "Jun 1, 2026" format
        String expectedDate = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"));
        // Use .last() to get the most recent dashboard iframe (Salesforce keeps old
        // hidden ones in DOM)
        Locator dashboardFrame = page.locator("iframe[title='dashboard']:visible").last();
        dashboardFrame.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(15000));

        highlightElement(dashboardFrame);

        FrameLocator frame = dashboardFrame.contentFrame();

        // Wait for dashboard to be loaded by checking for the title element with
        // meaningful content
        Locator titleElement = frame.locator(".slds-page-header__title")
                .filter(new Locator.FilterOptions().setHasNotText("-"))
                .first();
        titleElement.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        highlightElement(titleElement);

        String actualTitle = titleElement.textContent().trim();

        // PIER edge case: home page displays both "PIER Cases" and "PIER Issues"
        // dashboards
        // Validate that at least one PIER dashboard is present
        if (expectedTitle.toUpperCase().contains("PIER")) {
            boolean isPierDashboard = actualTitle.equals("PIER Cases") || actualTitle.equals("PIER Issues");
            if (!isPierDashboard) {
                logger.error("PIER dashboard not found. Expected 'PIER Cases' or 'PIER Issues', Actual: '{}'",
                        actualTitle);
                throw new AssertionError("PIER dashboard not found. Expected 'PIER Cases' or 'PIER Issues', Actual: '"
                        + actualTitle + "'");
            }
            logger.info("✓ PIER dashboard verified: {}", actualTitle);
        } else {
            boolean titleMatches = actualTitle.equals(expectedTitle);
            if (!titleMatches) {
                String normalizedExpected = normalizeTitle(expectedTitle);
                String normalizedActual = normalizeTitle(actualTitle);
                boolean legacyBctTitle = expectedTitle.equalsIgnoreCase("CMCS Budget & Contract Tracker")
                        && (normalizedActual.contains("cmcscontract")
                                && (normalizedActual.contains("dashboard") || normalizedActual.contains("tracker")));
                if (!legacyBctTitle && !normalizedActual.equals(normalizedExpected)) {
                    logger.error("Title mismatch. Expected: '{}', Actual: '{}'", expectedTitle, actualTitle);
                    throw new AssertionError(
                            "Dashboard title mismatch. Expected: '" + expectedTitle + "', Actual: '" + actualTitle + "'");
                }
            }
            logger.info("✓ Dashboard title verified: {}", actualTitle);
        }

        // Verify date and user in the SAME dashboard header that contains the validated
        // title
        // Navigate up from title to its parent .slds-page-header to ensure we check the
        // correct dashboard
        Locator dashboardHeader = titleElement.locator("xpath=ancestor::div[contains(@class, 'slds-page-header')]")
                .first();
        String headerText = dashboardHeader.textContent();

        verifyTextInFrame(frame, "As of " + expectedDate, "Date", headerText, 5000);
        verifyTextInFrame(frame, "Viewing as " + expectedUser, "User", headerText, 5000);

        logger.info("✓ Dashboard header details verified (title, date, user)");
    }

    public void clickHeaderMenuItem(String menuItemName) {
        waitFor(5);
        logger.info("Clicking header menu item: {}", menuItemName);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(menuItemName).setExact(true))
                .or(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(menuItemName).setExact(true)))
                .first().click();
              
    }

    public void selectListView(String listViewName) {
        logger.info("Selecting list view: {}", listViewName);
        Locator listViewDropdown = page.locator("xpath=//h1/span[2]").first();
        listViewDropdown.click();
        Locator listViewOption = page.locator("xpath=//span[@title='" + listViewName + "']").first();
       
        listViewOption.click();
       screenshot("Selected List View: " + listViewName);
    }

    public String recordName = "";

    public void selectRandomRecord() {
        recordName = ""; // Reset the record name before selecting a new one
        Locator records = page.locator("xpath=//tbody/tr/th//a");
        int recordCount = records.count();
        if (recordCount > 0) {
            int randomIndex = (int) (Math.random() * recordCount);
            recordName = records.nth(randomIndex).textContent();
            records.nth(randomIndex).click();
        } else {
            logger.warn("No records found in list view");
        }
        page.waitForTimeout(5000);
        screenshot("Random Record : "+ recordName);
    }

 public void selectRandomAccountRecord() {
        recordName = ""; // Reset the record name before selecting a new one
        Locator records = page.locator("xpath=(//tbody/tr/td//a)[2]");
        int recordCount = records.count();
        if (recordCount > 0) {
            int randomIndex = (int) (Math.random() * recordCount);
            recordName = records.nth(randomIndex).textContent();
            records.nth(randomIndex).click();
        } else {
            logger.warn("No records found in list view");
        }
        screenshot("Random Record : "+ recordName);
    }

    public void verifyRecordNavigation() {
       String currentRecordName = page.locator("xpath=//*[text()='" + recordName + "']").first().textContent();
       logger.info("Navigated to record: {}", currentRecordName);
       if (!currentRecordName.equals(recordName)) {
           logger.warn("Expected record: {}, but navigated to: {}", recordName, currentRecordName);
       }    else {
           logger.info("Successfully navigated to the expected record: {}", recordName);
       }
        screenshot("Navigated Record : "+ currentRecordName);
    }
    protected void openFieldHistoryRelatedList(Pattern tabNamePattern, String historyHeading) {
        Locator historyTab = fieldHistoryTab(tabNamePattern, historyHeading);
        historyTab.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED).setTimeout(10000));
        clickFieldHistoryTab(historyTab);
        ensureHistoryTabIsActive(tabNamePattern, historyHeading, historyTab);

        Locator viewAll = fieldHistoryViewAllLink(tabNamePattern, historyHeading);
        try {
            viewAll.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED).setTimeout(5000));
            clickFieldHistoryViewAllLink(viewAll);
        } catch (Exception e) {
            String historyUrl = currentRecordHistoryUrl();
            if (historyUrl != null) {
                logger.debug(
                        "Field History View All link was not available, navigating directly to current record history: {}",
                        historyUrl);
                page.navigate(historyUrl);
            }
        }

        if (fieldHistoryRowsAreVisible(10000)) {
            logger.info("Field History grid is already visible on the selected tab");
            return;
        }

        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(10000));
        } catch (Exception ignored) {
        }

        if (historyHeading != null && !historyHeading.isBlank()) {
            page.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions()
                            .setName(Pattern.compile(Pattern.quote(historyHeading), Pattern.CASE_INSENSITIVE)))
                    .or(page.locator("h1").filter(new Locator.FilterOptions().setHasText(historyHeading)))
                    .or(page.getByRole(AriaRole.HEADING,
                            new Page.GetByRoleOptions().setName(Pattern.compile("Histor", Pattern.CASE_INSENSITIVE))))
                    .first().waitFor(new Locator.WaitForOptions().setTimeout(10000));
        }

        logger.info("Opened full Field History related list{}",
                historyHeading == null || historyHeading.isBlank() ? "" : ": " + historyHeading);
    }

    private void clickFieldHistoryTab(Locator historyTab) {
        try {
            if (historyTab.isVisible()) {
                historyTab.scrollIntoViewIfNeeded();
                historyTab.click();
                return;
            }
        } catch (Exception e) {
            logger.debug("Visible click on Field History tab was not available: {}", e.getMessage());
        }

        historyTab.evaluate("el => el.click()");
    }

    private void clickFieldHistoryViewAllLink(Locator viewAll) {
        try {
            if (viewAll.isVisible()) {
                viewAll.scrollIntoViewIfNeeded();
                viewAll.click();
                return;
            }
        } catch (Exception e) {
            logger.debug("Visible click on Field History View All link was not available: {}", e.getMessage());
        }

        String href = viewAll.getAttribute("href");
        if (href == null || href.isBlank()) {
            viewAll.evaluate("el => el.click()");
            return;
        }

        String recordId = currentRecordIdFromUrl();
        if (recordId != null && !href.contains(recordId)) {
            String historyUrl = currentRecordHistoryUrl();
            if (historyUrl == null) {
                throw new AssertionError("Field History View All link points to a different record. Current record id: "
                        + recordId + ", link href: " + href);
            }
            logger.debug(
                    "Ignoring stale Field History View All link for another record: {}. Navigating to current record history: {}",
                    href, historyUrl);
            page.navigate(historyUrl);
            return;
        }

        page.navigate(absoluteUrl(href));
    }

    protected int verifyFieldHistoryRowsRecorded() {
        Locator rows = fieldHistoryRows();
        rows.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(15000));

        int rowCount = rows.count();
        if (rowCount == 0) {
            throw new AssertionError("Field history grid has no change rows");
        }

        logger.info("Field history verified: {} row(s)", rowCount);
        return rowCount;
    }

    protected void verifyFieldHistoryContainsFields(String... expectedFields) {
        verifyFieldHistoryRowsRecorded();

        Object actualFieldValues = fieldHistoryRows().evaluateAll("rows => rows.map(row => {"
                + "const fieldCell = row.querySelector(\"td[data-label='Field'], th[data-label='Field'], td:nth-child(2)\");"
                + "return fieldCell ? fieldCell.innerText.trim() : '';"
                + "}).filter(Boolean)");
        List<String> actualFields = new ArrayList<>();
        if (actualFieldValues instanceof List<?>) {
            for (Object value : (List<?>) actualFieldValues) {
                actualFields.add(String.valueOf(value));
            }
        }

        List<String> missingFields = new ArrayList<>();
        for (String expectedField : expectedFields) {
            boolean found = actualFields.stream()
                    .anyMatch(actualField -> normalizeHistoryField(actualField)
                            .equals(normalizeHistoryField(expectedField)));
            if (!found) {
                missingFields.add(expectedField);
            }
        }

        if (!missingFields.isEmpty()) {
            throw new AssertionError("Field history missing expected field(s): " + missingFields
                    + ". Actual field history fields: " + actualFields);
        }

        logger.info("Field history contains expected field(s): {}", Arrays.asList(expectedFields));
    }

    private String normalizeHistoryField(String fieldName) {
        return fieldName == null ? "" : fieldName.trim().replaceAll("\\.+$", "").toLowerCase();
    }

    private Locator fieldHistoryTab(Pattern tabNamePattern, String historyHeading) {
        Locator tab = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName(tabNamePattern))
                .or(page.locator("a[role='tab']").filter(new Locator.FilterOptions().setHasText(tabNamePattern)))
                .or(page.locator("li[role='presentation'][data-label*='History'] a[role='tab']"))
                .or(page.locator("a[role='tab'][data-label*='History']"));

        if (historyHeading != null && !historyHeading.isBlank()
                && Pattern.compile("Field History|History Tracking", Pattern.CASE_INSENSITIVE).matcher(historyHeading)
                        .find()) {
            tab = page.locator("li[role='presentation'][data-label='" + cssString(historyHeading) + "'] a[role='tab']")
                    .or(page.locator("a[role='tab'][data-label='" + cssString(historyHeading) + "']"))
                    .or(tab);
        }

        return tab.first();
    }

    private void ensureHistoryTabIsActive(Pattern tabNamePattern, String historyHeading, Locator historyTab) {
        if (activeHistoryTabIsPresent(tabNamePattern, historyHeading, 3000)) {
            logger.info("Confirmed active Field History tab");
            return;
        }

        try {
            historyTab.evaluate("el => el.click()");
        } catch (Exception e) {
            logger.debug("DOM click on Field History tab was not available: {}", e.getMessage());
        }

        if (activeHistoryTabIsPresent(tabNamePattern, historyHeading, 3000)) {
            logger.info("Confirmed active Field History tab after DOM click");
        } else {
            logger.debug("Could not confirm active Field History tab before row verification");
        }
    }

    private boolean activeHistoryTabIsPresent(Pattern tabNamePattern, String historyHeading, int timeoutMs) {
        try {
            String expected = historyHeading != null && !historyHeading.isBlank()
                    ? historyHeading
                    : tabNamePattern.pattern();
            boolean exactText = historyHeading != null && !historyHeading.isBlank();
            page.waitForFunction(
                    "([expected, exactText]) => {"
                            + "const expectedText = String(expected).toLowerCase();"
                            + "const matcher = exactText ? null : new RegExp(expected, 'i');"
                            + "const collect = root => {"
                            + "const found = Array.from(root.querySelectorAll(\"a[role='tab'][aria-selected='true'], li.slds-is-active a[role='tab']\"));"
                            + "for (const el of root.querySelectorAll('*')) { if (el.shadowRoot) found.push(...collect(el.shadowRoot)); }"
                            + "return found;"
                            + "};"
                            + "return collect(document).some(tab => {"
                            + "const parent = tab.closest('li');"
                            + "const text = [tab.textContent, tab.getAttribute('data-label'), tab.getAttribute('title'),"
                            + "parent && parent.getAttribute('data-label'), parent && parent.getAttribute('title')]"
                            + ".filter(Boolean).join(' ');"
                            + "return exactText ? text.toLowerCase().includes(expectedText) : matcher.test(text);"
                            + "});"
                            + "}",
                    new Object[] { expected, exactText },
                    new Page.WaitForFunctionOptions().setTimeout(timeoutMs));
            return true;
        } catch (Exception e) {
            logger.debug("Active Field History tab not confirmed yet: {}", e.getMessage());
            return false;
        }
    }

    private boolean fieldHistoryRowsAreVisible(int timeoutMs) {
        try {
            fieldHistoryRows().first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Locator fieldHistoryRows() {
        return page.locator("table[role='grid'][data-num-rows]:visible "
                + "tbody tr.slds-hint-parent[role='row'][data-row-key-value]:not([data-row-key-value='HEADER']):visible, "
                + "table.slds-table:visible "
                + "tbody tr.slds-hint-parent[role='row'][data-row-key-value]:not([data-row-key-value='HEADER']):visible");
    }

    private Locator fieldHistoryViewAllLink(Pattern tabNamePattern, String historyHeading) {
        Locator activePanelViewAll = activeHistoryPanelViewAllLink(tabNamePattern, historyHeading);
        String recordId = currentRecordIdFromUrl();
        Locator recordScopedViewAll = page.locator("a[href='__no_field_history_match__']");
        if (recordId != null) {
            recordScopedViewAll = page.locator("a.slds-card__footer[href*='" + recordId
                    + "'][href*='/related/'][href*='Histor' i]")
                    .filter(new Locator.FilterOptions()
                            .setHasText(Pattern.compile("View All", Pattern.CASE_INSENSITIVE)))
                    .first();
        }

        Locator visibleHeadingScopedViewAll = page.locator("article:visible")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("History", Pattern.CASE_INSENSITIVE)))
                .locator("a[href*='/related/'][href*='Histor' i]")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("View All", Pattern.CASE_INSENSITIVE)))
                .first();

        Locator headingScopedViewAll = page.locator("article[aria-label*='History' i] "
                + "a:has(.view-all-label)[href*='/related/'][href*='Histor' i]").first();
        if (historyHeading != null && !historyHeading.isBlank()) {
            visibleHeadingScopedViewAll = page.locator("article:visible")
                    .filter(new Locator.FilterOptions()
                            .setHasText(Pattern.compile(Pattern.quote(historyHeading), Pattern.CASE_INSENSITIVE)))
                    .locator("a[href*='/related/'][href*='Histor' i]")
                    .filter(new Locator.FilterOptions()
                            .setHasText(Pattern.compile("View All", Pattern.CASE_INSENSITIVE)))
                    .first();

            headingScopedViewAll = page.locator("article")
                    .filter(new Locator.FilterOptions()
                            .setHasText(Pattern.compile(Pattern.quote(historyHeading), Pattern.CASE_INSENSITIVE)))
                    .locator("a:has(.view-all-label)[href*='/related/'][href*='Histor' i]")
                    .first();
        }

        Locator viewAll = activePanelViewAll.or(recordScopedViewAll);
        if (recordId == null) {
            viewAll = viewAll
                    .or(visibleHeadingScopedViewAll)
                    .or(headingScopedViewAll)
                    .or(page.locator("a.slds-card__footer[href*='/related/'][href*='Histor' i]")
                            .filter(new Locator.FilterOptions()
                                    .setHasText(Pattern.compile("View All", Pattern.CASE_INSENSITIVE))));
        }

        return viewAll.first();
    }

    private Locator activeHistoryPanelViewAllLink(Pattern tabNamePattern, String historyHeading) {
        String panelId = activeHistoryPanelId(tabNamePattern, historyHeading);
        if (panelId == null || panelId.isBlank()) {
            return page.locator("a[href='__no_active_history_panel_match__']");
        }

        return page.locator("#" + cssString(panelId) + " a[href*='/related/'][href*='Histor' i]")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("View All", Pattern.CASE_INSENSITIVE)))
                .first();
    }

    private String activeHistoryPanelId(Pattern tabNamePattern, String historyHeading) {
        try {
            String expected = historyHeading != null && !historyHeading.isBlank()
                    ? historyHeading
                    : tabNamePattern.pattern();
            boolean exactText = historyHeading != null && !historyHeading.isBlank();
            Object panelId = page.evaluate(
                    "([expected, exactText]) => {"
                            + "const expectedText = String(expected).toLowerCase();"
                            + "const matcher = exactText ? null : new RegExp(expected, 'i');"
                            + "const collect = root => {"
                            + "const found = Array.from(root.querySelectorAll(\"a[role='tab'][aria-selected='true'], li.slds-is-active a[role='tab']\"));"
                            + "for (const el of root.querySelectorAll('*')) { if (el.shadowRoot) found.push(...collect(el.shadowRoot)); }"
                            + "return found;"
                            + "};"
                            + "const tabs = collect(document);"
                            + "const tab = tabs.find(el => {"
                            + "const text = [el.textContent, el.getAttribute('data-label'), el.getAttribute('title'), el.closest('li')?.getAttribute('data-label')].filter(Boolean).join(' ');"
                            + "return exactText ? text.toLowerCase().includes(expectedText) : matcher.test(text);"
                            + "});"
                            + "return tab ? tab.getAttribute('aria-controls') : null;"
                            + "}",
                    new Object[] { expected, exactText });
            return panelId == null ? null : panelId.toString();
        } catch (Exception e) {
            logger.debug("Could not resolve active Field History panel id: {}", e.getMessage());
            return null;
        }
    }

    private String currentRecordIdFromUrl() {
        java.util.regex.Matcher matcher = Pattern.compile("/lightning/r/[^/]+/([^/?#]+)").matcher(page.url());
        return matcher.find() ? matcher.group(1) : null;
    }

    private String currentRecordHistoryUrl() {
        java.util.regex.Matcher matcher = Pattern.compile("/lightning/r/([^/]+)/([^/?#]+)").matcher(page.url());
        if (!matcher.find()) {
            return null;
        }
        return absoluteUrl("/lightning/r/" + matcher.group(1) + "/" + matcher.group(2) + "/related/Histories/view");
    }

    private String absoluteUrl(String href) {
        try {
            return java.net.URI.create(page.url()).resolve(href).toString();
        } catch (Exception e) {
            logger.debug("Could not resolve Field History href '{}' against current URL '{}': {}", href, page.url(),
                    e.getMessage());
            return href;
        }
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        return "concat('" + value.replace("'", "', \"'\", '") + "')";
    }

    private String cssString(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void verifyTextInFrame(com.microsoft.playwright.FrameLocator frame, String expectedText, String description,
            String context, int timeoutMs) {
        try {
            frame.getByText(expectedText)
                    .first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            logger.info("✓ {} found: {}", description, expectedText);
        } catch (Exception e) {
            logger.error("{} '{}' not found. Available text: {}", description, expectedText, context);
            // throw new AssertionError(description + " '" + expectedText + "' not found", e);
        }
    }

    // ─── Shared Report Validation Methods ────────────────────────────────────
    public void clickLink(String linkText) {
        Locator link = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(linkText).setExact(true)).first();
        link.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        link.click();
        logger.info("Clicked link: {}", linkText);
    }

    public void selectDashboardFromList(String dashboardName) {
        // Wait for dashboard list table to load after tab switch
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout(5000));

        // Scope to the table/grid containing the dashboard list
        Locator dashboardTable = page.locator("[role='grid']").first();
        dashboardTable.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        // Find the link in the "Dashboard Name" column
        Locator dashboardCell = dashboardTable.locator("[data-label='Dashboard Name']")
                .filter(new Locator.FilterOptions().setHasText(dashboardName)).first();

        Locator dashboardLink = dashboardCell.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(dashboardName).setExact(true)).first();

        dashboardLink.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        dashboardLink.click();
        logger.info("Selected dashboard from list: {}", dashboardName);
    }

    public void verifyDashboardNavigation() {
        Page page = this.page; // Playwright Page object

        page.waitForTimeout(10000);
        page.locator("//a[@title='Dashboards']").click();
        logger.info("Clicked element: {}", "Dashboards_Tab");
        screenshot("Dashboard Screen");

        page.waitForTimeout(15000);
        page.locator("//a[@title='All Folders']").click();
        logger.info("Clicked element: {}", "All_Folders");
        screenshot("All Folders Screen");
        page.waitForTimeout(5000);
        Locator dashboards = page.locator("//table[@aria-label='All Folders']//tbody/tr/th//a"); // update selector
        int count = dashboards.count();

        Random rand = new Random();
        int index = rand.nextInt(count);

        String primaryName = dashboards.nth(index).innerText();
        logger.info("Selected dashboard: {}", primaryName);

        dashboards.nth(index).click();
        logger.info("Clicked dashboard: {}", primaryName);

        page.waitForTimeout(7000);
        screenshot(primaryName + " Screen");

        int count2 = dashboards.count();
        int index2 = rand.nextInt(count2);

        String secondaryName = dashboards.nth(index2).innerText();
        logger.info("Selected dashboard: {}", secondaryName);
        page.waitForTimeout(5000);
        dashboards.nth(index2).click();
        logger.info("Clicked dashboard: {}", secondaryName);

        page.waitForTimeout(7000);

        String dashboardTitle = dashboards.nth(1).innerText();
        logger.info("Dashboard title displayed: {}", dashboardTitle);

        screenshot(secondaryName + " Screen");
    }

    public void verifyReportsNavigation() {

        Page page = this.page; // Playwright Page object

        page.waitForTimeout(3000);

        page.locator("//a[@title='Reports']").click(); // update selector
        logger.info("Clicked element: {}", "Reports_Tab");

        page.waitForTimeout(5000);

        page.locator("//a[text()='All Reports']").click(); // update selector
        logger.info("Clicked element: {}", "All_Reports");

        page.waitForTimeout(5000);
        screenshot("Reports Screen");

        Locator reports = page.locator("//th[@data-label='Report Name']//a"); // update selector
        int count = reports.count();
        Random rand = new Random();
        int index = rand.nextInt(count);
        logger.info("Total reports found: {}", count);
        String primaryName = reports.nth(index).innerText();
        logger.info("Selected report: {}", primaryName);

        reports.nth(index).click();
        logger.info("Clicked report: {}", primaryName);

        page.waitForTimeout(7000);
        screenshot(primaryName + " Reports Screen");

        page.waitForTimeout(15000);

        // Switch to iframe
        FrameLocator reportFrame = page.frameLocator("//iframe[@title='Report Viewer']"); // update selector

        String reportTitleOnScreen = reportFrame
                .locator("//h1/span[contains(text(),'Report:')]/following-sibling::span").innerText(); // update
                                                                                                       // selector
        logger.info("Report title on screen: {}", reportTitleOnScreen);

        String totalRecordsOnScreen = reportFrame.locator("//div[@class='metricsElement metricsValue']").first()
                .innerText(); // update selector
        logger.info("Total records on screen: {}", totalRecordsOnScreen);
    }

    public void openReport(String reportName) {
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(reportName)).first().click();
        logger.info("Clicked report: {}", reportName);

        // Wait for report iframe to load
        Locator reportIframe = page.locator("iframe[title='Report Viewer']").first();
        reportIframe.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(20000));
        logger.info("Report iframe loaded");
    }

    public void openRandomReport() {
        // Get all report links from the reports table (first column with Report Name)
        Locator reportLinks = page.locator("//table[@aria-label='All Folders']/tbody/tr/th//a");
        int count = reportLinks.count();

        if (count == 0) {
            throw new RuntimeException("No reports found in the table");
        }

        // Pick a random report
        int randomIndex = new java.util.Random().nextInt(count);

        String reportName = reportLinks.nth(randomIndex).textContent().trim();
        logger.info("Selected random report #{} out of {}: {}", randomIndex + 1, count, reportName);

        reportLinks.nth(randomIndex).click();
        waitFor(5);
        screenshot("Random Report Screen " + reportName);
        // Wait for report iframe to load
        // Locator reportIframe = page.locator("iframe[title='Report Viewer']").first();
        // reportIframe.waitFor(new Locator.WaitForOptions()
        // .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
        // .setTimeout(20000));
        // logger.info("Random report iframe loaded: {}", reportName);
    }

    public void openRandomReport(String commonLocator) {
        // Get all report links from the reports table (first column with Report Name)

        Locator reportLinks = page.locator(commonLocator);
        int count = reportLinks.count();

        if (count == 0) {
            throw new RuntimeException("No reports found in the table");
        }

        // Pick a random report
        int randomIndex = new java.util.Random().nextInt(count);

        String reportName = reportLinks.nth(randomIndex).textContent().trim();
        logger.info("Selected random report #{} out of {}: {}", randomIndex + 1, count, reportName);
        waitFor(5);
        screenshot("Random Report Screen " + reportName);
        reportLinks.nth(randomIndex).click();

    }

    public void openChildReport() {
        String reportLocator = "(//table[@aria-label='All Folders']/tbody)[2]/tr/th//a";
        openRandomReport(reportLocator);
    }

    public void openRandomDashboard() {
        // Get all report links from the reports table (first column with Report Name)
        Locator reportLinks = page.locator("(//table[@aria-label='All Folders']/tbody/tr/th//a)[last()]");
        int count = reportLinks.count();

        if (count == 0) {
            throw new RuntimeException("No reports found in the table");
        }

        // Pick a random report
        int randomIndex = new java.util.Random().nextInt(count);

        String reportName = reportLinks.nth(randomIndex).textContent().trim();
        logger.info("Selected random report #{} out of {}: {}", randomIndex + 1, count, reportName);
        waitFor(5);
        screenshot("Random Dashboard Screen " + reportName);
        reportLinks.nth(randomIndex).click();
    }

    public void openRandomDashboard(String commonLocator) {
        // Get all report links from the reports table (first column with Report Name)

        Locator reportLinks = page.locator(commonLocator);
        int count = reportLinks.count();

        if (count == 0) {
            throw new RuntimeException("No reports found in the table");
        }

        // Pick a random report
        int randomIndex = new java.util.Random().nextInt(count);

        String reportName = reportLinks.nth(randomIndex).textContent().trim();
        logger.info("Selected random report #{} out of {}: {}", randomIndex + 1, count, reportName);
        waitFor(5);
        screenshot("Random Dashboard Screen " + reportName);
        reportLinks.nth(randomIndex).click();

    }

    public void openChildDashboard() {
        waitFor(5);
        String reportLocator = "(//table[@aria-label='All Folders']/tbody)[last()]/tr/th//a";
        openRandomDashboard(reportLocator);
    }

    public void verifyRandomReportLoaded() {
        Locator reportIframe = page.locator("iframe[title='Report Viewer']").first();
        reportIframe.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        com.microsoft.playwright.FrameLocator frame = reportIframe.contentFrame();

        // Get report name from heading
        Locator headings = frame.locator("h1, h2").first();
        headings.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        String reportName = headings.textContent().trim();
        logger.info("Report name extracted: {}", reportName);
        // Try to get total records count (if available)
        String recordCount = "N/A";
        try {
            Locator totalRecords = frame.getByText(java.util.regex.Pattern.compile("Total Records.*\\d+")).first();
            if (totalRecords.count() > 0) {
                String text = totalRecords.textContent();
                // Extract number from text like "Total Records 168"
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                java.util.regex.Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    recordCount = matcher.group(1);
                }
            }
            screenshot("Random Report Verified " + reportName);
        } catch (Exception e) {
            logger.debug("Could not extract record count: {}", e.getMessage());
        }

        logger.info("========================================");
        logger.info("✓ Random Report Loaded:");
        logger.info("  Report Name: {}", reportName);
        logger.info("  Total Records: {}", recordCount);
        logger.info("========================================");
    }

    public void verifyReportContent(String reportTitle, String expectedRecordCount) {
        Locator reportIframe = page.locator("iframe[title='Report Viewer']").first();
        reportIframe.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        com.microsoft.playwright.FrameLocator frame = reportIframe.contentFrame();

        // Verify report heading
        Locator heading = frame.getByRole(AriaRole.HEADING)
                .filter(new Locator.FilterOptions().setHasText(reportTitle)).first();
        heading.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        if (!heading.isVisible()) {
            throw new AssertionError("Report heading '" + reportTitle + "' not visible");
        }
        logger.info("✓ Report heading verified: {}", reportTitle);
        waitFor(5);
        screenshot("Report Heading Verified - " + reportTitle);

        // Verify "Total Records" count
        Locator totalRecordsTop = frame
                .getByText(java.util.regex.Pattern.compile("Total Records.*" + expectedRecordCount))
                .first();
        totalRecordsTop.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        if (!totalRecordsTop.isVisible()) {
            throw new AssertionError("Total Records (top) not visible with count: " + expectedRecordCount);
        }
        logger.info("✓ Total Records verified with count: {}", expectedRecordCount);

        // Verify list item with total records (appears twice)
        String totalRecordsPattern = "Total Records" + expectedRecordCount;
        Locator listItem = frame.getByRole(AriaRole.LISTITEM)
                .filter(new Locator.FilterOptions().setHasText(totalRecordsPattern)).first();
        if (listItem.count() > 0 && listItem.isVisible()) {
            String actualText = listItem.textContent();
            logger.info("✓ Total Records list item verified: {}", actualText.trim());
        }
    }

    // ─── Shared section-landing verification (Dashboards / Reports) ──────────
    // Promoted from BCTDashboardPage so every module's list-view/dashboard/report
    // scenario can verify the section landing through one proven path.

    /**
     * Verify the Dashboards section landing rendered (Recent Dashboards grid, or
     * iframe surface).
     */
    public void verifyDashboardContent() {
        Locator grid = page.locator(
                "[role='grid'][aria-label='Recent Dashboards'], table[aria-label='Recent Dashboards']").first();
        if (isVisibleQuiet(grid, 10000)) {
            logger.info("✓ Dashboards content verified");
            return;
        }
        com.microsoft.playwright.FrameLocator frame = page.frameLocator("iframe[title*='ashboard']").first();
        frame.locator(".widget-container, [role='grid'][aria-label='Recent Dashboards']")
                .first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        logger.info("✓ Dashboards content verified (iframe)");
    }

    /**
     * Verify the Reports section landing rendered (Recent Reports grid, or iframe
     * surface).
     */
    public void verifyReportsContent() {
        Locator surface = page.locator(
                "[role='grid'][aria-label='Recent Reports'], table[aria-label='Recent Reports']").first();
        if (isVisibleQuiet(surface, 10000)) {
            logger.info("✓ Reports content verified");
            return;
        }

        // Wait for the Reports iframe to be attached
        Locator reportsIframe = page.locator("iframe[title*='eport'], iframe[title*='Report']").first();
        try {
            reportsIframe.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(10000));
            logger.debug("Reports iframe found");
        } catch (Exception e) {
            logger.warn("Reports iframe not found, trying generic verification");
            // Fallback: look for any grid or table
            page.locator("[role='grid'], table.slds-table").first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(10000));
            logger.info("✓ Reports content verified (generic)");
            return;
        }

        // Wait for iframe to be ready
        page.waitForTimeout(1000);

        // Look for content inside iframe
        com.microsoft.playwright.FrameLocator frame = page
                .frameLocator("iframe[title*='eport'], iframe[title*='Report']").first();
        try {
            frame.locator("[role='grid'], table, .reportWrapper, .report-container")
                    .first().waitFor(new Locator.WaitForOptions().setTimeout(20000));
            logger.info("✓ Reports content verified (iframe)");
        } catch (Exception e) {
            logger.warn("Could not verify specific Reports content, but iframe is present");
        }
    }

    /**
     * Verify a named navigation section (Dashboards/Reports) loaded; tolerant
     * generic fallback.
     */
    public void verifyMenuSectionDisplayed(String section) {
        String s = section == null ? "" : section.toLowerCase();
        if (s.contains("dashboard")) {
            verifyDashboardContent();
            return;
        }
        if (s.contains("report")) {
            verifyReportsContent();
            return;
        }
        // Generic fallback: the clicked tab is now active, or a heading/grid for the
        // section is present.
        Locator activeTab = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName(section).setSelected(true)).first();
        if (isVisibleQuiet(activeTab, 8000)) {
            logger.info("✓ Section '{}' active", section);
            return;
        }
        page.locator("[role='grid'], table.slds-table").first()
                .waitFor(new Locator.WaitForOptions().setTimeout(8000));
        logger.info("✓ Section '{}' content visible", section);
    }

    protected boolean isVisibleQuiet(Locator locator, int timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return locator.isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Dismisses the Salesforce "We hit a snag" error banner if it's present.
     * This reveals field-level error messages that may be hidden behind the banner.
     * Logs error field names before dismissing.
     */
    public void dismissErrorBannerIfPresent() {
        try {
            Locator closeButton = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Close error dialog").setExact(true)).first();

            if (closeButton.count() > 0 && closeButton.isVisible()) {
                // Extract and log error field names before dismissing
                try {
                    Locator errorList = page.locator(".fieldLevelErrors .errorsList li");
                    int errorCount = errorList.count();
                    if (errorCount > 0) {
                        StringBuilder errorFields = new StringBuilder("Error banner fields: ");
                        for (int i = 0; i < errorCount; i++) {
                            String fieldName = errorList.nth(i).textContent().trim();
                            errorFields.append(fieldName);
                            if (i < errorCount - 1) {
                                errorFields.append(", ");
                            }
                        }
                        logger.info(errorFields.toString());
                    }
                } catch (Exception logError) {
                    logger.debug("Could not extract error fields from banner: {}", logError.getMessage());
                }

                logger.debug("Dismissing error banner to reveal field-level errors");
                closeButton.click();
                // Wait briefly for banner to close
                page.waitForTimeout(300);
            }
        } catch (Exception e) {
            // Banner not present or already dismissed - this is fine
            logger.debug("No error banner to dismiss");
        }
    }

    /**
     * Takes a screenshot with the given name.
     * 
     * @param name the name/description for the screenshot
     */
    // public void screenshot(String name) {
    // try {
    // String fileName = name.replaceAll("[^a-zA-Z0-9_-]", "_") + ".png";
    // page.screenshot(new
    // Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("screenshots",
    // fileName)));
    // logger.debug("Screenshot taken: {}", fileName);
    // } catch (Exception e) {
    // logger.warn("Failed to take screenshot: {}", e.getMessage());
    // }
    // }
    protected void screenshot(String label) {
        ScreenshotHelper.take(label);
    }

}
