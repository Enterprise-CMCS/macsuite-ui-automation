# @access-control @critical
# Feature: Validate User Access Control
#   Verify that users only see their assigned applications and modules based on their role
  
  
#   #Talk to Shital to get more details and scenarios
#   Scenario Outline: Validate <Role> user access in <Environment> environment
#     Given I navigate to "<Environment>" environment
#     When I login as "<Role>" user
#     And I open "<ExpectedApp>" app
#     Then I should see "<ExpectedApp>" displayed in the app header
#     And I should see only one module available for "<Role>" role
#     And I should NOT see unauthorized modules


#     Examples:
#       | Environment | Role | ExpectedApp                        |
#       | UAT         | BCT  | CMCS Budget & Contract Tracker     |
#       #| UAT         | SO   | State Operations                   |
#       #| UAT         | TA   | Technical Advisor                  |
#       #| UAT         | PIER | PIER Management                    |
#       #| UAT         | PRA  | Program Regulatory                 |
#       #| QA          | BCT  | CMCS Budget & Contract Tracker     |
#       #| QA          | SO   | State Operations                   |
#       #| SEI         | BCT  | CMCS Budget & Contract Tracker     |

