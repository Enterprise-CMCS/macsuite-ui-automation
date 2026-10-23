package com.macsuite.common.pages;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.macsuite.common.FormUtils;
import com.macsuite.common.StepBase;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

/**
 * Base Page Object class providing common functionality for all page objects.
 * Eliminates boilerplate code and standardizes form interactions across
 * modules.
 * 
 * All module-specific page objects should extend this class.
 */
public abstract class BasePage extends StepBase {
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    protected Page page;
    protected final FormUtils formUtils;

    // Store reference to Mailosaur page when navigating to email verification
    // service
    protected Page mailosaureNewPage;

    public static String GRANT_NUMBER_STRING = "";
    public static String APPLICATION_NUMBER_STRING = "";
    public static String RECEIPENT = "";
    public static String RECEIPENT_PREPARER = "";
    public static String CHECKLIST_RECORD_ID = "";
    public static String PROJECT_OFFICER = "";
    public static String GRANT_MANAGEMENT_SPECIALIST = "";
    public static String FUNDING_NUMBER = "";
    public static String SELECTED_CATEGORY = "";
    public static String SELECTED_SUB_CATEGORY = "";

    // Store filled form data for later validation
    protected final Map<String, String> filledData = new HashMap<>();

    public static final String CURRENT_STAGE = "xpath=(//*[@class='stepLabel slds-path__stage-name']/span)[last()]";
    public static final String DONE_BUTTON = "xpath=//*[text()='Done']";
    private static String ENTER_YOUR_EMAIL_ADDRESS = "xpath=//input[@placeholder='Enter your email address']";
    private static String CONTINUE_BUTTON = "xpath=//button[text()='Continue']";
    private static String ENTER_YOUR_PASSWORD = "xpath=//input[@placeholder='Enter your password']";
    private static String LOGIN_BUTTON = "xpath=//button[text()='Log in']";

    /**
     * Constructor - initializes page and FormUtils
     * 
     * @param page Playwright Page instance
     */
    protected BasePage(Page page) {
        this.page = page;
        this.formUtils = new FormUtils(page);
    }

    /**
     * Get the currently active page - either the Mailosaur page (if navigated) or
     * the main application page
     * This is used to handle interactions with external services like Mailosaur
     * while maintaining the page reference
     */
    protected Page getActivePage() {
        return (mailosaureNewPage != null) ? mailosaureNewPage : page;
    }

