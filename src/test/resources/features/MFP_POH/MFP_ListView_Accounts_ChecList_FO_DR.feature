@ListView_Acc_CHL_FO_DR
Feature: MFP POH Tracker

  @MFP_POH_Accounts_ListView
  Scenario: Verify Accounts list view validation
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "MFP" user
    And I open "MFP POH" app
    Then the "MFP POH Document Review Dashboard" should be fully loaded
    Then I verify refresh time and admin details are displayed on the dashboard
    When I click on "Accounts" menu
    Then I should see the "MFP POH State/Territory Accounts" list view
    Then I can validate the "MFP POH State/Territory Accounts" table columns
      | Column Name            |
      | Account Name           |
      | MFP POH Account Role   |
      | Billing State/Province |
      | Assigned GMS           |
      | Assigned PO            |
    When I select a random record from the list view
    Then I can navigate to the random record

  @MFP_POH_Accounts_ListView
  Scenario: Verify MFP POH Recipient Accounts view validation
    When I click on "Accounts" menu
    Then I should see the "MFP POH Recipient Accounts" list view
    Then I can validate the "MFP POH Recipient Accounts" table columns
      | Column Name            |
      | Account Name           |
      | MFP POH Account Role   |
      | Billing State/Province |
      | Grant Number           |
      | Assigned GMS           |
    When I select a random record from the list view
    Then I can navigate to the random record

  @MFP_POH_Accounts_ListView
  Scenario: Verify My MFP Accounts view validation
    When I click on "Accounts" menu
    Then I should see the "My MFP Accounts" list view
    Then I can validate the "My MFP Accounts" table columns
      | Column Name            |
      | Account Name           |
      | MFP POH Account Role   |
      | Billing State/Province |
      | Grant Number           |
      | Assigned GMS           |
      | Assigned PO            |
    When I select a random record from the list view
    Then I can navigate to the random record

  @MFP_POH_Checklist_ListView
  Scenario: Verify Checklists view validation
    When I click on "Checklists" menu
    Then I should see the "All" list view
    Then I can validate the "All" table columns
      | Column Name                          |
      | Checklist Number                     |
      | Recipient State/Territory            |
      | Submission Date                      |
      | Project Officer Submitting Checklist |
      | Grants Management Specialist         |
    When I select a random record from the list view
    Then I can navigate to the random record

  @MFP_POH_Checklist_ListView
  Scenario: Verify Withdrawn Checklists view validation
    When I click on "Checklists" menu
    Then I should see the "Withdrawn Checklists" list view
    Then I can validate the "Withdrawn Checklists" table columns
      | Column Name                          |
      | Budget Review Name                   |
      | Recipient State/Territory            |
      | Checklist Number                     |
      | Submission Date                      |
      | Project Officer Submitting Checklist |
      | Stage                                |
    When I select a random record from the list view
    Then I can navigate to the random record

  @MFP_POH_FO_ListView
  Scenario: Verify Funding Opportunities view validation
    When I click on "Funding Opportunities" menu
    Then I should see the "All" list view
    Then I can validate the "All" table columns
      | Column Name                          |
      | Funding Number                       |
      | Recipient State/Territory            |
      | Submission Date                      |
      | Stage                                |
      | Status                               |
      | Project Officer Submitting Checklist |
      | Grants Management Specialist         |
    When I select a random record from the list view
    Then I can navigate to the random record

  @MFP_POH_DocumentReview_ListView
  Scenario: Verify Document Reviews view validation
    When I click on "Document Reviews" menu
    Then I should see the "All" list view
    Then I can validate the "All" table columns
      | Column Name            |
      | Document Review Number |
      | Document Type          |
      | Project Officer        |
      | Review Due Date        |
      | Review Status          |
    When I select a random record from the list view
    Then I can navigate to the random record

  @MFP_POH_KnowledgeReview_ListView
  Scenario: Verify Knowledge view validation
  #  Given I navigate to MAC Suite Application in "UAT" environment
  #   When I login as "MFP" user
  #   And I open "MFP POH" app
  #   Then the "MFP POH Document Review Dashboard" should be fully loaded
    When I click on "Knowledge" menu
    Then I should see the "Published MFP POH Articles" list view
    Then I can validate the "Published MFP POH Articles" table columns
      | Column Name    |
      | Article Title  |
      | Summary        |
      | Article Number |
      | Language       |
    When I select a random record from the list view
    Then I can navigate to the random record

