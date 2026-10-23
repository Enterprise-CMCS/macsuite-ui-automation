@pra
Feature: CMCS PRA Tracker ICR Package Workflows

  @pra_icr_lifecycle @critical
  Scenario Outline: Create and progress a <Type> ICR package through lifecycle stages
   Given I kill the previous browser session and launch new browser instance
   Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "PRA" user
    And I open "CMCS PRA Tracker" app
    Then the "CMCS Active PRA Dashboard" should be fully loaded
    When I click on "CMCS ICR Packages" menu
    And I click New button
    And I select PRA record type "Standard"
    Then I should see the PRA ICR package form
    # Required-field validation: saving the empty Standard form must surface inline errors
    # (legacy ICR_PackageCreation_Page.createICR_Package saved empty first, asserting "Complete this field.")
    When I click the Save button
    Then I should see error "Complete this field."
    When I fill PRA ICR package details with:
      | Type        | <Type>       |
      | Group Owner | <GroupOwner> |
      | Title       | <Title>      |
    And I click the Save button
    Then I should see the success message
    When I update PRA package fields and verify stage "OSG Intake":
      | Initiated by OSG Date | ${today}   |
      | Expiration Date       | ${today+5} |
    When I update PRA package fields and verify stage "CMS Review":
      | OSG Intake Completed Date | ${today+1} |
    When I update PRA package fields and verify stage "Public Comment":
      | OCD First Review Completed Date | ${today+2} |
      | OSORA Review Completed Date     | ${today+2} |
      | OCD Clearance Completed Date    | ${today+2} |
    When I update PRA package fields and verify stage "OMB Approval":
      | Round 1 - Public Comment Posted Date | ${today+3} |
      | Public Comment Completed Date        | ${today+3} |
    # PREDICTED: legacy verifyNavigatingToComplete also set OMB NoA = Approved (OMB_NoA_Dropdown ->
    # OMB_Approved_Status). The "OMB NoA" picklist label is inferred — verify on first UAT run.
    When I update PRA package fields and verify stage "Completed":
      | OMB Approval Completed Date | ${today+4} |
      | Anticipated Completion Date | ${today+4} |
      | OMB NoA Decision Date       | ${today+4} |
      | OMB NoA                     | Approved   |
    And I request PRA action "<RequestAction>"
    Then the PRA package should be closed and the child ICR package should open
    And I should see generated PRA identifiers when required
    

    Examples: 
      | Type       | GroupOwner | Title                                  | RequestAction                |
       | Generic    | DSG        | PRA automation Generic ${timestamp}    | Revision (WITHOUT extension) |
       | MACPro     | CAHPG      | PRA automation MACPro ${timestamp}     | Discontinuance               |
      | Standalone | DSG        | PRA automation Standalone ${timestamp} | Revision (WITHOUT extension) |


 @pra_icr_lifecycle_Umbrella @critical
  Scenario Outline: Create and progress a <Type> ICR package through lifecycle stages
   Given I kill the previous browser session and launch new browser instance
   Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "PRA" user
    And I open "CMCS PRA Tracker" app
    Then the "CMCS Active PRA Dashboard" should be fully loaded
    When I click on "CMCS ICR Packages" menu
    And I click New button
    And I select PRA record type "Umbrella Package"
    Then I should see the PRA ICR package form
    # Required-field validation: saving the empty Standard form must surface inline errors
    # (legacy ICR_PackageCreation_Page.createICR_Package saved empty first, asserting "Complete this field.")
    When I click the Save button
    Then I should see error "Complete this field."
    When I fill PRA ICR package details with:
      | Type        | <Type>       |
      | Group Owner | <GroupOwner> |
      | Title       | <Title>      |
    And I click the Save button
    Then I should see the success message
    When I update PRA package fields and verify stage "OSG Intake":
      | Initiated by OSG Date | ${today}   |
      | Expiration Date       | ${today+5} |
    When I update PRA package fields and verify stage "CMS Review":
      | OSG Intake Completed Date | ${today+1} |
    And I request PRA action with "<RequestAction>"
    When I update PRA package fields and verify stage "Public Comment":
      | OCD First Review Completed Date | ${today+2} |
      | OSORA Review Completed Date     | ${today+2} |
      | OCD Clearance Completed Date    | ${today+2} |
    When I update PRA package fields and verify stage "OMB Approval":
      | Round 1 - Public Comment Posted Date | ${today+3} |
      | Public Comment Completed Date        | ${today+3} |
    # PREDICTED: legacy verifyNavigatingToComplete also set OMB NoA = Approved (OMB_NoA_Dropdown ->
    # OMB_Approved_Status). The "OMB NoA" picklist label is inferred — verify on first UAT run.
    When I update PRA package fields and verify stage "Completed":
      | OMB Approval Completed Date | ${today+4} |
      | Anticipated Completion Date | ${today+4} |
      | OMB NoA Decision Date       | ${today+4} |
      | OMB NoA                     | Approved   |
    # And I request PRA action "<RequestAction>"
    Then the PRA package should be closed and the child ICR package should open
    And I should see generated PRA identifiers when required
    
    Examples: 
      | Type       | GroupOwner | Title                                  | RequestAction                |
       | Generic    | DSG        | PRA automation Generic ${timestamp}    |Extension (WITHOUT change) |
        | MACPro     | CAHPG      | PRA automation MACPro ${timestamp}     | Extension (WITHOUT change)               |
      #  | Standalone | DSG        | PRA automation Standalone ${timestamp} | Extension (WITHOUT extension) |
