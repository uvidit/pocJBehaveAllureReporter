Scenario: 'severity' lable
Meta:
  @severity trivial
Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

Scenario: 'SEVERITY' lable
Meta:
  @SEVERITY TRIVIAL
Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

Scenario: 'Severity' lable
Meta:
  @Severity Trivial
Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

Scenario: 'sEVERITY' lable
Meta:
  @sEVERITY tRIVIAL
Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

