package com.macsuite.bct.pages;

import java.util.regex.Pattern;

import com.macsuite.common.pages.SfPage;
import com.macsuite.core.PlaywrightManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class ContractListPage extends SfPage {

    public ContractListPage(Page page) {
        super(page);
    }

    public void cloneListView(String newListViewName, String visibility) {
        openListViewControlsMenu();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions()
                .setName(Pattern.compile("^Clone$", Pattern.CASE_INSENSITIVE))).first().click();

        fillListViewForm(newListViewName, visibility);
        helper.clickButton("Save");
        logger.info("List view cloned: {}", newListViewName);
    }

    public void createNewListView(String listViewName) {
        openListViewControlsMenu();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions()
                .setName(Pattern.compile("^New$", Pattern.CASE_INSENSITIVE))).first().click();

        fillListViewForm(listViewName, null);
        helper.clickButton("Save");
        logger.info("List view created: {}", listViewName);
    }

    public void createOrCloneListView(String action, String listViewName) {
        createOrCloneListView(action, listViewName, "All");
    }

    public String newListViewName = "";
    public String clonedListViewName = "";

    public void createOrCloneListView(String action, String listViewName, String baseViewName) {
        // Switch to the base view if action is "Clone" to enable the Clone button
        // if ("Clone".equalsIgnoreCase(action)) {
        // selectListView(baseViewName);
        // // Wait for the list view to finish loading before accessing its controls
        // menu
        // try {
        // page.waitForLoadState(LoadState.NETWORKIDLE,
        // new Page.WaitForLoadStateOptions().setTimeout(3000));
        // } catch (Exception ignored) {}
        // }
        if ("Clone".equalsIgnoreCase(action)) {

            clonedListViewName = listViewName;
        } else {
            newListViewName = listViewName;
        }

        selectListView(baseViewName);

        openListViewControlsMenu();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions()
                .setName(Pattern.compile("^" + action + "$", Pattern.CASE_INSENSITIVE))).first().click();

        fillListViewForm(listViewName, null);
        helper.clickButton("Save");
        logger.info("List view {}: {}", action.toLowerCase() + "d", listViewName);
    }

    public void searchForListView(String listViewName) {
        selectListView("All");
        Locator searchInput = page.locator("//input[@aria-label='Search this list...']").first();
        searchInput.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        searchInput.fill(listViewName);
        searchInput.press("Enter");
        logger.info("Searched for list view: {}", listViewName);

    }

    public void cloneCurrentListView() {
        openListViewControlsMenu();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Clone")).first().click();
        helper.clickButton("Save");
        logger.info("Current list view cloned");
    }

    public void deleteCurrentListView() {
        openListViewControlsMenu();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Delete")).first().click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete").setExact(true))
                .first().click();
        logger.info("List view deleted");
    }

    public void selectListView(String listViewName) {
        Locator listViewButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName(Pattern.compile("Select a List View", Pattern.CASE_INSENSITIVE))).first();
        highlightElement(listViewButton);
        listViewButton.click();

        // Wait for dropdown to appear and select the list view
        Locator listViewOption = page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions()
                .setName(listViewName));

        if (listViewOption.count() == 0) {
            // Try with LISTITEM role (alternative in some Lightning versions)
            listViewOption = page.getByRole(AriaRole.LISTITEM).filter(new Locator.FilterOptions()
                    .setHasText(listViewName));
        }

        listViewOption.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        highlightElement(listViewOption.first());
        listViewOption.first().click();
        logger.info("Selected list view: {}", listViewName);
    }

    public void searchNewListView() {
        selectListView(newListViewName);
    }

    public void searchClonedListView() {
        selectListView(clonedListViewName);
    }

    public void verifyListViewCloned(String listViewName) {
        // Trim last 2 digits (seconds) from timestamp to handle time drift
        // E.g., "Test New List 060426112827" -> "Test New List 0604261128"
        String searchPattern = listViewName;
        if (listViewName.matches(".*\\d{12}$")) {
            searchPattern = listViewName.substring(0, listViewName.length() - 2);
            logger.debug("Trimmed timestamp for search: {} -> {}", listViewName, searchPattern);
        }

        page.locator("span.slds-page-header__title")
                .filter(new Locator.FilterOptions().setHasText(searchPattern))
                .first()
                .waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("List view verified: {}", listViewName);
    }

    public void verifyClonedListViewActive() {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                .setName(Pattern.compile("Copy of", Pattern.CASE_INSENSITIVE)))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("Cloned list view is active");
    }

    private void openListViewControlsMenu() {
        Locator menuButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName(Pattern.compile("List View Controls", Pattern.CASE_INSENSITIVE))).first();
        menuButton.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        // Check if menu items are already visible
        try {
            page.getByRole(AriaRole.MENUITEM).first().waitFor(new Locator.WaitForOptions().setTimeout(500));
        } catch (Exception e) {
            // Menu not open, click to open it
            menuButton.click();
            page.getByRole(AriaRole.MENUITEM).first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
        }
    }

    private void fillListViewForm(String name, String visibility) {
        Locator nameInput = page.locator("input[name='title']").first();
        nameInput.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        nameInput.fill(name);

        if (visibility != null && !visibility.isBlank()) {
            String label = switch (visibility.toUpperCase()) {
                case "PRIVATE", "ONLY I CAN SEE" -> "Only I can see this list view";
                case "GLOBAL", "ALL USERS" -> "All users can see this list view";
                case "SHARED", "SHARE WITH GROUPS" -> "Share list view with groups of users";
                default -> "Only I can see this list view";
            };
            page.getByLabel(label).first().check();
        }
    }

    public void verifyNewButtonNotVisible() {
        Locator newButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName(Pattern.compile("New", Pattern.CASE_INSENSITIVE))).first();
        try {
            newButton.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            throw new AssertionError("New button is visible");
        } catch (Exception e) {
            logger.info("New button is not visible as expected");
        }
        screenshot("NEW button not displayed");
    }

    public void validatePrintableViewScreen() {
        Locator printableViewButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
            .setName("Printable View").setExact(true)).first();
        printableViewButton.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE).setTimeout(10000));

        page = page.waitForPopup(printableViewButton::click);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED,
            new Page.WaitForLoadStateOptions().setTimeout(20000));
        PlaywrightManager.setActivePage(page);

        Locator printableView = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                .setName(Pattern.compile("All Records - Printable View", Pattern.CASE_INSENSITIVE))).first();
        printableView.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        logger.info("Printable view screen validated");
        screenshot("Printable View Screen");
    }
}
