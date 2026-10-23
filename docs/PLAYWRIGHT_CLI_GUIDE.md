# Playwright CLI Integration Guide

This guide shows how to use `playwright-cli` effectively with the MACSuite UI Automation framework.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Use Cases](#use-cases)
3. [Locator Development Workflow](#locator-development-workflow)
4. [Authentication State Management](#authentication-state-management)
5. [Debugging Failed Tests](#debugging-failed-tests)
6. [Trace Analysis](#trace-analysis)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Quick Start

### Installation

```powershell
# Install playwright-cli globally (if not already installed)
npm install -g @playwright/cli@latest

# Verify installation
playwright-cli --version
```

### Basic Commands

```powershell
# Open browser (headless by default)
playwright-cli open --browser=chrome --headed https://playwright.dev/

# Take snapshot to see element references
playwright-cli snapshot

# Interact with elements
playwright-cli click e15
playwright-cli fill e20 "Test value"

# Generate Java locator
playwright-cli generate-locator e15

# Save authentication state
playwright-cli state-save auth-states/qa/bct-qa.json

# Close browser
playwright-cli close
```

---

## Use Cases

### 1. Locator Development & Discovery

**Problem**: You need to create a new page object for a Salesforce Lightning component but aren't sure what locators will be stable.

**Solution**: Use playwright-cli to interactively explore and test locators before writing Java code.

```powershell
# Open the page in headed mode
playwright-cli open --browser=chrome --headed https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com

# Login manually (or use saved auth state - see section 4)

# Navigate to the page you need to automate
playwright-cli goto /lightning/o/Budget__c/list

# Take snapshot to see all elements
playwright-cli snapshot --filename=budget-list-page.yaml

# Test clicking an element
playwright-cli click e25  # "New" button

# Inspect element attributes
playwright-cli eval "el => el.getAttribute('name')" e25
# Output: "New"

# Generate Java-compatible locator
playwright-cli generate-locator e25
# Output: getByRole('button', { name: 'New' })

# Convert to Java (following P1 priority):
# page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New").setExact(true)).first().click();
```

**Benefits**:
- Verify locators work before writing code
- Discover stable attributes (data-test-id, name, aria-label)
- Test P1-P4 locator strategies interactively
- No compilation required

---

### 2. Salesforce Lightning Component Patterns

#### Inline Edit Fields

```powershell
# Take snapshot of detail page
playwright-cli snapshot

# Find the inline-edit trigger button
# Output shows: e42 [button "Edit Inquiry Title"]

# Test the interaction
playwright-cli click e42
playwright-cli snapshot  # Verify edit mode activated
playwright-cli fill e45 "New Title Value"
playwright-cli click e47  # Save button

# Generate locators for your page object
playwright-cli generate-locator e42  # Edit trigger
playwright-cli generate-locator e45  # Input field
playwright-cli generate-locator e47  # Save button
```

**Java Implementation**:
```java
// In InquiryTrackerPage.java
@Override
public void fillField(String label, String value) {
    // Check for inline edit trigger
    Locator editBtn = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions()
            .setName(Pattern.compile("Edit\\s+" + Pattern.quote(label), Pattern.CASE_INSENSITIVE)))
        .first();
    
    if (editBtn.count() > 0 && editBtn.isVisible()) {
        editBtn.click();
    }
    
    super.fillField(label, value);
}
```

#### Dependent Dropdowns (Category → Sub Category)

```powershell
# Test the dependency flow
playwright-cli snapshot
playwright-cli click e20  # "Group" dropdown
playwright-cli click e25  # Select "Budget" option
playwright-cli snapshot  # Check if "Category" appeared

# Verify timing - does it appear immediately or need wait?
playwright-cli eval "document.querySelector('[data-label=\"Category\"]') !== null"
```

**Java Implementation**:
```java
formUtils.fillField("Group", group);
waitForFieldLabelToAppear("Category", 15000);  // Wait for dependent field
formUtils.fillField("Category", category);
```

#### Lightning Comboboxes

```powershell
# Discover combobox structure
playwright-cli snapshot
# Output: e30 [combobox "Status"]

# Test interaction
playwright-cli click e30
playwright-cli snapshot  # See dropdown options
# Output: e35 [option "Draft"], e36 [option "Active"]

playwright-cli click e35

# Generate locators
playwright-cli generate-locator e30  # Combobox trigger
playwright-cli generate-locator e35  # Option
```

---

### 3. Authentication State Management

#### Generate Auth State for a Role

```powershell
# Manual generation (one-time setup per role/env)
playwright-cli open --browser=chrome --headed https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com

# Fill login form (get element refs from snapshot first)
playwright-cli snapshot
playwright-cli fill "#username" "pghosh@index-analytics.com.bct-qa"
playwright-cli fill "#password" "Tracker_bct0406"
playwright-cli click "#Login"

# Wait for login to complete (8-10 seconds)
# Verify you're logged in
playwright-cli eval "window.location.href"

# Save authenticated state
playwright-cli state-save auth-states/qa/bct-qa.json

playwright-cli close
```

#### Automated Generation (All Roles)

```powershell
cd auth-states
.\generate-auth-states.ps1

# Generate only specific role
.\generate-auth-states.ps1 -OnlyRole BCT -OnlyEnv QA

# Skip existing files
.\generate-auth-states.ps1 -SkipExisting
```

#### Use Auth State in Tests

**Automatic (Recommended)**:

```java
// In @Before hook or step definition:
String role = "BCT";
String env = "QA";
String authStateFile = String.format("auth-states/%s/%s-%s.json", 
    env.toLowerCase(), role.toLowerCase(), env.toLowerCase());

PlaywrightManager.init("test-video-name", authStateFile);

// Now navigate directly to app pages - no login needed!
page().navigate("https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com/lightning/o/Budget__c/list");
```

**Manual Testing**:

```powershell
# Test if auth state is still valid
playwright-cli open --browser=chrome --headed https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com
playwright-cli state-load auth-states/qa/bct-qa.json
playwright-cli goto https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com

# Take snapshot to verify you're logged in
playwright-cli snapshot
# If you see login page, regenerate the auth state

playwright-cli close
```

---

### 4. Debugging Failed Tests

**Scenario**: A Cucumber scenario fails in CI/CD with error: `TimeoutError: Locator not found`

#### Step 1: Reproduce Locally

```powershell
# Start browser with auth state
playwright-cli open --browser=chrome --headed https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com
playwright-cli state-load auth-states/qa/bct-qa.json
playwright-cli goto https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com

# Navigate to the failing page
playwright-cli goto /lightning/r/Budget__c/a0X5f000001abcD/view
```

#### Step 2: Take Snapshot

```powershell
playwright-cli snapshot --filename=budget-detail-debug.yaml
```

#### Step 3: Inspect Element

```powershell
# Check if element exists
playwright-cli eval "document.querySelector('[data-label=\"Stage\"]') !== null"
# Output: true or false

# Get actual label text
playwright-cli eval "el => el.textContent" e25

# Check visibility
playwright-cli eval "el => getComputedStyle(el).display" e25

# Get all attributes
playwright-cli eval "el => ({id: el.id, class: el.className, name: el.name, 'data-label': el.getAttribute('data-label')})" e25
```

#### Step 4: Test Alternative Locators

```powershell
# Try role-based locator
playwright-cli click "getByRole('button', { name: 'Edit' })"

# Try label-based locator
playwright-cli click "getByLabel('Stage')"

# Try CSS fallback
playwright-cli click "button[name='Edit']"
```

#### Step 5: Check Console Errors

```powershell
playwright-cli console
# Shows JavaScript errors that might be blocking elements
```

#### Step 6: Check Network Issues

```powershell
playwright-cli requests
# Shows failed API calls that might cause missing data
```

---

### 5. Trace Analysis

#### View Traces in Allure Reports

When a test fails, traces are automatically attached to the Allure report:

```powershell
# Generate Allure report
mvn allure:serve

# Or view static report
mvn allure:report
cd target/site/allure-maven-plugin
start index.html
```

In the Allure report:
1. Navigate to failed test
2. Look for "Attachments" section
3. Click "Playwright Trace - <scenario name>.zip"
4. Download and open with Playwright Trace Viewer

#### Open Trace in Playwright Viewer

```powershell
# Local trace file
npx playwright show-trace target/traces/Create_new_inquiry_20260513_153022.zip

# Or upload to online viewer
start https://trace.playwright.dev/
# Drag & drop the .zip file
```

#### What Traces Show You

- **Actions**: Every click, fill, navigation with exact timing
- **DOM Snapshots**: Page state before/after each action
- **Screenshots**: Visual state at each step
- **Network**: All API requests/responses (headers, bodies, timing)
- **Console Logs**: JavaScript errors and warnings
- **Source Code**: Which test line triggered each action

#### Manual Trace Recording

```powershell
# Start recording
playwright-cli tracing-start

# Perform actions
playwright-cli click e15
playwright-cli fill e20 "Test"
playwright-cli click e25

# Stop and save trace
playwright-cli tracing-stop
# Trace saved to: .playwright-cli/traces/trace-<timestamp>.zip

# View the trace
npx playwright show-trace .playwright-cli/traces/trace-<timestamp>.zip
```

---

### 6. Element Attribute Inspection

When snapshots don't show the attributes you need:

```powershell
playwright-cli snapshot
# Shows: e15 [button "Save"]

# Get element ID
playwright-cli eval "el => el.id" e15
# Output: "00D5f000001abcD"

# Get all CSS classes
playwright-cli eval "el => el.className" e15
# Output: "slds-button slds-button_brand"

# Get data attributes
playwright-cli eval "el => el.getAttribute('data-test-id')" e15
# Output: "budget-save-button"

# Get ARIA attributes
playwright-cli eval "el => el.getAttribute('aria-label')" e15
# Output: "Save Budget"

# Get name attribute (most stable for Salesforce)
playwright-cli eval "el => el.name" e15
# Output: "SaveEdit"

# Get computed styles
playwright-cli eval "el => getComputedStyle(el).display" e15
# Output: "block"
```

**Use in Java Locators**:

```java
// P3: Use name attribute (most stable)
page.locator("button[name='SaveEdit']").first().click();

// P3: Use data-test-id if available
page.locator("[data-test-id='budget-save-button']").first().click();

// P1: Prefer role + aria-label (most resilient)
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
    .setName("Save Budget").setExact(true)).first().click();
```

---

### 7. Network Mocking (Advanced)

Test edge cases without breaking the actual application:

#### Mock API Errors

```powershell
# Start browser
playwright-cli open --browser=chrome --headed https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com

# Mock Salesforce API to return error
playwright-cli route "**/services/data/v**" --status=500

# Trigger action that calls API
playwright-cli click e25  # Create Budget button

# Verify error toast appears
playwright-cli snapshot
# Should show error message in UI
```

#### Mock Slow Responses

```powershell
# Mock slow API (5 second delay)
playwright-cli route "**/services/data/v**" --delay=5000

# Test loading states
playwright-cli click e25
playwright-cli snapshot  # Should show loading spinner
```

---

## Best Practices

### 1. Use Headed Mode for Development

```powershell
# Always use --headed when developing locators
playwright-cli open --browser=chrome --headed <URL>
```

### 2. Save Snapshots for Documentation

```powershell
# Create snapshots for each major page state
playwright-cli snapshot --filename=docs/snapshots/budget-list-page.yaml
playwright-cli click e15
playwright-cli snapshot --filename=docs/snapshots/budget-create-form.yaml
```

### 3. Verify Locator Stability

Test your locators across:
- Different data states (empty, populated, error)
- Different user roles
- Different environments (QA, UAT, SEI)

```powershell
# Test in QA
playwright-cli open --browser=chrome --headed $env:APP_URL_QA
playwright-cli state-load auth-states/qa/bct-qa.json
playwright-cli goto /lightning/o/Budget__c/list
playwright-cli snapshot
# Verify element refs are consistent

# Test in UAT
playwright-cli close
playwright-cli open --browser=chrome --headed $env:APP_URL_UAT
playwright-cli state-load auth-states/uat/bct-uat.json
playwright-cli goto /lightning/o/Budget__c/list
playwright-cli snapshot
# Compare with QA snapshot
```

### 4. Clean Up Sessions

```powershell
# List all active sessions
playwright-cli list

# Close all browsers
playwright-cli close-all

# Force kill if browsers hang
playwright-cli kill-all
```

### 5. Use Named Sessions for Multi-Tab Work

```powershell
# Open session 1
playwright-cli -s=inquiry open https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com
playwright-cli -s=inquiry goto /lightning/r/Inquiry__c/a0X123/view

# Open session 2 (separate browser)
playwright-cli -s=budget open https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com
playwright-cli -s=budget goto /lightning/r/Budget__c/a0X456/view

# Work with both
playwright-cli -s=inquiry snapshot
playwright-cli -s=budget snapshot

# Close individually
playwright-cli -s=inquiry close
playwright-cli -s=budget close
```

---

## Troubleshooting

### Issue: `playwright-cli: command not found`

**Solution**:
```powershell
npm install -g @playwright/cli@latest
```

### Issue: `File C:\Users\...\playwright-cli.ps1 cannot be loaded`

**Cause**: PowerShell execution policy restriction

**Solution**: Use `.cmd` directly:
```powershell
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd open <URL>
```

### Issue: Elements Not Found After Login

**Cause**: Page hasn't fully loaded or element is in iframe

**Solution**:
```powershell
# Wait for navigation to complete
playwright-cli eval "document.readyState === 'complete'"

# Check for iframes
playwright-cli eval "document.querySelectorAll('iframe').length"

# If in iframe, scope to it first
playwright-cli eval "document.querySelector('iframe[title=\"...\"]').contentDocument"
```

### Issue: Auth State Doesn't Work

**Cause**: Session expired or IP restrictions

**Solution**: Regenerate auth state
```powershell
cd auth-states
.\generate-auth-states.ps1 -OnlyRole BCT -OnlyEnv QA
```

### Issue: Trace File Too Large for Allure

**Cause**: Long-running test with many actions

**Solution**: Traces are still available locally
```powershell
# View trace directly
npx playwright show-trace target/traces/<trace-name>.zip

# Or check Allure report for instructions attachment
```

---

## Integration with Java Framework

### Example: Create New Page Object

1. **Explore the page**:
   ```powershell
   playwright-cli open --browser=chrome --headed <URL>
   playwright-cli state-load auth-states/qa/bct-qa.json
   playwright-cli goto /lightning/o/NewModule__c/list
   playwright-cli snapshot
   ```

2. **Document element refs**:
   ```
   e15: New button
   e20: Title input
   e25: Status dropdown
   e30: Save button
   ```

3. **Generate Java locators**:
   ```powershell
   playwright-cli generate-locator e15
   # page.getByRole(AriaRole.BUTTON, ...)
   ```

4. **Create Page Object**:
   ```java
   public class NewModulePage extends SfPage {
       public void clickNew() {
           page.getByRole(AriaRole.BUTTON, 
               new Page.GetByRoleOptions().setName("New").setExact(true))
               .first().click();
       }
       
       public void fillTitle(String title) {
           formUtils.fillField("Title", title);
       }
   }
   ```

### Example: Debug Flaky Test

1. **Run test with trace enabled** (already automatic in framework)
2. **Test fails** → trace automatically saved to `target/traces/`
3. **Open trace**:
   ```powershell
   npx playwright show-trace target/traces/Flaky_Test_20260513_153022.zip
   ```
4. **Identify issue** in trace viewer (e.g., timing, wrong element clicked)
5. **Reproduce interactively**:
   ```powershell
   playwright-cli open --browser=chrome --headed <URL>
   # Recreate the exact scenario step-by-step
   ```
6. **Fix Java code** based on findings

---

## Quick Reference

```powershell
# Common commands
playwright-cli open --browser=chrome --headed <URL>
playwright-cli state-load auth-states/qa/bct-qa.json
playwright-cli goto <path>
playwright-cli snapshot
playwright-cli click e15
playwright-cli fill e20 "value"
playwright-cli generate-locator e15
playwright-cli eval "el => el.getAttribute('name')" e15
playwright-cli console
playwright-cli requests
playwright-cli tracing-start
playwright-cli tracing-stop
playwright-cli state-save auth-states/qa/role-env.json
playwright-cli close
```

For full command reference, see: [playwright-cli SKILL.md](../.claude/skills/playwright-cli/SKILL.md)
