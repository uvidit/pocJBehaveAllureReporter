Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: scenario description (mock2)
Meta: @owner LazyOwner
@severity trivial

Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

Examples:
| exNumber |
| 1        |
| 2        |

Scenario: SKIPED scenario description
Meta:
 @skip
 @Severity BLOCKER

Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

Scenario: 'all' types of lables scenario
Meta:
     @link AllureBlog|https://qameta.io
     @tms allureBinTray|https://bintray.com/search/packages/tag?query=allure#
     @issue postNewIssue|https://github.com/allure-framework/allure2/issues/new

     @epic EverBestEpic
     @feature Incredible Feature
     @story TinyStoryAbout

     @owner GreedyOwner

     @package JBehaveToAllurePack
     @testclass SomeUnknownTestClassName
     @testmethod SomeUknownToTestMethod

     @severity Trivial
Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

Scenario: 'ALL' types of lables scenario
Meta:
     @LINK ALLUREBLOG|HTTPS://QAMETA.IO
     @TMS ALLUREBINTRAY|HTTPS://BINTRAY.COM/SEARCH/PACKAGES/TAG?QUERY=ALLURE#
     @ISSUE POSTNEWISSUE|HTTPS://GITHUB.COM/ALLURE-FRAMEWORK/ALLURE2/ISSUES/NEW

     @EPIC EVERBESTEPIC
     @FEATURE INCREDIBLE FEATURE
     @STORY TINYSTORYABOUT

     @OWNER GREEDYOWNER

     @PACKAGE JBEHAVETOALLUREPACK
     @TESTCLASS SOMEUNKNOWNTESTCLASSNAME
     @TESTMETHOD SOMEUKNOWNTOTESTMETHOD

     @SEVERITY TRIVIAL
Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

Scenario: 'All' types of lables scenario
Meta:
     @Link AllureBlog|https://qameta.io
     @Tms allureBinTray|https://bintray.com/search/packages/tag?query=allure#
     @Issue postNewIssue|https://github.com/allure-framework/allure2/issues/new

     @Epic EverBestEpic
     @Feature Incredible Feature
     @Story TinyStoryAbout

     @Owner GreedyOwner

     @Package JBehaveToAllurePack
     @TestClass SomeUnknownTestClassName
     @TestMethod SomeUknownToTestMethod

     @Severity Trivial
Given a Calculator
When I increment 4 with 1
Then Calculator returns 5

