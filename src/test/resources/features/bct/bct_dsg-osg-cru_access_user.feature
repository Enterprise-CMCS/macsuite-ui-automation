@bct-dsg-osg-cru-access-user
Feature: BCT Contracts, CMCS Contracts and Contract Action permissions for dsg+osg cru access user

  @contract_creation_and_actions @regression @critical
  Scenario: BCT DSG+OSG cru access users can create and edit contracts
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "BCT-DSG-OSG-CRU" user
    And I open "CMCS Budget & Contract Tracker" app
#     Then the "CMCS Budget & Contract Tracker" should be fully loaded
#     When I click on "Contracts" menu

#     #============================================================================
#     #TC_01: Verify Contracts Modal with Sections, Fields and record creation
#     #============================================================================

#     And I click New button
#     And I fill the "Information" section with:
#       | Component         | CMCS                                      |
#       | Group             | DSG                                       |
#       | Division Owner    | DBES                                      |
#       | Contract Status   | Active                                    |
#       | Project #         | ${random}                                 |
#       | Product           | AMIT - New                                |
#       | Product Title     | Automated Test Contract Edit ${timestamp} |
#       | P.O.P. Start Date | 01/01/2026                                |
#       | P.O.P. End Date   | 12/31/2027                                |
#     And I fill the "Contract Details" section with:
#       | Contract Number        | AutoContract${timestamp} |
#       | Order Number           | ORD-${timestamp}         |
#       | Total Contract Amount  | 5000000                  |
#       | Total Obligated Amount | 4000000                  |
#       | Contract Type          | Time and Materials       |
#       | Award Type             | Contract (Definitive)    |
#     And I fill the "Contact Information" section with:
#       | OAGM Division                   | AGG - DBSC            |
#       | Contract Officer                | Jada Garrett          |
#       | Contract Specialist             | Yelena Buchynsky      |
#       | Contract Officer Representative | Mohit Arora           |
#       | Vendor                          | Pankaj_Test           |
#       | Vendor Point of Contact         | Shital Deshmukh       |
#       | Business Classification         | Woman-owned, Hub Zone |
#       | Vendor Vehicle                  | Stand Alone Contract  |
#     And I click the Save button
#     Then I should see the success message

#     #=========================================
#     #TC_02: Verify Contract Details Page
#     #=========================================

#     And I should see the contract header shows:
#       | Group           | DSG        |
#       | Division Owner  | DBES       |
#       | Product         | AMIT - New |
#       | Contract Status | Active     |
#     And I should see the contract number and order number in the header

#     #=====================================================================================================
#     #TC_03: Make updates to the Fields under Contract Details page and verify Field History Tracking
#     #=====================================================================================================


#     When I edit the contract details
#     And I update the "Information" section with:
#       | Component            | Other                                     |
#       | Other Component      | Test Other Component                      |
#       | Group                | Other                                     |
#       | Other Group          | Test Other Group                          |
#       | Division Owner       | Other                                     |
#       | Other Division Owner | Test Other Division Owner                 |
#       | Contract Status      | Technical Evaluation Panel (TEP)          |
#       | Project #            | ${random}                                 |
#       | Product              | Other                                     |
#       | Other Product        | Test Other Product                        |
#       | Product Title        | Automated Test Contract Edit ${timestamp} |
#       | P.O.P. Start Date    | 02/01/2026                                |
#       | P.O.P. End Date      | 01/01/2027                                |
#     And I click the Save button
#     When I navigate to the "Field History Tracking" tab
#     Then I should see field history for the following changes:
#       | Created.             |
#       | Record Number        |
#       | Component            |
#       | Other Component      |
#       | Project #            |
#       | Group                |
#       | Other Group          |
#       | Division Owner       |
#       | Other Division Owner |
#       | Contract Status      |
#       | Product              |
#       | Other Product        |
#       | Product Title        |
#       | P.O.P. Start Date    |
#       | P.O.P. End Date      |
#     When I navigate back to the record link
#     And I update the "Contract Details" section with:
#       | Contract Number        | AutoContract${timestamp} |
#       | Order Number           | ORD-${timestamp}         |
#       | Total Contract Amount  | 6000000                  |
#       | Total Obligated Amount | 5000000                  |
#       | Contract Type          | Firm Fixed Price         |
#       | Award Type             | Other                    |
#       | Other Award Type       | Test Other Award Type    |
#     And I click the Save button
#     When I navigate to the "Field History Tracking" tab
#     Then I should see field history for the following changes:
#       | Contract Number        |
#       | Order Number           |
#       | Total Contract Amount  |
#       | Total Obligated Amount |
#       | Contract Type          |
#       | Award Type             |
#       | Other Award Type       |
#     When I navigate back to the record link
#     And I update the "Contact Information" section with:
#       | OAGM Division           | Other                                     |
#       | Other OAGM Division     | Test Other OAGM Division                  |
#       | Business Classification | 8(a)                                      |
#       | Vendor Vehicle          | Sole Source/ No existing contract vehicle |
#     And I update the following fields:
#       | Contract Officier               | Yelena Buchynsky |
#       | Contract Specialist             | Jada Garrett     |
#       | Contract Officer Representative | Binu Chemmayath  |
#       | Vendor                          | Test             |
#       | Vendor Point of Contact         | S Deshmukh       |
#     And I click the Save button
#     When I navigate to the "Field History Tracking" tab
#     Then I should see field history for the following changes:
#       | OAGM Division                   |
#       | Other OAGM Division             |
#       | Contract Officer                |
#       | Contract Specialist             |
#       | Contract Officer Representative |
#       | Vendor                          |
#     When I navigate back to the record link

