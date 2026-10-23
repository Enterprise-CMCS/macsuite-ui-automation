@bct-osg-cru-access-user
Feature: BCT CMCS Contracts Create, Read, Update permissions for OSG users


  @bct @cmcs-contracts-tab @regression @critical
  Scenario: BCT OSG users can create, read and update CMCS Contracts, and manage notes on CMCS Contracts
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "BCT-OSG-CRU" user
    And I open "CMCS Budget & Contract Tracker" app
    Then the "CMCS Contracts Dashboard" should be fully loaded
    And I click on "CMCS Contracts" menu

    #===================================================
    #TC_01: Verify View Contract Actions functionality
    #===================================================
    When I click on "View Contract Actions" button
    And Verify "All Contract Action" list view is displayed
    Then I verify "View CMCS Contract" button is displayed
    And I click on "View CMCS Contract" button

    #=================================
    #TC_02: Verify Contract Dashboard
    #==================================
    When I click on "Contract Dashboard" button
    And I verify "CMCS Contracts Dashboard" dashboard title is displayed
    Then I verify "View Contract Records" button is displayed
    And I filter "CMCS Contracts Dashboard" with:
      | Filter Fiscal Year     | FY25       |
      | Group Procurement Lead | DSG        |
      | P.O.P. Start Date      | 1/1/2026   |
      | P.O.P. End Date        | 10/15/2026 |
    And I click the dashboard Search button
    Then I should see the dashboard Legend and Contract Timeline Calendar
    And I verify "Clear" button is displayed
    And I verify the "All CMCS Contract Actions" table columns are correct:
      | Contract Title | Contract Action | Contract Action Type | Fiscal Year | Anticipated Amount | Obligated Amount |
    Then I click on "View Contract Records" button

    #============================================================================
    #TC_03: Verify CMCS Contract Modal with Sections, Fields and record creation
    #============================================================================

    And I click New button
    And I fill the "Information" section with:
      | Component                           | CMCS                           |
      | Group Procurement Lead              | DSG                            |
      | Contract Title                      | AutoTest Contract ${timestamp} |
      | Contract Status                     | Active                         |
      | Ultimate P.O.P. Contract Start Date | 01/01/2026                     |
      | Ultimate P.O.P. Contract End Date   | 12/31/2027                     |
    And I fill the "Contract Details" section with:
      | Contract Number         | AutoContract${timestamp} |
      | Order Number            | ORD-${timestamp}         |
      | Contract Type           | Time & Materials         |
      | Contract Classification | Policy                   |
      | Lifecycle ID            | ${random}                |
    And I fill the "Contact Information" section with:
      | Contract Officer                | Jada Garrett     |
      | Contract Specialist             | Yelena Buchynsky |
      | Contract Officer Representative | Mohit Arora      |
      | Contractor Name                 | shital_test      |
    And I fill the "Contract Summary & Notes" section with:
      | What Does It Buy? | Test Question |
      | Comments & Notes  | Test comments |
    And I click the Save button
    Then I should see the success message

    #=========================================
    #TC_04: Verify CMCS Contract Details Page
    #=========================================

    And I should see the contract header shows:
      | Group Procurement Lead | DSG         |
      | Contract Status        | Active      |
      | Contractor Name        | shital_test |
    Then I should see the summary value "Total Anticipated Amount" is displayed
    And I should see the summary value "Total Obligated Amount" is displayed

    #=====================================================================================================
    #TC_05: Make updates to the Fields under CMCS Contract Details page and verify Field History Tracking
    #=====================================================================================================

    And I update the "Information" section with:
      | Component                           | Other                            |
      | Other Component                     | Test Other Component             |
      | Group Procurement Lead              | Other                            |
      | Other Group                         | Test Other Group                 |
      | Contract Title                      | AutoTest Contract ${timestamp}   |
      | Contract Status                     | Technical Evaluation Panel (TEP) |
      | Ultimate P.O.P. Contract Start Date | 02/01/2026                       |
      | Ultimate P.O.P. Contract End Date   | 11/30/2027                       |
    And I click the Save button
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Created.                            |
      | CMCS Contracts Number               |
      | Component                           |
      | Other Component                     |
      | Group Procurement Lead              |
      | Contract Title                      |
      | Contract Status                     |
      | Ultimate P.O.P. Contract Start Date |
      | Ultimate P.O.P. Contract End Date   |
    When I navigate back to the record link
    And I update the "Contract Details" section with:
      | Contract Type                 | Other                              |
      | Other Contract Type           | Test Other Contract Type           |
      | Contract Classification       | Other                              |
      | Other Contract Classification | Test Other Contract Classification |
      | Lifecycle ID                  | ${random}                          |
    And I click the Save button
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Contract Type       |
      | Other Contract Type |
    When I navigate back to the record link
    And I update the "Contact Information" section with:
      | Contract Officer                | Yelena Buchynsky |
      | Contract Specialist             | Mohit Arora      |
      | Contract Officer Representative | Jada Garrett     |
      | Contractor Name                 | Pankaj_Test      |
    And I click the Save button
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Contract Officer                |
      | Contract Specialist             |
      | Contract Officer Representative |
      | Contractor Name                 |
    When I navigate back to the record link

    #================================================
    #TC_06: Verify Notes section under CMCS Contract
    #================================================

    When I navigate to the "Notes" tab
    And I add a note with text "Add note test"
    Then I should see the note "Add note test" displayed
    When I edit the last note to "Edit note test"
    Then I should see the note "Edit note test" displayed
    Then I delete the last note
    Then I should see note deleted confirmation

    #===============================================
    #TC_07: Verify Contract Action Modal and fields
    #===============================================

    When I click on "Create Contract Action" button
    And I fill the "Create Contract Action" modal with:
      | Project # (required)        | Add or link Project # |
      | Project # to add (required) | ${random}             |
      | Fiscal Year                 | FY25                  |
      | Contract Action             | OY1                   |
      | Requirement #               | Req-${random}         |
      | P.O.P. Start Date           | 01/01/2026            |
      | P.O.P. End Date             | 12/31/2027            |
      | Anticipated Amount          | 10000                 |
      | Obligated Amount            | 40000                 |
      | Funding Source              | ACA 3021              |
    And I click the Save button
    Then I should see the success message

    #=======================================================================================================
    #TC_08: Make updates to the fields under Contract Action Details page and verify Field History Tracking
    #=======================================================================================================

    And I update the "Information" section with:
      | Fiscal Year        | FY26             |
      | Contract Action    | OY2              |
      | Requirement #      | Req-${random}    |
      | P.O.P. Start Date  | 02/01/2026       |
      | P.O.P. End Date    | 11/30/2027       |
      | Anticipated Amount | 20000            |
      | Obligated Amount   | 50000            |
      | Funding Source     | ACCESS Act 50103 |
    And I click the Save button
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Created.               |
      | Contract Action Number |
      | Fiscal Year            |
      | Contract Action        |
      | Requirement #          |
      | Anticipated Amount     |
      | Obligated Amount       |
    When I navigate back to the record link

    #======================================================
    #TC_09: Verify Notes section under Contract Action page
    #=======================================================

    When I navigate to the "Notes" tab
    And I add a note with text "Add note test"
    Then I should see the note "Add note test" displayed
    When I edit the last note to "Edit note test"
    Then I should see the note "Edit note test" displayed
    Then I delete the last note
    Then I should see note deleted confirmation

    #============================================
    #TC_10: Verify Add Projects Modal and fields
    #============================================

    When I verify "Add Projects" button is displayed
    Then I click on "Add Projects" button
    Then I fill the "Add Projects" modal with:
      | Project # (required)        | Add or link Project # |
      | Project # to add (required) | ${random}             |
    And I select the "Add multiple Projects" checkbox
    Then I verify "Add or link Project #" button is displayed
    When I click on "Add or link Project #" button
    And I fill the "Add Projects" modal with:
      | Project # to add (required) | ${random} |
    And I click the Save button
    And I should see the success message

    #=================================================================
    #TC_11: Verify Contract Action Details page and Edit capabilities
    #=================================================================

    # Then I verify the "Related Projects" table columns are correct:
    #   | Project #: Project # |

    #======================================================================================
    #TC_12: Navigate back to CMCS Contract record and verify Contract Option Years section
    #======================================================================================

    Then I navigate back to the record link
    When I navigate to the "Contract Option Years" tab
    And I verify the "Total Contract Action Amounts" section is displayed
    Then I should see the summary value "Total Anticipated Amount" is displayed
    And I should see the summary value "Total Obligated Amount" is displayed
    And I should see the "Contract Cost Details" table displayed
    Then I verify the "Contract Cost Details" table columns are correct:
      | Contract Action Number |
      | P.O.P. Start Date      |
      | P.O.P. End Date        |
      | Fiscal Year            |
      | Anticipated Amount     |
      | Obligated Amount       |
    Then I sum the "Anticipated Amount" column in the "Contract Cost Details" table
    And match it with the "Total Anticipated Amount" displayed
    Then I sum the "Obligated Amount" column in the "Contract Cost Details" table
    And match it with the "Total Obligated Amount" displayed

    #====================================================
    #TC_13: Verify CMCS Contract Field History Tracking
    #====================================================

    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Created.              |
      | CMCS Contracts Number |
