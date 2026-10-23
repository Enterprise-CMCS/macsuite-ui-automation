@cmcs-ta @smoke @regression @demo
Feature: CMCS TA Reports Validation

  @smoke @cmcs-ta-reports-validation
  Scenario Outline: Validate CMCS TA report "<reportName>" record counts
    Given I navigate to MAC Suite Application in "UAT" environment
    When I login as "TA" user
    And I open "CMCS Inquiry Tracker" app
    When I wait for dashboard to load
    When I click on CMCS TA report "<reportName>"
    Then I should see report page with title "<reportName>"
    When I capture the top record count
    And I scroll to bottom of report
    And I capture the bottom record count
    Then the top and bottom record counts should match
    When I close the current report tab

    Examples:
      | reportName                               |
      | TA CMCS Inquiries by Stage               |
      | TA CMCS Inquiries by Category            |
      | TA CMCS Inquiries by Triager             |
      | Open TA CMCS Inquiries                   |
      | TA CMCS Inquiries by Priority            |
      | TA CMCS Inquiries by Escalation Level    |
      | TA Highest Level of Review Report        |
      | TA CMCS Inquiries by Stage, Category     |
      | TA WFTCL CMCS Inquiries by Stage         |
      | TA WFTCL CMCS Inquiries by Category      |
      | Open TA WFTCL CMCS Inquiries             |
      | TA WFTCL CMCS Inquiries by Priority      |
      | TA WFTCL CMCS Inquiries by Triager       |
      | TA WFTCL Highest Level of Review Report  |
      | TA WFTCL CMCS Inquiries by Provision     |
      | WFTCL by Group                           |
      | TA WFTCL CMCS Inquiries by Stage, Catego |
      | TA WFTCL CMCS Inquiries by Escalation Lv |
