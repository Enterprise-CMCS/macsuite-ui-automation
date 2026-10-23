@pra @smoke
Feature: PRA ICR Packages List View, Dashboard, and Reports Navigation
 
  # ============================================================
  # TC_001: Home page header + list view add/clone
  # ============================================================
  @pra_listview @smoke
  Scenario: Verify PRA home page header, add a new ICR Packages list view and clone an event
   Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "PRA" user
    And I open "CMCS PRA Tracker" app
    Then the "CMCS Active PRA Dashboard" should be fully loaded
    When I click on "CMCS ICR Packages" menu
    And I create a "New" list with name "PRA New List"
    Then I should see the list view "PRA New List" is active
    When I delete the current list view
    Then I should see "List view deleted." message
    When I create a "Clone" list with name "PRA Cloned List"
    Then I should see the list view "PRA Cloned List" is active
    When I delete the current list view
    Then I should see "List view deleted." message

  # ============================================================
  # TC_002: Reports navigation
  # ============================================================

  @pra_reports
  Scenario: Verify reports navigation
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count

  # ============================================================
  # TC_003: Dashboard navigation
  # ============================================================

  @pra_dashboards
  Scenario: Verify dashboard navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard

