package jbehave.config;

import io.qameta.allure.*;
import io.qameta.allure.model.*;
import io.qameta.allure.model.Link;
import io.qameta.allure.util.ResultsUtils;
import org.jbehave.core.model.*;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Jbehave Reporter that map Stories/Scenarios/Steps to Allure test reportging model.
 */
// TODO: 11/7/17 need to implement valid SUITE/TEST/STEP status changing in case of existing any IGNORed/PENDed/FAILed/NOT_ALOWed/CANCEled
// TODO: 11/7/17 need to make calling from java code of: 1) test-results/test-reports dirs cleaning 2) allure report-generating
// TODO: 11/7/17 need make multithread version by remembering not only carrent step/test/suit uid and data, but generating catalog structure with uid and adding/updating test run info/stat inside using uid

public class AllureReporter implements StoryReporter {

  private static final Logger Log = LoggerFactory.getLogger(AllureLifecycle.class);

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
  private final AllureLifecycle lifecycle;

  public AllureReporter() {
    this(Allure.getLifecycle());
    suiteUid = generateSuiteUid("DEFAULT_EMPTY_SUITE_NAME");
    testUid = UUID.randomUUID().toString();
    stepUid = UUID.randomUUID().toString();
  }

  public AllureReporter(AllureLifecycle lifecycle) {
    this.lifecycle = lifecycle;

  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void storyNotAllowed(Story story, String s) {
    Log.warn("***SSS* storyNotAllowed");
    Log.warn("** storyNotAllowed (STORY) -> " + story.toString());
    Log.warn("** storyNotAllowed (SFilter) -> " + s);
  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void storyCancelled(Story story, StoryDuration storyDuration) {
    Log.warn("***SSS* storyCancelled");
    Log.warn("** storyCancelled (STORY) -> " + story.toString());
  }

  // TODO: 11/7/17 why we need this param : givenStory ???
  @Override
  public void beforeStory(Story story, boolean givenStory) {
//      Log.warn("***SSS* beforeStory");
//      Log.warn("** beforeStory (STORY) -> " + story);
//      Log.warn("** beforeStory (givenStory) -> " + givenStory);
    currentSuiteStatus = Status.BROKEN; // in any unknown issue status should be as Broken

    if (!givenStory) {
      suiteUid = generateSuiteUid(story.getName());
      curentStory = story;
      getLifecycle().startTestContainer(
          new TestResultContainer().withUuid(suiteUid)
              .withName(story.getName())
              .withDescription(story.getDescription().toString())
              .withLinks(new Link().withUrl(story.getPath()))
      );
    }
  }

  @Override
  public void afterStory(boolean givenStory) {
//      Log.warn("***SSS* afterStory");
//      Log.warn("** afterStory (givenStory) " + givenStory);
    getLifecycle().stopTestContainer(suiteUid);
  }

  // TODO: 11/7/17  need to add some stat notifying in TIERED DOWN CASE if any calls did
  //it happens after beforeStory and before lifecyle
  @Override
  public void narrative(Narrative narrative) {
//      Log.warn("**** narrative (Narrative) -> " + narrative);
  }

  //it happens after beforeStory()->narrative() and launch all scenarios inside
  @Override
  public void lifecyle(Lifecycle lifecycle) {
//      Log.warn("***LLL* (lifecycle)" + lifecycle);
  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void scenarioNotAllowed(Scenario scenario, String filter) {
    Log.warn("***TTT* scenarioNotAllowed");
    Log.warn("***TTT* scenarioNotAllowed (scenario) -> " + scenario);
    Log.warn("***TTT* scenarioNotAllowed (filter) -> " + filter);
  }


  @Override
  public void beforeScenario(String scenarioTitle) {
//      Log.warn("***TTT* beforeScenario" );
    currentTestStatus = Status.BROKEN; // in any unknown issue status should be as Broken
    testUid = UUID.randomUUID().toString();
    TestResult result = this.createTestResult(testUid, curentStory, scenarioTitle);
    this.getLifecycle().scheduleTestCase(result);
    this.getLifecycle().startTestCase(testUid);
  }

  // TODO: 11/7/17 need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void scenarioMeta(Meta meta) {
//    Log.warn("***TTT* scenarioMeta (meta) ->" + meta);
  }

  @Override
  public void afterScenario() {
//      Log.warn("***TTT* afterScenario (noParam)");
    this.getLifecycle().updateTestCase(testUid, (testResult) -> {
      if (Objects.isNull(testResult.getStatus())) {
        testResult.setStatus(currentTestStatus);
      }
    });
    this.getLifecycle().stopTestCase(testUid);
    try {
      this.getLifecycle().writeTestCase(testUid);
    } catch (Exception e) {
      Log.error("@@@@ " + e.getMessage() + " **** " + e.getStackTrace());
    }
  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void givenStories(GivenStories givenStories) {
    Log.warn("***???* givenStories 1 ");
    Log.warn("*** givenStories 1 (givenStories) ->" + givenStories);
  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void givenStories(List<String> storyPaths) {
    Log.warn("***???* givenStories 2");
    Log.warn("*** givenStories 2 (storyPaths) ->" + storyPaths);
  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void beforeExamples(List<String> steps, ExamplesTable examplesTable) {
    Log.warn("**** beforeExamples");
  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void example(Map<String, String> tableRow) {
    Log.warn("**** example");
  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void afterExamples() {
    Log.warn("**** afterExamples");
  }


  @Override
  public void beforeStep(String step) {
//      Log.warn("**** beforeStep");
    stepUid = UUID.randomUUID().toString();
    getLifecycle().startStep(stepUid
        , (new StepResult()).withName(step).withStatus(Status.BROKEN));
  }

  @Override
  public void successful(String step) {
//      Log.warn("**** successful");
    getLifecycle().updateStep(stepUid, stepResult -> {
      stepResult.withStatus(Status.PASSED);
    });
    getLifecycle().stopStep();
  }

  // usually it happens on comments and commented steps (without beforeStep calling)
  @Override
  public void ignorable(String step) {
//      Log.warn("**** ignorable");
    getLifecycle().updateStep(stepUid, (stepResult) -> {
      stepResult.withStatus(Status.SKIPPED).withStatusDetails(
          new StatusDetails().withMessage("IGNORED (step: " + step + "."));
    });
  }

  // usually it happens on undefined steps (without beforeStep calling)
  @Override
  public void pending(String step) {
//      Log.warn("**** pending");
    getLifecycle().updateStep(stepUid, (stepResult) -> {
      stepResult.withStatus(Status.BROKEN).withStatusDetails(
          new StatusDetails().withMessage("PENDING (step: " + step + "."));
    });
    currentTestStatus = Status.BROKEN;
  }

  // usually it happens on steps din't performed after any fail happens in current case (without beforeStep calling)
  @Override
  public void notPerformed(String step) {
//      Log.warn("**** notPerformed");
    getLifecycle().updateStep(stepUid, (stepResult) -> {
      stepResult.withStatus(Status.SKIPPED).withStatusDetails(
          new StatusDetails().withMessage("NOT PERFORMED (step: " + step + "."));
    });
  }

  @Override
  public void failed(String step, Throwable cause) {
//      Log.warn("**** failed");

    getLifecycle().addAttachment("postScenarioAttachment", "text/plain", "txt",
        "Here we could attach any post scenario text...".getBytes());

    getLifecycle().updateStep(stepUid, stepResult -> {
      stepResult.withStatus(Status.FAILED);
    });
    getLifecycle().stopStep(stepUid);
  }


  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void failedOutcomes(String step, OutcomesTable outcomesTable) {
    Log.warn("**** failedOutcomes");

  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void restarted(String step, Throwable throwable) {
    Log.warn("**** restarted");

  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void dryRun() {
    Log.warn("**** dryRun");

  }

  // TODO: 11/7/17 unknown used method - need to add some stat notifying in TIERED DOWN CASE if any calls did
  @Override
  public void pendingMethods(List<String> list) {
    Log.warn("**** pendingMethods");
  }

  private String generateSuiteUid(String suiteName) {
    String uid = UUID.randomUUID().toString();
    synchronized (getSuites()) {
      getSuites().put(suiteName, uid);
    }
    return uid;
  }

  //allure logger instance
  private AllureLifecycle getLifecycle() {
    return lifecycle;
  }


  private TestResult createTestResult(String uuid, Story story, String curentScenario) {
    String storyName = story.getName();
    String path = story.getPath();
    String name = Objects.nonNull(path) ? path : storyName;
    String fullName = Objects.nonNull(storyName) ? String.format("%s.%s", storyName, path) : storyName;
    TestResult testResult = (new TestResult()).withUuid(uuid)
        .withHistoryId(this.getHistoryId(story, curentScenario))
        .withStatus(Status.BROKEN)
        .withName(name).withFullName(fullName)
//          .withLinks(this.getLinks(description))
        .withLabels(new Label[]{
            (new Label()).withName("host").withValue(ResultsUtils.getHostName())
            , (new Label()).withName("thread").withValue(ResultsUtils.getThreadName())
            , (new Label()).withName("suite").withValue(storyName)
            , (new Label()).withName("SCENARIO").withValue("curentScenario")
//              , (new Label()).withName("package").withValue("Put PACK name here")
//              , (new Label()).withName("testClass").withValue("Put CLASS name here")
//              , (new Label()).withName("testMethod").withValue(name)
//            , (new Label()).withName("tag").withValue("Put TAG name here")
        });
//    testResult.getLabels().addAll(this.getLabels(story));
    this.getDisplayName(story).ifPresent(testResult::setName);
    return testResult;
  }

  //todo: metthods bellow need to refactor - it's a legacy code from junit adaptor
  private String getHistoryId(Story story, String curentScenario) {
    return this.md5(story.getPath() + story.getName() + curentScenario);
  }

  private Optional<String> getDisplayName(Story result) {
    return Optional.ofNullable(result.getName());
  }

  private Map<String, String> getSuites() {
    return suites;
  }

  private String md5(String source) {
    byte[] bytes = this.getMessageDigest().digest(source.getBytes(StandardCharsets.UTF_8));
    return (new BigInteger(1, bytes)).toString(16);
  }

  private MessageDigest getMessageDigest() {
    try {
      return MessageDigest.getInstance("md5");
    } catch (NoSuchAlgorithmException var2) {
      throw new IllegalStateException("Could not find md5 hashing algorithm", var2);
    }
  }

}
