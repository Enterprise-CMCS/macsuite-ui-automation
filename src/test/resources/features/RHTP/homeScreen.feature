Feature: Home Screen Validation

  @RHTP_Home
  Scenario: Verify home screen elements
    Given I navigate to MAC Suite RHTP Application in "UAT" environment
    When I login as "RHTP" user
  # Then I should see the home screen with all expected elements

  @RHTP_Request_Access
  Scenario: Verify Request Access functionality
    Given I kill the previous browser session and launch new browser instance
    Given I navigate to MAC Suite RHTP Application in "UAT" environment
    When I navigate to the Request Access page
    Then I validate all the mandatory fields on the Request Access page