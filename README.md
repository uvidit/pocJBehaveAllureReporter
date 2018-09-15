# simpleJUnitPrjct

## to run it just exec:
  ### with removing prev report
    ## rm -rf allure-report && mvn clean package && java -jar ./target/travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar && allure generate --clean allure-results
    
  ### ald resultes will be saved
    ##
    mvn clean package && java -jar ./target/travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar && allure generate allure-results/
  ### with collecting history 
    ## mkdir -p allure-results/history &&  mvn clean package && java -jar ./target/travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar && mvn allure:report && \cp target/allure-report/history/* allure-results/history/
  ### to run it in separate env with java and maven only
    ## mkdir -p allure-results/history &&java -jar travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar && mvn allure:report && \cp target/allure-report/history/* allure-results/history/
       
########################################
## maping jBehave lifecycle to Allure:
 
Story -> Allure Test Suite
Scenario -> Allure Test Case
Step -> Allure Step
Scenario with Examples -> Allure Test Cases
 
## maping METHODS:

S: dryRun             ()                                          SUITE_OPTION0 : SUITE_N/A    >>> EMULATE createing EMPTY SUITE with empty test inside 
S: storyNotAllowed    (Story story, String s)                     SUITE_OPTION1 : SUITE_SKIPED >>> need to emulate mocked skiped empty suite 
S: storyCancelled     (Story story, StoryDuration storyDuration)  SUITE_OPTION2 : SUITE_N/A    >>> need to emulate mocked skiped empty suite 
S:                                                                SUITE_OPTION3 : suite_  <<<<< usual run.......need update SUITE status later
S>T: beforeStory        (Story story, boolean givenStory)                  >>> testCase_suiteSetUp
S>T: afterStory         (boolean givenStory)                               >>> testCase_suiteTearDown
S>T: narrative          (Narrative narrative)                              >>> {testCase_suiteNarrative}
S>T: lifecyle           (Lifecycle lifecycle)                              >>> { %testCasesCycle%}

S>T: scenarioNotAllowed (Scenario scenario, String filter)                     OPTION1: test_SKIPPED (w/o beforeTest or afterTest event)
S>T:                                                                           OPTION2: test_ <<<< usual run.......need update TEST status later
                                                                          
S>T>ST: beforeScenario     (String scenarioTitle)                                   * step_testSetUp       (w/o beforeStep or afterStep event)
S>T>ST: scenarioMeta       (Meta meta)                                               > fill test params    (w/o beforeStep or afterStep event)
S>T>ST: afterScenario      ()                                                       * step_testTearDown    (w/o beforeStep or afterStep event)

????  givenStories       (GivenStories givenStories)                              
????  givenStories       (List<String> storyPaths)

S>T: beforeExamples     (List<String> steps, ExamplesTable examplesTable) 
S>T: example            (Map<String, String> tableRow)
S>T: afterExamples      ()

S>T>ST: beforeStep         (String step)                                                ** pre_Step
S>T>ST: ignorable          (String step)                                                ** postStep        (w/o beforeStep event) --- ON any CommentedString
S>T>ST: successful         (String step)                                                ** postStep_PASSED
S>T>ST: pending            (String step)                                                ** postStep_BROKEN (w/o beforeStep event) --- (ON not implemented or wasn't finded)
S>T>ST: notPerformed       (String step)                                                ** postStep_SKIPED (w/o beforeStep event) --- (ON any fail)
S>T>ST: failed             (String step, Throwable cause)                               ** postStep_FAILED 

S>T>ST: failedOutcomes     (String step, OutcomesTable outcomesTable)
S>T>ST: pendingMethods     (List<String> list)                                      * step_attach_PendingStepsToTestIssuesList

????  restarted          (String step, Throwable throwable)

-----------------------------------------------------------------
private String generateSuiteUid(String suiteName)
private AllureLifecycle getLifecycle()
private TestResult createTestResult(String uuid, Story story, String curentScenario)
private String getHistoryId(Story story, String curentScenario)
private Optional<String> getDisplayName(Story result)
private Map<String, String> getSuites()
private MessageDigest getMessageDigest()
public AllureReporter()
public AllureReporter(AllureLifecycle lifecycle) 

private static final Logger Log = Logger.getLogger("AllureLifecycle.class");

  public static final String MD_5 = "md5";                    // in current impl the value is unique for each story
  public static Story curentStory;
  private final Map<String, String> suites = new HashMap<>(); // list of known stories with generated suiteUid each
  private String suiteUid;
  private String testUid;
  private String stepUid;
  private Status currentSuiteStatus = Status.BROKEN;
  private Status currentTestStatus = Status.BROKEN;
  private Status currentStepStatus = Status.BROKEN;
  private boolean withExamples = false;
  private boolean isTestLevel = false;
  private final AllureLifecycle lifecycle  

########################################
Allurereporter for jBehave (Allure 1)
https://github.com/tapack/allure-jbehave-reporter

http://automation-remarks.com/allure-jbehave-adapter/



########################################
Allure 2

online docs
    https://docs.qameta.io/allure/
blog
    https://qameta.io/blog
    

Gitter rooms (https://gitter.im):
    allure-framework/allure-ru (https://gitter.im/allure-framework/allure-ru)
    allure-framework/allure-core (https://gitter.im/allure-framework/allure-core)
Git
    https://github.com/allure-framework
    https://github.com/allure-framework/allure2
    https://github.com/allure-framework/allure-docs
Allure + JBehave
    baev commented on Jul 9, 2017 (https://github.com/allure-framework/allure2/issues/464)
        just added first draft of jbehave integration https://github.com/allure-framework/allure-java/tree/master/allure-jbehave . Will be available after next allure-java release (ETA next week)
        Feel free to send us pull requests with improvements
        
        https://github.com/allure-framework/allure-java/blob/master/allure-jbehave/src/main/java/io/qameta/allure/jbehave/AllureJbehave.java
         