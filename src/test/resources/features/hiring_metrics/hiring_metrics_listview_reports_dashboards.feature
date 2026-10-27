@hiring-metrics_ListView @smoke @regression
Feature: Hiring Metrics Hub List View, Dashboard, and Reports Navigation

  # ============================================================
  # TC_001: List view — add new list + clone
  # ============================================================

  @hmh_list_view @smoke
  Scenario: Add a new Hiring Metrics Hub list view and clone a record
    Given I navigate to MAC Suite Application in "SEI" environment
    When I login as "HMH" user
    And I open "CMCS Hiring Metrics Hub" app
    Then the "CMCS Hiring Metrics Hub" should be fully loaded
    When I click on "CMCS Hiring Metrics Hub" menu
    And I create a new list view with name "HMH Test List ${timestamp}"
    Then I should see the list view "HMH Test List ${timestamp}" is active
    When I delete the current list view
    Then I should see a list view deleted confirmation
    When I clone a hiring metrics hub record
    Then I should see the cloned hiring metrics record
  # ============================================================
  # TC_002: Reports navigation
  # ============================================================

  @hmh_reports
  Scenario: Verify reports navigation
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count
  # ============================================================
  # TC_003: Dashboard navigation
  # ============================================================

  @hmh_dashboard
  Scenario: Verify dashboard navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard

  @CMCS_HMH_All
  Scenario: Verify CMCS HMH All view validation
    Given I kill the previous browser session and launch new browser instance
    Given I navigate to MAC Suite Application in "SEI" environment
    When I login as "HMH" user
    And I open "CMCS Hiring Metrics Hub" app
    When I click on "CMCS Hiring Metrics Hub" menu
    Then I should see the "All" list view
    Then I can validate the "All" table columns
      | Column Name                    |
      | CMCS Hiring Metrics Hub Number |
      | Group                          |
      | Branch                         |
      | Division                       |
      | Hiring Initiative              |
      | Position Serial Number         |
      | Status                         |
    When I select a random record from the list view
    Then I can navigate to the random record
