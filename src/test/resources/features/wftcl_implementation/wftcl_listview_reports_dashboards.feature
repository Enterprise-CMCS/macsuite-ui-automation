@wftcl_listview_dashboard_reports @regression
Feature: WFTCL Implementation Provisions Lifecycle

  # ============================================================
  # TC_003: List view — create, delete, clone
  # ============================================================

  @wftcl_impl_listview
  Scenario: Manage WFTCL Provisions list views
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "WFTCL" user
    And I open "WFTCL Implementation" app
    When I navigate to WFTCL Provisions
    And I create a "New" list with name "WFTCL-${timestamp-short}"
    Then I should see the list view "WFTCL-${timestamp-short}" is active
    When I delete the current list view
    Then I should see "List view deleted." message
    When I create a "Clone" list with name "Clone-${timestamp-short}"
    Then I should see the list view "Clone-${timestamp-short}" is active
    When I delete the current list view
    Then I should see "List view deleted." message

  # ============================================================
  # TC_004: Reports navigation
  # ============================================================

  @wftcl_impl_reports
  Scenario: Verify reports navigation
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count

  # ============================================================
  # TC_005: Dashboard navigation
  # ============================================================

  @wftcl_impl_dashboards
  Scenario: Verify dashboards navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard

  @WFTCL_Provisions_All  @WFTCL_Provisions
  Scenario: Verify WFTCL Provisions All view validation
    Given I kill the previous browser session and launch new browser instance
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "WFTCL" user
    And I open "WFTCL Implementation" app
    When I click on "Provisions" menu
    Then I should see the "All" list view
    Then I can validate the "All" table columns
      | Column Name            |
      | Provision              |
      | DSG Division           |
      | MACPro System Impacted |
      | OneMAC System Impacted |
      | WMS System Impacted    |
    When I select a random record from the list view
    Then I can navigate to the random record

  @wftcl_Provision_DBES_All  @WFTCL_Provisions
  Scenario: Verify WFTCL Provision - DBES All view validation
    When I click on "Provisions - DBES" menu
    Then I should see the "All" list view
    Then I can validate the "All" table columns
      | Column Name            |
      | Provision - DBES       |
      | MACFin System Impacted |
      | T-MSIS System Impacted |
    When I select a random record from the list view
    Then I can navigate to the random record
