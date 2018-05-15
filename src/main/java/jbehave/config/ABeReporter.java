package jbehave.config;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.*;
import io.qameta.allure.util.ResultsUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.*;
import org.jbehave.core.reporters.StoryReporter;

import javax.xml.bind.DatatypeConverter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ABeReporter implements StoryReporter{

/* ################################################
   # maping jBehave lifecycle to Allure:

  | JBEHAVE                | ALLURE              |
  | Story                  | TestSuite           |
  | Scenario               | TestCase            |
  | Scenario with Examples | TestCases           |
  | Step                   | Step                |

  | story:Narrative        | TestCase:Description (htmlDescription) |

*/

/* DISCLIMER's. .... yes, there're more than one %) TODO: check all these staff done

  * if story contains no scenario - report contains no suites and no tests (because suite is only aggregating entity)
  * if suite was canceled/ignored - report contains no suites and no tests (because suite is only aggregating entity)
  * some labling or grouping functionality will be maped using Meta tags in jBehave. It will be a list of keyword tags
  * for global issues loging will create an special suite and test or even tearDown STEP, and will collect all issues, wornings or not implemented or not processed staff to it
  * scenarios with examples will be transformed to separate tascases
  * // adapter will convert predefined @-tags to allure labels/parameters/links/attributes:
    // @link urlName|urlValue
    // @issue urlName|urlValue
    // @tms urlName|urlValue

    // @Severity {BLOCKER("blocker")|CRITICAL("critical")|NORMAL("normal")|MINOR("minor")|TRIVIAL("trivial")}

  //            // BEHAVIOURS-tab tree grouping:
  //            , ResultsUtils.createEpicLabel(">>> EPIC Label <<<<")
  //            , ResultsUtils.createFeatureLabel(">>> FEATURE Label <<<<")
  //            , ResultsUtils.createStoryLabel(">>> STORY Label <<<<")
  //            // PACKAGES-tav tree grouping
  //            , (new Label()).withName("package").withValue(">>> PACKAGE Label <<<<") //ok
  ////            , (new Label()).withName("testClass).withValue(">>> CLASS Label <<<<") // not working (( TODO: WHY? ask in GITTER!
  //            , (new Label()).withName("testMethod").withValue(">>> testMethod Label <<<<") //ok
  //            // TEST tab properties
  //            , ResultsUtils.createOwnerLabel(">>> OWNER Label <<<<")
  //            , ResultsUtils.createSeverityLabel(SeverityLevel.CRITICAL)

  //        .withRerunOf() //TODO: not works :(
 */

//  private static final Logger LOGGER = Logger.getLogger(AllureLifecycle.class.getName());

  private static final String MD_5 = "md5";
  private static final String EXAMPLE_TITLE_ENDING = "EXAMPLE #";

  private final AllureLifecycle lifecycle;
  private final ThreadLocal<Story>  stories = new InheritableThreadLocal<>();
  private final ThreadLocal<String> scenarios
      = InheritableThreadLocal.withInitial(() -> UUID.randomUUID().toString());
  private final Map<String, Status> scenarioStatusStorage = new ConcurrentHashMap<>();

  private Integer exampleInstanceLaunchCounter = 1; // TODO: do i need something thread safe here?
  private String currentScenarioTitle = "";         // TODO: do i need something thread safe here?

  public ABeReporter() {
    this(Allure.getLifecycle());
  }
  public ABeReporter(final AllureLifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  // # Story management level
  @Override
  public void storyNotAllowed(Story story, String s) {
  //TODO: need add logging about
    //except logging no need to do anything here due to allure show suite only if there's any test inside
  }

  @Override
  public void storyCancelled(Story story, StoryDuration storyDuration) {
  //TODO: need add logging about
    //except logging no need to do anything here due to allure show suite only if there's any test inside
  }

  @Override
  public void beforeStory(final Story story, final boolean givenStory) {
    stories.set(story);
  }

  @Override
  public void afterStory(final boolean givenStory) {
    stories.remove();
  }

  // # Story management level
  @Override
  public void narrative(Narrative narrative) {
   Story story = stories.get();
   story.getNarrative();
  }

  @Override
  public void lifecyle(Lifecycle lifecycle) {

  }

  // # Scenario management level
  @Override
  public void scenarioNotAllowed(Scenario scenario, String filter) {
    currentScenarioTitle = scenario.getTitle();
    startNewAllureTestCase(currentScenarioTitle);
    // TODO: need update all data manually, because JBehave will do nothing for this scenario so allure get nothing in common way ((
    scenarioMeta(scenario.getMeta());
    getLifecycle().updateTestCase(scenarios.get(),
        testResult -> testResult.withStatusDetails(new StatusDetails()
            .withMessage("Test was ignored by jBehave engine. Root cause is existing '@skip' metatag or story-filter excluding parameter.")
            .withTrace("No trace here.")));
    finalizeAllureTestCaseWithStatusByUuid(scenarios.get(), Status.SKIPPED);
  }

  @Override
  public void beforeScenario(String scenarioTitle) {
    currentScenarioTitle = scenarioTitle;
    startNewAllureTestCase(scenarioTitle);
  }

  @Override
  public void afterScenario() {
    finalizeCurrentAllureTestCase();
  }

  @Override
  public void scenarioMeta(Meta meta) {
    // all Meta-data will be attached to testResult artifact inside allureTestResult creation method
    // so do nothing here %)
  }

  @Override
  public void givenStories(GivenStories givenStories) {
    //TODO: ?????
  }

  @Override
  public void givenStories(List<String> list) {
    //TODO: ?????
  }

  @Override
  public void beforeExamples(List<String> list, ExamplesTable examplesTable) {
    exampleInstanceLaunchCounter = 0;
  }

  @Override
  public void example(Map<String, String> map) {
    // only the first allure-testCase-artifact already created on scenario started event
    //   so we do nothing for the first example instance, but
    //   should finalize previous and start create new allure-test-case artifact for all next examples instances
    if (exampleInstanceLaunchCounter > 0) {
      finalizeCurrentAllureTestCase();
      startNewAllureTestCase(currentScenarioTitle);
    }
    // update allureTestCaseArtifact with sufix 'EXAMPLE #n' -
    //   that means we need update all scenario name related properties (actually name, fullNme, and calculated historyId)
    exampleInstanceLaunchCounter++; //
    String currentUuid = scenarios.get();

    List<Parameter> parameterList = new ArrayList<>();
    map.keySet().forEach(key -> parameterList.add(new Parameter().withName(key).withValue(map.get(key))));

    getLifecycle().updateTestCase(currentUuid, testResult -> {
      parameterList.addAll(testResult.getParameters());

      testResult.withName(String.format("%s (%s%d)", currentScenarioTitle, EXAMPLE_TITLE_ENDING, exampleInstanceLaunchCounter ))
        .withFullName(String.format("%s (%s%d)", testResult.getFullName(), EXAMPLE_TITLE_ENDING, exampleInstanceLaunchCounter ))
        .withHistoryId(testResult.getFullName())
        .withParameters(parameterList)
        .withStatusDetails(new StatusDetails().withMessage("Unknown test status.").withTrace("No trace yet.")
        );
      }
    );
  }

  @Override
  public void afterExamples() {
    exampleInstanceLaunchCounter = 0;
  }

  @Override
  public void beforeStep(final String step) {
    final String stepUuid = UUID.randomUUID().toString();
    getLifecycle().startStep(stepUuid, new StepResult().withName(step));
  }

  @Override
  public void successful(final String stepName) {
    getLifecycle().updateStep(result -> result.withStatus(Status.PASSED));
    getLifecycle().stopStep();

    getLifecycle().updateTestCase(scenarios.get(),
        testResult -> {testResult.withStatusDetails(new StatusDetails()
            .withMessage("Test passed."));});

    updateScenarioStatus(Status.PASSED);
  }

  //JBehave provide all commented strings inside story file as ignored steps
  @Override
  public void ignorable(final String stepName) {
    beforeStep(stepName); // jBehave doesn't need it, but Allure does
    getLifecycle().updateStep(result -> result.withStatus(Status.SKIPPED));
    getLifecycle().stopStep();
    updateScenarioStatus(Status.SKIPPED);
  }

  // usually it happens on undefined steps (without beforeStep calling)
  @Override
  public void pending(final String stepName) {
    beforeStep(stepName); // jBehave doesn't need it, but Allure does
    getLifecycle().updateStep(result -> result
        .withStatus(Status.BROKEN)
        .withName(String.format("PENDING (not implemented): %s", stepName)));
    getLifecycle().stopStep();
    updateScenarioStatus(Status.BROKEN);
  }

  // usually it happens on steps din't performed after any fail happens in current case (without beforeStep calling)
  @Override
  public void notPerformed(String stepName) {
    beforeStep(stepName); // jBehave doesn't need it, but Allure does

    getLifecycle().updateStep(result -> result.withStatus(Status.SKIPPED)
        .withName(String.format("NOT PERFORMED: %s", stepName)));
    getLifecycle().stopStep();
    updateScenarioStatus(Status.SKIPPED);
  }

  @Override
  public void failed(final String step, final Throwable cause) {
    getLifecycle().updateStep( result -> result.withStatus(Status.FAILED));
    getLifecycle().stopStep();
    getLifecycle().updateTestCase(scenarios.get(),
        testResult ->  {
          StringWriter traceStringWriter = new StringWriter();
          cause.printStackTrace(new PrintWriter(traceStringWriter));

          testResult.withStatusDetails(new StatusDetails()
            .withMessage(cause.getCause().toString())
            .withTrace(traceStringWriter.toString())
          );
        });
    updateScenarioStatus(Status.FAILED);
  }

  @Override
  public void failedOutcomes(String s, OutcomesTable outcomesTable) {
    //TODO: ????????
  }

  @Override
  public void restarted(String s, Throwable throwable) {
    //TODO: ????????
  }

  @Override
  public void dryRun() {
    //TODO: ????????
  }

  @Override
  public void pendingMethods(List<String> list) {
    //TODO: ????????
  }

  //////////////////////////////////////////////////////////////////////////////
  // reporter staff methods
  public AllureLifecycle getLifecycle() {
    return lifecycle;
  }

  // update scenario status if it became less then current
  protected void updateScenarioStatus(final Status passed) {
    final String scenarioUuid = scenarios.get();
    max(scenarioStatusStorage.get(scenarioUuid), passed)
        .ifPresent(status -> scenarioStatusStorage.put(scenarioUuid, status));
//    getLifecycle().getCurrentTestCase().ifPresent();
  }

  // Status comparator (from max to min there are statuses : FAILED/BROKEN/PASSED/SKIPPED)
  private Optional<Status> max(final Status first, final Status second) {
    return Stream.of(first, second)
        .filter(Objects::nonNull)
        .min(Status::compareTo);
  }

  //finalizing current allure-report-test-case-artifact started before
  private void finalizeCurrentAllureTestCase(){
    final String uuid = scenarios.get();
    finalizeAllureTestCaseByUuid(uuid);
  }


  private void finalizeAllureTestCaseByUuid(String uuid){
    final Status status = scenarioStatusStorage.getOrDefault(uuid, Status.PASSED);

    getLifecycle().updateTestCase(uuid, testResult -> testResult.withStatus(status));
    getLifecycle().stopTestCase(uuid);
    getLifecycle().writeTestCase(uuid);
    scenarios.remove();
  }

  // be careful!! this method rewrite test case status ignoring the previous one
  private void finalizeAllureTestCaseWithStatusByUuid(String uuid, Status status){
    getLifecycle().updateTestCase(uuid, testResult -> testResult.withStatus(status));
    getLifecycle().stopTestCase(uuid);
    getLifecycle().writeTestCase(uuid);
    scenarios.remove();
  }

  //start new allure-report-test-case-artifact started before
  private void startNewAllureTestCase(String scenarioTitle){
    final Story story = stories.get();
    final String uuid = scenarios.get();
    final String fullName = String.format("%s: %s", story.getName(), scenarioTitle);

    final TestResult result = new TestResult()
        .withUuid(uuid)
        .withStage(Stage.SCHEDULED)
        .withHistoryId(md5(fullName)) // historyId used to group on TEST-RETRIES
        // TEST-tab properties
        .withName(scenarioTitle)
        .withFullName(fullName)
        .withStatusDetails(new StatusDetails().withMessage("Unknown test status.").withTrace("No trace yet."))
        // TODO: possible solution - get info from tags or ...
        // TODO: this labels/tags/links need to be moved to separate methods and applied not hard coded data
        .withParameters( new Parameter().withName("someParamName").withValue("someParamValue"))

        //jBehave narative to allure test description
                // commented simple description because: we all know - description and html is much better than description only %)
                // .withDescription(story.getNarrative().asString(new Keywords())) // simple text description
        .withDescriptionHtml(story.getNarrative().asString(new Keywords())
            .replaceAll("^\n|\n$", "") // don't like leading and ending line breaks
            .replaceAll("\n|\r|\r\n", "<br>")) // tada!! it's html now! %)

        .withLabels(
            ResultsUtils.createHostLabel()
            , ResultsUtils.createThreadLabel()

            // SUITES-tab tree grouping:
            , (new Label()).withName("suite").withValue(story.getName()) // auch! a little hardcode here
        );
                                // update with meta tags
                                //TODO: refactor this code
                                // need update all Meta for each examples with prev example instance (overvise meta data will be lost for the next examples)
                                // in other words we need to copy manually all existed metaTags from example to example
                                //// TAGS
                                List<Meta> metaList = new ArrayList<>();
                                stories.get().getScenarios().stream().filter(scenario ->
                                    scenario.getTitle().equals(scenarioTitle)).forEach(findedScenario ->
                                    metaList.add(findedScenario.getMeta()));

        result.withLabels(addTagLabelsToTestResultLabels( convertJBehaveMetaListToAllureLabels(metaList), result ));
        Meta scenarioMeta = metaList.get(0);

    // collect all meta from jBehave
    Map<String,String> metaMap = new HashMap<>();
    scenarioMeta.getPropertyNames().stream().forEach(key -> metaMap.put(key, scenarioMeta.getProperty(key)));
    //// LABELS
    //convert collected Meta data to Allure test result Labels
    List<Label> labels = new ArrayList<>();
    metaMap.entrySet().stream().forEach(meta -> labels.add(entrySetToLabel(meta)));
    //test result updated with Labels
    result.withLabels(addTagLabelsToTestResultLabels( labels, result));
    //// LINKS
    //convert collected Mets data to Links
    List<Link> links = new ArrayList<>();
    metaMap.entrySet().stream().forEach(meta -> links.add(entrySetToLink(meta))); // there's an issue : not parsed links will be null , so we need to filter them later,....but it's a fast crunch solving way
    result.withLinks(links.stream().filter(link -> link.getName() != null).collect(Collectors.toList()));

    getLifecycle().scheduleTestCase(result);
    getLifecycle().startTestCase(result.getUuid());
  }

  private Link entrySetToLink(Map.Entry<String,String> meta){
    Link link = new Link();
    switch (meta.getKey().toUpperCase()){
        case "LINK":
          try {
            link = new Link()
                .withName(meta.getValue().trim().split("\\|")[0])
                .withUrl(meta.getValue().trim().split("\\|")[1]);
          } catch (Exception e){
            //do nothing with parse exception issue
          }
          break;
        case "ISSUE":
          try {
            link = ResultsUtils.createIssueLink(meta.getKey())
                .withName(meta.getValue().trim().split("\\|")[0])
                .withUrl(meta.getValue().trim().split("\\|")[1]);
          } catch (Exception e){
            //do nothing with parse exception issue
          }
          break;
        case "BUG":
          try {
            link = ResultsUtils.createIssueLink(meta.getKey())
                .withName(meta.getValue().trim().split("\\|")[0])
                .withUrl(meta.getValue().trim().split("\\|")[1]);
          } catch (Exception e){
            //do nothing with parse exception issue
          }
          break;
        case "TMS":
          try {
            link = ResultsUtils.createTmsLink(meta.getKey())
                .withName(meta.getValue().trim().split("\\|")[0])
                .withUrl(meta.getValue().trim().split("\\|")[1]);
          } catch (Exception e){
            //do nothing with parse exception issue
          }
          break;
        default:
          //link = (new Label()).withName(meta.getKey()).withValue(meta.getValue().trim());
      }
      return link;
  }

  private Label entrySetToLabel(Map.Entry<String,String> meta){
    Label label = new Label();
    switch (meta.getKey().toUpperCase()){
        case "EPIC":
          label = ResultsUtils.createEpicLabel(meta.getValue());
          break;
        case "FEATURE":
          label = ResultsUtils.createFeatureLabel(meta.getValue());
          break;
        case "STORY":
          label = ResultsUtils.createStoryLabel(meta.getValue());
          break;
        case "OWNER":
          label = ResultsUtils.createOwnerLabel(meta.getValue());
          break;
        case "PACKAGE":
          label = (new Label()).withName("package").withValue(meta.getValue());
          break;
        case "TESTCLASS": // not working ((
          label = (new Label()).withName("testClass").withValue(meta.getValue());
          break;
        case "TESTMETHOD":
          label = (new Label()).withName("testMethod").withValue(meta.getValue());
          break;
        case "SEVERITY":
          label = ResultsUtils.createSeverityLabel(meta.getValue().toLowerCase().trim());
          break;
        default:
          label = (new Label()).withName(meta.getKey()).withValue(meta.getValue().trim());
      }
      return label;
  }

  private List<Label> convertJBehaveMetaListToAllureLabels(List<Meta> listOfMeta){
    // be sure curent method process only first list item and ignore all other // TODO: need to refactor w/o 'get(0)' because metaList contains only one item of Meta tag set
    List<Label> metaTagList = new ArrayList<>();
    Meta singleMetaSet = listOfMeta.get(0);
    singleMetaSet.getPropertyNames().forEach(item ->
        metaTagList.add(ResultsUtils.createTagLabel(String.format("%s='%s'", item, singleMetaSet.getProperty(item))))
    );
    return metaTagList;
  }

  private List<Label>  addTagLabelsToTestResultLabels(List<Label> listOfLabels, TestResult result){

    List<Label> originLabels = result.getLabels();
    originLabels.addAll(listOfLabels);
    return originLabels;
  }

  //////////////////////////////////////////////////////////////////////////////
  // util methods

  private String md5(final String string) {
    return DatatypeConverter.printHexBinary(getMessageDigest()
        .digest(string.getBytes(StandardCharsets.UTF_8))
    );
  }
  /* get needed for md5 calculating */

  private MessageDigest getMessageDigest() {
    try {
      return MessageDigest.getInstance(MD_5);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Could not find md5 hashing algorithm", e);
    }
  }
}
