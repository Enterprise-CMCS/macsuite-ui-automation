Feature: Home Screen Validation

@RHTP_Home
  Scenario: Verify home screen elements 
    Given I navigate to MAC Suite RHTP Application in "QA" environment
    When I login as "RHTP" user
    # Then I should see the home screen with all expected elements 