@bct-dsg-osg-readonly-access-user
Feature: BCT Contracts, CMCS Contracts and Contract Action permissions for dsg+osg read only access user

  @contract_creation_and_actions @regression @critical
  Scenario: BCT DSG+OSG read only access users can create and edit contracts
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "BCT-DSG-OSG-READ" user
    And I open "CMCS Budget & Contract Tracker" app
    Then the "CMCS Budget & Contract Tracker" should be fully loaded
    When I click on "Contracts" menu

    #============================================================================
    #TC_01: Verify Contracts Modal with Sections, Fields and record creation
    #============================================================================

    And I click New button
    And I fill the "Information" section with:
      | Component         | CMCS                                      |
      | Group             | DSG                                       |
      | Division Owner    | DBES                                      |
      | Contract Status   | Active                                    |
      | Project #         | ${random}                                 |
      | Product           | AMIT - New                                |
      | Product Title     | Automated Test Contract Edit ${timestamp} |
      | P.O.P. Start Date | 01/01/2026                                |
      | P.O.P. End Date   | 12/31/2027                                |
    And I fill the "Contract Details" section with:
      | Contract Number        | AutoContract${timestamp} |
      | Order Number           | ORD-${timestamp}         |
      | Total Contract Amount  | 5000000                  |
      | Total Obligated Amount | 4000000                  |
      | Contract Type          | Time and Materials       |
      | Award Type             | Contract (Definitive)    |
    And I fill the "Contact Information" section with:
      | OAGM Division                   | AGG - DBSC            |
      | Contract Officer                | Jada Garrett          |
      | Contract Specialist             | Yelena Buchynsky      |
      | Contract Officer Representative | Mohit Arora           |
      | Vendor                          | Pankaj_Test           |
      | Vendor Point of Contact         | Shital Deshmukh       |
      | Business Classification         | Woman-owned, Hub Zone |
      | Vendor Vehicle                  | Stand Alone Contract  |
    And I click the Save button
    Then I should see the success message

    #=========================================
    #TC_02: Verify Contract Details Page
    #=========================================

    And I should see the contract header shows:
      | Group           | DSG        |
      | Division Owner  | DBES       |
      | Product         | AMIT - New |
      | Contract Status | Active     |
    And I should see the contract number and order number in the header

    #=====================================================================================================
    #TC_03: Make updates to the Fields under Contract Details page and verify Field History Tracking
    #=====================================================================================================


    When I edit the contract details
    And I update the "Information" section with:
      | Component            | Other                                     |
      | Other Component      | Test Other Component                      |
      | Group                | Other                                     |
      | Other Group          | Test Other Group                          |
      | Division Owner       | Other                                     |
      | Other Division Owner | Test Other Division Owner                 |
      | Contract Status      | Technical Evaluation Panel (TEP)          |
      | Project #            | ${random}                                 |
      | Product              | Other                                     |
      | Other Product        | Test Other Product                        |
      | Product Title        | Automated Test Contract Edit ${timestamp} |
      | P.O.P. Start Date    | 02/01/2026                                |
      | P.O.P. End Date      | 01/01/2027                                |
    And I click the Save button
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Created.             |
      | Record Number        |
      | Component            |
      | Other Component      |
      | Project #            |
      | Group                |
      | Other Group          |
      | Division Owner       |
      | Other Division Owner |
      | Contract Status      |
      | Product              |
      | Other Product        |
      | Product Title        |
      | P.O.P. Start Date    |
      | P.O.P. End Date      |
    When I navigate back to the record link
    And I update the "Contract Details" section with:
      | Contract Number        | AutoContract${timestamp} |
      | Order Number           | ORD-${timestamp}         |
      | Total Contract Amount  | 6000000                  |
      | Total Obligated Amount | 5000000                  |
      | Contract Type          | Firm Fixed Price         |
      | Award Type             | Other                    |
      | Other Award Type       | Test Other Award Type    |
    And I click the Save button
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Contract Number        |
      | Order Number           |
      | Total Contract Amount  |
      | Total Obligated Amount |
      | Contract Type          |
      | Award Type             |
      | Other Award Type       |
    When I navigate back to the record link
    And I update the "Contact Information" section with:
      | OAGM Division           | Other                                     |
      | Other OAGM Division     | Test Other OAGM Division                  |
      | Business Classification | 8(a)                                      |
      | Vendor Vehicle          | Sole Source/ No existing contract vehicle |
    And I update the following fields:
      | Contract Officier               | Yelena Buchynsky |
      | Contract Specialist             | Jada Garrett     |
      | Contract Officer Representative | Binu Chemmayath  |
      | Vendor                          | Test             |
      | Vendor Point of Contact         | S Deshmukh       |
    And I click the Save button
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | OAGM Division                   |
      | Other OAGM Division             |
      | Contract Officer                |
      | Contract Specialist             |
      | Contract Officer Representative |
      | Vendor                          |
    When I navigate back to the record link

    # ==================================================
    # TC_04: Verify Notes section under Contracts tab.
    # ==================================================


    When I navigate to the "Notes" tab
    And I add a note with text "Add note test"
    Then I should see the note "Add note test" displayed
    When I edit the last note to "Edit note test"
    Then I should see the note "Edit note test" displayed
    Then I delete the last note
    Then I should see note deleted confirmation

    #===============================================
    #TC_05: Verify Create Contract Action Modal and fields
    #===============================================


    When I click on "Create Contract Action" button
    And I fill the "Create Contract Action" modal with:
      | Fiscal Year             | FY26       |
      | Contract Action         | New        |
      | Contract Action Type    | OY1        |
      | Contract Obligation     | 450        |
      | P.O.P. Start Date       | 01/01/2026 |
      | P.O.P. End Date         | 12/31/2027 |
      | PAA/DER Submission Date | 01/01/2028 |
      | PAA Approval Date       | 10/01/2028 |
      | DER Approval Date       | 11/01/2028 |
    And I click the Save button
    When I open the contract actions menu
    Then I should see the contract actions menu is displayed
    Then I click on Contract Action Number under Contract Actions table

    #=======================================================================================================
    #TC_06: Make updates to the fields under Contract Action Details page and verify Field History Tracking
    #=======================================================================================================


    When I edit the details page
    And I update the "Information" section with:
      | Fiscal Year             | FY27       |
      | Contract Action         | MOD        |
      | Contract Action Type    | OY2        |
      | Contract Obligation     | 500        |
      | P.O.P. Start Date       | 01/01/2027 |
      | P.O.P. End Date         | 12/31/2028 |
      | PAA/DER Submission Date | 01/01/2029 |
      | PAA Approval Date       | 10/01/2029 |
      | DER Approval Date       | 11/01/2029 |
    And I click the Save button
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Created.               |
      | Contract Action Number |
      | Fiscal Year            |
      | Contract Action Type   |
      | Contract Obligation    |
      | P.O.P. Start Date      |
      | P.O.P. End Date        |
    When I navigate back to the record link

  #======================================================
  #TC_07: Verify Notes section under Contract Action page
  #=======================================================


    When I navigate to the "Notes" tab
    And I add a note with text "Add note test"
    Then I should see the note "Add note test" displayed
    When I edit the last note to "Edit note test"
    Then I should see the note "Edit note test" displayed
    Then I delete the last note
    Then I should see note deleted confirmation


  #===================================================================================
  #TC_08: Verify Create Optional Milestone Tasks functionality under Contracts page
  #=================================================================================

  # When I click on "Create Optional Milestone Tasks" button
  # Then I should see the "Create Optional Milestone Tasks" modal is displayed
  # And I should see the following tasks in the "Create Optional Milestone Tasks" modal:
  #   | TEP Vendor Clarification Questions to AQD                                                                                                                          |
  #   | Vendor Clarification Questions to Vendors                                                                                                                          |
  #   | Vendor Clarification Responses to AQD                                                                                                                              |
  #   | Technical Evaluations Completed                                                                                                                                    |
  #   | Cost/Price Analysis Completed                                                                                                                                      |
  #   | Discussions/Negotiations, If Necessary                                                                                                                             |
  #   | Final Quote Revisions Due, If Requested                                                                                                                            |
  #   | Final Evaluations, If Revisions Requested                                                                                                                          |
  #   | Source Selection Documentation Completed                                                                                                                           |
  #   | Contract Award and File Prepared                                                                                                                                   |
  #   | Contract Award and File Reviewed (Including Legal)                                                                                                                 |
  #   | Contract Awarded                                                                                                                                                   |
  #   | Request for Debriefings from Unsuccessful Offerors Due (3 days from notice)                                                                                        |
  #   | Protest Time Frame [FAR 33.103] (10 days after contract award or within 5 days after a debriefing date offered to the protestor under a timely debriefing request) |
  # And I select the following tasks in the "Create Optional Milestone Tasks" modal:
  #   | Vendor Clarification Responses to AQD |
  #   | Technical Evaluations Completed       |
  # And I click on "Create Tasks" button
  # Then I navigate to the "Tasks" tab
  # And I should see the following tasks are displayed in the "Tasks" tab:
  #   | Vendor Clarification Responses to AQD |
  #   | Technical Evaluations Completed       |
  # Then I click on "Create Tasks" button
  # And I fill the "Create Tasks" section with:
  #   | Subject  | Test Task ${timestamp} |
  #   | Due Date | 10/01/2026             |
  #   | Name     | Shital Deshmukh        |
  # And I verify the "Assigned To" field is pre-populated with the "Pankaj UAT BCT DSG OSG Read Only" user
  # Then I click the Save button
  # And I should see the success message
  # Then I should see the task "Test Task ${timestamp}" is displayed in the "Tasks" tab
  # When I click on the task "Test Task ${timestamp}"
  # Then I update the task with:
  #   | Subject  | Test Task Edit ${timestamp} |
  #   | Due Date | 10/21/2026                  |
  #   | Name     | Pankaj Ghosh                |
  # Then I click the Save button

  #====================================================================
  #TC_08: Verify CMCS Contracts tab, list views and displayed buttons
  #====================================================================

  @bct_readyonly_access_CMCSContractstab @regression
  Scenario: BCT DSG+OSG read only access users have read only access under CMCS Contracts tab
    When I navigate to the "Home" page
    Then the "CMCS Contracts Dashboard" should be fully loaded
    And I click on "CMCS Contracts" menu
    And I should not see New button
    And Verify "All Contracts" list view is displayed
    Then I verify the "All Contracts" table columns are correct:
      | Contract / Order Number     |
      | Contract Title              |
      | Group Procurement Lead      |
      | Contract Type               |
      | CMCS Contract Record Number |
      | Contract Status             |
      | Component                   |

    #========================================================================
    #TC_09: Verify "View Contract Actions" functionality CMCS Contracts tab
    #========================================================================


    When I click on "View Contract Actions" button
    Then I verify "View CMCS Contracts" button is displayed
    And I click on "View CMCS Contracts" button
    And I click on "View Contract Actions" button
    And Verify "All Contract Action" list view is displayed
    And I click the first Contract Order Number link
    And I should not see Edit button
    And I should not see Create Contract Action button

    #========================================================================
    #TC_10: Verify "Contract Option Years" tab under CMCS Contract page
    #========================================================================


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
    # When I click View All
    Then I sum the "Anticipated Amount" column in the "Contract Cost Details" table
    And match it with the "Total Anticipated Amount" displayed
    Then I sum the "Obligated Amount" column in the "Contract Cost Details" table
    And match it with the "Total Obligated Amount" displayed

    #======================================================
    #TC_11: Verify Notes section under CMCS Contract page
    #=======================================================


    When I navigate to the "Notes" tab
    And I add a note with text "Add note test"
    Then I should see the note "Add note test" displayed
    # When I edit the last note to "Edit note test"
    # Then I should see the note "Edit note test" displayed
    # Then I delete the last note
    # Then I should see note deleted confirmation

    #====================================================
    #TC_12: Verify CMCS Contract Field History Tracking
    #====================================================
    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Created.              |
      | CMCS Contracts Number |
