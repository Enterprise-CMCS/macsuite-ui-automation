@bct-osg-readonly-access-user
Feature: BCT CMCS Contracts and View Contract Action permissions for OSG readonly access users

  @bct @read-only @regression
  Scenario: Read-only BCT OSG users can open and review contracts and contract actions
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "BCT_OSG_READ" user
    And I open "CMCS Budget & Contract Tracker" app

    #======================================================
    #TC_01: Verify "CMCS Contracts Dashboard" for OSG User
    #======================================================

    Then the "CMCS Contracts Dashboard" should be fully loaded

    #===============================================================
    #TC_02: Verify read only capabilities under "CMCS Contracts" tab
    #================================================================
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
    When I click on "View Contract Actions" button
    Then I verify "View CMCS Contracts" button is displayed
    And I click on "View CMCS Contracts" button
    And I click on "View Contract Actions" button
    And Verify "All Contract Action" list view is displayed
    And I click the Contract Order Number link
    And I should not see Edit button
    And I should not see Create Contract Action button

    # =====================================================================
    # TC_03: Verify Contract Option Years section under CMCS Contract page
    # =====================================================================
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
    #TC_04: Verify Notes section under CMCS Contract page
    #=======================================================

    When I navigate to the "Notes" tab
    And I add a note with text "Add note test"
    Then I should see the note "Add note test" displayed
    When I edit the last note to "Edit note test"
    Then I should see the note "Edit note test" displayed
    Then I delete the last note
    Then I should see note deleted confirmation

    #=============================================================
    #TC_05: Verify Field History Tracking under CMCS Contract page
    #==============================================================

    When I navigate to the "Field History Tracking" tab
    Then I should see field history for the following changes:
      | Created.              |
      | CMCS Contracts Number |
