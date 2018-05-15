Bunch of test for validating different type of test steps

Scenario: ignored and commented JBehave strings should be as ignored steps
Meta:

Given a Calculator
!-- some ignored step among others
When I increment 4 with 1
Then Calculator returns 5

Scenario: comments in varios places
!-- comment after TITLE
Meta:
!-- comment after META

Given a Calculator
!-- MIDDLE step comment
When I increment 4 with 1
Then Calculator returns 5
!-- LAST step comment

Scenario: various step types
!-- comment after TITLE
Meta:
!-- comment after META

Given a Calculator
!-- MIDDLE#1  step comment
When i have NOT IMPLEMENTED step w/o
When I increment 4 with 1
Then Calculator returns 5
!-- MIDDLE#2 step comment
Then Calculator returns 5
Then test suddenly failed...
!-- LAST step comment

Scenario: with not implemented step after failing
iven a Calculator
!-- MIDDLE step comment
When I increment 4 with 1
Then Calculator returns 5
!-- LAST step comment
Then test suddenly failed...
When i have NOT IMPLEMENTED step w/o