@MFP_POH_ListView_Report_Dashboards @smoke @regression
Feature: MFP POH Tracker

  @MFP_POH_Checklist_ListView
  Scenario: Verify Checklist list view validation
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "MFP" user
    And I open "MFP POH" app
    Then the "MFP POH Budget Review Dashboard" should be fully loaded
    Then I verify refresh time and admin details are displayed on the dashboard
    When I click on "Checklists" menu
    And I create a "New" list with name "MFP POH Checklist ${timestamp-short}"
    Then I should see the list view "MFP POH Checklist ${timestamp-short}" is active
    When I create a "Clone" list with name "MFP POH Checklist Clone ${timestamp-short}"
    Then I should see the list view "MFP POH Checklist Clone ${timestamp-short}" is active
    Then I search for new list view
    When I delete the current list view
    Then I should see a list view deleted confirmation
    Then I search for cloned list view
    When I delete the current list view
    Then I should see a list view deleted confirmation

  @MFP_POH_Opportunity_ListView
  Scenario: Verify Funding Opportunities list view validation
    When I click on "Funding Opportunities" menu
    And I create a "New" list with name "MFP POH FO ${timestamp-short}"
    Then I should see the list view "MFP POH FO ${timestamp-short}" is active
    When I create a "Clone" list with name "MFP POH FO Clone ${timestamp-short}"
    Then I should see the list view "MFP POH FO Clone ${timestamp-short}" is active
    Then I search for new list view
    When I delete the current list view
    Then I should see a list view deleted confirmation
    Then I search for cloned list view
    When I delete the current list view
    Then I should see a list view deleted confirmation

  @MFP_POH_Reports
  Scenario: Verify reports navigation
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count

  @MFP_POH_Dashboard
  Scenario: Verify dashboard navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard
