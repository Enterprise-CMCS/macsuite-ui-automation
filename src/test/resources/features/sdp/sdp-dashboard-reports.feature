#@sdp
#Feature: SDP Dashboards and Rating Period Validation
  #Validates SDP dashboard display with report count widgets, and the SDP Rating Period
  #iframe with status-based counts (Approved, Under Review, Submitted, Withdrawn and combinations).
#
  #Background:
    #Given I navigate to MAC Suite Application in "UAT" environment
    #When I login as "SDP" user
    #And I open "SDP" app
    #Then the "SDP" dashboard should be fully loaded
#
  # ============================================================
  # verify_Dashboard_Reports: Dashboard page with 6 report count widgets
  # ============================================================
  #@sdp-dashboard-counts @smoke
  #Scenario: Verify SDP dashboard displays all report count widgets correctly
    #When I navigate to the SDP dashboard page
    #Then I should see all SDP dashboards displayed
    #And I should see the "SDP Submitted Last 12 Months" report count widget
    #And I should see the "SDP Approved/Closed Last 12 Months" report count widget
    #And I should see the "SDP Pending Last 12 Months" report count widget
    #And I should see the "SDP Submitted Last Month" report count widget
    #And I should see the "SDP Approved/Closed Last Month" report count widget
    #And I should see the "SDP Pending Last Month" report count widget
#
  # ============================================================
  # verify_SDP_Rating_Period: Rating Period iframe status verification
  # ============================================================
  #@sdp-rating-period @regression
  #Scenario: Verify SDP Rating Period iframe status count display
    #When I navigate to the SDP Rating Period screen
    #And I switch to the SDP Rating Period iframe
    #Then I should see "Approved" status count displayed correctly
    #And I should see "Under Review" status count displayed correctly
    #And I should see "Submitted" status count displayed correctly
    #And I should see "Withdrawn" status count displayed correctly
    #And I should see combined "Under Review + Submitted" status count displayed correctly
    #And I should see combined "Approved = Withdrawn" status count displayed correctly
