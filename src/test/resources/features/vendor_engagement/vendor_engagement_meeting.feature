#@vendor-engagement @smoke @regression
#Feature: Vendor Engagement Meeting Scheduling
#
#	  Background:
    #Given I navigate to MAC Suite Application in "UAT" environment
    #When I login as "VE" user
    #And I open "Vendor Engagement" app
    #Then the "WFTCL Vendor Engagement Discussions" dashboard should be fully loaded
#
#
  #@meeting-scheduling @critical
  #Scenario: Schedule a vendor engagement meeting
    #When I click on "Vendor Engagements" menu
    #And I click New button
    #And I fill the meeting details with:
      #| Meeting Name       | Automated Test Meeting ${timestamp} |
      #| Subject            | Q2 Vendor Review Meeting    |
      #| Meeting Date       | 05/15/2026                  |
      #| Meeting Start Time | 10:00 AM                    |
      #| Meeting End Time   | 12:00 PM                    |
      #| Status             | Requested                   |
      #| Meeting Type       | Ad-Hoc                      |
      #| Vendor Name        | Test Vendor Inc             |
    #And I click the Save button
    #Then I should see the success message
    #And I should see the meeting details are correct
#
  # Legacy VE_Creation_Page.validateAllMandotoryFields_Before_EngagementCreation:
  # saving a blank meeting must surface inline required-field errors.
  #@ve-mandatory-validation @smoke
  #Scenario: Validate required-field enforcement on a blank Vendor Engagement meeting
    #When I click on "Vendor Engagements" menu
    #And I click New button
    #And I click the Save button
    #Then I should see error "Complete this field"
#
  # Legacy HomePage.updateDetails: edit an existing meeting after creation. PREDICTED, not
  # runtime-verified; isolated scenario (cannot affect the create flow). Edits a text field to
  # avoid guessing a picklist option value.
  #@ve-edit @smoke
  #Scenario: Edit a vendor engagement meeting after creation
    #When I click on "Vendor Engagements" menu
    #And I open the first vendor engagement record
    #And I edit the meeting with:
      #| Subject | Updated Subject ${timestamp} |
    #And I click the Save button
    #Then I should see the success message
#
#
#
     #@cmcs-ta-list-view-operations @smoke
  #Scenario: Verify CMCS VE list view create, clone and delete operations
    #When I click on "Vendor Engagements" menu
    #And I create a new CMCS TA list view with name "Vendor Engagements List"
    #Then I should see the CMCS TA list view "Vendor Engagements List" is active
    #When I delete the current CMCS TA list view
    #Then I should see "List view deleted." message
    #When I create a new CMCS TA list view with name "Vendor Engagements Clone Source"
    #Then I should see the CMCS TA list view "Vendor Engagements Clone Source" is active
    #When I clone the current CMCS TA list view
    #Then I should see the cloned CMCS TA list view is active
    #When I delete the current CMCS TA list view
    #Then I should see "List view deleted." message
    #And I select CMCS TA list view "Vendor Engagements Clone Source"
    #When I delete the current CMCS TA list view
#
  #@cmcs-ta-dashboard-navigation @smoke
  #Scenario: Navigate to a CMCS VE dashboard and verify it loads with correct title and refresh date
    #When I open CMCS TA dashboards
    #And I view all available CMCS TA dashboards
    #And I open the first available CMCS TA dashboard
    #Then the dashboard title should match the selected dashboard name
    #And I should see dashboard refresh date is today
    