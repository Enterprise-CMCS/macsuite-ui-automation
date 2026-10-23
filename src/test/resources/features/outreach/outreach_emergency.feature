@outreach @outreach-emergency @regression 
Feature: CMCS Emergency Outreach Extended Lifecycle

  # ============================================================
  # TC_001: Verify Outreach Dashboard display
  # ============================================================
@Out_E_D
  Scenario: Verify Outreach Dashboard display
    Given I navigate to MAC Suite Application in "QA" environment
    When I login as "SO" user
    And I open "CMCS Inquiry Tracker" app
    When I click on "CMCS Outreach" menu

  # ============================================================
  # TC_002: Legacy chained Emergency lifecycle
  # ============================================================
  @Out_E_D @outreach_emergency_state_lifecycle @critical
  Scenario: Validate Emergency mandatory fields, create State Outreach, email response, and status lifecycle
    # Given I navigate to MAC Suite Application in "QA" environment
    # When I login as "SO" user
    # And I open "CMCS Inquiry Tracker" app
    # When I click on "CMCS Outreach" menu
    And I click Create a New Event button
    And I select outreach event type "Emergencies and Disasters" and click Next
    And I click the Save button
    Then I should see mandatory field error messages on the blank Emergency Outreach event form
    When I fill Emergency OutReach details with:
      | Outreach Type     | Emergency                                  |
      | Event Name        | Emergency E2E Extended ${timestamp}        |
      | Category          | Natural Disaster                           |
      | Event Description | Emergency extended lifecycle automation    |
      | Attachment File   | outreach-emergency.pdf                     |
      | File Description  | Emergency lifecycle attachment             |
    Then I upload the file
    And I click the Save button
    Then I should see the message "Event record created successfully!"
    When I attempt to create a State Outreach without required fields
    Then I should see mandatory field error messages on the blank Emergency State Outreach form
    Then the removed Presidential Declaration fields should not display
    And I validate Secretary Declaration Date fields

# ============================================================
  # TC_003: Create State Outreach with day-count color alerts 
  # ============================================================
@Out_E_D
   Scenario: Create State Outreach with day-count color alerts 
    When I create a State Outreach record linked to this emergency event
    Then I should see the State Outreach record created successfully
    When I select HHS declaration checkbox and URL
    Then I should see the 30-day alert indicator on the State Outreach
    And I should see the 60-day alert indicator on the State Outreach
    And I should see the 90-day alert indicator on the State Outreach

# ============================================================
  # TC_004: Create email response + preview + send
  # ============================================================

  Scenario: Create email response + preview + send
    When I create an outreach email response with template "Standard"
    And I preview the outreach email content
    And I send the outreach email response
    Then I should see the emailed date recorded
    When I navigate back to the State Outreach record

# ============================================================
  # TC_005: Close + reopen + withdrawn lifecycle
  # ============================================================
    Scenario: Close + reopen + withdrawn lifecycle
    When I navigate to the Outreach Event from the State Outreach
    When I update the outreach status to "Closed"
    Then I should see the close date recorded
    When I update the outreach status to "Withdrawn" with reason "Other"
    Then I should see the close date recorded
    And I verify close date cannot be a future date for "SO"
    When I verify the final "Closed" status
    When I update the outreach status to "Reopened" with reason "Reopening for additional emergency review"
    Then the outreach event status should be "Reopened"

  # ============================================================
  # TC_006: Emergency with linked CMCS TA 
  # ============================================================
  @outreach_emergency_cmcs_ta_link @critical
  Scenario: Create Emergency Outreach, link a CMCS TA inquiry, and manage SOE status lifecycle
    When I click on "CMCS Outreach" menu
    And I click Create a New Event button
    And I select outreach event type "Emergencies and Disasters" and click Next
    When I fill Emergency OutReach details with:
      | Outreach Type     | Emergency                                          |
      | Event Name        | Emergency with CMCS TA ${timestamp}                |
      | Category          | Natural Disaster                                   |
      | Event Description | Emergency outreach with CMCS TA linkage automation |
      | Attachment File   | outreach-emergency.pdf                             |
      | File Description  | Emergency CMCS TA attachment                       |
    And I click the Save button
    Then I should see the message "Event record created successfully!"
    When I create a State Outreach record linked to this emergency event
    Then I should see the State Outreach record created successfully
    When I create an outreach email response with template "Standard"
    And I preview the outreach email content
    And I send the outreach email response
    When I navigate back to the State Outreach record
    When I create a CMCS TA inquiry linked to the Emergency Outreach
    Then I should see the CMCS TA inquiry created successfully
    And I should be able to update State Outreach info from the linked CMCS TA
    When I navigate back to the State Outreach record

   # ============================================================
  # TC_007:  SOE status lifecycle
  # ============================================================ 

  Scenario: TC_007: SOE close/reopen/withdrawn status lifecycle
    When I navigate to the Outreach Event from the State Outreach
    When I update the outreach status to "Closed"
    Then I should see the close date recorded
    When I update the outreach status to "Reopened" with reason "Reopening after linked CMCS TA review"
    Then the outreach event status should be "Reopened"
    When I update the outreach status to "Withdrawn" with reason "Other"
    Then I should see the close date recorded
    And I verify close date cannot be a future date for "SOE"
    When I verify the final "Closed" status

  # ============================================================
  # TC_008: Field history on Emergency SOE, State Outreach, and Response
  # ============================================================
  @outreach_emergency_field_history @regression
  Scenario: Verify field history on Emergency Outreach SOE, State Outreach, and Response
    When I click on "CMCS Outreach" menu
    And I click Create a New Event button
    And I select outreach event type "Emergencies and Disasters" and click Next
    When I fill Emergency OutReach details with:
      | Outreach Type     | Emergency                                      |
      | Event Name        | Emergency Field History ${timestamp}           |
      | Category          | Natural Disaster                               |
      | Event Description | Emergency field history automation             |
      | Attachment File   | outreach-emergency.pdf                         |
      | File Description  | Emergency field history attachment             |
    And I click the Save button
    Then I should see the message "Event record created successfully!"
    When I create a State Outreach record linked to this emergency event
    Then I should see the State Outreach record created successfully
    When I create an outreach email response with template "Standard"
    And I preview the outreach email content
    And I send the outreach email response
    When I navigate back to the State Outreach record
    When I navigate to the Outreach Event from the State Outreach
    When I update fields on the Emergency Outreach details screen
    Then I should see field history recorded on the "Emergency SOE" record
    When I open the first State Outreach related to the Outreach Event
    When I update fields on the Emergency State Outreach record
    Then I should see field history recorded on the "Emergency State Outreach" record
    When I update the State Outreach Response and verify field history for "Emergency" type
