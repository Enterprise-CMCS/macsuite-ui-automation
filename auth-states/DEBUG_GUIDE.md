# CMCS TA Reports Test - Debugging Guide

## Quick Debug Status

The debugging script has saved snapshots to `auth-states/debug/`:
- `home.yaml` - Home page structure
- `app-launcher.yaml` - App launcher modal
- `search-results.yaml` - CMCS Inquiry Tracker search results
- `app-loaded.yaml` - CMCS Inquiry Tracker app loaded
- `navigation.yaml` - Navigation structure

## Next Steps

### 1. Find the Reports Navigation Element

Open `auth-states/debug/navigation.yaml` and search for:
- "Reports"
- "CMCS TA Reports"
- Tab or link element

Example from snapshot:
```yaml
- tab "Reports" [ref=e42] [selected=false]
```

### 2. Click on Reports Section

```powershell
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd click e42
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd snapshot --filename=debug/reports-section.yaml
```

### 3. Find the Report Link

Open `auth-states/debug/reports-section.yaml` and search for:
- "TA CMCS Highest Level Of Review"
- "Highest Level"

Example from snapshot:
```yaml
- link "TA CMCS Highest Level Of Review" [ref=e78]
```

### 4. Generate Java Locator

```powershell
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd generate-locator e78
```

Output will be something like:
```javascript
getByRole('link', { name: 'TA CMCS Highest Level Of Review' })
```

### 5. Convert to Java

Based on the generated locator:

```java
// In your page object (e.g., CMCSInquiryTrackerPage.java)
public void clickReportLink(String reportName) {
    page.getByRole(AriaRole.LINK, 
        new Page.GetByRoleOptions().setName(reportName).setExact(true))
        .first().click();
}
```

### 6. Check for iframes

If the report is in an iframe:

```powershell
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd eval "document.querySelectorAll('iframe').length"
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd eval "[...document.querySelectorAll('iframe')].map(f => f.title)"
```

If iframe exists:
```java
// Scope to iframe first
Locator iframe = page.locator("iframe[title='report']").first();
FrameLocator frame = iframe.contentFrame();
frame.getByText("record count text").first();
```

### 7. Verify Record Count Elements

```powershell
# Take snapshot of the report page
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd snapshot --filename=debug/report-page.yaml

# Search for record count text
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd eval "document.body.textContent" --raw | Select-String -Pattern "record|count|total"
```

### 8. Check Console Errors

```powershell
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd console error
```

### 9. Check Failed Network Requests

```powershell
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd requests | Select-String -Pattern "failed|404|500"
```

## Common Issues & Solutions

### Issue 1: "Reports" tab not found
**Solution**: Check if reports are under a different name or nested menu
```powershell
# Search for all navigation items
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd snapshot | Select-String -Pattern "tab|navigation|menu"
```

### Issue 2: Report link not clickable
**Solution**: Wait for element to be ready
```java
// Add wait before click
Locator reportLink = page.getByRole(AriaRole.LINK, 
    new Page.GetByRoleOptions().setName(reportName));
reportLink.waitFor(new Locator.WaitForOptions().setTimeout(10000));
reportLink.first().click();
```

### Issue 3: Record counts don't match
**Solution**: Check if counts are in different formats or locations
```powershell
# Inspect the entire page text
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd eval "document.body.innerText" --raw > debug/page-text.txt
```

### Issue 4: New tab opens for report
**Solution**: Handle tab switching
```java
// Wait for new tab
Page newTab = context.waitForPage(() -> {
    reportLink.click();
});
newTab.waitForLoadState();
// Validate in new tab
// Close and return to original
newTab.close();
```

## Update Step Definition

Once you have the correct locators, update your step definition:

```java
@When("I validate all CMCS TA reports with matching record counts:")
public void validateAllReports(DataTable dataTable) {
    List<String> reportNames = dataTable.asList(String.class);
    
    for (String reportName : reportNames) {
        logger.info("Validating report: {}", reportName);
        
        // Navigate to reports section (update with correct locator)
        getModulePage().clickReportsTab();
        
        // Click report link
        getModulePage().clickReportLink(reportName);
        
        // Validate counts match
        getModulePage().validateReportCounts(reportName);
        
        // Go back or close tab
        getModulePage().returnToReportsList();
    }
}
```

## Test the Fix

```powershell
# Run the specific scenario
mvn test -Dcucumber.filter.tags="@cmcs-ta-reports-validation"
```

## Clean Up

```powershell
# Close browser when done
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd close

# Clean up debug files (optional)
Remove-Item auth-states\debug\* -Force
```
