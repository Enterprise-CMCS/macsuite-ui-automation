@MFP_POH @smoke @regression
Feature: MFP POH Tracker

  @MFP_Home @MFP_FO
  Scenario: verify home page navigation and basic details validation
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "MFP" user
    And I open "MFP POH" app
    Then the "MFP POH Document Review Dashboard" should be fully loaded
    #########Then I verify refresh time and admin details are displayed on the dashboard
    Then I verify the "Budget Review Dashboard" header content
    And I verify the App name under production release announcements section


  @MFP_Checklist @MFP_FO
  Scenario: verify Account Creation and Add checklist functionality
    Given I navigate to Account Creation page in "MFP POH" app
    When I navigte to already created account details page
    ## When I create a new account
    ##  Then the account should be created successfully
    When I add a checklist item to the created account
    When I verify details under workbook under review stage
    Then I navigate to field history tracking tab and verify the history of changes
    Then I validate the checklist details in main window and sub window
    When I navigate to comments under review
    Then I verify the details of current stage comments under review stage
    When I navigate to Recommendation stage
    Then I verify the details of current stage Recommendation stage
    When I navigate to Request Funding Memo stage
    Then I verify the details of current stage Request Funding Memo stage
    When I navigated to Converted To Funding stage
    Then I verify the details of current stage Converted To Funding stage

  @MFP_FO
  Scenario: verify Funding Opportunity
    Given I navigate to Funding Opportunity page
    When I upload the funding memo document
    Then I verify the uploaded document details and validate the document content
    When I skip previous checklist then i can validate error message for skipped checklist
    When I submit the Section checklist details and verify the details in checklist tab
    When I clear the Section checklist one by one and verify the Funding memo navigation back
    Then I verify the details in checklist tab after clearing the checklist details

  @MFP_POH_Tracker
  Scenario: verify the details in field history tracking tab
    Given I kill the previous browser session and launch new browser instance
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "MFP" user
    And I open "MFP POH" app
    # Given I navigate to Funding Opportunity page to verify the details of Field History Tracking
    Given I navigate to Account Creation page in "MFP POH" app
    When I navigte to already created account details page
    ## When I create a new account
    ## Then the account should be created successfully
    When I add a checklist item to the created account
    When I verify details under workbook under review stage
    Then I navigate to field history tracking tab and verify the history of changes
    Then I validate the checklist details in main window and sub window
    When I navigate to comments under review
    Then I verify the details of current stage comments under review stage
    When I navigate to Recommendation stage
    # Then I verify the details of current stage Recommendation stage
    When I navigate to Request Funding Memo stage
    Then I verify the details of current stage Request Funding Memo stage
    When I navigated to Converted To Funding stage
    When I update the checklist status
    When I navigte to field history tracking tab
    Then I verify the history of changes for different fields in field history tracking tab
    When I clear the Section checklist to validate error message
    Then I verify the history of changes for different fields in field history tracking tab after clearing the checklist details
    When I update cleared checklist details and verify the history of changes in field history tracking tab
    When I send the email template to the user
    Then I validate the email template details in mailbox "UAT" and verify the email content

  @MFP_Document_Review
  Scenario: verify the Document Review functionality
    Given I kill the previous browser session and launch new browser instance
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "MFP" user
    And I open "MFP POH" app
    Given I navigate to Account Creation page in "MFP POH" app
    When I navigte to already created account details page
    Then I open document review modal popup and verify mandatory fields
    Then I submit document review form and verify success message
    Then I verify the Document Review header details
    Then I verify the Chatter functionality in document review modal popup
    Then I verify Review Status Accepted in document review
    Then I verify Review Status Complete in document review
    Then I navigate to child Account
    Then I create another document review in child account
    Then I verify the Complete Review by clicking on Complete Review button and checking the review status


  @MFP_Document_Review_Add_Task
  Scenario: verify the Document Review Add Task functionality
    Given I kill the previous browser session and launch new browser instance
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "MFP" user
    And I open "MFP POH" app
    Given I navigate to Account Creation page in "MFP POH" app
    When I navigte to already created account details page
    Then I open document review modal popup and verify mandatory fields
    Then I submit document review form and verify success message
    Then I verify the Document Review header details
    Then I add a task in document review modal popup and verify the task details
    When I navigate to the task then I verify the task details
    And I complete the task and verify its status
    When I navigate to the related document and I add another task
    Then I verify that the task is canceled
    And I verify the open and completed tasks in the related document