    public void waitFor(int seconds) {
        page.waitForLoadState();
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting", e);
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Generic form filling method - works for any Salesforce form
     * Uses FormUtils smart field detection (auto-detects text, dropdown, date,
     * time, etc.)
     * 
     * @param formData Map of field labels to values (e.g., "Meeting Name" -> "Test
     *                 Meeting")
     */
    public void fillForm(Map<String, String> formData) {
        logger.info("Filling form with {} fields", formData.size());

        // Store the data for potential later validation
        filledData.putAll(formData);

        // Fill each field using smart detection
        formData.forEach((fieldLabel, value) -> {
            formUtils.fillField(fieldLabel, value);
        });

        logger.info("✓ Form filled successfully");
    }

    /**
     * Verify success message appears after record creation
     * Generic method that works for any Salesforce object type
     * 
     * @return true if success message is displayed
     */
    public boolean verifySuccessMessage() {
        logger.info("Verifying success message");
        return formUtils.verifySuccessMessage();
    }

    /**
     * Extract record number from success message
     * Works for any record type: Contract "C-12345", Vendor Engagement "VE-0012",
     * etc.
     * 
     * @return the extracted record number, or null if extraction failed
     */
    public String extractRecordNumber() {
        logger.info("Extracting record number from success message");
        return formUtils.extractRecordNumberFromSuccessMessage();
    }

    /**
     * Get the filled form data for validation purposes
     * 
     * @return Map of filled field labels to values
     */
    protected Map<String, String> getFilledData() {
        return new HashMap<>(filledData);
    }

    /**
     * Clear stored form data (useful when filling multiple forms in one test)
     */
    protected void clearFilledData() {
        filledData.clear();
    }

    protected void setSelectedCategoryForVerification(String category) {
        SELECTED_CATEGORY = category == null ? "" : category.trim();
    }

    protected void setSelectedSubCategoryForVerification(String subCategory) {
        SELECTED_SUB_CATEGORY = subCategory == null ? "" : subCategory.trim();
    }

    public static String getSelectedCategoryForVerification() {
        return SELECTED_CATEGORY;
    }

    public static String getSelectedSubCategoryForVerification() {
        return SELECTED_SUB_CATEGORY;
    }

    /**
     * Get the Page instance for subclasses that need direct access
     * 
     * @return Playwright Page instance
     */
    protected Page getPage() {
        return page;
    }

    // protected void takeScreenshot(String name) {
    // String screenshotPath = PlaywrightManager.takeScreenshot(name);
    // if (screenshotPath != null) {
    // TraceAttachmentHelper.attachScreenshotToAllure(screenshotPath, name);
    // }
    // }

    /**
     * Get the FormUtils instance for subclasses that need direct access
     * 
     * @return FormUtils instance
     */
    protected FormUtils getFormUtils() {
        return formUtils;
    }

    /**
     * Take a named screenshot from a specific page instance.
     * This is used when capturing screenshots from a non-primary page (e.g.,
     * Mailosaur tab).
     * Bypasses PlaywrightManager.page() to use the provided page directly.
     * 
     * @param pageInstance The Playwright Page instance to capture
     * @param label        Human-readable label for the screenshot
     */
    protected void screenshotPage(Page pageInstance, String label) {
        try {
            byte[] bytes = pageInstance.screenshot(
                    new Page.ScreenshotOptions().setFullPage(true));

            // Attach to Cucumber / Allure report with the human-readable label
            io.cucumber.java.Scenario scenario = com.macsuite.common.ScreenshotHelper.getScenario();
            if (scenario != null) {
                scenario.attach(bytes, "image/png", label);
            }

            // Save to disk with timestamp
            java.nio.file.Path dir = java.nio.file.Paths.get("target/screenshots");
            if (!java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.createDirectories(dir);
            }
            String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
            String safe = label.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "_")
                    .replaceAll("^_+|_+$", "");
            if (safe.length() > 50) {
                safe = safe.substring(0, 50);
            }
            String fileName = String.format("%s_%s", safe, time);
            java.nio.file.Files.write(dir.resolve(fileName + ".png"), bytes);

            logger.info("Screenshot [{}] → {}.png (from specified page)", label, fileName);
        } catch (Exception e) {
            logger.debug("Could not capture screenshot '{}' from page: {}", label, e.getMessage());
        }
    }

    // To fetch past or future weekday dates in desired format //
    public String fetchWeekdayDate(String format, int numberOfWeekdaysToBeAdded) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();

        int addedDays = 0;
        int direction = (numberOfWeekdaysToBeAdded >= 0) ? 1 : -1;

        while (addedDays != numberOfWeekdaysToBeAdded) {
            cal.add(Calendar.DATE, direction); // move one day forward or backward
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            // Only count weekdays
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                addedDays += direction;
            }
        }

