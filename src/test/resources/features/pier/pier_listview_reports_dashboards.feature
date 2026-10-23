@pier_View
Feature: PIER List View, Dashboard, and Reports Navigation

  # ============================================================
  # TC_001: List view — add new list + clone event
  # ============================================================
  @pier_listview
  Scenario: Add a new PIER list view and clone an event
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "PIER" user
    And I open "PIER Tracker" app
    When I click on "Cases" menu
    And I create a "New" list with name "PIER New List"
    Then I should see the list view "PIER New List" is active
    When I delete the current list view
    Then I should see "List view deleted." message
    When I create a "Clone" list with name "PIER Cloned List"
    Then I should see the list view "PIER Cloned List" is active
    When I delete the current list view
    Then I should see "List view deleted." message
    When I click on "Cases" menu
    Then I should see the "PIER : All Open Cases" list view
    Then I can validate the "PIER : All Open Cases" table columns
      | Column Name            |
      | Case Number            |
      | Account Name           |
      | Subject                |
      | Current Case Lead      |
      | Status                 |
      | Case Owner             |
      | PIER Outreach Due Date |
      | PIER Follow-up Date    |
    When I select a random record from the list view
    Then I can navigate to the random record

  @pier_Accounts_listview
  Scenario: Add a new PIER list view and clone an event
    When I click on "Accounts" menu
    Then I should see the "All Accounts" list view
    Then I can validate the "All Accounts" table columns
      | Column Name                        |
      | Last Modified Date                 |
      | Last Modified By Alias             |
      | Account Name                       |
      | Billing State/Province (text only) |
      | Phone                              |
      | Type                               |
      | Owner First Name                   |
      | Owner Last Name                    |
    When I select a random Account record from the list view
    Then I can navigate to the random record

  @pier_Issues_listview
  Scenario: Add a new PIER list view and clone an event
    When I click on "PIER Issues" menu
    Then I should see the "All Issues" list view
    Then I can validate the "All Issues" table columns
      | Column Name            |
      | PIER ID                |
      | Issue ID             |
      | Account                |
      | Start Month            |
      | Start Year             |
      | Indicator Affected     |
      | Sub Indicator Affected |
      | Report Type            |
      | Issue Type             |
      | PI Data Type           |
      | Closed                 |
    When I select a random record from the list view
    Then I can navigate to the random record

  # ============================================================
  # TC_002: Reports navigation
  # ============================================================

  @pier_reports
  Scenario: Verify reports navigation
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count

  # ============================================================
  # TC_003: Dashboard navigation
  # ============================================================

  @pier_dashboards
  Scenario: Verify dashboard navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard









