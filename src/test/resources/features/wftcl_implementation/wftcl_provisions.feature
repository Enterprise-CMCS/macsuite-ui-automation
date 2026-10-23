@wftcl-implementation @smoke @regression
Feature: WFTCL Implementation Provisions Lifecycle

  # ============================================================
  # TC_001: Home page header verification
  # ============================================================
  @homepage_header @smoke
  Scenario: Home page header verification
  Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "WFTCL" user
    And I open "WFTCL Implementation" app
    Then the "WFTCL Provisions Dashboard" should be fully loaded

  # ============================================================
  # TC_002: Full Provisions creation lifecycle
  # ============================================================
  @wftcl_impl_provisions_creation @critical
  Scenario: Create a WFTCL Provision record and complete all sub-sections and tracking
    When I navigate to WFTCL Provisions
    And I click New to create a Provision record
    Then I should see the WFTCL Provision form
    When I enter the Provision overview details
    And I click the Save button
    Then I should see the success message
    When I submit the Provision dates section
    Then I should see the Provision dates saved successfully
    When I submit the Meetings dates section
    Then I should see the Meetings dates saved successfully
    When I submit the Document Tracking section
    Then I should see the Document Tracking entry saved
    When I upload a file on the Provision record
    Then I should see the file uploaded successfully
    When I add a note on the Provision record
    Then I should see the note added successfully
    When I post a Chatter message on the Provision record
    Then I should see the Chatter message posted
    When I open the Field History Tracking on the Provision record
    Then I should see field history entries for the Provision

