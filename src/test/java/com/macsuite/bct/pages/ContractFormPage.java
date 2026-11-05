package com.macsuite.bct.pages;

import com.macsuite.common.pages.SfPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class ContractFormPage extends SfPage {

    public ContractFormPage(Page page) {
        super(page);
    }

    public void verifyContractCreated() {
        helper.waitForToast("success");
    }

    public String extractContractNumber() {
        return helper.extractRecordNumber();
    }

    public void verifyErrorMessageAppears() {
        page.getByRole(AriaRole.ALERT).waitFor(new Locator.WaitForOptions().setTimeout(5000));
        logger.info("Error message appeared");
    }

    public String getValidationError() {
        Locator alert = page.getByRole(AriaRole.ALERT);
        alert.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        return alert.textContent().trim();
    }

    public void clickSave() {
        Locator saveButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save").setExact(true)).first();
        saveButton.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        saveButton.click();
        logger.info("Clicked Save button");
    }

    public void verifySuccessMsg() {
        helper.waitForToast("success");
        logger.info("Success message verified");
    }
}
