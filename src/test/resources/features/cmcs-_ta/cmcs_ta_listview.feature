@cmcs-ta-list-view-operation @smoke @regression
Feature: CMCS TA List View, Dashboard and Home Navigation

  @cmcs_ta_list_view_operations @smoke
  Scenario: Verify CMCS TA list view create, clone and delete operations
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "TA" user
    And I open "CMCS Inquiry Tracker" app
    Then the "TA CMCS Inquiries" should be fully loaded
    When I click on "CMCS TA" menu
    And I create a new CMCS TA list view with name "Test TA List"
    Then I should see the CMCS TA list view "Test TA List" is active
    When I delete the current CMCS TA list view
    Then I should see "List view deleted." message
    When I create a new CMCS TA list view with name "Test TA Clone Source"
    Then I should see the CMCS TA list view "Test TA Clone Source" is active
    When I clone the current CMCS TA list view
    Then I should see the cloned CMCS TA list view is active
    When I delete the current CMCS TA list view
    Then I should see "List view deleted." message
    And I select CMCS TA list view "Test TA Clone Source"
    When I delete the current CMCS TA list view
    When I click on "CMCS TA" menu
    Then I should see the "All" list view
    Then I can validate the "All" table columns
      | Column Name        |
      | Inquiry No         |
      | Inquiry Type       |
      | Inquiry Title      |
      | Category           |
      | Sub-Category       |
      | Stage              |
      | Submitted Date     |
      | Last Modified Date |
      | Created Date       |
      | Created By         |
    When I select a random record from the list view
    Then I can navigate to the random record


  @cmcs_ta_dashboard_navigation @smoke
  Scenario: Navigate to a CMCS TA dashboard and verify it loads with correct title and refresh date
    When I open CMCS TA dashboards
    And I view all available CMCS TA dashboards
    And I open the first available CMCS TA dashboard
    Then the dashboard title should match the selected dashboard name
    And I should see dashboard refresh date is today
    When I click on "Reports" menu
    And I click on "All Reports" tab
    Then I should see breadcrumbs navigation
    And I should see "88" reports displayed
    When I open a random report
    Then I should see the report loaded with record count

  @cmcs_ta_reports
  Scenario: Verify reports navigation
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count

  @cmcs_ta_dashboard
  Scenario: Verify dashboard navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard
