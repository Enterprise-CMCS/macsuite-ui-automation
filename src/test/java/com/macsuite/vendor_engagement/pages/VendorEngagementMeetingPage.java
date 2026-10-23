package com.macsuite.vendor_engagement.pages;

import com.macsuite.common.pages.SfPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class VendorEngagementMeetingPage extends SfPage {

    public VendorEngagementMeetingPage(Page page) {
        super(page);
    }

    public void verifyMeetingCreated() {
        helper.waitForToast("success");
    }

    /**
     * Verify the saved meeting reflects the values that were entered.
     *
     * <p>Legacy {@code VE_Creation_Page.validateMandatoryFields} asserted the persisted field
     * values. The prior version keyed a non-existent "Meeting Title" field (the form uses
     * "Meeting Name"), so the only check was skipped and the method unconditionally returned
     * {@code true} — a false-green. This now asserts the entered values actually persisted and
     * throws if any are missing. Dynamic values containing {@code ${...}} templates are skipped
     * because {@code getFilledData()} stores the unresolved template (a re-resolved
     * {@code ${timestamp}} would not match the value rendered at fill time).
     */
    public boolean verifyMeetingDetails() {
        page.getByRole(AriaRole.HEADING).first()
            .waitFor(new Locator.WaitForOptions().setTimeout(10000));

        java.util.Map<String, String> filled = getFilledData();
        String[] fieldsToVerify = {"Subject", "Vendor Name", "Status"};
        for (String field : fieldsToVerify) {
            String value = filled.get(field);
            if (value == null || value.isBlank() || value.contains("${")) continue;
            try {
                page.getByText(value, new Page.GetByTextOptions().setExact(false))
                    .first().waitFor(new Locator.WaitForOptions().setTimeout(8000));
            } catch (Exception e) {
                throw new AssertionError(
                    "Saved meeting is missing expected '" + field + "' = '" + value + "'");
            }
        }
        logger.info("Meeting details verified against entered values");
        return true;
    }

    // PREDICTED (not runtime-verified): edit-after-create coverage (legacy HomePage.updateDetails),
    // a new isolated scenario that cannot affect the existing create flow.
    public void openFirstVendorEngagementRecord() {
        Locator firstRow = page.locator("table tbody tr a, [role='grid'] tbody tr a").first();
        firstRow.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        firstRow.click();
        logger.info("Opened first Vendor Engagement record");
    }

    public void editMeetingFields(java.util.Map<String, String> updates) {
        clickEditIfVisible();
        updates.forEach(this::fillField);
        logger.info("Edited Vendor Engagement meeting fields: {}", updates.keySet());
    }
}
