@hiring-metrics @smoke @regression
Feature: CMCS Hiring Metrics Hub Workflow

  @status_transitions @critical
  Scenario: Complete hiring metrics hub lifecycle with all status transitions
   Given I navigate to MAC Suite Application in "SEI" environment
    When I login as "HMH" user
    And I open "CMCS Hiring Metrics Hub" app
    Then the "CMCS Hiring Metrics Hub" should be fully loaded
    When I click on "CMCS Hiring Metrics Hub" menu
    And I click New button
    Then I should see the hiring metrics form
    When I fill in the hiring metrics details with:
      | Group                  | DSG          |
      | Hiring Initiative      | FY26Q2       |
      | Position Serial Number | ${timestamp} |
      | HRITS Number           | ${timestamp} |
    And I click the Save button
    Then I should see the success message
    And I should see the record number displayed
    #Pre Announcement stage
    When I update the status to "Pre-Announcement"
    And I add a note "Position requisition prepared for approval"
    When I click on "Details" tab
    Then I should see the status as "Pre-Announcement"
    #Active Posting stage
    When I update the status to "Active Posting"
    And I add a note "Job posting published on USAJobs and internal portal"
    When I click on "Details" tab
    Then I should see the status as "Active Posting"
    #Qualifications Review stage
    When I update the status to "Qualifications Review"
    And I add a note "Reviewing 45 qualified applications"
    When I click on "Details" tab
    Then I should see the status as "Qualifications Review"
    #Manager Interview stage
    When I update the status to "Manager Interview"
    And I add a note "Conducting interviews with 12 candidates"
    When I click on "Details" tab
    Then I should see the status as "Manager Interview"
    #Selections Identified stage
    When I update the status to "Selections Identified"
    And I add a note "Selected top 2 candidates for final approval"
    When I click on "Details" tab
    Then I should see the status as "Selections Identified"
    #Tentative Job Offer stage
    When I update the status to "Tentative Job Offer (Accepted)"
    And I add a note "Tentative offers extended to selected candidates"
    When I click on "Details" tab
    Then I should see the status as "Tentative Job Offer (Accepted)"
    #Final Job Offers stage
    When I update the status to "Final Job Offers (EOD)"
    And I add a note "Final offers accepted by both candidates"
    When I click on "Details" tab
    Then I should see the status as "Final Job Offers (EOD)"
    #Onboard stage
    When I update the status to "Onboard"
    And I add a note "New hires scheduled to start on May 1, 2026"
    When I click on "Details" tab
    Then I should see the status as "Onboard"
    #Verify field history tracking
    When I click on "Field History" tab
    And I should see all status transitions in field history:
      | From Status                    | To Status                      |
      | Final Job Offers (EOD)         | Onboard                        |
      | Tentative Job Offer (Accepted) | Final Job Offers (EOD)         |
      | Selections Identified          | Tentative Job Offer (Accepted) |
      | Manager Interview              | Selections Identified          |
      | Qualifications Review          | Manager Interview              |
      | Active Posting                 | Qualifications Review          |
      | Pre-Announcement               | Active Posting                 |
      | Not Started                    | Pre-Announcement               |
