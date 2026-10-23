@cmcs-ta-report_dashboard @smoke @regression
Feature: CMCS TA List View, Dashboard and Home Navigation

  @ta_reports
  Scenario: Verify reports navigation
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "TA" user
    And I open "CMCS Inquiry Tracker" app
    Then the "TA CMCS Inquiries" should be fully loaded
    When I click on "Reports" menu
    And I click on "All Folders" tab
    When I open a random report
    Then I should see the report loaded with record count

  @ta_dashboards
  Scenario: Verify dashboard navigation
    When I click on "Dashboards" menu
    And I click on "All Folders" tab
    And I select a random dashboard
    Then I verify refresh time and admin details are displayed on the dashboard
