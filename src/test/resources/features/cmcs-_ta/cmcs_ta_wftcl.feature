@cmcs-ta @cmcs_ta_wftcl @smoke @regression @demo
Feature: CMCS TA WFTCL Inquiry Creation and Validation
  # ============================================================
  # TC_001: WFTCL inquiry creation, validation, and Provision Other field
  # ============================================================

  @cmcs_ta_wftcl_creation @critical
  Scenario: Create a CMCS TA WFTCL inquiry and validate WFTCL-specific fields
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "TA" user
    And I open "CMCS Inquiry Tracker" app
    Then the "TA CMCS Inquiries" should be fully loaded
    When I click on "CMCS TA" menu
    And I click New button
    Then I should see the CMCS inquiry form
    When I select WFTCL inquiry type
    And I click the Save button
    Then I should see error toast "Inquiry Details is required."
    And I should see error "Complete this field with format 12/31/2024."
    And I should see error "Inquiry Title is required"
    And I should see error "Group is required"
    And I should see error "Category is required"
    And I should see error "Contact is required"
    And I should see error "Provision is required"
    # Sub-Category "Other" validation
    When I fill the inquiry group with "MCOG"
    And I fill the inquiry category with "1115 Demonstration"
    And I select Sub-Category "Other"
    And I click the Save button
    Then I should see error "Other Sub-Category Details is required when the Sub-Category selected as Other Value"
    # WFTCL-specific: Provision required when Provision = Other
    And I select Provision "Other"
    And I click the Save button
    Then I should see error "Other Provision is required"
    When I fill "Other (Please Describe)" with 101 characters
    Then I should see error "Limit reached. You can only use 100 characters in this field."
    # Other Organization validation — two saves required to trigger this error
    When I check "No Contact Found" checkbox
    And I select State_Territory as "Other"
    And I click the Save button
    Then I should see error "First name value is required"
    And I should see error "Last name value is required"
    And I should see error "Email value is required"
    Then I should see error toast "Other Organization is required if state is selected as Other"
    When I fill "Other Organization" with 76 characters
    Then I should see error "Limit reached. You can only use 75 characters in this field."
    When I close the inquiry form
    # ---- Full WFTCL inquiry creation ----
    When I click New button
    Then I should see the CMCS inquiry form
    When I fill CMCS inquiry details dynamically with:
      | Inquiry Type                 | Technical Assistance - WFTCL   |
      | Inquiry Title                | CMCS WFTCL e2e ${timestamp}    |
      | Inquiry Details              | CMCS WFTCL end-to-end coverage |
      | Provision                    |           70201-No tax on tips |
      | Submitted Date               | ${today}                       |
      | No Contact Found             | true                           |
      | First Name                   | Auto                           |
      | Last Name                    | WFTCL                          |
      | Email                        | wftcl${timestamp}@mail.com     |
      | State/Territory/Tribal/Other | Other                          |
      | Other Organization           | WFTCL Org ${timestamp}         |
    And I click the Save button
    Then I should see the success message
    And I should see "Inquiry Type" as "Technical Assistance - WFTCL" on the details page
    # And I should see "Group" as "DSG" on the details page
    Then I validate WFTCL details on the screen
    And I verify Supporting Groups and Triager fields should "Display"
    Then I select supporting group "Supporting Group 1"
    When I select supporting group triager "Supporting Group Triager 1":
      | Supporting Group Triager 1 | Raja Gangavaram |
    When I select supporting group triager "Supporting Group Triager (Additional)  1":
      | Supporting Group Triager (Additional)  1 | Peter Gutmann |
    Then I save the changes
    Then I select supporting group "Supporting Group 2"
    When I select supporting group triager "Supporting Group Triager 2":
      | Supporting Group Triager 2 | Pankaj TA App Ghosh |
    When I select supporting group triager "Supporting Group Triager (Additional) 2":
      | Supporting Group Triager (Additional) 2 | Jada Garrett |
    Then I save the changes
    Then I navigate to mailosure "UAT" environment
    Then I validate the Emails received on the account of supporting group updated
    When I add a CMCS inquiry question with:
      | Comment | What is the Progress |
      | Mention | @shital              |
    Then I should return to the inquiry details view
  # ============================================================
  # TC_002: Stage navigation + title verification ----
  # ============================================================

  Scenario: TC_002: Stage navigation + title verification
    Then I should see "Submitted" stage is selected by default
    And the inquiry title should start with "CMCS WFTCL e2e"
    When I change the submitted date to last year
    When I restore the submitted date to today
    #When I ask a question "What is the update", answer it, then delete it
    When I update CMCS inquiry reviewers and verify stage on WFTCL "Triager":
      | Triager | Pankaj TA App Ghosh |
    When I update CMCS inquiry reviewers and verify stage on WFTCL "Triager (Additional)":
      | Triager (Additional) | Shital ServiceCloud Deshmukh |
    Then I should not see the "Escalate" button
    When I update CMCS inquiry reviewers and verify stage on WFTCL "In Group Review":
      | SME | Raja Gangavaram |
    Then I should see field "Highest Level of Review" with value "SME"
    And I should see the "Escalate Inquiry" button
    When I update CMCS inquiry reviewers and verify stage on WFTCL "Senior Review":
      | Senior Reviewer / Technical Director | Peter Gutmann |
    Then I should see field "Highest Level of Review" with value "Senior Reviewer"
    When I clear CMCS senior reviewer and verify stage "In Group Review"
    And I update CMCS inquiry reviewers and verify stage on WFTCL "Senior Review":
      | Senior Reviewer / Technical Director | Peter Gutmann |
  # ============================================================
  # TC_003: Escalation, notes, and response readiness
  # ============================================================

  Scenario: TC_003: Escalation, notes, and response readiness
    When I escalate CMCS inquiry with:
      | Escalation Level            | Division Leadership, Group Leadership                           |
      | Comments                    | Formal escalation — high priority, requesting timely resolution |
      | Division Leadership         | true                                                            |
      | Group Leadership            | true                                                            |
      | Validate Max Comment Length | true                                                            |
    Then I should see CMCS inquiry stage "Escalated"
    When I add a note "Add note test" to the inquiry
    Then I should see the note "Add note test" is displayed
    When I edit the last note to "Edit note test"
    Then I should see the note "Edit note test" is displayed
    When I create CMCS inquiry response with:
      | Response Type    | Response                                         |
      | Response Details | CMCS TA response without resolution ${timestamp} |
    Then I should see "Send Response" button is not visible
    When I navigate back to the inquiry
    And I update the escalation resolution date to today
    When I create CMCS inquiry response with:
      | Response Type    | Response                                             |
      | Response Details | CMCS TA response with resolution & ready to send out |
  # ============================================================
  # TC_004: OGC field validations
  # ============================================================

  Scenario: TC_004: OGC field validations
    When I navigate back to the inquiry
    And I enable OGC Consultation on the inquiry
    And I click the Save button
    Then I should see error "An OGC Escalation Date is required when OGC Consultation is set to true"
    When I fill "OGC Escalation Date" with a future date
    And I click the Save button
    Then I should see error "The OGC Escalation Date cannot be a future date."
    When I fill "OGC Escalation Date" with "7 days ago"
    And I click the Save button
    When I create CMCS inquiry response with:
      | Response Type    | Response                                    |
      | Response Details | CMCS TA OGC pending resolution ${timestamp} |
    Then I should see "Send Response" button is not visible
    When I navigate back to the inquiry
    And I fill "OGC Resolution" with 5 characters
    And I click the Save button
    Then I should see error "Please enter an \"OGC Resolution Date\" if an \"OGC Resolution\" is being provided."
    When I fill "OGC Resolution" with 256 characters
    Then I should see error "Limit reached. You can only use 255 characters in this field."
    When I fill "OGC Resolution" with 256 characters
    Then I should see error "Limit reached. You can only use 255 characters in this field."
    When I fill "OGC Resolution Date" with a future date
    And I click the Save button
    Then I should see error "The OGC Resolution Date cannot be a future date."
    When I fill "OGC Resolution Date" with "8 days ago"
    And I click the Save button
    Then I should see error "OGC Resolution Date cannot be prior to OGC Escalation Date."
    When I fill "OGC Resolution Date" with "4 days ago"
    And I click the Save button
    And I create CMCS inquiry response with:
      | Response Type    | Response                                |
      | Response Details | CMCS TA OGC final response ${timestamp} |
    Then I should see "Send Response" button is visible
    When I preview the response email
    Then I should see preview email contact and subject details
    When I close the response email preview
    And I send the CMCS inquiry response
    Then I should see response status is "Sent"
    And I should see CMCS inquiry stage "Response Issued"
 # ============================================================
  # TC_005: Related inquiry
  # ============================================================

  Scenario: TC_005: Related inquiry
    When I navigate back to the inquiry
    And I remember the current CMCS inquiry title
    And I create a related CMCS inquiry with:
      | Inquiry Type                 | Technical Assistance - General   |
      | Inquiry Title                | Related CMCS TA e2e ${timestamp} |
      | Inquiry Details              | Related inquiry e2e coverage     |
      | Group                        | MCOG                             |
      | Category                     | Benefits and Coverage            |
      | Sub Category                 | Coverage                         |
      | Submitted Date               | ${today}                         |
      | No Contact Found             | true                             |
      | First Name                   | Related                          |
      | Last Name                    | Contact                          |
      | Email                        | related${timestamp}@mail.com     |
      | State/Territory/Tribal/Other | Other                            |
      | Other Organization           | Related Org ${timestamp}         |
    Then I should see the success message
    And I should not see the add related inquiry action on the child inquiry
    When I open the parent inquiry from related inquiries
    Then I should return to the remembered CMCS inquiry
  # ============================================================
  # TC_006: Full response lifecycle (create, verify fields, preview, send)
  # ============================================================

  Scenario: TC_006: Full response lifecycle (create, verify fields, preview, send)
    When I read the default response email template
    And I create CMCS inquiry response with:
      | Response Type               | Response                            |
      | State Contact Email Address | auto${timestamp}@test.com           |
      | Additional CC               | cc${timestamp}@test.com             |
      | Additional BCC              | bcc${timestamp}@test.com            |
      | Response Details            | CMCS TA final response ${timestamp} |
      | Validate Email Format       | true                                |
    Then I should see response code starts with "RES"
    And I should see the response email subject includes the inquiry title
    And I should see the response email address matches the contact email
    When I preview the response email
    Then I should see preview email contact, subject, CC, BCC and sent date
    When I close the response email preview
    And I send the CMCS inquiry response
    Then I should see response status is "Sent"
    And I should see the response sent date is today
    And I should see CMCS inquiry stage "Response Issued"
    # ============================================================
    # TC_007: Put on hold and release (standalone)
    # ============================================================

  Scenario: TC_007: Put on hold and release (standalone)
    When I navigate back to the inquiry
    When I put the inquiry on hold with reason "On hold for additional information"
    Then I should see "Inquiry Is On Hold" on hold message
    When I release the inquiry from hold with reason "Released from on hold"
    # Then I should see the inquiry stage returned to "Submitted"
    # ============================================================
    # TC_008: Withdrawn + close/reopen transitions (standalone)
    # ============================================================

  Scenario: TC_008: Withdrawn + close/reopen transitions (standalone)
    When I edit inquiry status
    And I select inquiry status "None"
    And I click the Save button
    Then I should see error "Complete this field."
    #Withdrawal with Other reason — required field validation
    When I select inquiry status "Withdrawn"
    And I select withdrawal reason "Other"
    And I click the Save button
    Then I should see error "Complete this field."
    #Character limit validation (76 chars > 75 limit)
    When I fill other withdrawal reason with "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ"
    Then I should see error "Limit reached. You can only use 75 characters in this field."
    #Valid withdrawal
    When I fill other withdrawal reason with "Withdrawn by requester for testing purposes"
    And I click the Save button
    Then I should see inquiry status "Withdrawn"
    And I should see closed date is today
    #Invalid: Withdrawn → Closed
    When I edit inquiry status
    And I select inquiry status "Closed"
    And I click the Save button
    Then I should see error "You cannot move from Withdrawn to Open or Closed."
    And the withdrawn transition to "Closed" should be blocked
    And I should see closed date is today
    #Invalid: Withdrawn → Open
    When I select inquiry status "Open"
    And I click the Save button
    Then I should see error "You cannot move from Withdrawn to Open or Closed."
    And the withdrawn transition to "Open" should be blocked
    #Valid: Reopen from Withdrawn
    When I select inquiry status "Reopened"
    And I fill reopen reason with "Status Reopened to verify close functionality"
    And I click the Save button
    Then I should see inquiry status "Reopened"
    And I should see closed date is empty
    #Close the inquiry
    When I edit inquiry status
    And I select inquiry status "Closed"
    And I click the Save button
    Then I should see inquiry status "Closed"
    And I should see closed date is today
    And I should see edit buttons are reduced after inquiry close
    #Reopen from Closed — empty reopen reason validation
    When I edit inquiry status
    And I select inquiry status "Reopened"
    When I fill reopen reason with ""
    And I click the Save button
    Then I should see error "Complete this field."
    #Valid reopen from Closed
    When I fill reopen reason with "Status Reopened after required validation"
    And I click the Save button
    Then I should see inquiry status "Reopened"
    And I should see edit buttons are restored after reopen
    And I should see closed date is empty
  # ============================================================
  # TC_009: Field history tracking (standalone)
  # ============================================================

  Scenario: TC_009: Field history tracking (standalone)
    When I update CMCS inquiry for field history with:
      | Triager  | Binu |
      | Priority | Expedited       |
    And I should see CMCS inquiry field history entries:
      | Stage          |
      | Inquiry Status |
      | Closed Date    |
    When I open the last sent response from the responses tab
    And I update the sent response fields with:
      | Type                         | Follow-up                      |
      | Additional CC                | cc${timestamp}@update.com      |
      | Additional BCC               | bcc${timestamp}@update.com     |
      | State Contact Email Address  | contact${timestamp}@update.com |
      | State/Territory/Tribal/Other | California                     |
    And I click the Save button
    And I should see response field history entries:
      | Type                         |
      | Additional CC                |
      | Additional BCC               |
      | State Contact Email Address  |
      | State/Territory/Tribal/Other |
      | Sent Date                    |
  # ============================================================
  # TC_010: Response details field and history (standalone)
  # ============================================================

  @cmcs_ta_response_details
  Scenario: Verify response details field functionality and its field history entry
    Given I kill the previous browser session and launch new browser instance
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "TA" user
    And I open "CMCS Inquiry Tracker" app
    Then the "TA CMCS Inquiries" should be fully loaded
    When I click on "CMCS TA" menu
    And I click New button
    Then I should see the CMCS inquiry form
    When I fill CMCS inquiry details with:
      | Inquiry Type                 | Technical Assistance - General    |
      | Inquiry Title                | CMCS TA resp-details ${timestamp} |
      | Inquiry Details              | Response details field coverage   |
      | Group                        | MCOG                              |
      | Category                     | Benefits and Coverage             |
      | Sub Category                 | Coverage                          |
      | Submitted Date               | ${today}                          |
      | No Contact Found             | true                              |
      | First Name                   | Auto                              |
      | Last Name                    | Contact                           |
      | Email                        | auto${timestamp}@mail.com         |
      | State/Territory/Tribal/Other | Other                             |
      | Other Organization           | Org ${timestamp}                  |
    And I click the Save button
    Then I should see the success message
    And the inquiry title should start with "CMCS TA resp-details"
    When I create CMCS inquiry response with:
      | Response Type               | Follow-up                                  |
      | State Contact Email Address | auto${timestamp}@test.com                  |
      | Additional CC               | cc${timestamp}@test.com                    |
      | Additional BCC              | bcc${timestamp}@test.com                   |
      | Response Details            | CMCS TA response details text ${timestamp} |
      | Validate Email Format       | true                                       |
    When I navigate back to the inquiry
    And I edit Response Details to "Verbal (e.g., National Call/Webinar, Phone Call)" with notes ""
    And I click the Save button
    Then I should see error "Complete this field."
    When I edit Response Details to "Verbal (e.g., National Call/Webinar, Phone Call)" with notes "Test"
    And I click the Save button
    When I edit Response Details to "Other" with notes "Test"
    And I click the Save button
    Then I should see CMCS inquiry stage "Response Issued"
    And I should see response details field history entry
