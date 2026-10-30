@pra_ListView_All_readOnly @smoke
Feature: PRA ICR Packages List View, Dashboard, and Reports Navigation

  # ============================================================
  # TC_001: Home page header + list view add/clone
  # ============================================================
  @pra_listview_readOnly @smoke
  Scenario: Verify PRA home page header, add a new ICR Packages list view and clone an event
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "PRAREADONLY" user
    And I open "CMCS PRA Tracker" app
    # Then the "CMCS Active PRA Dashboard" should be fully loaded
    When I click on "CMCS ICR Packages" menu
    Then I should see the "All Records" list view
    Then I can verify that New button is NOT visible
    Then I validate the printable view screen
   
  # ============================================================
  # TC_002: Reports navigation
  # ============================================================

  # @pra_reports
  # Scenario: Verify reports navigation
  #   When I click on "Reports" menu
  #   And I click on "All Folders" tab
  #   When I open a random report
  #   Then I should see the report loaded with record count

  # ============================================================
  # TC_003: Dashboard navigation
  # ============================================================

  # @pra_dashboards
  # Scenario: Verify dashboard navigation
  #   When I click on "Dashboards" menu
  #   And I click on "All Folders" tab
  #   And I select a random dashboard
  #   Then I verify refresh time and admin details are displayed on the dashboard

  @CMCS_ICR_Packages_ListView

  Scenario: Verify CMCS ICR Packages view validation
    Given I kill the previous browser session and launch new browser instance
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "PRAREADONLY" user
    And I open "CMCS PRA Tracker" app
    When I click on "CMCS ICR Packages" menu
    Then I should see the "All Records" list view
    Then I can validate the "All Records" table columns
      | Column Name                 |
      | Unique ID #                 |
      | Title                       |
      | CMS ID #                    |
      | OMB Number                  |
      | Generic IC #                |
      | OMB Approval Completed Date |
      | Last Modified Date          |
      | Group Owner                 |
    When I select a random record from the list view
    Then I can navigate to the random record

  @Knowledge_ListView
  Scenario: Verify Knowledge Published Articles view validation
    When I click on "Knowledge" menu
    Then I should see the "Published Articles" list view
    Then I can validate the "Published Articles" table columns
      | Column Name    |
      | Article Title  |
      | Summary        |
      | Article Number |
      | Language       |
      | Published Date |
    When I select a random record from the list view
    Then I can navigate to the random record

  # @Knowledge_ListView_PRA_readOnly
  # Scenario: Verify Knowledge Published PRA Articles view validation
  #  Given I navigate to MAC Suite Application in "UAT" environment
  #   When I login as "PRAREADONLY" user
  #   And I open "CMCS PRA Tracker" app
  #   When I click on "Knowledge" menu
  #   Then I should see the "Published PRA Articles" list view
  #   Then I can validate the "Published PRA Articles" table columns
  #     | Column Name    |
  #     | Article Title  |
  #     | Summary        |
  #     | Article Number |
  #     | Language       |
  #   When I select a random record from the list view
  #   Then I can navigate to the random record

  @Contacts_ListView
  Scenario: Verify Contacts view validation
    When I click on "Contacts" menu
    Then I should see the "All Contacts" list view
    Then I can validate the "All Contacts" table columns
      | Column Name      |
      | Account Name     |
      | Name             |
      | Email            |
      | Title            |
      | Phone            |
      | Created By Alias |
      | Owner First Name |
      | Owner Last Name  |
    When I select a random record from the list view
    Then I can navigate to the random record