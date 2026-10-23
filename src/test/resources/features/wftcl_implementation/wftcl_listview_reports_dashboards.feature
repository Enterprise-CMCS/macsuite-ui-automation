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


