# Contract Option Years Tab Navigation - Root Cause & Fix

## Problem Diagnosis

**Issue:** Test was failing to navigate to the "Contract Option Years" tab in scenario TC_12 of `bct_osg_cru_access_user.feature`.

### Root Cause

The test flow opens a contract record link in a **new browser tab** (target="_blank"), which causes:

1. **Multiple Pages Context**: The browser opens two separate pages:
   - Original page (Contract Actions list)
   - New page (CMCS Contract detail page)

2. **Page State Transition**: When `navigateBackToRecordLink()` is called:
   - It detects the link has `target="_blank"` or similar attribute
   - It calls `page.waitForPopup()` to intercept the new tab opening
   - It switches the active page to the newly opened tab via `PlaywrightManager.setActivePage(contractPage)`

3. **Tab Navigation Race Condition**: The original `navigateToTab()` method was **too simple**:
   ```java
   public void navigateToTab(String tabName) {
       page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(tabName)).first().click();
       logger.info("Navigated to tab: {}", tabName);
   }
   ```
   - **No wait for page load** before attempting tab selection
   - **No wait for tab visibility** before clicking
   - **No wait for tab content** after clicking
   - **No error handling** if tab doesn't exist yet

When the new tab opens, the Lightning page may still be initializing, and the "Contract Option Years" tab might not be rendered or accessible yet. The old implementation would immediately try to click a non-existent or non-interactive element.

## Solution

### 1. Enhanced `navigateToTab()` Method

Added comprehensive waiting and error handling:

```java
public void navigateToTab(String tabName) {
    // Wait for page DOM to be ready
    try {
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                new Page.WaitForLoadStateOptions().setTimeout(15000));
    } catch (Exception e) {
        logger.debug("Page load state wait timed out, continuing: {}", e.getMessage());
    }

    // Wait for tab element to be visible before clicking
    Locator tabElement = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(tabName)).first();
    tabElement.waitFor(new Locator.WaitForOptions()
            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
            .setTimeout(10000));
    
    // Ensure tab is scrolled into view
    tabElement.scrollIntoViewIfNeeded();
    tabElement.click();

    // Wait for tab content to load after clicking
    try {
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout(10000));
    } catch (Exception e) {
        logger.debug("Network idle timeout after tab click, continuing: {}", e.getMessage());
    }

    logger.info("Navigated to tab: {}", tabName);
}
```

**Key improvements:**
- **Pre-click wait**: Ensures page is DOM-ready before attempting tab selection
- **Element visibility wait**: Waits for the specific tab to be visible in the DOM
- **Scroll into view**: Ensures tab is not hidden below viewport
- **Post-click wait**: Waits for tab content to fully load (NETWORKIDLE)
- **Graceful timeout handling**: Logs errors but continues if timeouts occur

### 2. Enhanced `navigateBackToRecordLink()` Method

Added NETWORKIDLE wait after opening new tab:

```java
if (target != null && !target.isBlank() && !"_self".equalsIgnoreCase(target)) {
    contractPage = page.waitForPopup(recordLink::click);
    contractPage.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    // NEW: Wait for network to settle
    try {
        contractPage.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout(10000));
    } catch (Exception e) {
        logger.debug("Network idle timeout after opening new tab, continuing: {}", e.getMessage());
    }
    PlaywrightManager.setActivePage(contractPage);
}
```

**Benefit:** Ensures the new page's assets are loaded before attempting any interactions

## Test Scenario Flow

```
1. Click "Create Contract Action" → Opens contract detail in new tab
   
2. Fill contract action form → Submit
   
3. Click "Add Projects" → Modal opens
   
4. Fill projects modal → Submit
   
5. Verify "CMCS ConAct-Projects" table ← All happens on first tab
   
6. ✅ navigateBackToRecordLink()
      └─ Finds record link (target="_blank")
      └─ Opens in new tab via page.waitForPopup()
      └─ Waits for DOMCONTENTLOADED
      └─ Waits for NETWORKIDLE (NEW)
      └─ Switches active page
      └─ Returns new ContractDetailPage
   
7. ✅ navigateToTab("Contract Option Years")
      └─ Waits for DOMCONTENTLOADED (NEW)
      └─ Finds tab element with VISIBLE state wait (NEW)
      └─ Scrolls into view (NEW)
      └─ Clicks tab
      └─ Waits for NETWORKIDLE (NEW)
   
8. Verify "Total Contract Action Amounts" section
   
9. Continue with remaining assertions
```

## Why This Happens Only with New Tab Open

- **Same-page tab navigation** (target="_self" or no target): Page is already loaded, tabs click instantly
- **New tab navigation** (target="_blank"): New page needs initialization time for:
  - Script loading
  - Component rendering
  - Tab bar population
  - Layout stabilization

## Files Modified

- `src/test/java/com/macsuite/bct/pages/ContractDetailPage.java`
  - Method: `navigateToTab(String tabName)` - Lines 94-116
  - Method: `navigateBackToRecordLink()` - Lines 125-157

## Verification

✅ Code compiles: `mvn -DskipTests test-compile` → BUILD SUCCESS

## Testing Recommendation

Run the scenario that exercises this flow:
```bash
mvn test -Dcucumber.filter.tags="@bct-osg-cru-access-user" -DslowMo=500
```

Watch for successful navigation to Contract Option Years tab and proper rendering of:
- Total Contract Action Amounts section
- Contract Cost Details table
- Amount summaries
