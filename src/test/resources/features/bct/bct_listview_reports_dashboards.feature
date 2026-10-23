
Feature: BCT Dashboard, Reports and List View Navigation
 # ============================================================
  # TC_001: List view operations
  # ============================================================

  @bct_list_view_operations @smoke
  Scenario: Verify list view create, clone and delete operations
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "BCT" user
    And I open "CMCS Budget & Contract Tracker" app
    Then the "CMCS Budget & Contract Tracker" should be fully loaded
    When I click on "Contracts" menu
    And I create a "New" list with name "Test New List ${timestamp}"
    Then I should see the list view "Test New List ${timestamp}" is active
    When I delete the current list view
    Then I should see "List view deleted." message
    When I create a "Clone" list with name "Cloned List ${timestamp}"
    Then I should see the list view "Cloned List ${timestamp}" is active
    When I delete the current list view
    Then I should see "List view deleted." message
  # ============================================================
  # TC_002: Reports navigation
  # ============================================================

  @bct_reports
  Scenario: Verify reports navigation
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count
  # ============================================================
  # TC_003: Dashboard navigation
  # ============================================================

  @bct_dashboard
  Scenario: Verify dashboard navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard

    # ============================================================
  # TC_004: Contract Dashboard
  # ============================================================

  # @bct_Contract_dashboard
  # Scenario: Verify Contract Dashboard navigation
  #   When I click on "Contracts" menu
  #   And I click on "Contract Dashboard" button
  #   Then I should see the Contract Dashboard is displayed
  #   And I click on "Export to PDF" button
  #   And I click on "View Contract Records" button

