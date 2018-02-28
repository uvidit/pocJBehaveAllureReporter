Some Story Name for 'simpleTestMock.story'.
Meta:
 @storyLabel_withNoVal
 @storyLabel_withVal storyLabelVal

Narrative:
As a user
I want to perform an action MOCK
So that I can achieve a business goal

Scenario: 1. scenario with EXAMPLES instances
Meta: @tc1Label_withNoVal
 @tc1Label_withVal tc1LabelVal

Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

Examples:
| exNumber | exVal |
| 1        | val1  |
| 2        | val2  |
| 3        | val3  |

Scenario: 2. scenario w/o EXAMPLES instances
Meta: @tc2Label_withNoVal
 @tc2Label_withVal storyLabelVal

Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

