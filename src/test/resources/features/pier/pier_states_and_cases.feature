@pier_core
Feature: PIER State Names and Cases
  # ============================================================
  # TC_001: Home page header + state names under Accounts tab
  # ============================================================

  @pier_home_header @pier_state_names @pier_issue @pier_cases_lifecycle
  Scenario: Verify PIER Tracker home page header and state names under Accounts tab
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "PIER" user
    And I open "PIER Tracker" app
    Then the "PIER Cases" should be fully loaded
    Then I verify refresh time and admin details are displayed on the dashboard
    When I click on "Accounts" menu
    Then I should see all state names displayed for list view "PIER All Accounts"

  @pier_issue @critical @smoke
  Scenario: Complete PIER Issue workflow - validation, create, update, and field history
    # Validate mandatory fields and year range
    When I click on "PIER Issues" menu
    And I click New button
    Then I should see the PIER issue form
    When I attempt to save an empty PIER issue form
    Then I should see mandatory field error messages on the blank PIER Issue form
    When I fill the PIER issue details with:
      | Account            | OHIO DEPARTMENT OF MEDICAID |
      | Start Month        | January                     |
      | Start Year         |                        2012 |
      | Description        | Year validation test        |
      | Indicator Affected |       1. Call Center Volume |
      | Related Check      | N/A                         |
    And I click the Save button
    Then I should see start year validation error "Please enter a year between 2013 and the current year."
    When I update the PIER issue year to "2027"
    And I click the Save button
    Then I should see start year validation error "Please enter a year between 2013 and the current year."
    # Create valid PIER issue
    When I update the PIER issue year to "2026"
    And I update the PIER issue details with:
      | Description            | Automated PIER issue full lifecycle test         |
      | Related Check          |                                        12H.C1.01 |
      | Report Type            | Preliminary                                      |
    And I click the Save button
    Then I should see the success message
    And I should see the PIER issue details are correct:
      | Start Year  |                                     2026 |
      | Description | Automated PIER issue full lifecycle test |
    # Update profile section fields
    When I update the PIER Issue profile section fields
    # Verify activity sections
    When I verify the forward data activity section on the PIER Issue
    And I verify the historical data activity section on the PIER Issue
    # Update issue with all field details
    When I update the PIER issue with:
      | Indicator Affected     | 3. Call Center Abandonment Rate |
      | Sub Indicator Affected |   3a. Abandonment rate top-line |
      | Start Month            | April                           |
      | End Month              | June                            |
      | Start Year             |                            2025 |
      | End Year               |                            2025 |
      | Closed                 | true                            |
      | Issue related to E&E?  | true                            |
      | Report Type            | Preliminary                     |
      | Issue Type             | Data context                    |
    # Update specific fields
    When I update the PIER issue details with:
      | Start Year  |                                      2025 |
      | Description | Updated PIER issue by automation workflow |
      | Report Type | Updated                                   |
    And I click the Save button
    When I update the PIER issue with:
      # | Fixed - Forward Data              | true        |
      | State will not fix - Forward Data | true        |
      | Timeline Date - Forward Data      | ${today}    |
      #  | Fixed - Historical Data              | true        |
      | State will not fix - Historical Data | true        |
      | Timeline Date - Historical Data      | ${today}    |
    And I click the Save button
    Then I will validate the fixed data already marked error message
     Then I Deselect the Fixed Data
      And I click the Save button
      Then I Select the Fixed Data and deselect the State will not fix Data
      And I click the Save button
    And I should see the PIER issue details are correct:
      | Start Year  |                                      2025 |
      | Description | Updated PIER issue by automation workflow |
    # Verify field history tracking
    When I navigate to the field history tab on the PIER Issue
    Then I should see PIER Issue field history entries recorded
    
  # ============================================================
  # TC_003: PIER Cases — creation, update, stages, outreach, history
  # ============================================================

  @pier_cases_lifecycle @critical
  Scenario: Create and manage a PIER Case through all stages and validate field history
    # --- Mandatory field + start year validation before creating a Case ---
    When I navigate to PIER Cases
    And I click New button
    Then I should see the PIER Case form
    And I should see mandatory field error messages on the blank PIER Case form
    When I fill the PIER Case details with:
      | Account     | OHIO DEPARTMENT OF MEDICAID      |
      | Subject     | Automated PIER Case ${timestamp} |
      | Start Month | January                          |
      | Start Year  |                             2012 |
    And I click the Save button
    Then I should see start year validation error "Please enter a year between 2013 and the current year."
    When I update the PIER Case year to "2027"
    And I click the Save button
    Then I should see start year validation error "Please enter a year between 2013 and the current year."
    When I update the PIER Case year to "2026"
    And I click the Save button
    Then I should see the success message
    # --- Update Case details ---
    When I update the PIER Case details with:
      | Description | Updated PIER Case description |
      | UAT          | true                          |
      | IsEscalated | true                          |
    And I click the Save button
    Then I should see the success message
    # --- Update Case stages ---
    When I progress the PIER Case through stages
    Then I should see the PIER Case stage updated
    # --- Outreach Activity tracking ---
    When I verify outreach activity tracking on the PIER Case
    # --- Second update to Case details ---
    When I update the PIER Case details a second time with:
      | Status            | State Review and Response    |
      | Start Month       | March                        |
      | Start Year        |                         2024 |
      | Current Case Lead | Pankaj PIER App Ghosh |
      | UAT                | false                        |
      | IsEscalated       | false                        |
      | PIER Outreach Due Date       | ${today}                        |
    And I click the Save button
    When I update the PIER Case owner to "Shital ServiceCloud Deshmukh"
    # When I click the Change Owner button
    # --- Field history ---
    When I navigate to the field history tab on the PIER Case
    Then I should see PIER Case field history entries recorded