#     #==================================================
#     #TC_04: Verify Notes section under Contracts tab.
#     #==================================================


#     When I navigate to the "Notes" tab
#     And I add a note with text "Add note test"
#     Then I should see the note "Add note test" displayed
#     When I edit the last note to "Edit note test"
#     Then I should see the note "Edit note test" displayed
#     Then I delete the last note
#     Then I should see note deleted confirmation

#     #===============================================
#     #TC_05: Verify Create Contract Action Modal and fields
#     #===============================================


#     When I click on "Create Contract Action" button
#     And I fill the "Create Contract Action" modal with:
#       | Fiscal Year             | FY26       |
#       | Contract Action         | New        |
#       | Contract Action Type    | OY1        |
#       | Contract Obligation     | 450        |
#       | P.O.P. Start Date       | 01/01/2026 |
#       | P.O.P. End Date         | 12/31/2027 |
#       | PAA/DER Submission Date | 01/01/2028 |
#       | PAA Approval Date       | 10/01/2028 |
#       | DER Approval Date       | 11/01/2028 |
#     And I click the Save button
#     When I open the contract actions menu
#     Then I should see the contract actions menu is displayed
#     Then I click on Contract Action Number under Contract Actions table

#     #=======================================================================================================
#     #TC_06: Make updates to the fields under Contract Action Details page and verify Field History Tracking
#     #=======================================================================================================


#     When I edit the details page
#     And I update the "Information" section with:
#       | Fiscal Year             | FY27       |
#       | Contract Action         | MOD        |
#       | Contract Action Type    | OY2        |
#       | Contract Obligation     | 500        |
#       | P.O.P. Start Date       | 01/01/2027 |
#       | P.O.P. End Date         | 12/31/2028 |
#       | PAA/DER Submission Date | 01/01/2029 |
#       | PAA Approval Date       | 10/01/2029 |
#       | DER Approval Date       | 11/01/2029 |
#     And I click the Save button
#     When I navigate to the "Field History Tracking" tab
#     Then I should see field history for the following changes:
#       | Created.               |
#       | Contract Action Number |
#       | Fiscal Year            |
#       | Contract Action Type   |
#       | Contract Obligation    |
#       | P.O.P. Start Date      |
#       | P.O.P. End Date        |
#     When I navigate back to the record link

#     #======================================================
#     #TC_07: Verify Notes section under Contract Action page
#     #=======================================================


#     When I navigate to the "Notes" tab
#     And I add a note with text "Add note test"
#     Then I should see the note "Add note test" displayed
#     When I edit the last note to "Edit note test"
#     Then I should see the note "Edit note test" displayed
#     Then I delete the last note
#     Then I should see note deleted confirmation


#     #===================================================================================
#     #TC_08: Verify Create Optional Milestone Tasks functionality under Contracts page
#     #=================================================================================

