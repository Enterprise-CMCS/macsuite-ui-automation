@outreach_report @smoke @regression
Feature: CMCS Outreach List View, Dashboard, and Reports

  # ============================================================
  # TC_001: List View navigation
  # ============================================================

  @outreach_list_view
  Scenario: Add a new Outreach list view and clone an event
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "SO" user
    And I open "CMCS Inquiry Tracker" app
    When I click on "CMCS Outreach" menu
    And I create a "New" list with name "Outreach New List"
    Then I should see the list view "Outreach New List" is active
    When I delete the current list view
    Then I should see "List view deleted." message
    When I create a "Clone" list with name "Outreach Cloned List"
    Then I should see the list view "Outreach Cloned List" is active
    When I delete the current list view
    Then I should see "List view deleted." message
    When I click on "CMCS Outreach" menu
    Then I should see the "All Events/Topics" list view
    Then I can validate the "All Events/Topics" table columns
      | Column Name  |
      | Event ID     |
      | Event Name   |
      | Topic Name   |
      | Event Type   |
      | Category     |
      | Status       |
      | Date Created |
    When I select a random record from the list view
    Then I can navigate to the random record

  # ============================================================
  # TC_002: Reports navigation
  # ============================================================

  @outreach_reports
  Scenario: Verify reports navigation
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count
  # ============================================================
  # TC_003: Dashboard navigation
  # ============================================================

  @outreach_dashboards
  Scenario: Verify dashboard navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard
