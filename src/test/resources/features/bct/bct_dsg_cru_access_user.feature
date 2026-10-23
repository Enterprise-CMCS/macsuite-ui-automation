@bct-contract
Feature: BCT Contract Creation, Validation and Actions

  @contract_creation_and_actions @field_history @regression @critical
  Scenario: Create a contract, validate header, edit fields, open actions menu, add a note and verify field history
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "BCT-DSG-CRU" user
    And I open "CMCS Budget & Contract Tracker" app
    Then the "CMCS Budget & Contract Tracker" should be fully loaded
    When I click on "Contracts" menu
    And I click New button
    And I fill the "Information" section with:
      | Component         | CMCS                                      |
      | Group             | DSG                                       |
      | Division Owner    | DBES                                      |
      | Contract Status   | Active                                    |
      | Project           | ${random}                                 |
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
    And I should see the contract header shows:
      | Group           | DSG        |
      | Division Owner  | DBES       |
      | Product         | AMIT - New |
      | Contract Status | Active     |
    And I should see the contract number and order number in the header
    When I edit the contract details
    And I update the "Information" section with:
      | Component            | Other                                     |
      | Other Component      | Test Other Component                      |
      | Group                | Other                                     |
      | Other Group          | Test Other Group                          |
      | Division Owner       | Other                                     |
      | Other Division Owner | Test Other Division Owner                 |
      | Contract Status      | Technical Evaluation Panel (TEP)          |
      | Project              | ${random}                                 |
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
    When I navigate to the "Notes" tab
    And I add a note with text "Add note test"
    Then I should see the note "Add note test" displayed
    When I edit the last note to "Edit note test"
    Then I should see the note "Edit note test" displayed
    Then I delete the last note
    Then I should see note deleted confirmation
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
    When I navigate to the "Notes" tab
    And I add a note with text "Add note test"
    Then I should see the note "Add note test" displayed
    When I edit the last note to "Edit note test"
    Then I should see the note "Edit note test" displayed
    Then I delete the last note
    Then I should see note deleted confirmation





