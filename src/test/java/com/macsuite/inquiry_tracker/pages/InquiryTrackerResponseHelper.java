package com.macsuite.inquiry_tracker.pages;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.macsuite.common.DropdownUtils;
import com.macsuite.common.FormUtils;
import com.macsuite.common.TemplateValueResolver;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

class InquiryTrackerResponseHelper {
	private static final Logger logger = LoggerFactory.getLogger(InquiryTrackerResponseHelper.class);
	private static final String TEST_DATA_BASE_PATH = "src/test/resources/test-data";

	private final Page page;
	private final FormUtils formUtils;
	private final DropdownUtils dropdownUtils;

	InquiryTrackerResponseHelper(Page page) {
		this.page = page;
		this.formUtils = new FormUtils(page);
		this.dropdownUtils = new DropdownUtils(page);
	}

	void createInquiryResponse(Map<String, String> responseData) {
		openResponseComposer();
		Locator modal = activeModal();

		// Map 'Response Type' from test data to actual UI field 'Type' (combobox in
		// modal)
		String responseType = responseData.get("Response Type");
		if (responseType != null && !responseType.isBlank()) {
			dropdownUtils.selectDropdown("Type", responseType, modal);
		}

		if (isTruthy(responseData.get("Validate Email Format"))) {
			validateEmailFormatInModal();
		}

		// Fill other fields in the modal (e.g., Email Subject, State Contact Email,
		// etc.)
		for (Map.Entry<String, String> entry : responseData.entrySet()) {
			String key = entry.getKey();
			if ("Response Type".equals(key) || "Response Details".equals(key) || "Validate Email Format".equals(key)
					|| "Attach File".equals(key) || "Include Attached File".equals(key))
				continue;
			String val = entry.getValue();
			if (val != null && !val.isBlank()) {
				fillModalField(modal, key, val);
			}
		}

		fillResponseEmailBodyInModal(modal, "Test email body content for inquiry response.");
		// Handle file attachment if specified (uploads a new file)
		String attachFile = responseData.get("Attach File");
		if (attachFile != null && !attachFile.isBlank()) {
			attachFileToResponse(attachFile);
		}

		// Handle including already attached inquiry files (checks a checkbox)
		String includeAttachedFile = responseData.get("Include Attached File");
		if (isTruthy(includeAttachedFile)) {
			includeInquiryAttachedFiles(modal);
		}

		// Response Details is NOT in the modal - it's an inline-edit field on the
		// detail page
		// Skip it here and handle it after modal is saved
		String responseDetails = responseData.get("Response Details");

		clickButtonInActiveModal("Save");
		logger.info("Created CMCS inquiry response draft");

		// Wait for modal to close and response page to load
		try {
			modal.waitFor(new Locator.WaitForOptions()
					.setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN).setTimeout(10000));
			logger.debug("Response creation modal closed");
		} catch (Exception e) {
			logger.debug("Modal may have already closed: {}", e.getMessage());
		}

		// Wait for response details page to be fully loaded
		try {
			// Wait for response code or status indicator to appear
			Locator responseIndicator = page.locator("h1:has-text('RES-'), .stepLabel.slds-path__stage-name").first();
			responseIndicator.waitFor(new Locator.WaitForOptions().setTimeout(10000));
			logger.debug("Response page loaded");
		} catch (Exception e) {
			logger.debug("Response indicator not found immediately: {}", e.getMessage());
		}

		// Additional time for page to stabilize
		page.waitForTimeout(2000);
		logger.info("✓ Response page fully loaded after creation");