#     # When I click on "Create Optional Milestone Tasks" button
#     # Then I should see the "Create Optional Milestone Tasks" modal is displayed
#     # And I should see the following tasks in the "Create Optional Milestone Tasks" modal:
#     #   | TEP Vendor Clarification Questions to AQD                                                                                                                          |
#     #   | Vendor Clarification Questions to Vendors                                                                                                                          |
#     #   | Vendor Clarification Responses to AQD                                                                                                                              |
#     #   | Technical Evaluations Completed                                                                                                                                    |
#     #   | Cost/Price Analysis Completed                                                                                                                                      |
#     #   | Discussions/Negotiations, If Necessary                                                                                                                             |
#     #   | Final Quote Revisions Due, If Requested                                                                                                                            |
#     #   | Final Evaluations, If Revisions Requested                                                                                                                          |
#     #   | Source Selection Documentation Completed                                                                                                                           |
#     #   | Contract Award and File Prepared                                                                                                                                   |
#     #   | Contract Award and File Reviewed (Including Legal)                                                                                                                 |
#     #   | Contract Awarded                                                                                                                                                   |
#     #   | Request for Debriefings from Unsuccessful Offerors Due (3 days from notice)                                                                                        |
#     #   | Protest Time Frame [FAR 33.103] (10 days after contract award or within 5 days after a debriefing date offered to the protestor under a timely debriefing request) |
#     # And I select the following tasks in the "Create Optional Milestone Tasks" modal:
#     #   | Vendor Clarification Responses to AQD |
#     #   | Technical Evaluations Completed       |
#     # And I click on "Create Tasks" button
#     # Then I navigate to the "Tasks" tab
#     # And I should see the following tasks are displayed in the "Tasks" tab:
#     #   | Vendor Clarification Responses to AQD |
#     #   | Technical Evaluations Completed       |
#     # Then I click on "Create Tasks" button
#     # And I fill the "Create Tasks" section with:
#     #   | Subject  | Call |
#     #   | Due Date | 10/01/2026             |
#     #   | Name     | Shital Deshmukh        |
#     # And I verify the "Assigned To" field is pre-populated with the "Pankaj UAT BCT DSG OSG CRU User" user
#     # Then I click the Save button
#     # And I should see the success message
#     # Then I should see the task "Call" is displayed in the "Tasks" tab
#     # When I click on the task "Call"
#     # Then I update the task with:
#     #   | Subject  | Send Letter |
#     #   | Due Date | 10/21/2026                  |
#     #   | Name     | Pankaj Ghosh                |
#     # Then I click the Save button

# #====================================================================
# #TC_08: Verify CMCS Contracts tab, list views and displayed buttons
# #====================================================================

@bct_cru_access_CMCSContractstab @regression
 Scenario: BCT DSG+OSG cru access users have create, read and update access under CMCS Contracts tab
    # When I navigate to the "Home" page
    # Then the "CMCS Contracts Dashboard" should be fully loaded
    And I click on "CMCS Contracts" menu

    # #===================================================
    # #TC_01: Verify View Contract Actions functionality
    # #===================================================
    # When I click on "View Contract Actions" button
    # And Verify "All Contract Action" list view is displayed
    # Then I verify "View CMCS Contracts" button is displayed
    # And I click on "View CMCS Contracts" button

    # #=================================
    # #TC_02: Verify Contract Dashboard
    # #==================================
    # When I click on "Contract Dashboard" button
    # And I verify "CMCS Contracts Dashboard" dashboard title is displayed
    # Then I verify "View Contract Records" button is displayed
    # And I filter "CMCS Contracts Dashboard" with:
    #   | Filter Fiscal Year     | FY25       |
    #   | Group Procurement Lead | DSG        |
    #   | P.O.P. Start Date      | 1/1/2026   |
    #   | P.O.P. End Date        | 10/15/2026 |
    # And I click the dashboard Search button
    # Then I should see the dashboard Legend and Contract Timeline Calendar
    # And I verify "Clear" button is displayed
    # And I verify the "All CMCS Contract Actions" table columns are correct:
    #   | Contract Title | Contract Action | Contract Action Type | Fiscal Year | Anticipated Amount | Obligated Amount |
    # Then I click on "View Contract Records" button

    #============================================================================
    #TC_03: Verify CMCS Contract Modal with Sections, Fields and record creation
    #============================================================================

    And I click New button
    And I fill the "Information" section with:
      | Component                           | CMCS                           |
      | Group Procurement Lead              | DSG                            |
      # | Contract Title                      | AutoTest Contract ${timestamp} |
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
    #TC_07: Verify Create Contract Action Modal and fields
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

     Then I verify the "CMCS ConAct-Projects" table columns are correct:
     | Project # |

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