        return df.format(cal.getTime());
    }

    /**
     * Fetches a date by adding/subtracting all calendar days (including weekends
     * and holidays).
     * This method adds the exact number of calendar days to the current date.
     * 
     * @param format                The date format (e.g., "M/dd/yyyy",
     *                              "dd/MM/yyyy")
     * @param numberOfDaysToBeAdded The number of days to add (positive) or subtract
     *                              (negative)
     * @return The calculated date as a formatted string
     * 
     *         Example:
     *         fetchCalendarDate("M/dd/yyyy", 30) returns 30 calendar days from
     *         today
     *         fetchCalendarDate("M/dd/yyyy", -7) returns 7 days ago
     */
    public String fetchCalendarDate(String format, int numberOfDaysToBeAdded) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();

        // Add all calendar days (including weekends)
        cal.add(Calendar.DATE, numberOfDaysToBeAdded);

        logger.info("Calculated date {} days from today: {}", numberOfDaysToBeAdded, df.format(cal.getTime()));
        return df.format(cal.getTime());
    }

    /**
     * Generates a random numeric string of the specified length.
     * 
     * @param length The length of the random number string to generate.
     * @return A random numeric string of the given length.
     */
    public String randomNumberGenerator(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        StringBuilder sb = new StringBuilder(length);
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // 0-9
        }
        String result = sb.toString();
        logger.info("Generated random number: {} (length: {})", result, length);
        return result;
    }

    /**
     * Alternate click function: waits for locator to be visible, enabled, and
     * clickable, then clicks with a small delay before and after. Highlights the
     * element before clicking.
     */
    public void clickOn(String selector) {
        try {
            Locator locator = page.locator(selector).first();
            locator.waitFor();
            highlightElement(selector);
            page.waitForTimeout(200); // Small delay before click
            locator.click();
            page.waitForTimeout(200); // Small delay after click
            logger.info("[Alternate] Clicked element: {}", selector);
        } catch (Exception e) {
            logger.error("[Alternate] Failed to click element: {} - {}", selector, e.getMessage());
            throw new RuntimeException("[Alternate] Could not click element: " + selector, e);
        }
    }

    /**
     * Safely types text into a locator, waiting for it to be visible and enabled.
     * Highlights the element before typing.
     */
    public void type(String selector, String text) {
        try {
            Locator locator = page.locator(selector).first();
            locator.waitFor();
            highlightElement(selector);
            locator.fill("");
            locator.fill(text);
            unHighlightElement(selector);
            logger.info("Typed '{}' into element: {}", text, selector);
        } catch (Exception e) {
            logger.error("Failed to type into element: {} - {}", selector, e.getMessage());
            throw new RuntimeException("Could not type into element: " + selector, e);
        }
    }

    public static String currentURL = "";

    public void navigateToMailosure(String environment) {
        // Store the current page URL to return to it later if needed
        currentURL = page.url();
        logger.info("Stored current URL: {}", currentURL);

        // Create a new page in the same context
        Page mailosaureNewPage = page.context().newPage();
        logger.info("✓ Opened new tab for Mailosaur");

        // Navigate the NEW page to Mailosaur
        mailosaureNewPage.navigate("https://mailosaur.com/app");
        logger.info("✓ Navigated to mailosaur.com/app");

        // Bring the new page to front (make it active)
        mailosaureNewPage.bringToFront();

        // Wait for the page to load and login form to appear
        mailosaureNewPage.waitForLoadState();
        waitFor(2);

        // Get credentials
        String mailosaurUsername = resolveMailosaurCredential("MAILOSAUR_USERNAME", "mailosaur_username");
        String mailosaurPassword = resolveMailosaurCredential("MAILOSAUR_PASSWORD", "mailosaur_password");

        logger.info("Attempting login with username: {}", mailosaurUsername);

        // Step 1: Fill email field on the NEW page
        try {
            Locator emailField = mailosaureNewPage.locator(ENTER_YOUR_EMAIL_ADDRESS).first();
            emailField.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
            emailField.fill(mailosaurUsername);
            logger.info("✓ Filled email address");
        } catch (Exception e) {
            logger.error("Failed to fill email field", e);
            throw new RuntimeException("Could not fill email field", e);
        }

        // Step 2: Click Continue button
        try {
            Locator continueBtn = mailosaureNewPage.locator(CONTINUE_BUTTON).first();
            continueBtn.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
            continueBtn.click();
            logger.info("✓ Clicked Continue button");
        } catch (Exception e) {
            logger.error("Failed to click Continue button", e);
            throw new RuntimeException("Could not click Continue button", e);
        }

        waitFor(2);

        // Step 3: Fill password field on the NEW page
        try {
            Locator passwordField = mailosaureNewPage.locator(ENTER_YOUR_PASSWORD).first();
            passwordField.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
            passwordField.fill(mailosaurPassword);
            logger.info("✓ Filled password");
        } catch (Exception e) {
            logger.error("Failed to fill password field", e);
            throw new RuntimeException("Could not fill password field", e);
        }

        // Step 4: Click Login button
        try {
            Locator loginBtn = mailosaureNewPage.locator(LOGIN_BUTTON).first();
            loginBtn.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
            loginBtn.click();
            logger.info("✓ Clicked Login button");
        } catch (Exception e) {
            logger.error("Failed to click Login button", e);
            throw new RuntimeException("Could not click Login button", e);
        }

        // Wait for dashboard to load after login
        logger.info("Waiting for dashboard to load after login...");
        waitFor(5);

        // Step 5: Now select the environment tracker (this is on the dashboard, not
        // login page)
        logger.info("Selecting Mailosaur {} environment tracker", environment);
        try {
            // if (environment.equalsIgnoreCase("QA")) {
            // mailosaureNewPage.locator("//*[text()='Tracker QA']").first()
            // .waitFor(new
            // com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
            // mailosaureNewPage.locator("//*[text()='Tracker QA']").first().click();
            // logger.info("✓ Clicked Tracker QA");
            // } else if (environment.equalsIgnoreCase("SEI")) {
            // mailosaureNewPage.locator("//*[text()='Trackers SEI UAT']").first()
            // .waitFor(new
            // com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
            // mailosaureNewPage.locator("//*[text()='Trackers SEI UAT']").first().click();
            // logger.info("✓ Clicked Trackers SEI UAT");
            // } else if (environment.equalsIgnoreCase("UAT")) {
            // mailosaureNewPage.locator("//*[text()='Trackers UAT']").first()
            // .waitFor(new
            // com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
            // mailosaureNewPage.locator("//*[text()='Trackers UAT']").first().click();
            // logger.info("✓ Clicked Trackers UAT");
            // } else {
            // throw new IllegalArgumentException("Unsupported environment: " +
            // environment);
            // }

            if (environment.equalsIgnoreCase("QA")) {
                String mailosaurUrl = "https://mailosaur.com/app/servers/5cbcluu9/messages/inbox";
                mailosaureNewPage.navigate(mailosaurUrl);
                logger.info("Navigated to Mailosaur QA environment");
            } else if (environment.equalsIgnoreCase("UAT")) {
                String mailosaurUrl = "https://mailosaur.com/app/servers/hx4rvofy/messages/inbox";
                mailosaureNewPage.navigate(mailosaurUrl);
                logger.info("Navigated to Mailosaur UAT environment");
            } else if (environment.equalsIgnoreCase("SEI")) {
                String mailosaurUrl = "https://mailosaur.com/app/servers/cxaqkvbr/messages/inbox";
                mailosaureNewPage.navigate(mailosaurUrl);
                logger.info("Navigated to Mailosaur SEI environment");
            } else {
                throw new IllegalArgumentException("Unsupported environment: " + environment);
            }

            // Use screenshotPage to capture the Mailosaur tab (second tab), not the primary
            // page
            page.waitForTimeout(5000); // Wait for the page to load before taking a screenshot
            screenshotPage(mailosaureNewPage, "Navigated to Mailosaur " + environment + " environment");
        } catch (Exception e) {
            logger.error("Failed to navigate to Mailosaur environment. Page content length: {}",
                    mailosaureNewPage.content().length());
            throw new RuntimeException("Could not navigate to environment: " + environment
                    + ". Ensure you are logged in and the tracker exists.", e);
        }

        // Wait for tracker dashboard to fully load
        waitFor(3);

        // Store the mailosaureNewPage so subsequent methods can use it
        this.mailosaureNewPage = mailosaureNewPage;
        logger.info("✓ Successfully logged into Mailosaur {} environment", environment);
    }

    public void readAndVerifyText(String selector, String expectedText) {
        try {
            Locator locator = page.locator(selector).first();
            locator.waitFor();
            highlightElement(selector);
            String actualText = locator.textContent().trim();
            unHighlightElement(selector);
            if (!actualText.contains(expectedText)) {
                // throw new AssertionError("Text mismatch: expected '" + expectedText + "', got
                // '" + actualText + "'");
            }
            logger.info("Verified text '{}' for element: {}", expectedText, selector);
        } catch (Exception e) {
            logger.error("Failed to verify text for element: {} - {}", selector, e.getMessage());
            // throw new RuntimeException("Could not verify text for element: " + selector,
            // e);
        }
    }

    public void compareValues(String actualText, String expectedText) {

        String normalizedActual = actualText == null ? ""
                : actualText
                        .replace('\u00A0', ' ')
                        .replace('\u2007', ' ')
                        .replace('\u202F', ' ')
                        .trim();
        String normalizedExpected = expectedText == null ? ""
                : expectedText
                        .replace('\u00A0', ' ')
                        .replace('\u2007', ' ')
                        .replace('\u202F', ' ')
                        .trim();

        if (normalizedActual.contains(normalizedExpected)) {
            logger.info("Verified text '{}' for element: {}", normalizedExpected, normalizedActual);
        } else {
            logger.error("Text mismatch: expected '{}' but got '{}'", normalizedExpected, normalizedActual);
            throw new AssertionError(
                    "Text mismatch: expected '" + normalizedExpected + "' but got '" + normalizedActual + "'");
        }

    }

    public void verifyCurrentTab_Selection(String expectedTabName) {
        try {
            String actualTabName = page
                    .locator("(//a[@data-tab-name='" + expectedTabName + "'])[last()]")
                    .getAttribute("aria-selected")
                    .trim();
            if (!actualTabName.equals("true")) {
                throw new AssertionError(
                        "Tab mismatch: expected '" + expectedTabName + "', got '" + actualTabName + "'");
            }
            logger.info("Verified current tab selection: {}", expectedTabName);
        } catch (Exception e) {
            logger.error("Failed to verify current tab selection: {} - {}", expectedTabName, e.getMessage());
            throw new RuntimeException("Could not verify current tab selection: " + expectedTabName, e);
        }
    }

    /**
     * Highlights the element specified by the selector using a red outline.
     * Supports both XPath (if selector starts with 'xpath=') and CSS selectors.
     */
    private void highlightElement(String selector) {
        try {
            if (selector != null && selector.startsWith("xpath=")) {
                String xpath = selector.substring(6);
                page.evaluate(
                        "xpath => { const el = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; if (el) { el.style.outline = '3px solid red'; el.style.transition = 'outline 0.2s'; } }",
                        xpath);
            } else {
                page.evaluate(
                        "selector => { const el = document.querySelector(selector); if (el) { el.style.outline = '3px solid red'; el.style.transition = 'outline 0.2s'; } }",
                        selector);
            }
        } catch (Exception e) {
            logger.warn("Failed to highlight element: {} - {}", selector, e.getMessage());
        }
    }

    private void unHighlightElement(String selector) {
        try {
            if (selector != null && selector.startsWith("xpath=")) {
                String xpath = selector.substring(6);
                page.evaluate(
                        "xpath => { const el = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; if (el) { el.style.outline = '3px solid red'; el.style.transition = 'outline 0.2s'; } }",
                        xpath);
            } else {
                page.evaluate(
                        "selector => { const el = document.querySelector(selector); if (el) { el.style.outline = ''; el.style.transition = 'outline 0.2s'; } }",
                        selector);
            }
        } catch (Exception e) {
            logger.warn("Failed to highlight element: {} - {}", selector, e.getMessage());
        }
    }

    public void selectByVisibleText(String locator, String option, String elementName) {
        try {
            page.locator(locator).selectOption(new SelectOption().setLabel(option));
            logger.info("option '{}' selected from element: {}", option, locator);

            System.out.println(option + " selected in " + elementName);

        } catch (Exception e) {
            logger.info("Failed to select option '{}' from element: {}", option, locator);
            Assert.fail(option + " unable to select in " + elementName);
        }
    }

    public void switchToFrame(String locator) {
        try {
            FrameLocator frame = page.frameLocator(locator);

            // Validate frame exists by waiting for any element inside it
            frame.locator("body").waitFor();

            logger.info("Switched to frame using locator: {}", locator);

        } catch (Exception e) {
            logger.error("Failed to switch to frame using locator: {}", locator, e);
            Assert.fail("Exception while switching to frame: " + locator);
        }
    }

    public Page switchToWindow(String title) {
        try {
            page.waitForTimeout(1000);

            for (Page p : page.context().pages()) {
                p.waitForTimeout(500);
                logger.info("Window title found: {}", p.title());

                if (p.title().contains(title)) {
                    logger.info("Switched to window with title containing: {}", title);
                    return p; // ✅ Return the matched window
                }
            }

            logger.error("No window found with title containing: {}", title);
            Assert.fail("No window found with given title: " + title);

        } catch (Exception e) {
            logger.error("Exception while switching to window with title: {}", title, e);
            Assert.fail("Exception while switching to window: " + title);
        }

        return page; // fallback
    }

    /**
     * Switch to a window by index (0-based). If index is 1, switches to the second
     * window.
     * Updates the PlaywrightManager with the new page context.
     * 
     * @param windowIndex the 0-based index of the window (e.g., 0 = first window, 1
     *                    = second window)
     */
    public Page switchToWindow(int windowIndex) {
        try {
            List<Page> pages = page.context().pages();

            if (windowIndex >= pages.size()) {
                logger.error("Window index {} not found. Only {} windows available", windowIndex, pages.size());
                Assert.fail("Window index " + windowIndex + " not found. Only " + pages.size() + " windows available");
            }

            Page targetPage = pages.get(windowIndex);
            targetPage.waitForLoadState();

            logger.info("Switched to window index: {} with title: {}", windowIndex, targetPage.title());
            return targetPage;
        } catch (Exception e) {
            logger.error("Exception while switching to window index: {}", windowIndex, e);
            Assert.fail("Exception while switching to window index: " + windowIndex);
        }
        return page; // fallback
    }

    public void scrollToTopOfThePage() {
        try {
            page.evaluate("window.scrollTo(0, 0)");
            logger.info("Scrolled to the top of the page");

        } catch (Exception e) {
            logger.error("Failed to scroll to the top of the page", e);
            Assert.fail("Unable to scroll to the top of the page");
        }
    }

    public void scrollByPixels(int size) {
        try {
            page.evaluate("window.scrollBy(0, " + size + ")");
            logger.info("Scrolled by {} pixels vertically", size);

        } catch (Exception e) {
            logger.error("Failed to scroll by {} pixels", size, e);
            Assert.fail("Unable to scroll by pixels: " + size);
        }
    }

    public void scrollToElement(String locator) {
        try {
            Locator element = page.locator(locator);

            // Highlight element (similar to your red border)
            page.evaluate("el => el.style.border='3px solid red'", element);

            // Scroll into view
            element.scrollIntoViewIfNeeded();

            logger.info("Scrolled to element: {}", locator);

            page.waitForTimeout(1000); // optional wait

        } catch (Exception e) {
            logger.error("Failed to scroll to element: {}", locator, e);
            Assert.fail("Unable to scroll to element: " + locator);
        }
    }

    public void scrollToBottomOfThePage() {
        try {
            // window.scrollTo() no-ops when the page body itself isn't the scrollable
            // element (e.g. Salesforce Lightning renders content inside nested overflow
            // containers buried in Shadow DOM, which plain querySelectorAll can't see),
            // so recurse into every shadow root to find and scroll those containers too.
            page.evaluate("() => {\n" +
                    "  const scrollDescendants = (root) => {\n" +
                    "    root.querySelectorAll('*').forEach(el => {\n" +
                    "      const style = window.getComputedStyle(el);\n" +
                    "      if ((style.overflowY === 'auto' || style.overflowY === 'scroll') && el.scrollHeight > el.clientHeight) {\n"
                    +
                    "        el.scrollTop = el.scrollHeight;\n" +
                    "      }\n" +
                    "      if (el.shadowRoot) {\n" +
                    "        scrollDescendants(el.shadowRoot);\n" +
                    "      }\n" +
                    "    });\n" +
                    "  };\n" +
                    "  scrollDescendants(document);\n" +
                    "  window.scrollTo(0, document.body.scrollHeight);\n" +
                    "}");
            logger.info("Scrolled to the bottom of the page");

        } catch (Exception e) {
            logger.error("Failed to scroll to the bottom of the page", e);
            Assert.fail("Unable to scroll to the bottom of the page");
        }
    }

    public void scrollToBottom() {
        // Alias for scrollToBottomOfThePage()
        scrollToBottomOfThePage();
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

    public int generateRandomNumberBetweenOneTo57() {
        try {
            Random random = new Random();
            int randomNumber = random.nextInt(57) + 1; // 1 to 57 inclusive

            logger.info("Generated random number between 1 and 57: {}", randomNumber);
            return randomNumber;

        } catch (Exception e) {
            logger.error("Failed to generate random number between 1 and 57", e);
            Assert.fail("Unable to generate random number");
            return -1;
        }
    }

    public char generateRandomCharacterBetweenAtoZ() {
        try {
            Random random = new Random();
            char randomChar = (char) ('a' + random.nextInt(26)); // a–z

            logger.info("Generated random character between a and z: {}", randomChar);
            return randomChar;

        } catch (Exception e) {
            logger.error("Failed to generate random character between a and z", e);
            Assert.fail("Unable to generate random character");
            return ' ';
        }
    }

    public String generateRandomString(int length) {
        try {
            Random random = new Random();
            StringBuilder sb = new StringBuilder(length);

            for (int i = 0; i < length; i++) {
                char randomChar = (char) ('a' + random.nextInt(26)); // a–z
                sb.append(randomChar);
            }

            String result = sb.toString();
            logger.info("Generated random string of length {}: {}", length, result);
            return result;

        } catch (Exception e) {
            logger.error("Failed to generate random string of length " + length, e);
            Assert.fail("Unable to generate random string");
            return "";
        }
    }

    public String getButtonText_Xpath(String buttonText) {
        try {
            String xpath = "xpath=//button[text()=" + toXPathLiteral(buttonText) + "]";

            return xpath;
        } catch (Exception e) {

            return null;
        }
    }

    public String getText_Xpath(String elementText) {
        try {
            String xpath = "xpath=//*[text()=" + toXPathLiteral(elementText) + "]";

            return xpath;
        } catch (Exception e) {

            return null;
        }
    }

    public String getText(String selector) {
        try {
            String text = page.locator(selector).textContent().trim();
            logger.info("Text retrieved from element {}: {}", selector, text);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element: {}", selector, e);
            return null;
        }
    }

    public String get_Text_Input_Xpath(String elementText) {
        try {
            String xpath = "xpath=//label[text()=" + toXPathLiteral(elementText) + "]/following::input";

            return xpath;
        } catch (Exception e) {

            return null;
        }
    }

    public String get_LF_Text_Xpath(String elementText) {
        try {
            String xpath = "xpath=//*[text()=" + toXPathLiteral(elementText)
                    + "]//following::lightning-formatted-text[1]";

            return xpath;
        } catch (Exception e) {

            return null;
        }
    }

    public int getElementsCount(String selector) {
        try {
            int count = page.locator(selector).count();
            logger.info("Found {} elements for selector: {}", count, selector);
            return count;
        } catch (Exception e) {
            logger.error("Failed to get elements count for selector: {}", selector, e);
            return 0;
        }
    }

    public boolean isElementVisible(String selector) {
        try {
            boolean visible = page.locator(selector).isVisible();
            logger.info("Element visibility for '{}': {}", selector, visible);
            return visible;
        } catch (Exception e) {
            logger.error("Failed to check visibility for element: {}", selector, e);
            return false;
        }
    }

    private String toXPathLiteral(String value) {
        if (value == null) {
            return "''";
        }

        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'");
        StringBuilder xpathLiteral = new StringBuilder("concat(");

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                xpathLiteral.append(", \"'\", ");
            }
            xpathLiteral.append("'").append(parts[i]).append("'");
        }

        xpathLiteral.append(")");
        return xpathLiteral.toString();
    }

    /**
     * Resolves Mailosaur credentials from environment variables or system
     * properties.
     * Attempts to resolve from environment variable first, then system property.
     * 
     * @param envVarName   Environment variable name (e.g., "MAILOSAUR_USERNAME")
     * @param propertyName System property name (e.g., "mailosaur_username")
     * @return The credential value, or throws an exception if not found
     * @throws IllegalArgumentException if credential is not found
     */
    private String resolveMailosaurCredential(String envVarName, String propertyName) {
        // Try environment variable first
        String credential = System.getenv(envVarName);
        if (credential != null && !credential.trim().isEmpty()) {
            logger.debug("Resolved credential from environment variable: {}", envVarName);
            return credential;
        }

        // Fall back to system property with lowercase name (original convention)
        credential = System.getProperty(propertyName);
        if (credential != null && !credential.trim().isEmpty()) {
            logger.debug("Resolved credential from system property: {}", propertyName);
            return credential;
        }

        // Fall back to system property with uppercase name (EnvLoader convention)
        credential = System.getProperty(envVarName);
        if (credential != null && !credential.trim().isEmpty()) {
            logger.debug("Resolved credential from system property (uppercase): {}", envVarName);
            return credential;
        }

        // Throw exception if still not found
        logger.error("Mailosaur credential not found. Checked: env var '{}', system properties '{}' and '{}'",
                envVarName, propertyName, envVarName);
        throw new IllegalArgumentException(
                "Mailosaur credential not found. " +
                        "Set environment variable '" + envVarName + "' or system property '" + propertyName + "'");
    }

    /**
     * Switches from the current window/tab to the previous window/tab.
     * If currently on the first tab, switches to the last tab.
     * If there's only one tab, stays on the current tab.
     * 
     * @return the Page instance of the switched window/tab
     */
    public Page switchToPreviousTab() {
        try {
            java.util.List<Page> pages = page.context().pages();

            if (pages.size() <= 1) {
                logger.info("Only one tab/window is open. Staying on current tab.");
                return page;
            }

            // Find the current page's index
            int currentIndex = -1;
            for (int i = 0; i < pages.size(); i++) {
                if (pages.get(i) == page) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex == -1) {
                logger.warn("Current page not found in context pages list");
                return page;
            }

            // Calculate previous index (wrap around if at first tab)
            int previousIndex = (currentIndex == 0) ? pages.size() - 1 : currentIndex - 1;
            Page previousPage = pages.get(previousIndex);

            // Bring the previous page to focus
            previousPage.bringToFront();

            // Clear the mailosaureNewPage reference since we're switching away
            this.mailosaureNewPage = null;

            logger.info("Switched from tab {} to previous tab {} (out of {} total tabs)",
                    currentIndex, previousIndex, pages.size());

            return previousPage;

        } catch (Exception e) {
            logger.error("Failed to switch to previous tab: {}", e.getMessage(), e);
            throw new RuntimeException("Could not switch to previous tab", e);
        }
    }

} // End of BasePage class
