@outreach @outreach-general @smoke @regression
Feature: CMCS General Outreach End-to-End Lifecycle

  # ============================================================
  # TC_001 + TC_002 + TC_003 + TC_004 + TC_005 + TC_006: Chained E2E lifecycle
  # ============================================================

  @outreach_general_lifecycle @critical
  Scenario: Create a General Outreach event and progress through full lifecycle
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "SO" user
    And I open "CMCS Inquiry Tracker" app
    When I click on "CMCS Outreach" menu
    And I click Create a New Event button
    And I select outreach event type "General" and click Next
    Then I validate the mandatory fields on the General Outreach event details screen
     And I click Create a New Event button
    And I select outreach event type "General" and click Next
    When I fill General OutReach event details with:
      | Topic Name        | General Outreach E2E ${timestamp}               |
      | Category          | CHIP                                            |
      | Topic Description | General outreach end-to-end automation coverage |
    Then I upload the file
    And I click the Save button
    Then I should see the message "Event record created successfully!"
    And I should see the General Outreach event details are correct
    # ---- TC_002: Create State Outreach and set State Lead + Date Initiated ----
    When I create a State Outreach record linked to this general event
    When I set the State Lead and Date Initiated on the State Outreach
    # ---- TC_003: Create Outreach Email and manage lifecycle ----
    When I create an outreach email response with template "Advanced Notification of Event to State email"
    And I preview the outreach email content
    And I send the outreach email response
    Then I should see the emailed date recorded
    When I navigate back to the State Outreach record
    # ---- TC_004: Add Q&A and progress to Closed ----
    When I add a Q&A entry to the State Outreach
    And I navigate back to the State Outreach record
    Then the outreach event status should be "Open"
    When I update the outreach status to "Closed"
    And I navigate back to the State Outreach record
    Then the outreach event status should be "Closed"
    # ---- TC_005: SOE close/reopen/withdrawn lifecycle ----
    When I navigate to the Outreach Event from the State Outreach
    When I update the outreach status to "Closed"
    Then I should see the close date recorded
    When I update the outreach status to "Reopened" with reason "Reopening for additional review"
    Then the outreach event status should be "Reopened"
    When I update the outreach status to "Withdrawn" with reason "Other"
    Then I should see the close date recorded
    And I verify close date cannot be a future date for "SOE"
    When I verify the final "Closed" status
    # ---- TC_006: Field history on SOE, State Outreach, and Response ----
    #This is failing, need to check if all steps are applicable.
    When I update fields on the Outreach Event details screen
    Then I should see field history recorded on the "SOE" record
    When I navigate back to the State Outreach record
    When I update fields on the State Outreach record
    Then I should see field history recorded on the "General State Outreach" record
    When I update the State Outreach Response and verify field history for general type