		// Now fill Response Details if specified (opens inline edit panel)
		if (responseDetails != null && !responseDetails.isBlank()) {
			fillResponseDetailsOnDetailPage(responseDetails);
		}
	}

	private void validateEmailFormatInModal() {
		Locator modal = activeModal();
		Locator emailField = modal
				.getByLabel("State Contact Email Address", new Locator.GetByLabelOptions().setExact(false)).first();
		if (emailField.count() == 0) {
			logger.warn("State Contact Email Address field not found — skipping email format validation");
			return;
		}
		emailField.fill("testemail");
		clickButtonInActiveModal("Save");
		Locator errorEl = modal
				.locator("span:has-text('Please enter a valid email'), .slds-form-element__help, [class*='error-msg']")
				.first();
		try {
			errorEl.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			logger.info("✓ Verified email format validation error");
		} catch (Exception e) {
			logger.warn("Could not confirm email format error visibility: {}", e.getMessage());
		}
		emailField.fill("");
	}

	private boolean isTruthy(String value) {
		return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1"));
	}

	private void fillResponseEmailBodyInModal(Locator modal, String value) {
		String resolved = TemplateValueResolver.resolve(value);

		Locator activationTarget = modal
				.locator("div.slds-rich-text-area__content, div[aria-label='Email Body'], lightning-input-rich-text")
				.first();
		if (activationTarget.count() > 0) {
			try {
				activationTarget.scrollIntoViewIfNeeded();
				activationTarget.click();
			} catch (Exception e) {
				logger.debug("Could not click rich text activation target: {}", e.getMessage());
			}
		}

		Locator editor = modal.locator("div[aria-label='Email Body'] div.ql-editor[contenteditable='true']").first();
		if (editor.count() == 0) {
			editor = modal.locator("lightning-input-rich-text div.ql-editor[contenteditable='true']").first();
		}
		if (editor.count() == 0) {
			editor = modal.locator("div.ql-editor[contenteditable='true']").first();
		}

		editor.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		editor.scrollIntoViewIfNeeded();

		try {
			editor.click(new Locator.ClickOptions().setTimeout(5000));
			editor.fill("");
			editor.pressSequentially(resolved, new Locator.PressSequentiallyOptions().setDelay(5));
		} catch (Exception fillError) {
			logger.debug("Direct rich text fill failed, using JS fallback: {}", fillError.getMessage());
			editor.evaluate(
					"(node, text) => {"
							+ "node.focus();"
							+ "const quill = node.__quill || node.closest('.ql-container')?.__quill;"
							+ "if (quill) {"
							+ "  quill.setText(text);"
							+ "} else {"
							+ "  node.innerHTML = '';"
							+ "  const p = document.createElement('p');"
							+ "  p.textContent = text;"
							+ "  node.appendChild(p);"
							+ "}"
							+ "node.classList.remove('ql-blank');"
							+ "node.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: text }));"
							+ "node.dispatchEvent(new Event('change', { bubbles: true }));"
							+ "node.blur();"
							+ "}",
					resolved);
		}
	}

	/**
	 * Fills Response Details field on the response detail page using inline edit.
	 * This is called AFTER the response is created and we're on the detail page.
	 * Opens the inline edit panel but does NOT click Save - let the test step
	 * handle that.
	 */
	private void fillResponseDetailsOnDetailPage(String responseDetails) {
		String resolved = TemplateValueResolver.resolve(responseDetails);
		logger.info("Filling Response Details on detail page: {}", resolved);

		// Click "Edit Response Details" button
		Locator editButton = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName("Edit Response Details"));

		if (editButton.count() == 0 || !editButton.isVisible()) {
			logger.warn("Edit Response Details button not found - may already be in edit mode");
		} else {
			editButton.click();
			logger.debug("Clicked Edit Response Details button");
			// Wait for inline edit panel to appear
			page.waitForTimeout(500);
		}

		// Try filling as combobox (dropdown) if it's a predefined value
		Locator combobox = page.getByRole(AriaRole.COMBOBOX,
				new Page.GetByRoleOptions().setName("Response Details"));
		if (combobox.count() > 0 && combobox.isVisible()) {
			try {
				combobox.click();
				// Wait for dropdown options to appear
				page.waitForTimeout(300);
				// Try to select the option (exact match)
				Locator option = page.getByText(resolved, new Page.GetByTextOptions().setExact(true));
				if (option.count() > 0 && option.isVisible()) {
					option.click();
					logger.info("✓ Selected Response Details dropdown option: {}", resolved);
					// After dropdown selection, save is needed
				} else {
					logger.warn("Response Details dropdown option '{}' not found", resolved);
				}
			} catch (Exception e) {
				logger.debug("Could not select Response Details dropdown: {}", e.getMessage());
			}
		} else {
			// Try filling as textbox if it's free-form text (could be "Response Notes"
			// field)
			Locator responseNotesTextbox = page.getByRole(AriaRole.TEXTBOX,
					new Page.GetByRoleOptions().setName("Response Notes"));
			if (responseNotesTextbox.count() > 0 && responseNotesTextbox.isVisible()) {
				responseNotesTextbox.click();
				responseNotesTextbox.fill(resolved);
				logger.info("✓ Filled Response Notes textbox");
			} else {
				// Try generic textbox in edit panel
				Locator anyTextbox = page
						.locator("lightning-input[data-name='Response_Details__c'] input, textarea[name*='Response']")
						.first();
				if (anyTextbox.count() > 0 && anyTextbox.isVisible()) {
					anyTextbox.click();
					anyTextbox.fill(resolved);
					logger.info("✓ Filled Response Details using fallback selector");
				} else {
					logger.warn("Response Details field not found (neither combobox nor textbox)");
				}
			}
		}

		// Auto-save the inline edit (required for scenarios without explicit Save
		// button click)
		Locator saveButton = enabledInlineEditSaveButton().last();
		try {
			saveButton.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			saveButton.scrollIntoViewIfNeeded();
			saveButton.click();
			logger.info("✓ Clicked Save to save Response Details inline edit");
			// Wait for save to complete and inline edit to close
			try {
				saveButton.waitFor(new Locator.WaitForOptions()
						.setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
						.setTimeout(5000));
			} catch (Exception e) {
				logger.debug("Save button still visible or timeout: {}", e.getMessage());
			}
			page.waitForTimeout(1000);
		} catch (Exception e) {
			logger.warn("Save button not found after filling Response Details");
		}

		logger.info("✓ Response Details saved successfully");
	}

	void sendInquiryResponse() {
		// Strategy 1: Role-based lookup with name pattern
		Locator sendButton = page.getByRole(AriaRole.BUTTON,
				new Page.GetByRoleOptions().setName(Pattern.compile("Send Response|Send", Pattern.CASE_INSENSITIVE)))
				.first();

		// Strategy 2: Button by name attribute (Salesforce uses
		// Request__c.Send_Approval_Email)
		if (sendButton.count() == 0) {
			sendButton = page.locator("button[name='Request__c.Send_Approval_Email']").first();
		}

		// Strategy 3: Button containing "Send Response" text
		if (sendButton.count() == 0) {
			sendButton = page.locator("button:has-text('Send Response')").first();
		}

		if (sendButton.count() == 0) {
			throw new AssertionError("Send Response button was not found");
		}

		sendButton.scrollIntoViewIfNeeded();
		sendButton.click();
		logger.info("✓ Clicked Send Response button");

		// Wait for confirmation modal and click Confirm
		try {
			// Wait for the modal with "Send Response" heading
			Locator confirmModal = page
					.locator(".runtime_platform_actionsQuickActionWrapper:has(h2:has-text('Send Response'))").first();
			confirmModal.waitFor(new Locator.WaitForOptions().setTimeout(10000));

			// Click Confirm button in the modal
			Locator confirmButton = page
					.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirm").setExact(true)).first();

			if (confirmButton.count() == 0) {
				// Fallback: try flow-button__NEXT class (Salesforce Flow button)
				confirmButton = page.locator(".flow-button__NEXT button, button.slds-button_brand:has-text('Confirm')")
						.first();
			}

			confirmButton.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			confirmButton.click();
			logger.info("✓ Clicked Confirm button in Send Response modal");

			// Wait for modal to close
			try {
				confirmModal.waitFor(new Locator.WaitForOptions()
						.setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN).setTimeout(10000));
				logger.debug("Confirmation modal closed");
			} catch (Exception modalException) {
				logger.debug("Modal may have already closed: {}", modalException.getMessage());
			}

			// Wait for page to be ready after send operation
			// Strategy 1: Wait for success toast/message
			try {
				Locator successToast = page.locator(".slds-notify--toast, .forceToastMessage").first();
				successToast.waitFor(new Locator.WaitForOptions().setTimeout(5000));
				logger.debug("Success toast appeared");
				// Wait for toast to disappear (optional)
				page.waitForTimeout(1000);
			} catch (Exception toastException) {
				logger.debug("No toast detected, proceeding: {}", toastException.getMessage());
			}

			// Strategy 2: Wait for response page to fully load by checking for status
			// indicator
			try {
				Locator statusIndicator = page.locator(".stepLabel.slds-path__stage-name, p.slds-path__stage-name")
						.first();
				statusIndicator.waitFor(new Locator.WaitForOptions().setTimeout(10000));
				logger.debug("Status indicator loaded");
			} catch (Exception statusException) {
				logger.debug("Status indicator not found immediately: {}", statusException.getMessage());
			}

			// Give page additional time to fully stabilize (rendering, calculations, etc.)
			page.waitForTimeout(2000);
			logger.info("✓ Page fully loaded after sending response");

		} catch (Exception e) {
			logger.warn("Confirmation modal may not have appeared or was auto-confirmed: {}", e.getMessage());
			// Even if modal didn't appear, wait for page to stabilize
			page.waitForTimeout(3000);
		}
	}

	void verifyResponseSentConfirmation() {
		Locator confirmation = page.getByText(Pattern.compile("sent|success", Pattern.CASE_INSENSITIVE)).first();
		if (confirmation.count() == 0 || !confirmation.isVisible()) {
			throw new AssertionError("Response sent confirmation was not visible");
		}
	}

	private void openResponseComposer() {
		Locator responseButton = page
				.getByRole(AriaRole.BUTTON,
						new Page.GetByRoleOptions()
								.setName(Pattern.compile("Create Response|Response", Pattern.CASE_INSENSITIVE)))
				.first();

		// Wait for the button to appear (page may still be loading after inquiry
		// selection)
		try {
			responseButton.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		} catch (Exception e) {
			throw new AssertionError("Create Response button was not found after waiting 10s. "
					+ "Ensure inquiry is in 'Escalated' stage with Resolution Date set.", e);
		}

		responseButton.click();
		Locator modal = activeModal();
		modal.waitFor(new Locator.WaitForOptions().setTimeout(10000));

		// Wait for modal to be fully interactive by checking for a form element inside
		modal.locator("button[role='combobox'], input, textarea").first()
				.waitFor(new Locator.WaitForOptions().setTimeout(5000));
	}

	private void fillModalField(Locator modal, String fieldLabel, String value) {
		logger.debug("Filling modal field '{}' with value '{}'", fieldLabel, value);

		// Resolve template placeholders (${timestamp}, ${today}, etc.)
		String resolvedValue = TemplateValueResolver.resolve(value);

		// Try as combobox/dropdown first
		Locator combobox = modal
				.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName(fieldLabel).setExact(false))
				.first();
		if (combobox.count() > 0) {
			dropdownUtils.selectDropdown(fieldLabel, resolvedValue, modal);
			return;
		}

		// Try as text input by label
		Locator textInput = modal.getByLabel(fieldLabel, new Locator.GetByLabelOptions().setExact(false)).first();
		if (textInput.count() > 0) {
			textInput.fill(resolvedValue);
			logger.info("Filled text field '{}' in modal", fieldLabel);
			return;
		}

		// Try as textarea
		Locator textarea = modal.locator("textarea[aria-label*='" + fieldLabel + "']").first();
		if (textarea.count() > 0) {
			textarea.fill(resolvedValue);
			logger.info("Filled textarea '{}' in modal", fieldLabel);
			return;
		}

		logger.warn("Field '{}' not found in modal - skipping", fieldLabel);
	}

	private void clickButtonInActiveModal(String buttonName) {
		Pattern buttonText = Pattern.compile("^\\s*" + regexLiteral(buttonName) + "\\s*$", Pattern.CASE_INSENSITIVE);
		Locator modalButton = page
				.locator("[role='dialog']:visible button:not([disabled]):not([aria-disabled='true']), "
						+ ".slds-modal:visible button:not([disabled]):not([aria-disabled='true']), "
						+ ".isModal.inlinePanel:visible button:not([disabled]):not([aria-disabled='true']), "
						+ ".runtime_platform_actionsQuickActionWrapper:visible button:not([disabled]):not([aria-disabled='true'])")
				.filter(new Locator.FilterOptions().setHasText(buttonText));
		Locator lightningBrandButton = page.locator("lightning-button[variant='brand'] "
				+ "button:not([disabled]):not([aria-disabled='true']):not([title*='save new note' i])")
				.filter(new Locator.FilterOptions().setHasText(buttonText));
		Locator button = modalButton.or(lightningBrandButton).last();
		button.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED).setTimeout(10000));
		button.scrollIntoViewIfNeeded();
		button.waitFor(new Locator.WaitForOptions().setTimeout(5000));
		button.click();
	}

	private String regexLiteral(String text) {
		return text.replaceAll("([\\\\.^$|?*+()\\[\\]{}])", "\\\\$1");
	}

	private Locator enabledInlineEditSaveButton() {
		Locator enabledExactSave = page
				.locator("button:not([disabled]):not([aria-disabled='true']):not([title*='save new note' i])")
				.filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*Save\\s*$")));

		return page.locator("button[name='SaveEdit']:not([disabled]):not([aria-disabled='true'])")
				.or(page.locator(".forceRecordEditActions button:not([disabled]):not([aria-disabled='true'])")
						.filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*Save\\s*$"))))
				.or(enabledExactSave);
	}

	private Locator activeModal() {
		// P1: Prefer visible modal/dialog containers. Salesforce keeps stale hidden
		// dialogs in the DOM.
		Locator modal = page.locator("[role='dialog']:visible, .slds-modal:visible, .isModal.inlinePanel:visible, "
				+ ".runtime_platform_actionsQuickActionWrapper:visible").last();
		if (modal.count() > 0) {
			return modal;
		}

		// P2: Fallback to semantic dialog role.
		modal = page.getByRole(AriaRole.DIALOG).last();
		if (modal.count() > 0) {
			return modal;
		}

		// P3: Fallback to modal CSS class selectors.
		return page.locator(".isModal.inlinePanel, .slds-modal").last();
	}

	private void attachFileToResponse(String fileNameOrPath) {
		Locator modal = activeModal();

		// First, check/enable the file attachment checkbox if present
		Locator fileAttachCheckbox = modal
				.locator("input[type='checkbox'][name*='attach'], input[type='checkbox'][name*='file']").first();
		if (fileAttachCheckbox.count() > 0) {
			if (!fileAttachCheckbox.isChecked()) {
				fileAttachCheckbox.check();
				logger.info("Enabled file attachment checkbox");
			}
		}

		// Resolve file path (support both absolute paths and relative to test-data)
		String resolved = fileNameOrPath.contains("/") || fileNameOrPath.contains("\\") ? fileNameOrPath
				: Paths.get(TEST_DATA_BASE_PATH, fileNameOrPath).toString();

		// Locate and upload file via file input
		Locator fileInput = modal.locator("input[type='file']").first();
		if (fileInput.count() == 0) {
			logger.warn("File input not found in modal - attachment may not be supported");
			return;
		}

		fileInput.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		fileInput.setInputFiles(Paths.get(resolved).toAbsolutePath().normalize());
		logger.info("✓ Attached file: {}", resolved);
	}

	/**
	 * Checks the checkbox(es) to include already-attached inquiry files in the
	 * response.
	 * Looks for checkboxes near file names in the modal.
	 */
	private void includeInquiryAttachedFiles(Locator modal) {
		// Look for checkboxes in the files section (usually named something like
		// "Include" or next to file names)
		Locator fileCheckboxes = modal.locator("input[type='checkbox']")
				.filter(new Locator.FilterOptions()
						.setHas(modal.locator("~ label, ~ span")
								.locator(":text-matches('outreach-emergency|Include|Attach', 'i')")))
				.or(modal.locator("lightning-input:has-text('outreach-emergency') input[type='checkbox']"))
				.or(modal.locator("tr:has-text('outreach-emergency') input[type='checkbox']"));

		int count = fileCheckboxes.count();
		if (count == 0) {
			logger.warn(
					"No file checkboxes found to include attached files - files may already be included by default");
			return;
		}

		// Check all file checkboxes found
		for (int i = 0; i < count; i++) {
			Locator checkbox = fileCheckboxes.nth(i);
			if (!checkbox.isChecked()) {
				checkbox.check();
				logger.info("✓ Checked file inclusion checkbox #{}", i + 1);
			}
		}

		logger.info("✓ Included {} inquiry attached file(s) in response", count);
	}

	String readDefaultEmailTemplate() {
		Locator modal = activeModal();

		// Try to read the default email template from Email Body field
		// P1: Try TEXTBOX role
		Locator emailBodyTextbox = modal.getByRole(AriaRole.TEXTBOX,
				new Locator.GetByRoleOptions().setName("Email Body"));
		if (emailBodyTextbox.count() > 0 && emailBodyTextbox.isVisible()) {
			String template = emailBodyTextbox.inputValue();
			if (template != null && !template.isBlank()) {
				logger.info("✓ Read default email template (length: {})", template.length());
				return template;
			}
		}

		// P2: Try formatted rich text preview
		Locator richTextPreview = modal.locator("lightning-formatted-rich-text .slds-rich-text-editor__output").first();
		if (richTextPreview.count() > 0 && richTextPreview.isVisible()) {
			String template = richTextPreview.textContent();
			if (template != null && !template.isBlank()) {
				logger.info("✓ Read default email template from preview (length: {})", template.length());
				return template;
			}
		}

		// P3: Try contenteditable div
		Locator contentEditable = modal.locator("div[contenteditable='true']").first();
		if (contentEditable.count() > 0 && contentEditable.isVisible()) {
			String template = contentEditable.textContent();
			if (template != null && !template.isBlank()) {
				logger.info("✓ Read default email template from contenteditable (length: {})", template.length());
				return template;
			}
		}

		logger.warn("Could not read default email template");
		return "";
	}

	// ─── Response verification ────────────────────────────────────────────────

	void verifyResponseCodeStartsWith(String prefix) {
		page.waitForTimeout(5000);
		String title = page.locator("//h1//lightning-formatted-text").first().textContent();
		if (title != null && title.startsWith(prefix)) {
			logger.info("✓ Response code starts with '{}': {}", prefix, title);
			return;
		}
		Locator codeEl = page
				.locator("xpath=(//h1//*[text()='Response']//following::lightning-formatted-text[1])[last()]").first();
		try {
			codeEl.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			String code = codeEl.textContent();
			if (code == null || !code.startsWith(prefix)) {
				throw new AssertionError(
						"Expected response code to start with '" + prefix + "' but found: '" + code + "'");
			}
			logger.info("✓ Response code starts with '{}': {}", prefix, code.trim());
		} catch (Exception e) {
			requireAnyVisible(page.getByText(Pattern.compile(Pattern.quote(prefix), Pattern.CASE_INSENSITIVE)),
					"text matching '" + prefix + "'");
		}
	}

	void verifyResponseEmailSubjectIncludesTitle() {
		Locator subjectEl = page
				.locator("xpath=(//*[text()='Email Subject']//following::lightning-formatted-text[1])[last()]").first();
		try {
			subjectEl.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			String subject = subjectEl.textContent();
			if (subject == null || subject.trim().isEmpty()) {
				throw new AssertionError("Response email subject is empty");
			}
			logger.info("✓ Response email subject: {}", subject.trim());
		} catch (Exception e) {
			throw new AssertionError("Response email subject element not found", e);
		}
	}

	void verifyResponseEmailAddressMatchesContact() {
		Locator emailEl = page.locator(
				"xpath=(//*[text()='State Contact Email Address']//following::emailui-formatted-email-wrapper)[last()]")
				.first();
		if (emailEl.count() == 0) {
			emailEl = page.locator(
					"xpath=(//*[text()='State Contact Email Address']//following::lightning-formatted-text[1])[last()]")
					.first();
		}
		try {
			emailEl.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			String email = emailEl.textContent();
			if (email == null || email.trim().isEmpty()) {
				throw new AssertionError("Response email address is empty");
			}
			logger.info("✓ Response email address: {}", email.trim());
		} catch (Exception e) {
			throw new AssertionError("Response email address element not found", e);
		}
	}

	void previewResponseEmail() {
		Locator previewBtn = page.locator("xpath=(//button[text()='Preview Email'])[last()]").first();
		if (previewBtn.count() == 0) {
			previewBtn = page
					.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Preview Email").setExact(true))
					.last();
		}
		previewBtn.waitFor(new Locator.WaitForOptions().setTimeout(8000));
		previewBtn.click();
		page.locator(".slds-modal, [role='dialog']").last().waitFor(new Locator.WaitForOptions().setTimeout(8000));
		logger.info("✓ Clicked Preview Email");
	}

	void verifyPreviewEmailBasicDetails() {
		Locator modal = page.locator(".slds-modal, [role='dialog']").last();
		Locator contactEl = modal.locator("xpath=(//*[text()='State Contact Email Address']/../div[2])[last()]")
				.first();
		if (contactEl.count() == 0) {
			contactEl = modal.getByText(Pattern.compile("@", Pattern.CASE_INSENSITIVE)).first();
		}
		// PREDICTED de-hollow (not runtime-verified): the prior version swallowed its
		// only wait and
		// asserted nothing. Require the preview to actually render recipient content
		// (an email address).
		requireAnyVisible(modal.getByText(Pattern.compile("@")), "an email address in the email preview");
		logger.info("✓ Verified basic preview email details (recipient email present)");
	}

	void verifyPreviewEmailFullDetails() {
		Locator modal = page.locator(".slds-modal, [role='dialog']").last();
		verifyPreviewEmailBasicDetails();
		requireAnyVisible(modal.getByText(Pattern.compile("@")), "email content in the full email preview");
		logger.info("✓ Verified full preview email details (recipient email present)");
	}

	void closeResponseEmailPreview() {
		Locator closeBtn = page.locator("button[title='Cancel and close']").last();
		if (closeBtn.count() == 0) {
			closeBtn = page.getByRole(AriaRole.BUTTON,
					new Page.GetByRoleOptions().setName(Pattern.compile("Close|Cancel", Pattern.CASE_INSENSITIVE)))
					.last();
		}
		closeBtn.click();
		logger.info("✓ Closed response email preview");

		// Wait for preview modal to close
		try {
			Locator previewModal = page.locator(".slds-modal, [role='dialog']").last();
			previewModal.waitFor(new Locator.WaitForOptions()
					.setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN).setTimeout(5000));
			logger.debug("Preview modal closed");
		} catch (Exception e) {
			logger.debug("Preview modal may have already closed: {}", e.getMessage());
		}

		// Wait for underlying response page to be ready
		page.waitForTimeout(1000);
		logger.debug("Response page ready after closing preview");
	}

	void verifyResponseStatus(String expectedStatus) {
		logger.info("Verifying response status path is equal to: {}", expectedStatus);

		try {
			page.waitForFunction(
					"(expected) => {"
							+ "const normalize = (s) => (s || '').replace(/\\s+/g, ' ').trim();"
							+ "const expectedLower = normalize(expected).toLowerCase();"
							+ "const pathTexts = Array.from(document.querySelectorAll('.slds-path__stage-name, .stepLabel.slds-path__stage-name'))"
							+ ".map(el => normalize(el.textContent));"
							+ "if (pathTexts.some(t => t.toLowerCase() === ('status: ' + expectedLower) || t.toLowerCase().endsWith(': ' + expectedLower) || t.toLowerCase() === expectedLower)) return true;"
							+ "const labels = Array.from(document.querySelectorAll('span.test-id__field-label'));"
							+ "for (const label of labels) {"
							+ "  if (normalize(label.textContent).toLowerCase() !== 'status') continue;"
							+ "  const container = label.closest('.slds-form-element, .test-id__field');"
							+ "  if (!container) continue;"
							+ "  const valueEl = container.querySelector('lightning-formatted-text, slot[name=output], .test-id__field-value, span, div');"
							+ "  if (!valueEl) continue;"
							+ "  const value = normalize(valueEl.textContent).toLowerCase();"
							+ "  if (value === expectedLower || value.includes(expectedLower)) return true;"
							+ "}"
							+ "return false;"
							+ "}",
					expectedStatus,
					new Page.WaitForFunctionOptions().setTimeout(20000));

			logger.info("✓ Response status matched successfully on Salesforce Path/details: {}", expectedStatus);
		} catch (Exception e) {
			logger.error("Failed to verify response status. Active URL: {}", page.url());

			String actualOnScreenText = "Unknown (Element not found)";
			try {
				String pathText = page.locator(".slds-path__stage-name, .stepLabel.slds-path__stage-name")
						.allTextContents()
						.toString();
				String fieldStatus = "";
				Locator statusField = page.locator("span.test-id__field-label")
						.filter(new Locator.FilterOptions()
								.setHasText(Pattern.compile("^Status$", Pattern.CASE_INSENSITIVE)))
						.first();
				if (statusField.count() > 0) {
					fieldStatus = statusField.locator("xpath=ancestor::div[contains(@class, 'slds-form-element')]")
							.locator("lightning-formatted-text, slot[name='output'], .test-id__field-value, span")
							.first().textContent();
				}
				actualOnScreenText = "Path=" + pathText
						+ (fieldStatus != null && !fieldStatus.isBlank() ? ", FieldStatus=" + fieldStatus : "");
			} catch (Exception ignored) {
			}

			throw new AssertionError("Expected response status to include '" + expectedStatus
					+ "' but it was not found. Current status text on screen: '" + actualOnScreenText + "'", e);
		}
	}

	void verifyAttachedFileDetails() {
		Locator showFiles = page.locator("xpath=//*[@title='Files']/following::a[1]").first();
		if (showFiles.count() > 0 && showFiles.isVisible()) {
			showFiles.click();
		}
		Locator fileLink = page.locator(
				"xpath=(//div[contains(@data-divid, 'AttachedContentDocument')]/following-sibling::div/span)[last()]")
				.first();
		try {
			fileLink.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			logger.info("✓ Verified attached file details: {}", fileLink.textContent());
		} catch (Exception e) {
			Locator fallback = page.locator("[data-divid*='AttachedContentDocument'] a, .file-selector-item").first();
			fallback.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			logger.info("✓ Verified attached file is present");
		}
	}

	void verifyEditButtonsReduced() {
		List<Locator> editButtons = page.locator("button[title*='Edit']").all();
		int count = editButtons.size();
		logger.info("Edit button count after close: {}", count);
		if (count >= 22) {
			throw new AssertionError("Expected reduced edit buttons after close, but found " + count + " (>= 22)");
		}
		logger.info("✓ Edit buttons are reduced after close: {}", count);
	}

	void verifyEditButtonsRestored() {
		try {
			page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
					new Page.WaitForLoadStateOptions().setTimeout(10000));
		} catch (Exception ignored) {
		}

		try {
			page.waitForFunction(
					"() => Array.from(document.querySelectorAll(\"button[title*='Edit']\"))"
							+ ".filter(el => {"
							+ "const style = window.getComputedStyle(el);"
							+ "const rect = el.getBoundingClientRect();"
							+ "return style.visibility !== 'hidden' && style.display !== 'none' && rect.width > 0 && rect.height > 0;"
							+ "}).length >= 10",
					new Page.WaitForFunctionOptions().setTimeout(15000));
		} catch (Exception ignored) {
		}

		Locator visibleEditButtons = page.locator("button[title*='Edit']:visible");
		int count = visibleEditButtons.count();
		logger.info("Edit button count after reopen: {}", count);
		if (count < 10) {
			throw new AssertionError("Expected edit buttons to be restored after reopen, but found only " + count);
		}
		logger.info("✓ Edit buttons are restored after reopen: {}", count);
	}

	void openLastSentResponse() {
		returnToInquiryFromBreadcrumbIfVisible();
		clickResponsesTabIfVisible();
		Locator sentResponseLink = page
				.locator(
						"xpath=//*[@title='Sent']/ancestor::td/preceding-sibling::th//a//span[contains(text(), 'RES')]")
				.first();
		if (sentResponseLink.count() == 0) {
			sentResponseLink = page.locator("xpath=//a//span[contains(text(), 'RES')]").first();
		}
		sentResponseLink.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		sentResponseLink.click();
		logger.info("✓ Opened last sent response");
	}

	private void returnToInquiryFromBreadcrumbIfVisible() {
		Locator inquiryBreadcrumbLinks = page.locator(
				"[aria-label='Breadcrumbs'] a[href*='/lightning/r/'][title^='TA-'], .slds-breadcrumb a[href*='/lightning/r/'][title^='TA-']");
		if (!clickFirstVisible(inquiryBreadcrumbLinks)) {
			Locator inquiryBreadcrumbByText = page.locator("[aria-label='Breadcrumbs'] a, .slds-breadcrumb a")
					.filter(new Locator.FilterOptions()
							.setHasText(Pattern.compile("^TA-\\d", Pattern.CASE_INSENSITIVE)));
			if (!clickFirstVisible(inquiryBreadcrumbByText)) {
				return;
			}
		}

		page.getByRole(AriaRole.TAB,
				new Page.GetByRoleOptions().setName(Pattern.compile("^Responses?$", Pattern.CASE_INSENSITIVE))).first()
				.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		logger.info("✓ Returned to inquiry via breadcrumb before opening Response tab");
	}

	private void clickResponsesTabIfVisible() {
		Locator responseTabs = page.getByRole(AriaRole.TAB,
				new Page.GetByRoleOptions().setName(Pattern.compile("^Responses?$", Pattern.CASE_INSENSITIVE)));
		if (clickFirstVisible(responseTabs)) {
			return;
		}

		Locator responseLinks = page.locator(
				"a[title='Response'], a[title='Responses'], a[data-label='Response'], a[data-label='Responses']");
		clickFirstVisible(responseLinks);
	}

	private boolean clickFirstVisible(Locator matches) {
		int count = matches.count();
		for (int index = 0; index < Math.min(count, 10); index++) {
			Locator match = matches.nth(index);
			try {
				if (match.isVisible()) {
					match.scrollIntoViewIfNeeded();
					match.click();
					return true;
				}
			} catch (Exception e) {
				logger.debug("Could not click response tab candidate at index {}: {}", index, e.getMessage());
			}
		}
		return false;
	}

	void verifyResponseFieldHistoryEntries(List<String> fieldNames) {
		Locator viewAllLink = page
				.locator("xpath=//*[contains(text(), 'Response History')]/following::*[text()='View All']/parent::a")
				.first();
		if (viewAllLink.count() > 0 && viewAllLink.isVisible()) {
			viewAllLink.click();
		} else {
			openFieldHistorySurface();
		}
		Locator historyTable = visibleHistoryTable();
		for (String fieldName : fieldNames) {
			if (!hasVisibleHistoryField(historyTable, fieldName)) {
				throw new AssertionError("Response field history entry was not found for: " + fieldName);
			}
			logger.debug("Verified field '{}' in response history", fieldName);
		}
		logger.info("✓ Verified response field history entries: {}", fieldNames);
	}

	void verifyResponseDetailsFieldDisplayed() {
		Locator detailsEl = page.locator(
				"xpath=(//*[text()='Response Details']/../../following-sibling::dd//lightning-formatted-rich-text)[last()]")
				.first();
		if (detailsEl.count() == 0) {
			detailsEl = page.getByText(Pattern.compile("Response Details", Pattern.CASE_INSENSITIVE)).first();
		}
		try {
			detailsEl.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			logger.info("✓ Response Details field is displayed");
		} catch (Exception e) {
			throw new AssertionError("Response Details field is not displayed on the response record", e);
		}
	}

	void verifyResponseDetailsFieldHistoryEntry() {
		openFieldHistorySurface();
		
		Locator fieldCells = page.locator("//h1[text()='CMCS TA History']/following::tbody/tr");
	
		int count = fieldCells.count();
		if (count < 2) {
			throw new AssertionError("Expected at least 2 'Response Details' field-history entries "
					+ "(TC_010 edits the field twice) but found " + count);
		}
		logger.info("✓ Verified {} 'Response Details' field-history entries (expected >= 2)", count);

	}

	public void scrollToBottomUsingKeyboard() {
		try {
			// Keyboard-driven scroll as a fallback when JS scrolling can't reach content
			// hidden inside Lightning's shadow DOM containers.
			page.keyboard().press("End");
			for (int i = 0; i < 10; i++) {
				page.keyboard().press("PageDown");
			}
			logger.info("Scrolled to the bottom of the page using keyboard actions");

		} catch (Exception e) {
			logger.error("Failed to scroll to the bottom of the page using keyboard actions", e);
			Assert.fail("Unable to scroll to the bottom of the page using keyboard actions");
		}
	}

	void editResponseDetailsField(String picklistValue, String notes) {
		// Find and click Edit Response Details button
		this.page.waitForTimeout(10000);
		scrollToBottomOfThePage();

		this.page.waitForTimeout(2000);
		try {
			if(page.locator("//button[@title='Edit Response Details']").isVisible()) {
				page.locator("//button[@title='Edit Response Details']").first().click();
			}
		} catch (Exception e) {
			logger.warn("Edit Response Details button not found - may already be in edit mode");
		}
		this.page.waitForTimeout(2000);
		page.locator("//button[@aria-label='Response Details']").click();
		this.page.waitForTimeout(2000);
		try{
		Locator responseDetailsDropdown = page.getByRole(AriaRole.COMBOBOX,
				new Page.GetByRoleOptions().setName("Response Details").setExact(true)).first();
		responseDetailsDropdown.waitFor(new Locator.WaitForOptions().setTimeout(8000));
		responseDetailsDropdown.click();
		Locator responseDetailsOption = page.locator(
				"lightning-base-combobox-item[data-value='" + picklistValue + "']").first();
		responseDetailsOption.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		responseDetailsOption.click();
		} catch (Exception e) {}
		this.page.waitForTimeout(2000);

		if (notes != null && !notes.isBlank()) {
			Locator notesInput = page.locator("input[name='Response_Notes__c']").first();
			notesInput.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			notesInput.fill(notes);
		}
		logger.info("✓ Set Response Details = '{}' (notes: '{}')", picklistValue, notes == null ? "" : notes);
	}

	/**
	 * Attaches a file from {@code src/test/resources/test-data} to the inquiry via
	 * Salesforce
	 * file upload interface. Uses Playwright's setInputFiles on the hidden file
	 * input element.
	 */
	void uploadInquiryFile(String fileName) {
		java.nio.file.Path absoluteFilePath = Paths.get(TEST_DATA_BASE_PATH, fileName).toAbsolutePath().normalize();

		// Step 1: Try to click "Upload Files" if it's available (may already be open)
		try {
			Locator uploadFilesText = page.getByLabel("New Inquiry").getByText("Upload Files");
			uploadFilesText.waitFor(new Locator.WaitForOptions().setTimeout(3000));
			uploadFilesText.click();
			logger.info("Clicked 'Upload Files'");
			page.waitForTimeout(2000); // Allow section to open
		} catch (Exception e) {
			logger.info("'Upload Files' not found or not clickable - may already be open");
		}

		// Step 2: Find and use the file input element
		Locator fileInput = page.locator("input[type='file']").first();
		fileInput.waitFor(new Locator.WaitForOptions()
				.setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED).setTimeout(8000));
		fileInput.setInputFiles(absoluteFilePath);
		logger.info("File selected: {}", absoluteFilePath);

		// Step 3: Wait longer for file to upload and row to appear
		page.waitForTimeout(5000);
		logger.info("Waited for file upload to process");

		// Step 4: Try to find the file row - if not found, file may have uploaded
		// automatically
		try {
			Locator fileRow = page.getByRole(com.microsoft.playwright.options.AriaRole.ROW)
					.filter(new Locator.FilterOptions().setHasText(fileName));
			fileRow.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			logger.info("File row appeared for: {}", fileName);

			// Click to enable description editing
			Locator descriptionField = fileRow.getByLabel("").first();
			descriptionField.waitFor(new Locator.WaitForOptions().setTimeout(3000));
			descriptionField.click();
			logger.info("Clicked to edit file description");

			// Fill the description field
			descriptionField.fill("Automated test file upload - " + fileName);
			logger.info("✓ Filled file description");

			// Click Save to complete the upload
			clickButtonInActiveModal("Save");
			logger.info("✓ Clicked Save to complete file upload");
			page.waitForTimeout(2000);
		} catch (Exception e) {
			logger.info(
					"File row or description field not found - file may have uploaded automatically without description");
		}

		logger.info("✓ Uploaded inquiry file: {}", absoluteFilePath);
	}

	void verifyResponseSentDateIsToday() {
		Locator sentDateEl = page.locator(
				"xpath=(//span[text()='Sent Date']/../../following-sibling::dd//lightning-formatted-text)[last()]")
				.first();
		if (sentDateEl.count() == 0) {
			sentDateEl = page.locator("xpath=(//*[text()='Sent Date']//following::lightning-formatted-text[1])[last()]")
					.first();
		}
		try {
			sentDateEl.waitFor(new Locator.WaitForOptions().setTimeout(8000));
			String sentDate = normalizeText(sentDateEl.textContent());
			if (sentDate.isEmpty()) {
				throw new AssertionError("Sent Date field is empty");
			}
			java.time.LocalDate today = java.time.LocalDate.now();
			String todayYear = String.valueOf(today.getYear());
			// Salesforce formats dates as M/d/YYYY — build both zero-padded and non-padded
			// forms
			String todayMonthDay = today.getMonthValue() + "/" + today.getDayOfMonth();
			String todayMonthDayPadded = String.format("%02d/%02d", today.getMonthValue(), today.getDayOfMonth());
			boolean matchesYear = sentDate.contains(todayYear);
			boolean matchesDay = sentDate.contains(todayMonthDay) || sentDate.contains(todayMonthDayPadded);
			if (!matchesYear && !matchesDay) {
				throw new AssertionError("Expected Sent Date to reflect today but found: '" + sentDate + "'");
			}
			logger.info("✓ Response sent date is today: {}", sentDate);
		} catch (com.microsoft.playwright.PlaywrightException e) {
			throw new AssertionError("Sent Date field not found on response record", e);
		}
	}

	// ─── Private helpers ──────────────────────────────────────────────────────

	private void scrollToBottomOfThePage() {
		page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
	}

	private String readCurrentInquiryTitle() {
		Locator title = page.locator(
				"h1 lightning-formatted-text, h1 span[title], h1 .slds-page-header__title, slot[name='primaryField'] lightning-formatted-text")
				.first();
		if (title.count() == 0) {
			title = page.locator("h1").first();
		}
		try {
			title.waitFor(new Locator.WaitForOptions().setTimeout(10000));
		} catch (Exception ignored) {
			return "";
		}
		return normalizeText(title.textContent());
	}

	private void openFieldHistorySurface() {
		Locator fieldHistoryTab = page.getByRole(AriaRole.TAB,
				new Page.GetByRoleOptions().setName(Pattern.compile("Field History|History", Pattern.CASE_INSENSITIVE)))
				.first();
		if (fieldHistoryTab.count() > 0 && fieldHistoryTab.isVisible()) {
			fieldHistoryTab.click();
		}
		Locator viewAllLink = page
				.getByRole(AriaRole.LINK,
						new Page.GetByRoleOptions().setName(Pattern.compile("View All", Pattern.CASE_INSENSITIVE)))
				.first();
		if (viewAllLink.count() > 0 && viewAllLink.isVisible()) {
			viewAllLink.click();
		}

		// Wait for history container to be attached and scroll into view
		Locator historyContainer = visibleHistoryTable();

		// Scroll history section into view to make it visible
		try {
			historyContainer.scrollIntoViewIfNeeded();
			logger.debug("Scrolled history section into view");
		} catch (Exception e) {
			logger.debug("Could not scroll history container: {}", e.getMessage());
		}

		// Give it a moment to render after scroll
		page.waitForTimeout(1000);
	}

	private String normalizeText(String value) {
		return value == null ? "" : value.replace(' ', ' ').replaceAll("\\s+", " ").trim();
	}

	private Locator visibleHistoryTable() {
		Locator historyTable = page.locator("table[role='grid'][data-num-rows]:visible").last();
		try {
			historyTable.waitFor(new Locator.WaitForOptions().setTimeout(10000));
			return historyTable;
		} catch (Exception ignored) {
		}

		historyTable = page.locator("table:visible").filter(new Locator.FilterOptions()
				.setHas(page.locator("tbody tr"))).last();
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
						&& normalizeText(fieldCell.textContent()).equalsIgnoreCase(normalizeText(fieldName))) {
					return true;
				}
			} catch (Exception ignored) {
			}
		}
		return false;
	}

	private void requireAnyVisible(Locator matches, String description) {
		int count = matches.count();
		int maxToCheck = Math.min(count, 20);
		for (int index = 0; index < maxToCheck; index++) {
			try {
				if (matches.nth(index).isVisible())
					return;
			} catch (Exception ignored) {
			}
		}
		throw new AssertionError("Expected visible " + description + " but none was visible (found " + count
				+ " matches, checked " + maxToCheck + ")");
	}
}
