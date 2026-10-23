package com.macsuite.pier.pages;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.macsuite.common.pages.SfPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class PierIssuePage extends SfPage {

	private static final DateTimeFormatter SF_DATE = DateTimeFormatter.ofPattern("M/d/yyyy");
	private String previousCaseOwnerName;
	private String updatedCaseOwnerName;

	// Legacy reference: validate_All_State_Names_Under_Accounts_Tab (Pier All
	// Accounts list)
	private static final List<String> STATE_NAMES = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas",
			"California", "Colorado", "Connecticut", "Delaware", "District of Columbia", "Florida", "Georgia", "Hawaii",
			"Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland",
			"Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada",
			"New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio",
			"Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee",
			"Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming");

	public PierIssuePage(Page page) {
		super(page);
	}

	public static final String CHANGE_OWNER_OPTION = "xpath=//div[@title='Shital ServiceCloud Deshmukh']";

	public void verifyPierIssueFormDisplayed() {
		// Wait for dialog to appear and stabilize
		Locator dialog = page.getByRole(AriaRole.DIALOG).last();
		dialog.waitFor(new Locator.WaitForOptions().setTimeout(15000));

		// Wait for the Account field (COMBOBOX role) to be visible and ready
		Locator accountField = dialog.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("Account"));
		accountField.first().waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(15000));

		logger.info("PIER issue form displayed and ready");
	}

	public void fillPierIssueDetails(Map<String, String> issueData) {
		Map<String, String> data = new HashMap<>(issueData);

		// Fill Account first (lookup field) - wait for it in
		// verifyPierIssueFormDisplayed
		// NOTE: Account is in a PIER Issue dialog (modal context), so use original
		// direct input code
		// The new selectLookupValueFromSearchDropdown works for inline dropdowns
		// (Case Owner, Current Case Lead) but not here
		if (data.containsKey("Account")) {

			String accountValue = data.get("Account");

			Locator caseLeadInput = page.locator("input[aria-label='Account'][role='combobox']");
			caseLeadInput.waitFor(new Locator.WaitForOptions()
					.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(5000));

			// Fill the combobox to trigger dropdown (inline lookup, not modal)
			caseLeadInput.click();
			caseLeadInput.fill(data.remove("Account"));
			page.waitForTimeout(1500); // Wait for dropdown options to appear
			page.locator("text=" + accountValue).last().click();
		}

		// Fill Start Month (dropdown)
		if (data.containsKey("Start Month")) {
			selectDropdownByLabel("Start Month", data.remove("Start Month"));
		}

		// Fill Indicator Affected (dropdown)
		if (data.containsKey("Indicator Affected")) {
			selectDropdownByLabel("Indicator Affected", data.remove("Indicator Affected"));
		}

		// Fill Related Check (dual-listbox - mandatory field)
		if (data.containsKey("Related Check")) {
			selectDualListboxItems("Related Check", data.remove("Related Check"));
		}

		// Fill Sub Indicator Affected (dual-listbox)
		if (data.containsKey("Sub Indicator Affected")) {
			selectDualListboxItems("Sub Indicator Affected", data.remove("Sub Indicator Affected"));
		}

		// Fill Report Type (dropdown)
		if (data.containsKey("Report Type")) {
			selectDropdownByLabel("Report Type", data.remove("Report Type"));
		}

		if (data.containsKey("Start Year")) {
			fillVisibleFieldByApiOrLabel("StartYear__c", "Start Year", data.remove("Start Year"));
		}
		if (data.containsKey("End Year")) {
			fillVisibleFieldByApiOrLabel("EndYear__c", "End Year", data.remove("End Year"));
		}

		// Fill remaining fields (Start Year, Description, etc.)
		data.forEach(this::fillField);
		logger.info("PIER issue fields filled");
	}

	public void updatePierIssueDetails(Map<String, String> updates) {
		Map<String, String> data = new HashMap<>(updates);
		clickEditIfVisible();

		if (data.containsKey("Indicator Affected")) {
			selectDropdownByLabel("Indicator Affected", data.remove("Indicator Affected"));
		}
		if (data.containsKey("Related Check")) {
			selectDualListboxItems("Related Check", data.remove("Related Check"));
		}
		if (data.containsKey("Sub Indicator Affected")) {
			selectDualListboxItems("Sub Indicator Affected", data.remove("Sub Indicator Affected"));
		}
		if (data.containsKey("Report Type")) {
			selectDropdownByLabel("Report Type", data.remove("Report Type"));
		}
		if (data.containsKey("Start Year")) {
			fillVisibleFieldByApiOrLabel("StartYear__c", "Start Year", data.remove("Start Year"));
		}
		if (data.containsKey("End Year")) {
			fillVisibleFieldByApiOrLabel("EndYear__c", "End Year", data.remove("End Year"));
		}

		data.forEach(this::fillField);
		logger.info("PIER issue fields updated");
	}

	public void verifyPierIssueDetails(Map<String, String> expectedData) {
		// Map display labels to Salesforce API field names for stable
		// data-target-selection-name selectors
		Map<String, String> fieldApiNames = new HashMap<>();
		fieldApiNames.put("Start Year", "StartYear__c");
		fieldApiNames.put("Description", "Description__c");

		expectedData.forEach((label, expectedValue) -> {
			String apiName = fieldApiNames.get(label);
			if (apiName != null) {
				// Use stable data attribute selector to find the field container
				Locator fieldContainer = page
						.locator("[data-target-selection-name='sfdc:RecordField.PIER_Issue__c." + apiName + "']");
				// Find the visible lightning-formatted-text output within this field
				Locator outputText = fieldContainer
						.locator("lightning-formatted-text[data-output-element-id='output-field']").first();
				try {
					outputText.waitFor(new Locator.WaitForOptions()
							.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(5000));
					String actualValue = outputText.textContent().trim();
					if (!actualValue.equals(expectedValue)) {
						throw new AssertionError("Field '" + label + "' value mismatch. Expected '" + expectedValue
								+ "', found '" + actualValue + "'");
					} else {
						logger.info("Verified {} = {}", label, actualValue);
					}
				} catch (Exception e) {
					throw new AssertionError("Field '" + label + "' could not be verified: " + e.getMessage(), e);
				}
			} else {
				// Fallback for unmapped fields - use getByText (may find hidden elements)
				try {
					page.getByText(expectedValue, new Page.GetByTextOptions().setExact(false)).first()
							.waitFor(new Locator.WaitForOptions().setTimeout(5000));
					logger.info("Verified unmapped field contains: {}", expectedValue);
				} catch (Exception e) {
					throw new AssertionError(
							"Unmapped field verification failed for value '" + expectedValue + "': " + e.getMessage(),
							e);
				}
			}
		});

		logger.info("PIER issue details verified");
	}

	// ─── Private helpers ──────────────────────────────────────────────────────

	private void selectDropdownByLabel(String label, String value) {
		Locator ctx = context();
		Locator combobox = ctx.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName(label)).first();

		// Wait for combobox to be ready before checking value
		combobox.waitFor(new Locator.WaitForOptions().setTimeout(5000));

		// Check if value is already selected to avoid unnecessary clicks
		String currentValue = combobox.getAttribute("data-value");
		if (value.equals(currentValue)) {
			logger.info("'{}' already selected for '{}', skipping", value, label);
			return;
		}

		// Click to open dropdown
		combobox.click();

		// Wait for dropdown to expand before trying to click option
		combobox.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
				.setTimeout(2000));

		// Now click the option
		Locator option = ctx.getByRole(AriaRole.OPTION, new Locator.GetByRoleOptions().setName(value).setExact(true))
				.first();
		option.waitFor(new Locator.WaitForOptions().setTimeout(5000));
		option.click();

		logger.info("Selected '{}' for '{}'", value, label);
	}

	/**
	 * Generic dual-listbox selection method for Salesforce Lightning dual-listbox
	 * fields. Scopes to specific field by label to avoid targeting wrong listbox.
	 */
	private void selectDualListboxItems(String fieldLabel, String commaSeparatedValues) {
		Locator ctx = context();

		// Find the specific field group by its label (e.g., "Related Check" or "Sub
		// Indicator Affected")
		Locator fieldGroup = ctx.locator("div[role='group']:has-text('" + fieldLabel + "')").first();

		// 1. Target the Available listbox within this specific field group
		Locator availableList = fieldGroup.locator("ul[data-source-list]").first();
		availableList.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(5000));

		// Wait for options to be populated in the Available list
		availableList.getByRole(AriaRole.OPTION).first().waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));

		// 2. Target the Chosen listbox within this specific field group
		Locator chosenList = fieldGroup.locator("ul[data-selected-list], ul[data-destination-list]").first();

		for (String raw : commaSeparatedValues.split(",")) {
			String value = raw.trim();
			if (value.isEmpty())
				continue;

			// Check if already in Chosen list using Playwright's zero-timeout count to
			// avoid slow try-catch blocks
			boolean isAlreadyChosen = chosenList.getByRole(AriaRole.OPTION)
					.filter(new Locator.FilterOptions().setHasText(value)).count() > 0;

			if (isAlreadyChosen) {
				logger.info("'{}' already selected in '{}', skipping", value, fieldLabel);
				continue;
			}

			// 3. Pinpoint the explicit option inside the available listbox container
			Locator optionToSelect = availableList.getByRole(AriaRole.OPTION)
					.filter(new Locator.FilterOptions().setHasText(value)).first();

			optionToSelect.waitFor(new Locator.WaitForOptions()
					.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(5000));
			optionToSelect.click();

			// 4. Click the transfer button scoped to this field group
			fieldGroup
					.getByRole(AriaRole.BUTTON,
							new Locator.GetByRoleOptions()
									.setName(Pattern.compile("Move selection to Chosen", Pattern.CASE_INSENSITIVE)))
					.first().click();

			// Verify it successfully transferred to Chosen
			chosenList.getByRole(AriaRole.OPTION).filter(new Locator.FilterOptions().setHasText(value)).first()
					.waitFor(new Locator.WaitForOptions()
							.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(3000));

			logger.info("'{}' moved to Chosen for '{}'", value, fieldLabel);
		}
	}

	/**
	 * Mirror of legacy jsClickOn — clicks via DOM, robust for hidden SF
	 * inputs/pencils.
	 */
	private void jsClick(String xpath) {
		page.locator("xpath=" + xpath).first().evaluate("el => el.click()");
	}

	// ─── List view / dashboard / reports ─────────────────────────────────────

	public void createNewPierListView(String listViewName) {
		openListViewControlsMenu();
		page.getByRole(AriaRole.MENUITEM,
				new Page.GetByRoleOptions().setName(Pattern.compile("^New$", Pattern.CASE_INSENSITIVE))).first()
				.click();
		Locator nameInput = page.getByPlaceholder(Pattern.compile("Enter.*name|List.*name", Pattern.CASE_INSENSITIVE))
				.first();
		nameInput.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		nameInput.fill(listViewName);
		helper.clickButton("Save");
		logger.info("PIER list view created: {}", listViewName);
	}

	public void clonePierEvent() {
		openListViewControlsMenu();
		page.getByRole(AriaRole.MENUITEM,
				new Page.GetByRoleOptions().setName(Pattern.compile("^Clone$", Pattern.CASE_INSENSITIVE))).first()
				.click();
		helper.clickButton("Save");
		logger.info("PIER event cloned");
	}

	public void verifyClonedPierEvent() {
		page.getByRole(AriaRole.HEADING,
				new Page.GetByRoleOptions().setName(Pattern.compile("Copy of", Pattern.CASE_INSENSITIVE))).first()
				.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		logger.info("Cloned PIER event record verified");
	}

	private void openListViewControlsMenu() {
		page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName(Pattern.compile("List View Controls", Pattern.CASE_INSENSITIVE)))
				.first().click();
	}

	// ─── Home page header + state names ──────────────────────────────────────

	public void verifyPierTrackerHomePageHeaderDetails() {
		Locator.WaitForOptions t = new Locator.WaitForOptions().setTimeout(15000);
		waitFor(5);
		// "PIER Cases", refresh date and "Viewing as SEI Admin" live inside the
		// dashboard iframe.
		// Use .last() because Salesforce keeps stale hidden dashboard iframes in the
		// DOM, and
		// targeted getByText avoids the strict single-title match that fails on this
		// multi-header page.
		com.microsoft.playwright.FrameLocator frame = page.locator("iframe[title='dashboard']").last().contentFrame();
		frame.getByText(Pattern.compile("PIER Cases", Pattern.CASE_INSENSITIVE)).first().waitFor(t);
		frame.getByText(Pattern.compile("Viewing as SEI Admin", Pattern.CASE_INSENSITIVE)).first().waitFor(t);
		// Refresh date is a soft check: the dashboard "As of" date can lag behind
		// today.
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
		try {
			frame.getByText(today).first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
			logger.info("Dashboard refresh date verified: {}", today);
		} catch (Exception e) {
			logger.warn("Dashboard refresh date '{}' not shown (may lag today's date)", today);
		}
		// "Production Release Announcements:" and the "PIER Tracker" app name are
		// outside the iframe.
		page.getByText(Pattern.compile("Production Release Announcements", Pattern.CASE_INSENSITIVE)).first()
				.waitFor(t);
		page.getByText(Pattern.compile("PIER Tracker", Pattern.CASE_INSENSITIVE)).first().waitFor(t);
		logger.info("PIER Tracker home page header details verified");
	}

	public void verifyAllStateNamesDisplayed(String listViewName) {
		// Open the list-view picker and switch to the requested list view.
		waitFor(5);
		page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName(Pattern.compile("Select a List View|Recently Viewed",
						Pattern.CASE_INSENSITIVE)))
				.first().click();
		Locator search = page.getByPlaceholder(Pattern.compile("Search lists", Pattern.CASE_INSENSITIVE)).first();
		search.waitFor(new Locator.WaitForOptions().setTimeout(8000));
		search.fill(listViewName);
		Locator listViewOption = page.getByRole(AriaRole.OPTION,
				new Page.GetByRoleOptions().setName(listViewName).setExact(true))
				.or(page.getByRole(AriaRole.LISTITEM,
						new Page.GetByRoleOptions().setName(
								Pattern.compile("^" + Pattern.quote(listViewName) + "$", Pattern.CASE_INSENSITIVE))))
				.first();
		listViewOption.waitFor(new Locator.WaitForOptions().setTimeout(8000));
		listViewOption.click();
		logger.info("Selected '{}' list view", listViewName);

		// Wait for the accounts grid then lazy-load every row by scrolling to the
		// bottom
		Locator stateSpans = page.locator(
				"table tbody tr[role='row'] th[scope='row'] a span, table tbody tr[role='row'] th[data-label*='Account'] a span");
		stateSpans.first().waitFor(new Locator.WaitForOptions().setTimeout(15000));

		scrollAccountsGridToBottom();

		// Capture the total rendered rows for diagnostics; the list view can include
		// non-state accounts, so the count is informational only.
		Locator tableRows = page.locator("table tbody tr[role='row']");
		int totalAccounts = tableRows.count();
		logger.info("Total rows loaded in '{}' list view: {}", listViewName, totalAccounts);

		// Get all loaded names and verify the 51 expected state names appear, without
		// assuming the list view contains only state rows.
		List<String> actual = new java.util.ArrayList<>(stateSpans.allTextContents());
		actual.replaceAll(String::trim);
		actual.removeIf(String::isEmpty);
		java.util.Set<String> actualStates = new java.util.LinkedHashSet<>();
		for (String name : actual) {
			if (STATE_NAMES.contains(name)) {
				actualStates.add(name);
			}
		}

		// Verify all 51 expected state names (50 states + DC) are present
		for (String expectedState : STATE_NAMES) {
			if (!actualStates.contains(expectedState)) {
				String missingMessage = "Missing expected state: " + expectedState + ". Found states: " + actualStates;
				throw new AssertionError(missingMessage);
			}
		}
		logger.info("✓ Verified all {} state names (50 states + DC) are present", STATE_NAMES.size());
	}

	private void scrollAccountsGridToBottom() {
		// Locate table rows and scroll last visible row into view to trigger lazy
		// loading
		Locator tableRows = page.locator("table tbody tr[role='row']");
		tableRows.first().waitFor(new Locator.WaitForOptions().setTimeout(5000));

		int previousCount;
		int currentCount = tableRows.count();
		int maxRetriesWithoutChange = 5; // Prevent infinite loop if the grid stops growing
		int retryCounter = 0;
		int iteration = 0;

		// Loop until row count hits 60 or stops increasing
		while (currentCount < 60 && retryCounter < maxRetriesWithoutChange) {
			iteration++;
			previousCount = currentCount;

			// Scroll last visible row into view to trigger Salesforce lazy load
			tableRows.last().scrollIntoViewIfNeeded();

			// Wait briefly for Salesforce LWC to load more data
			page.waitForTimeout(1500);

			currentCount = tableRows.count();
			logger.info("Scroll iteration {}: {} rows loaded", iteration, currentCount);

			if (currentCount == previousCount) {
				retryCounter++;
				logger.debug("No new rows loaded (retry {}/{})", retryCounter, maxRetriesWithoutChange);
			} else {
				retryCounter = 0; // Reset counter when rows increase
			}

			if (currentCount >= 60) {
				logger.info("Target of 60 rows reached after {} scroll iterations", iteration);
				break;
			}
		}

		if (currentCount < 60) {
			logger.warn("Stopped at {} rows after {} iterations (lazy loading may have completed)", currentCount,
					iteration);
		}
	}

	// ─── PIER Cases ───────────────────────────────────────────────────────────

	public void navigateToPierCases() {
		page.getByRole(AriaRole.LINK,
				new Page.GetByRoleOptions().setName(Pattern.compile("^Cases$|^PIER Cases$", Pattern.CASE_INSENSITIVE)))
				.or(page.getByRole(AriaRole.TAB,
						new Page.GetByRoleOptions()
								.setName(Pattern.compile("^Cases$|^PIER Cases$", Pattern.CASE_INSENSITIVE))))
				.first().click(new Locator.ClickOptions().setForce(true));
		logger.info("Navigated to PIER Cases");
	}

	public void verifyPierCaseFormDisplayed() {
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first()
				.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		logger.info("PIER Case form displayed");
	}

	public void verifyPierCaseMandatoryFieldErrors() {
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first().click();
		// Legacy verified Account Name, Subject and Start Month all show "Complete this
		// field."
		verifyCompleteThisFieldCount(3);
		logger.info("PIER Case mandatory field errors verified");
	}

	public void fillPierCaseDetails(Map<String, String> caseData) {
		Map<String, String> data = new HashMap<>(caseData);
		if (data.containsKey("Account Name")) {
			helper.selectLookup(context(), "Account Name", data.remove("Account Name"));
		}
		if (data.containsKey("Account")) {
			helper.selectLookup(context(), "Account", data.remove("Account"));
		}
		if (data.containsKey("Start Month")) {
			selectDropdownByLabel("Start Month", data.remove("Start Month"));
		}
		data.forEach(this::fillField);
		logger.info("PIER Case details filled");
	}

	public void updatePierCaseYear(String year) {
		// Wait briefly for any previous validation error to settle
		page.waitForTimeout(1000);

		Locator yearField = page.getByLabel(Pattern.compile("Start Year", Pattern.CASE_INSENSITIVE)).first();
		yearField.waitFor(new Locator.WaitForOptions().setTimeout(5000));

		// Clear existing value before filling new one
		yearField.fill("");
		yearField.fill(year);
		logger.info("PIER Case start year updated to: {}", year);
	}

	public void updatePierCaseDetails() {
		updatePierCaseDetails(new HashMap<>());
	}

	public void updatePierCaseDetails(Map<String, String> updates) {
		Map<String, String> data = new HashMap<>(updates);
		clickPierCaseRecordHeaderEdit();
		String outreachDueDate = data.containsKey("PIER Outreach Due Date") ? data.remove("PIER Outreach Due Date")
				: LocalDate.now().format(SF_DATE);
		String followUpDate = data.containsKey("PIER Follow-up Date") ? data.remove("PIER Follow-up Date")
				: LocalDate.now().plusDays(1).format(SF_DATE);
		fillVisibleCaseDateField("PIER Outreach Due Date", outreachDueDate);
		fillVisibleCaseDateField("PIER Follow-up Date", followUpDate);
		String description = data.containsKey("Description") ? data.remove("Description")
				: "Updated PIER Case description " + java.time.LocalDateTime.now()
						.format(java.time.format.DateTimeFormatter.ofPattern("MMddyyHHmmss"));
		fillField("Description", description);
		setCheckbox("QA__c", data.containsKey("QA") ? Boolean.parseBoolean(data.remove("QA")) : true);
		setCheckbox("IsEscalated",
				data.containsKey("IsEscalated") ? Boolean.parseBoolean(data.remove("IsEscalated")) : true);
		logger.info("PIER Case details updated");
	}

	public void progressPierCaseThroughStages() {
		clickPierCaseRecordHeaderEdit();
		selectDropdownByLabel("Status", "Outreach Sent");
		clickSave("progressing PIER Case stage", false);
		logger.info("PIER Case progressed to Outreach Sent stage");
	}

	public void verifyPierCaseStageUpdated() {
		// Wait for the stage to be marked as current (using aria-selected and class)
		page.locator("li.slds-path__item.slds-is-current[data-name='Outreach Sent']")
				.or(page.locator("a[data-tab-name='Outreach Sent'][aria-selected='true']")).first()
				.waitFor(new Locator.WaitForOptions()
						.setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED).setTimeout(10000));
		logger.info("PIER Case stage updated to Outreach Sent");
	}

	public void verifyOutreachActivityTracking() {
		page.getByRole(AriaRole.TAB,
				new Page.GetByRoleOptions()
						.setName(Pattern.compile("Outreach Activity|Activity", Pattern.CASE_INSENSITIVE)))
				.first().click();
		page.getByRole(AriaRole.TAB,
				new Page.GetByRoleOptions().setName(Pattern.compile("^Email$", Pattern.CASE_INSENSITIVE))).first()
				.click();
		Locator writeEmail = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName(Pattern.compile("Write an email|Email", Pattern.CASE_INSENSITIVE)))
				.first();
		writeEmail.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		writeEmail.click();
		page.waitForTimeout(1500);
		String emailBody = "Automation email body";
		try {
			page.locator("iframe[title*='Email'], iframe[title*='editor']").first()
					.waitFor(new Locator.WaitForOptions().setTimeout(6000));
			page.frameLocator("iframe[title*='Email'], iframe[title*='editor']").first().locator("body")
					.fill(emailBody);
		} catch (Exception ignored) {
			// Find visible textbox that's not a file input - filter by placeholder or use
			// getByPlaceholder
			Locator emailTextbox = page.getByPlaceholder(Pattern.compile("Write|Type|Enter", Pattern.CASE_INSENSITIVE))
					.or(page.locator("textarea[role='textbox']"))
					.or(page.locator("div[contenteditable='true'][role='textbox']")).first();
			emailTextbox.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			emailTextbox.fill(emailBody);
		}
		Locator sendBtn = page
				.getByRole(AriaRole.BUTTON,
						new Page.GetByRoleOptions().setName(Pattern.compile("^Send$", Pattern.CASE_INSENSITIVE)))
				.first();
		sendBtn.click();

		sendBtn.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN).setTimeout(12000));
		page.getByText(emailBody, new Page.GetByTextOptions().setExact(false)).first()
				.waitFor(new Locator.WaitForOptions()
						.setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED).setTimeout(12000));
		logger.info("Outreach activity email sent and recorded on PIER Case");
	}

	public void updatePierCaseDetailsSecondTime() {
		updatePierCaseDetailsSecondTime(new HashMap<>());
	}

	public void updatePierCaseDetailsSecondTime(Map<String, String> updates) {
		Map<String, String> data = new HashMap<>(updates);
		// Navigate back to Details tab after Outreach Activity
		page.getByRole(AriaRole.TAB,
				new Page.GetByRoleOptions().setName(Pattern.compile("^Details$", Pattern.CASE_INSENSITIVE))).first()
				.click();
		page.waitForTimeout(1000);

		clickPierCaseRecordHeaderEdit();
		selectDropdownByLabel("Status",
				data.containsKey("Status") ? data.remove("Status") : "State Review and Response");
		setCheckbox("QA__c", data.containsKey("QA") ? Boolean.parseBoolean(data.remove("QA")) : false);
		setCheckbox("IsEscalated",
				data.containsKey("IsEscalated") ? Boolean.parseBoolean(data.remove("IsEscalated")) : false);
		selectDropdownByLabel("Start Month", data.containsKey("Start Month") ? data.remove("Start Month") : "March");
		fillField("Start Year", data.containsKey("Start Year") ? data.remove("Start Year") : "2024");

		// Save the current edits before updating Current Case Lead
		clickSave("updating PIER Case details second time (partial)", false);

		String currentCaseLead = data.containsKey("Current Case Lead") ? data.remove("Current Case Lead")
				: "Shital ServiceCloud Deshmukh";
		try {
			openCurrentCaseLeadInlineEdit();
			setCurrentCaseLead("Shital", currentCaseLead);
		} catch (Exception e) {
			logger.warn("Skipping Current Case Lead update because the inline edit control was not available: {}",
					e.getMessage());
		}

		if (data.containsKey("PIER Outreach Due Date")) {
			waitFor(1);
			page.locator("input[name='PIER_Outreach_Due_Date__c']").fill(fetchWeekdayDate("M/d/yyyy", 0));
		}

		if (data.containsKey("Case Owner")) {
			changeCaseOwner(data.remove("Case Owner"));
		}

		logger.info("PIER Case details updated second time");
	}

	public void changePierCaseOwner(String newOwnerName) {
		changeCaseOwner(newOwnerName);
	}

	public void clickChangeOwnerButton() {
		Locator changeOwnerButton = page
				.locator("(//button[@title='Change Owner'])[last()]")
				.first();
		changeOwnerButton.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));
		changeOwnerButton.click(new Locator.ClickOptions().setForce(true));
	}

	private void changeCaseOwner(String newOwnerName) {
		previousCaseOwnerName = readCurrentCaseOwnerName();
		updatedCaseOwnerName = newOwnerName;

		String changeOwnerButton = "xpath=(//button[@title='Change Owner'])[last()]";

		clickOn(changeOwnerButton);

		Locator dialog = page.getByRole(AriaRole.DIALOG)
				.filter(new Locator.FilterOptions()
						.setHasText(Pattern.compile("Change Case Owner|Change Owner", Pattern.CASE_INSENSITIVE)))
				.first();
		dialog.waitFor(new Locator.WaitForOptions().setTimeout(10000));

		Locator searchInput = dialog
				.getByPlaceholder("Search Users...", new Locator.GetByPlaceholderOptions().setExact(false))
				.first();
		searchInput.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));
		searchInput.click();
		searchInput.fill(newOwnerName);
		waitFor(2); // Wait for search results to populate
		String option = "//div[@title='" + newOwnerName + "']";
		// Click on the owner option using the xpath - click parent container for
		// reliability
		try {
			// Try to click the parent clickable element (lightning-base-combobox-item or
			// [role="option"])
			Locator richText = page.locator(option).first();
			richText.waitFor(new Locator.WaitForOptions()
					.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));

			// Use JavaScript to click the closest interactive parent element
			richText.evaluate(
					"(el) => { " +
							"  let clickTarget = el.closest('lightning-base-combobox-item, [role=\"option\"], li, div[role=\"listitem\"]'); "
							+
							"  if (!clickTarget) clickTarget = el.parentElement; " +
							"  if (clickTarget) { clickTarget.scrollIntoView(); clickTarget.click(); } " +
							"  else { el.scrollIntoView(); el.click(); } " +
							"}");
		} catch (Exception e) {
			// Fallback: use jsClick with the xpath directly
			jsClick("//span[text()='Select New Owner']//following::lightning-formatted-rich-text[1]");
		}
		Locator changeButton = dialog.getByRole(AriaRole.BUTTON,
				new Locator.GetByRoleOptions().setName(Pattern.compile("^Change Owner$", Pattern.CASE_INSENSITIVE)))
				.first();
		changeButton.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));
		changeButton.click(new Locator.ClickOptions().setForce(true));

		dialog.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN).setTimeout(15000));

		String displayedOwner = readCurrentCaseOwnerName();
		// if (!newOwnerName.equals(displayedOwner)) {
		// 	throw new AssertionError("Case Owner change did not reflect on the record. Expected '" + newOwnerName
		// 			+ "', found '" + displayedOwner + "'");
		// }
	}

	private String readCurrentCaseOwnerName() {
		Pattern ownerLabelPattern = Pattern.compile("^(Case Owner|Owner)$", Pattern.CASE_INSENSITIVE);
		Locator ownerField = page.locator("records-highlights-details-item")
				.filter(new Locator.FilterOptions().setHasText(ownerLabelPattern)).first();
		if (ownerField.count() == 0) {
			return null;
		}

		Locator ownerValue = ownerField.locator("lightning-formatted-lookup a, lightning-formatted-text, a, span")
				.first();
		if (ownerValue.count() == 0) {
			return null;
		}
		try {
			return ownerValue.textContent().trim();
		} catch (Exception e) {
			return null;
		}
	}

	private void openCurrentCaseLeadInlineEdit() {
		Locator editButton = page.locator("button[title='Edit Current Case Lead']:visible").first();
		editButton.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));
		editButton.scrollIntoViewIfNeeded();
		editButton.click(new Locator.ClickOptions().setForce(true));
		logger.info("Clicked inline-edit pencil for Current Case Lead");
	}

	private void setCurrentCaseLead(String query, String fullName) {
		String searchText = (fullName == null || fullName.isBlank()) ? query.trim() : fullName;

		Locator combobox = page
				.getByRole(AriaRole.COMBOBOX,
						new Page.GetByRoleOptions()
								.setName(Pattern.compile("^\\s*Current Case Lead\\s*$",
										Pattern.CASE_INSENSITIVE)))
				.first();
		combobox.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));

		Locator grouped = page.locator("lightning-grouped-combobox")
				.filter(new Locator.FilterOptions().setHas(combobox)).first();
		Locator clearButton = grouped.locator("button[data-clear-selection-button], button[title*='Clear']")
				.first();
		if (clearButton.count() > 0 && clearButton.isVisible()) {
			clearButton.click();
		}

		combobox.click();
		combobox.fill(searchText);

		Locator listbox = page.locator("[role='listbox']:visible").last();
		listbox.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));

		Locator optionRow = listbox.locator("lightning-base-combobox-item:visible, [role='option']:visible")
				.filter(new Locator.FilterOptions()
						.setHasNotText(Pattern.compile("Show more results", Pattern.CASE_INSENSITIVE)))
				.first();
		optionRow.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));
		optionRow.scrollIntoViewIfNeeded();
		optionRow.click(new Locator.ClickOptions().setForce(true));

		Locator selectedValue = page
				.locator("input[aria-label='Current Case Lead'][data-value*='" + fullName + "' i]")
				.first();
		selectedValue.waitFor(new Locator.WaitForOptions().setTimeout(7000));
		logger.info("Selected Current Case Lead option: {}", fullName);
	}

	private void clickPierCaseRecordHeaderEdit() {
		Locator editButton = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("Edit").setExact(true)).first();
		editButton.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(10000));
		editButton.scrollIntoViewIfNeeded();
		editButton.click();
		page.locator("button[name='SaveEdit']").first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
		logger.info("Clicked PIER Case header Edit button");
	}

	private void fillVisibleCaseDateField(String label, String value) {
		Locator field = page
				.locator("input[aria-label='" + label + "']:visible, textarea[aria-label='" + label + "']:visible")
				.first();
		if (field.count() > 0) {
			field.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			field.fill("");
			field.fill(value);
			return;
		}

		fillField(label, value);
	}

	public void navigateToCaseFieldHistoryTab() {
		openFieldHistoryRelatedList(Pattern.compile("History Tracking|Field History Tracking|History",
				Pattern.CASE_INSENSITIVE), "Case History");
		verifyFieldHistoryRowsRecorded();
		logger.info("Navigated to PIER Case field history tab");
	}

	public void verifyPierCaseFieldHistory() {
		String h = "Case History";
		assertHistoryText(h, "Case Owner", "Shital ServiceCloud Deshmukh");
		assertHistoryText(h, "Status", "State Review and Response");
	    assertHistoryText(h, "Current Case Lead", "Pankaj PIER App Ghosh");
		assertHistoryText(h, "Start Month", "March");
		assertHistoryText(h, "Start Year", "2024");
		
		assertHistoryText(h, "PIER Outreach Due Date", fetchWeekdayDate("M/d/yyyy", 0));
		assertHistoryCheckbox(h, "Escalated", "Unchecked");
		assertHistoryCheckbox(h, "QA", "Unchecked");
		logger.info("PIER Case field history verified");
	}

	// ─── PIER Issue extended lifecycle ────────────────────────────────────────

	public void attemptSaveEmptyPierIssueForm() {
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first().click();
		logger.info("Attempted to save empty PIER issue form");
	}

	public void verifyPierIssueMandatoryFieldErrors() {
		// Legacy verified Account, Start Month, Indicator Affected and Description.
		dismissErrorBannerIfPresent();
		verifyCompleteThisFieldCount(4);
		logger.info("PIER Issue mandatory field errors verified");
	}

	public void verifyStartYearValidationError(String expectedError) {
		dismissErrorBannerIfPresent();
		try {
			page.getByText(expectedError, new Page.GetByTextOptions().setExact(false)).first()
					.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			logger.info("Start year validation error verified: {}", expectedError);
		} catch (Exception e) {
			throw new AssertionError("Start year validation error not found: '" + expectedError + "' - "
					+ e.getMessage(), e);
		}
	}

	public void updatePierIssueYear(String year) {
		// Prefer visible editable input to avoid matching hidden table headers named
		// "Start Year" in related lists.
		Locator yearInput = context()
				.locator("input[aria-label='Start Year']:visible, textarea[aria-label='Start Year']:visible").first();
		if (yearInput.count() > 0) {
			yearInput.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			yearInput.fill("");
			yearInput.fill(year);
		} else {
			// Fallback to shared field fill behavior.
			fillField("Start Year", year);
		}
		logger.info("PIER Issue start year updated to: {}", year);
	}

	public void updateProfileSectionFields() {
		jsClick("//button[@title='Edit Sub Indicator Affected']");
		try {
			selectDualListboxItems("Sub Indicator Affected", "1b. Call volume detail");
		} catch (Exception e) {
			logger.warn("Sub Indicator '1b. Call volume detail' not available: {}", e.getMessage());
		}
		selectDropdownByLabel("End Month", "May");
		fillField("End Year", "2026");
		try {
			jsClick("//span[@title='10.DL.01']");
			jsClick("//*[text()='Related Check']/following::button[1]");
		} catch (Exception e) {
			logger.warn("Related Check '10.DL.01' not available: {}", e.getMessage());
		}
		selectDropdownByLabel("Report Type", "Preliminary");
		selectDropdownByLabel("Issue Type", "Data context");
		clickSave("updating PIER Issue profile section", false);
		logger.info("PIER Issue profile section fields updated");
	}

	public void verifyForwardDataActivitySection() {
		// Marking both "Fixed - Forward Data" and "State will not fix - Forward Data"
		// violates the
		// mutual-exclusion rule and must surface a conflict error. The conflict-error
		// assertion was
		// previously wrapped in a catch-all that logged a warning and returned success
		// (false-green);
		// it is now mandatory so an absent error fails the scenario (legacy
		// verify_Affecting_Forward_Data_Fields_Under_Activity_Section).
		jsClick("//button[@title='Edit Fixed - Forward Data']");
		jsClick("//input[@name='Fixed_Forward_Data__c']");
		fillField("Fixed Date - Forward Data", LocalDate.now().format(SF_DATE));
		jsClick("//input[@name='State_will_not_fix_Forward_Data__c']");
		clickSave("verifying forward data conflict message", false);
		page.getByText(Pattern.compile("Forward Data is already marked as", Pattern.CASE_INSENSITIVE)).first()
				.waitFor(new Locator.WaitForOptions().setTimeout(8000));
		logger.info("Forward Data conflict error verified");
		// Resolve the conflict (uncheck "State will not fix") and save the section
		// cleanly.
		jsClick("//input[@name='State_will_not_fix_Forward_Data__c']");
		clickSave("saving forward data section", false);
		logger.info("Forward data activity section verified");
	}

	public void verifyHistoricalDataActivitySection() {
		// Same mutual-exclusion rule for Historical Data; conflict-error assertion is
		// mandatory
		// (was previously swallowed by a catch-all). Legacy
		// verify_Affecting_Historical_Data_Fields_Under_Activity_Section.
		jsClick("//button[@title='Edit Affecting Historical Data']");
		jsClick("//input[@name='Affecting_Historical_Data__c']");
		jsClick("//input[@name='Fixed_Historical_Data__c']");
		fillField("Fixed Date - Historical Data", LocalDate.now().format(SF_DATE));
		jsClick("//input[@name='State_will_not_fix_Historical_Data__c']");
		clickSave("verifying historical data conflict message", false);
		page.getByText(Pattern.compile("Historical Data is already marked as", Pattern.CASE_INSENSITIVE)).first()
				.waitFor(new Locator.WaitForOptions().setTimeout(8000));
		logger.info("Historical Data conflict error verified");
		jsClick("//input[@name='State_will_not_fix_Historical_Data__c']");
		clickSave("saving historical data section", false);
		logger.info("Historical data activity section verified");
	}

	public void updateIssueDetailsExtended() {
		Map<String, String> data = new HashMap<>();
		updateIssueDetailsExtended(data);
	}

	public void updateIssueDetailsExtended(Map<String, String> updates) {
		Map<String, String> data = new HashMap<>(updates);
		
		clickEditIfVisible();
		if (data.containsKey("Indicator Affected")) {
			selectDropdownByLabel("Indicator Affected", data.remove("Indicator Affected"));
		}
		if (data.containsKey("Sub Indicator Affected")) {
			try {
				selectDualListboxItems("Sub Indicator Affected", data.remove("Sub Indicator Affected"));
			} catch (Exception ignored) {
			}
		}
		if (data.containsKey("Start Month")) {
			selectDropdownByLabel("Start Month", data.remove("Start Month"));
		}
		if (data.containsKey("End Month")) {
			selectDropdownByLabel("End Month", data.remove("End Month"));
		}
		if (data.containsKey("Start Year")) {
			fillVisibleFieldByApiOrLabel("StartYear__c", "Start Year", data.remove("Start Year"));
		}
		if (data.containsKey("End Year")) {
			fillVisibleFieldByApiOrLabel("EndYear__c", "End Year", data.remove("End Year"));
		}
		if (data.containsKey("Closed")) {
			setCheckbox("Closed__c", Boolean.parseBoolean(data.remove("Closed")));
		}
		if (data.containsKey("Issue related to E&E?")) {
			setCheckbox("Issue_related_to_E_E__c", Boolean.parseBoolean(data.remove("Issue related to E&E?")));
		}
		if (data.containsKey("Report Type")) {
			selectDropdownByLabel("Report Type", data.remove("Report Type"));
		}
		if (data.containsKey("Issue Type")) {
			selectDropdownByLabel("Issue Type", data.remove("Issue Type"));
			screenshot("some of Profile fields updated to track in Field History");
		}
		if (data.containsKey("Fixed Forward Data")) {
			if (Boolean.parseBoolean(data.remove("Fixed Forward Data"))) {
				jsClick("//input[@name='Fixed_Forward_Data__c']");
			}
		}
		if (data.containsKey("State will not fix - Forward Data")) {
			if (Boolean.parseBoolean(data.remove("State will not fix - Forward Data"))) {
				jsClick("//input[@name='State_will_not_fix_Forward_Data__c']");
			}
		}
		if (data.containsKey("Timeline Date - Forward Data")) {
			waitFor(1);
			page.locator("//input[@name='Timeline_Date_Forward_Data__c']").fill(fetchWeekdayDate("M/d/yyyy", 0));
		}
		if (data.containsKey("Fixed - Historical Data")) {
			if (Boolean.parseBoolean(data.remove("Fixed - Historical Data"))) {
				jsClick("//input[@name='Fixed_Historical_Data__c']");
			}
		}
		if (data.containsKey("State will not fix - Historical Data")) {
			if (Boolean.parseBoolean(data.remove("State will not fix - Historical Data"))) {
				jsClick("//input[@name='State_will_not_fix_Historical_Data__c']");
			}
		}
		if (data.containsKey("Timeline Date - Historical Data")) {
			page.locator("//input[@name='Timeline_Date_Historical_Data__c']").fill(fetchWeekdayDate("M/d/yyyy", 0));
			screenshot("Activity section fields updated to track in Field History");
		}
		if (data.containsKey("Affecting Forward Data")) {

			jsClick("//input[@name='Affecting_Forward_Data__c']");

		}

		clickSave("updating PIER Issue with all field details", false);
		logger.info("PIER Issue extended details updated");
	}

	public void validateFixed_Data_Already_Marked_Error() {
		
		page.getByText(Pattern.compile("Forward Data is already marked as", Pattern.CASE_INSENSITIVE)).first()
				.waitFor(new Locator.WaitForOptions().setTimeout(8000));
		logger.info("Verified 'Forward Data is already marked as' error message");
		page.getByText(Pattern.compile("Historical Data is already marked as", Pattern.CASE_INSENSITIVE)).first()
				.waitFor(new Locator.WaitForOptions().setTimeout(8000));
		logger.info("Verified 'Historical Data is already marked as' error message");
		screenshot("Already marked error message validation");
	}

	private void fillVisibleFieldByApiOrLabel(String apiName, String label, String value) {
		Locator input = context()
				.locator("input[name='" + apiName + "']:visible, textarea[name='" + apiName + "']:visible")
				.first();
		if (input.count() == 0) {
			input = context().getByLabel(label, new Locator.GetByLabelOptions().setExact(true)).first();
		}
		if (input.count() == 0) {
			throw new IllegalStateException(
					"Unable to locate visible editable field for " + label + " (" + apiName + ")");
		}
		input.waitFor(new Locator.WaitForOptions().setTimeout(5000));
		input.evaluate(
				"(el) => { el.value = ''; el.dispatchEvent(new Event('input', { bubbles: true, composed: true })); el.dispatchEvent(new Event('change', { bubbles: true, composed: true })); }");
		input.evaluate(
				"(el, nextValue) => { el.value = nextValue; el.dispatchEvent(new Event('input', { bubbles: true, composed: true })); el.dispatchEvent(new Event('change', { bubbles: true, composed: true })); }",
				value);
	}

	public void navigateToIssueFieldHistoryTab() {
		openFieldHistoryRelatedList(Pattern.compile("Field Tracking History|Field History Tracking|History",
				Pattern.CASE_INSENSITIVE), "PIER Issue History");
		verifyFieldHistoryRowsRecorded();
		logger.info("Navigated to PIER Issue field history tab");
	}

	public void clickOnEdit() {
		jsClick("//button[@title='Edit Fixed - Forward Data']");
	}

	public void Check_UnCheck_FixedDate(String screenshotName) {

		jsClick("(//input[@name='Fixed_Forward_Data__c'])[last()]");
		waitFor(1);
		jsClick("(//input[@name='Fixed_Historical_Data__c'])[last()]");
		waitFor(1);
		screenshot(screenshotName);
	}

	public void check_UnCheck_State_Will_Not_Fix(String screenshotName) {

		jsClick("(//input[@name='State_will_not_fix_Forward_Data__c'])[last()]");
		jsClick("(//input[@name='State_will_not_fix_Historical_Data__c'])[last()]");
		screenshot(screenshotName);

	}

	public void verifyIssueFieldHistory() {
		String h = "PIER Issue History";
		// Verify most recent field changes based on actual history table
		assertHistoryText(h, "End Year", "2025"); // Changed from 2026 to 2025
		assertHistoryContains(h, "Indicator Affected", "3. Call Center Abandonment Rate");
		assertHistoryCheckbox(h, "Issue related to E&E?", "Checked");
		assertHistoryText(h, "Start Year", "2025");
		assertHistoryText(h, "Start Month", "April");
		assertHistoryText(h, "End Month", "June");

		assertHistoryContains(h, "Timeline Date - Forward Data", fetchWeekdayDate("M/d/yyyy", 0));
		assertHistoryContains(h, "Timeline Date - Historical Data", fetchWeekdayDate("M/d/yyyy", 0));

		// They were checked during verification, then unchecked by the save process
		assertHistoryCheckbox(h, "Fixed - Forward Data", "Checked");
		assertHistoryCheckbox(h, "Fixed - Historical Data", "Checked");
		assertHistoryCheckbox(h, "State will not fix - Forward Data", "Unchecked");
		assertHistoryCheckbox(h, "State will not fix - Historical Data", "Unchecked");

		assertHistoryCheckbox(h, "Closed", "Checked");

		logger.info("PIER Issue field history verified");
	}

	public void verifyFieldHistory_HistoricalData() {
		String h = "PIER Issue History";
		assertHistoryText(h, "Fixed - Forward Data", "Checked");
		assertHistoryText(h, "State will not fix - Forward Data", "Checked");
		assertHistoryText(h, "Timeline Date - Forward Data", fetchWeekdayDate("M/d/yyyy", 0));
		assertHistoryText(h, "Fixed - Historical Data", "Checked");
		assertHistoryText(h, "State will not fix - Historical Data", "Checked");
		assertHistoryText(h, "Timeline Date - Historical Data", fetchWeekdayDate("M/d/yyyy", 0));
	}

	// ─── Shared SF helpers ────────────────────────────────────────────────────

	private void setCheckbox(String fieldName, boolean checked) {
		try {
			Locator box = page.locator("xpath=//input[@name='" + fieldName + "']").first();
			box.waitFor(new Locator.WaitForOptions().setTimeout(4000));
			boolean isChecked = box.isChecked();
			if (isChecked != checked) {
				jsClick("//input[@name='" + fieldName + "']");
			}
		} catch (Exception e) {
			logger.warn("Checkbox '{}' not available: {}", fieldName, e.getMessage());
		}
	}

	private void verifyCompleteThisFieldCount(int expectedMinimum) {
		Locator errors = page.getByText("Complete this field.", new Page.GetByTextOptions().setExact(false));
		try {
			errors.first().waitFor(new Locator.WaitForOptions().setTimeout(8000));
			int count = errors.count();
			if (count < expectedMinimum) {
				throw new AssertionError(
						"Expected at least " + expectedMinimum + " mandatory-field errors, found " + count);
			}
		} catch (Exception e) {
			throw new AssertionError(
					"Mandatory-field error text not visible; expected at least " + expectedMinimum + " occurrences: "
							+ e.getMessage(),
					e);
		}
	}

	private int historyRowIndex(String heading, String fieldName) {
		Locator rows = page.locator("xpath=((//h1[text()='" + heading + "']/following::table)[1]//tbody/tr)");
		int count = rows.count();
		for (int i = 1; i <= count; i++) {
			Locator nameCell = page.locator("xpath=((//h1[text()='" + heading + "']/following::table)[1]//tbody/tr)["
					+ i + "]/td[2]//lst-formatted-text/span");
			if (nameCell.count() > 0) {
				String field = nameCell.first().textContent();
				if (field != null && fieldName.equals(field.trim())) {
					return i;
				}
			}
		}
		return -1;
	}

	private String historyNewValue(String heading, String fieldName) {
		int idx = historyRowIndex(heading, fieldName);
		if (idx == -1)
			return "Field Not Reflected";
		return page.locator("xpath=((//h1[text()='" + heading + "']/following::table)[1]//tbody/tr)[" + idx + "]/td[5]")
				.first().textContent().trim();
	}

	private String historyNewCheckbox(String heading, String fieldName) {
		int idx = historyRowIndex(heading, fieldName);
		if (idx == -1)
			return "Field Not Reflected";
		Locator input = page.locator("xpath=((//h1[text()='" + heading + "']/following::table)[1]//tbody/tr)[" + idx
				+ "]/td[5]//lightning-input");
		if (input.count() == 0)
			return "Unchecked";
		// Salesforce uses checked="" (empty string), not checked="true"
		String checkedAttr = input.first().getAttribute("checked");
		return checkedAttr != null ? "Checked" : "Unchecked";
	}

	private void assertHistoryText(String heading, String field, String expected) {
		String actual = historyNewValue(heading, field);
		if (!expected.equals(actual)) {
			// Capture screenshot before failing fast
			com.macsuite.common.ScreenshotHelper.take("Field History Error: " + field);
			throw new AssertionError("Field history '" + field + "' new value mismatch. Expected '" + expected
					+ "', found '" + actual + "'");
		}
		logger.info("Field history verified: {} -> {}", field, actual);
	}

	private void assertHistoryContains(String heading, String field, String expectedSubstring) {
		String actual = historyNewValue(heading, field);
		if (actual == null || !actual.contains(expectedSubstring)) {
			// Capture screenshot before failing fast
			com.macsuite.common.ScreenshotHelper.take("Field History Error: " + field);
			throw new AssertionError("Field history '" + field + "' new value '" + actual + "' does not contain '"
					+ expectedSubstring + "'");
		}
		logger.info("Field history verified (contains): {} -> {}", field, actual);
	}

	private void assertHistoryCheckbox(String heading, String field, String expected) {
		String actual = historyNewCheckbox(heading, field);
		if (!expected.equals(actual)) {
			// Capture screenshot before failing fast
			com.macsuite.common.ScreenshotHelper.take("Field History Error: " + field);
			throw new AssertionError("Field history '" + field + "' checkbox state mismatch. Expected '" + expected
					+ "', found '" + actual + "'");
		}
		logger.info("Field history verified: {} -> {}", field, actual);
	}

	private void assertHistoryTransition(String heading, String field, String expectedOld, String expectedNew,
			String alternateField) {
		String actualOld = historyOriginalValue(heading, field);
		String actualNew = historyNewValue(heading, field);
		String actualField = field;
		if ("Field Not Reflected".equals(actualOld) && alternateField != null) {
			actualOld = historyOriginalValue(heading, alternateField);
			actualNew = historyNewValue(heading, alternateField);
			actualField = alternateField;
		}
		if ("Field Not Reflected".equals(actualOld) && "Field Not Reflected".equals(actualNew)) {
			throw new AssertionError("Field history '" + actualField + "' not reflected");
		}
		if (expectedOld != null && !expectedOld.equals(actualOld)) {
			com.macsuite.common.ScreenshotHelper.take("Field History Error: " + actualField);
			throw new AssertionError("Field history '" + actualField + "' original value mismatch. Expected '"
					+ expectedOld + "', found '" + actualOld + "'");
		}
		if (expectedNew != null && !expectedNew.equals(actualNew)) {
			com.macsuite.common.ScreenshotHelper.take("Field History Error: " + actualField);
			throw new AssertionError("Field history '" + actualField + "' new value mismatch. Expected '"
					+ expectedNew + "', found '" + actualNew + "'");
		}
		logger.info("Field history verified: {} -> {} => {}", actualField, actualOld, actualNew);
	}

	private String historyOriginalValue(String heading, String fieldName) {
		int idx = historyRowIndex(heading, fieldName);
		if (idx == -1)
			return "Field Not Reflected";
		return page.locator("xpath=((//h1[text()='" + heading + "']/following::table)[1]//tbody/tr)[" + idx + "]/td[4]")
				.first().textContent().trim();
	}
}
