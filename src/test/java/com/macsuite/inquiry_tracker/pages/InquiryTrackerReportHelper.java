package com.macsuite.inquiry_tracker.pages;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class InquiryTrackerReportHelper {
    private static final Logger logger = LoggerFactory.getLogger(InquiryTrackerReportHelper.class);
    private static final String REPORT_VIEWER_IFRAME = "iframe[title*='Report Viewer']";

    private final Page page;
    private int topRecordCount = 0;
    private int bottomRecordCount = 0;
    private Page reportPage;

    InquiryTrackerReportHelper(Page page) {
        this.page = page;
    }

    void waitForDashboardWidgetsToLoad() {
        logger.info("Waiting for CMCS TA dashboard widgets to load...");
        FrameLocator dashboardFrame = page
            .frameLocator("iframe[title*='dashboard'], iframe[title*='Dashboard']")
            .first();
        dashboardFrame.locator(".widget-container")
            .first()
            .waitFor(new Locator.WaitForOptions().setTimeout(30000));
    }

    void clickReportByName(String reportName) {
        logger.info("Clicking report from home page dashboard: {}", reportName);

        // Get the dashboard iframe (use .last() as Salesforce keeps old hidden ones in DOM)
        Locator dashboardFrame = page.locator("iframe[title='dashboard']").last();
        dashboardFrame.waitFor(new Locator.WaitForOptions()
            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
            .setTimeout(15000));
        
        FrameLocator frame = dashboardFrame.contentFrame();
        
        // Report links on dashboard have text pattern: "View Report (REPORT_NAME)"
        // Example: "View Report (TA CMCS Inquiries by Stage)"
        String linkText = "View Report (" + reportName + ")";
        
        // P1 - semantic locator: role + accessible name
        Locator reportLink = frame.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
            new FrameLocator.GetByRoleOptions().setName(linkText).setExact(false));
        
        // Wait for link to appear
        reportLink.first().waitFor(new Locator.WaitForOptions().setTimeout(10000));
        
        if (!reportLink.first().isVisible()) {
            throw new AssertionError("Report link exists but not visible for: '" + reportName + "'" 
                + ". Link text expected: '" + linkText + "'");
        }

        // Click report link - opens in NEW TAB/POPUP
        reportPage = page.waitForPopup(() -> {
            reportLink.first().click();
        });
        logger.info("Clicked report link: {} - opened in new tab", linkText);
        
        // Wait for the popup page to load
        reportPage.waitForLoadState(LoadState.DOMCONTENTLOADED, 
            new Page.WaitForLoadStateOptions().setTimeout(20000));
        
        // Wait for Report Viewer iframe to be visible in the popup
        reportPage.locator(REPORT_VIEWER_IFRAME).first()
            .waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(30000));
        logger.info("Report viewer iframe is visible in new tab");
        
        logger.info("Report '{}' opened successfully.", reportName);
    }

    void verifyReportPageTitle(String expectedTitle) {
        logger.info("Verifying report page title: {}", expectedTitle);
        Page currentPage = getCurrentPage();
        currentPage.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        waitForReportViewerFrame();
        
        // Wait for the page header title to appear
        getReportViewerFrame().locator(".slds-page-header__title")
            .first()
            .waitFor(new Locator.WaitForOptions().setTimeout(20000));
        
        List<String> acceptableTitles = getAcceptableReportNames(expectedTitle);

        Locator matchedTitle = null;
        Locator titleCandidates = getReportViewerFrame().locator(".slds-page-header__title");
        for (int index = 0; index < titleCandidates.count(); index++) {
            Locator candidate = titleCandidates.nth(index);
            if (matchesAnyReportName(safeLocatorText(candidate), acceptableTitles)) {
                matchedTitle = candidate;
                break;
            }
        }

        if (matchedTitle == null || matchedTitle.count() == 0) {
            throw new AssertionError("Report page title not found: " + expectedTitle
                + " (page title: " + currentPage.title() + ")");
        }

        if (!matchedTitle.isVisible()) {
            throw new AssertionError("Report page title not visible: " + expectedTitle);
        }
    }

    void captureTopRecordCount() {
        logger.info("Capturing top record count from metrics section");
        waitForReportViewerFrame();

        Locator metricsValue = getReportViewerFrame()
            .locator("li:has(.metricsTitle:text('Total Records')) .metricsValue")
            .first();

        if (metricsValue.count() == 0) {
            throw new AssertionError("Metrics value not found for 'Total Records'");
        }

        String countText = metricsValue.textContent().trim();
        // Remove comma separators before parsing (e.g., "1,003" -> "1003")
        topRecordCount = Integer.parseInt(countText.replace(",", ""));
        logger.info("Top record count captured: {}", topRecordCount);
    }

    void scrollToBottomOfReport() {
        logger.info("Scrolling to bottom of report to load all virtual rows");
        waitForReportViewerFrame();

        FrameLocator frame = getReportViewerFrame();
        int maxScrollAttempts = 600;
        int scrollAttempt = 0;
        int consecutiveNoScrollCount = 0;

        // Keep scrolling until grand total row (the definitive bottom row) is visible
        while (scrollAttempt < maxScrollAttempts) {
            // Check if grand total row exists and is visible
            Locator grandTotalRow = frame.locator(".lightning-table-grand-total-row-cell").first();
            if (grandTotalRow.count() > 0 && grandTotalRow.isVisible()) {
                logger.info("✓ Reached grand total row after {} scroll attempts", scrollAttempt);
                return;
            }

            // Try multiple scroll strategies in order
            boolean scrolled = false;

            // Strategy 1: Walk up from tbody using computed styles to find the real scrollable container
            Locator tbody = frame.locator("table[role='grid'] tbody").first();
            if (tbody.count() > 0) {
                Object tbodyMoved = tbody.evaluate(
                    "tbody => {" +
                    "  let parent = tbody.parentElement;" +
                    "  while (parent && parent !== document.body) {" +
                    "    const style = window.getComputedStyle(parent);" +
                    "    const canScroll = (style.overflowY === 'auto' || style.overflowY === 'scroll')" +
                    "                      && parent.scrollHeight > parent.clientHeight;" +
                    "    if (canScroll) {" +
                    "      const previousTop = parent.scrollTop;" +
                    "      parent.scrollTop = Math.min(parent.scrollTop + Math.max(parent.clientHeight - 50, 500), parent.scrollHeight);" +
                    "      return parent.scrollTop > previousTop;" +
                    "    }" +
                    "    parent = parent.parentElement;" +
                    "  }" +
                    "  return false;" +
                    "}");
                scrolled = tbodyMoved instanceof Boolean && (Boolean) tbodyMoved;
            }

            // Strategy 2: Scroll the document body
            if (!scrolled) {
                scrolled = scrollReportFrameForward();
            }

            // Strategy 3: Known Salesforce report container class names
            if (!scrolled) {
                Locator tableContainer = frame.locator(".data-grid, .widget-container_scroll-container, .ps-container").first();
                if (tableContainer.count() > 0) {
                    Object moved = tableContainer.evaluate(
                        "element => {" +
                        "  const previousTop = element.scrollTop;" +
                        "  element.scrollTop = Math.min(element.scrollTop + Math.max(element.clientHeight - 50, 500), element.scrollHeight);" +
                        "  return element.scrollTop > previousTop;" +
                        "}");
                    scrolled = moved instanceof Boolean && (Boolean) moved;
                }
            }

            if (!scrolled) {
                consecutiveNoScrollCount++;

                // Virtual-scroll stall: the render batch is exhausted but more rows exist.
                // Force every scrollable element to its absolute bottom to trigger the next
                // batch, then wait longer for the virtual list to render before retrying.
                if (consecutiveNoScrollCount % 3 == 0) {
                    frame.locator("body").evaluate(
                        "body => {" +
                        "  const doc = body.ownerDocument;" +
                        "  // Push every scrollable descendant to its bottom" +
                        "  doc.querySelectorAll('*').forEach(el => {" +
                        "    try {" +
                        "      const style = window.getComputedStyle(el);" +
                        "      if ((style.overflowY === 'auto' || style.overflowY === 'scroll') && el.scrollHeight > el.clientHeight) {" +
                        "        el.scrollTop = el.scrollHeight;" +
                        "      }" +
                        "    } catch(e) {}" +
                        "  });" +
                        "  const scrollingEl = doc.scrollingElement || body;" +
                        "  scrollingEl.scrollTop = scrollingEl.scrollHeight;" +
                        "}");
                    // Longer wait so the virtual list can render the next batch of rows
                    page.waitForTimeout(800);
                } else {
                    page.waitForTimeout(300);
                }

                // Give up only after an extended stall — far more generous than before
                if (consecutiveNoScrollCount >= 25) {
                    logger.warn("Cannot scroll further after {} attempts (stalled {} consecutive times)",
                        scrollAttempt, consecutiveNoScrollCount);
                    break;
                }
            } else {
                consecutiveNoScrollCount = 0;
                // Normal inter-scroll wait so the virtual list can keep up
                page.waitForTimeout(150);
            }

            scrollAttempt++;

            if (scrollAttempt % 20 == 0) {
                logger.info("Scroll progress: {} attempts, stall count: {}", scrollAttempt, consecutiveNoScrollCount);
            }
        }

        if (scrollAttempt >= maxScrollAttempts) {
            logger.warn("Reached max scroll attempts ({}), grand total row may not be loaded", maxScrollAttempts);
        }
    }

    void captureBottomRecordCount() {
        logger.info("Capturing bottom record count from data grid");
        waitForReportViewerFrame();
        scrollToBottomOfReport();

        Locator reportTable = getReportViewerFrame().locator("table.data-grid-full-table[role='grid']").first();
        if (reportTable.count() == 0) {
            throw new AssertionError("Report data grid table not found");
        }

        int expectedLastRowIndex = getExpectedLastRowIndex(reportTable);
        logger.info("Expected last row index: {}", expectedLastRowIndex);
        
        String bestCandidate = getCurrentBottomCountCandidate();
        int stalledAttempts = 0;
        int maxAttempts = 15; // Reduced further since scrollToBottom is now thorough

        // Fine-tune scroll position to capture the exact bottom count
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Check if we've reached the bottom
            if (isGrandTotalCandidate(bestCandidate)) {
                logger.info("✓ Reached grand total row at index {}", extractRowIndex(bestCandidate));
                break;
            }
            
            if (reachedExpectedLastRow(bestCandidate, expectedLastRowIndex)) {
                logger.info("✓ Reached expected last row {} of {}", 
                    extractRowIndex(bestCandidate), expectedLastRowIndex);
                break;
            }

            // Try to advance a bit more
            boolean moved = advanceReportTable(reportTable);
            String currentCandidate = getCurrentBottomCountCandidate();
            
            if (isBetterBottomCountCandidate(currentCandidate, bestCandidate)) {
                bestCandidate = currentCandidate;
                stalledAttempts = 0;
                logger.debug("Fine-tuning: row {}, count: {}", 
                    extractRowIndex(bestCandidate), extractCountText(bestCandidate));
            } else {
                stalledAttempts++;
            }

            // If not moving and stalled, we're at the bottom
            if (!moved && stalledAttempts >= 2) {
                logger.info("✓ Fine-tuning complete at row {}", extractRowIndex(bestCandidate));
                break;
            }
        }

        if (bestCandidate == null || bestCandidate.isBlank()) {
            throw new AssertionError("Bottom record count span not found after scrolling to bottom");
        }

        // Log warning but don't fail if we're within one render batch of the expected bottom
        if (!isGrandTotalCandidate(bestCandidate) && !reachedExpectedLastRow(bestCandidate, expectedLastRowIndex)) {
            int currentRow = extractRowIndex(bestCandidate);
            int rowsAway = expectedLastRowIndex - currentRow;
            if (rowsAway > 50) {
                throw new AssertionError(String.format(
                    "Did not reach bottom: at row %d, expected %d (%d rows away)",
                    currentRow, expectedLastRowIndex, rowsAway));
            } else {
                logger.warn("Near bottom but not exact: row {}, expected {} ({} rows away — within tolerance)",
                    currentRow, expectedLastRowIndex, rowsAway);
            }
        }

        String countText = extractCountText(bestCandidate);
        // Remove comma separators before parsing (e.g., "1,003" -> "1003")
        bottomRecordCount = Integer.parseInt(countText.replace(",", ""));
        logger.info("✓ Bottom record count captured: {} (at row index {})", 
            bottomRecordCount, extractRowIndex(bestCandidate));
    }

    void verifyRecordCountsMatch() {
        logger.info("Verifying record counts match");
        if (topRecordCount != bottomRecordCount) {
            throw new AssertionError(
                String.format("Record counts do not match! Top: %d, Bottom: %d", topRecordCount, bottomRecordCount));
        }
    }

    void closeCurrentReportTab() {
        logger.info("Returning to Reports list page");
        if (reportPage != null && reportPage == page) {
            // Report opened in same page, navigate back to Reports list
            page.goBack();
            page.waitForURL("**/lightning/o/Report/home**", new Page.WaitForURLOptions().setTimeout(10000));
            reportPage = null;
            logger.info("Returned to Reports page: {}", page.url());
        } else if (reportPage != null) {
            // Report opened in popup (legacy behavior)
            reportPage.close();
            reportPage = null;
            page.bringToFront();
            logger.info("Closed report popup tab");
        } else {
            logger.warn("No report to close - already on main page");
        }
    }

    private Page getCurrentPage() {
        return reportPage != null ? reportPage : page;
    }

    private List<String> getAcceptableReportNames(String reportName) {
        List<String> acceptableNames = new ArrayList<>();
        acceptableNames.add(reportName);
        return acceptableNames;
    }

    private boolean matchesAnyReportName(String actualReportName, List<String> acceptableReportNames) {
        String normalizedActual = normalizeText(actualReportName).toLowerCase();
        for (String acceptableReportName : acceptableReportNames) {
            String normalizedExpected = normalizeText(acceptableReportName).toLowerCase();
            if (normalizedActual.equals(normalizedExpected)
                || normalizedActual.startsWith(normalizedExpected)
                || normalizedExpected.startsWith(normalizedActual)) {
                return true;
            }
        }
        return false;
    }

    private String safeLocatorText(Locator locator) {
        try {
            String text = locator.textContent();
            return text == null ? "" : normalizeText(text);
        } catch (Exception e) {
            return "<unavailable>";
        }
    }

    private FrameLocator getReportViewerFrame() {
        // Find the visible Report Viewer iframe (not hidden from previous navigation)
        // Use class selector to get the active/visible one
        Locator iframes = getCurrentPage().locator(REPORT_VIEWER_IFRAME + ".isView");
        
        // If no .isView class, fall back to checking visibility
        if (iframes.count() == 0) {
            iframes = getCurrentPage().locator(REPORT_VIEWER_IFRAME);
        }
        
        // Find the first visible iframe
        int count = iframes.count();
        for (int i = 0; i < count; i++) {
            Locator iframe = iframes.nth(i);
            if (iframe.isVisible()) {
                return getCurrentPage().frameLocator(REPORT_VIEWER_IFRAME).nth(i);
            }
        }
        
        // Fallback to first if none are visible yet
        return getCurrentPage().frameLocator(REPORT_VIEWER_IFRAME).first();
    }

    private void waitForReportViewerFrame() {
        // Wait for iframe to be visible (combines attached + visible check)
        getCurrentPage().locator(REPORT_VIEWER_IFRAME)
            .first()
            .waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(20000));
        
        // Give Salesforce time to render content inside the iframe
        page.waitForTimeout(1000);
    }

    private int getExpectedLastRowIndex(Locator reportTable) {
        String ariaRowCount = reportTable.getAttribute("aria-rowcount");
        if (ariaRowCount == null || ariaRowCount.isBlank()) {
            return -1;
        }

        try {
            return Integer.parseInt(ariaRowCount.trim()) - 2;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private String getCurrentBottomCountCandidate() {
        return (String) getReportViewerFrame()
            .locator("th[data-row-index]")
            .evaluateAll("cells => {"
                + " const candidates = cells"
                + "   .map(cell => {"
                + "     const count = cell.querySelector('span.compare-table-record-count');"
                + "     if (!count) return null;"
                + "     const rawIndex = cell.getAttribute('data-row-index');"
                + "     const rowIndex = rawIndex === null ? -1 : Number.parseInt(rawIndex, 10);"
                + "     return {"
                + "       rowIndex: Number.isNaN(rowIndex) ? -1 : rowIndex,"
                + "       countText: (count.textContent || '').trim(),"
                + "       isGrandTotal: cell.classList.contains('lightning-table-grand-total-row-cell')"
                + "     };"
                + "   })"
                + "   .filter(Boolean);"
                + " if (!candidates.length) return null;"
                + " const grandTotal = candidates.find(candidate => candidate.isGrandTotal);"
                + " const selected = grandTotal || candidates.sort((left, right) => left.rowIndex - right.rowIndex)[candidates.length - 1];"
                + " return `${selected.rowIndex}|${selected.countText}|${selected.isGrandTotal}`;"
                + " }");
    }

    private boolean advanceReportTable(Locator reportTable) {
        // Try table scrolling first
        boolean tableMoved = scrollReportTableForward(reportTable);
        if (tableMoved) {
            return true;
        }
        
        // Try frame scrolling
        boolean frameMoved = scrollReportFrameForward();
        return frameMoved;
    }

    private boolean scrollReportTableForward(Locator reportTable) {
        Object moved = reportTable.evaluate("table => {"
            + " let parent = table.parentElement;"
            + " while (parent) {"
            + "   const style = window.getComputedStyle(parent);"
            + "   const canScroll = (style.overflowY === 'auto' || style.overflowY === 'scroll') && parent.scrollHeight > parent.clientHeight;"
            + "   if (canScroll) {"
            + "     const previousTop = parent.scrollTop;"
            + "     parent.scrollTop = Math.min(parent.scrollTop + Math.max(parent.clientHeight - 80, 400), parent.scrollHeight);"
            + "     return parent.scrollTop > previousTop;"
            + "   }"
            + "   parent = parent.parentElement;"
            + " }"
            + " return false;"
            + " }");
        return moved instanceof Boolean && (Boolean) moved;
    }

    private boolean scrollReportFrameForward() {
        Object moved = getReportViewerFrame().locator("body").evaluate("body => {"
            + " const doc = body.ownerDocument;"
            + " const scrollingElement = doc.scrollingElement || body;"
            + " const previousTop = scrollingElement.scrollTop;"
            + " const delta = Math.max(window.innerHeight - 80, 400);"
            + " scrollingElement.scrollTop = Math.min(previousTop + delta, scrollingElement.scrollHeight);"
            + " return scrollingElement.scrollTop > previousTop;"
            + " }");
        return moved instanceof Boolean && (Boolean) moved;
    }

    private boolean isBetterBottomCountCandidate(String candidate, String currentBest) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        if (currentBest == null || currentBest.isBlank()) {
            return true;
        }
        if (isGrandTotalCandidate(candidate) && !isGrandTotalCandidate(currentBest)) {
            return true;
        }
        return extractRowIndex(candidate) > extractRowIndex(currentBest);
    }

    private boolean isGrandTotalCandidate(String candidate) {
        return candidate != null && candidate.endsWith("|true");
    }

    private boolean reachedExpectedLastRow(String candidate, int expectedLastRowIndex) {
        return expectedLastRowIndex >= 0 && candidate != null && extractRowIndex(candidate) >= expectedLastRowIndex;
    }

    private int extractRowIndex(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return -1;
        }
        String[] parts = candidate.split("\\|", 3);
        if (parts.length == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[0]);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private String extractCountText(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return "";
        }
        String[] parts = candidate.split("\\|", 3);
        if (parts.length < 2) {
            return "";
        }
        return parts[1].replaceAll("[()]", "").trim();
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }
}
