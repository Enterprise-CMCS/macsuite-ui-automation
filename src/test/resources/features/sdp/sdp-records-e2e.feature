#@sdp @smoke @regression
#Feature: SDP Record Creation and Lifecycle Validation
  #Validates SDP record mandatory field errors, boundary value validation, creation with
  #various SDP statuses, control name duplicate detection, naming convention changes,
  #review type transitions with parent record rules, and PDF generation.
#
  #Background:
    #Given I navigate to MAC Suite Application in "UAT" environment
    #When I login as "SDP" user
    #And I open "SDP" app
    #Then the "SDP" dashboard should be fully loaded
#
  # ============================================================
  # TC_001: Mandatory field validation + field-specific verifications
  # ============================================================
  #@sdp-mandatory-fields @smoke
  #Scenario: Validate all mandatory fields and field-specific constraints on blank SDP record form
    #When I navigate to the SDP record creation form
    #And I click the Save button
    #Then I should see mandatory field error messages on the blank SDP record form
    #When I verify input field "Control Name" accepts text and shows validation
    #And I verify dropdown field "State" with option "AL"
    #And I verify dropdown field "Review Type" with option "New"
    #And I verify search field "DMCP Lead" search and selection
    #And I verify date field "State Submitted to CMS" with offset 0
    #And I verify date field "Rating Period Begin Date" with offset -10
    #And I verify date field "Rating Period End Date" with offset 10
    #And I verify date field "Payment Arrangement Start Date" with offset 5
    #When I handle the error snag on the SDP form
    #And I drag the "Separate Payment Term" option to selected
    #And I verify drag option field "Method of Payment to Plans"
    #And I drag the "Inpatient Hospital Service" option to selected
    #And I verify drag option field "Provider Type"
    #When I close the SDP record form without saving
    #Then I should see the SDP record form closed
#
  # ============================================================
  # TC_002: Payment Arrangement Start Date boundary value validation
  # ============================================================
  #@sdp-payment-date-boundary @regression
  #Scenario: Validate Payment Arrangement Start Date boundary value constraints
    #When I navigate to the SDP record creation form
    #And I fill in the SDP record details without a status but requiring a parent record
    #And I validate Payment Arrangement Start Date boundary values
    #When I close the SDP record form without saving
    #Then I should see the SDP record form closed
#
  # ============================================================
  # TC_003: Create SDP record with "Under Review" status + Key Contacts + Provider Type
  # ============================================================
  #@sdp-under-review-creation @critical
  #Scenario: Create SDP record with Under Review status and verify Key Contacts and Provider Type
    #When I navigate to the SDP record creation form
    #And I fill in the SDP record details with status "Under Review" requiring a parent record
    #And I click the Save button
    #Then I should see the success message
    #When I select Key Contacts for the SDP record
    #And I select a Provider Type for the SDP record
    #Then I should see the SDP record updated with Key Contacts and Provider Type
#
  # ============================================================
  # TC_004: Create SDP records with various statuses (parameterized)
  # ============================================================
  #@sdp-status-creation @smoke
  #Scenario Outline: Create an SDP record with status "<SdpStatus>" and verify success
    #When I navigate to the SDP record creation form
    #And I fill in the SDP record details with status "<SdpStatus>" requiring a parent record
    #And I click the Save button
    #Then I should see the success message
#
    #Examples:
      #| SdpStatus    |
      #| Under Review |
      #| Approved     |
      #| Submitted    |
#
  # ============================================================
  # TC_005: Control Name duplicate error + naming convention after Rating Period End Date change
  # ============================================================
  #@sdp-control-name-duplicate @regression
  #Scenario: Verify Control Name duplicate error and naming convention update after Rating Period End Date change
    #When I navigate to the SDP record creation form
    #And I fill in the SDP record details with an empty status and requiring a parent record
    #And I click the Save button
    #Then I should see the success message
    #When I verify the Control Name duplicate error message
    #And I verify naming convention updates after Rating Period End Date change
#
  # ============================================================
  # TC_006 + TC_007: Record without parent + review type transitions + PDF generation (chained)
  # ============================================================
  #@sdp-review-type-transitions @sdp-pdf @critical
  #Scenario: Verify SDP record review type transitions, parent record rules, and PDF generation
    #When I navigate to the SDP record creation form
    #And I fill in the SDP record details with no status and no parent record requirement
    #And I click the Save button
    #Then I should see the success message
#
    #When I change the Review Type to "Amendment"
    #And I click the Save button
    #Then I should see a parent record alert message for Amendment
#
    #When I change the Review Type to "Renewal"
    #And I click the Save button
    #Then I should see a parent record error message for Renewal
#
    #When I link a parent SDP record and verify child record updates with the chosen Review Type
    #Then I should see the CMS Clock field displayed on the SDP record
    #And I should see the most recent action activity on the SDP record
#
    #When I generate a PDF for the SDP record
    #Then I should see the PDF created successfully
