package jbehave.config;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.SilentStepMonitor;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Properties;

import static org.jbehave.core.reporters.Format.*;

public class ConfiguredEmbedder extends Embedder {

    private int _threads;
    private int _timeout;
    private Configuration configuration;

    public ConfiguredEmbedder(int threads, int timeout){
        super();
        this.useMetaFilters(Collections.singletonList("-skip"));
        _threads = threads;
        if(timeout > 0){
            _timeout = (int)Math.round(2*timeout/Math.sqrt(threads));
        }
        else{
            _timeout = 7200;
        }

        // initialise configuration in order to invoke method configuration() a few time
        Class<? extends ConfiguredEmbedder> embedderClass = this.getClass();
        Properties viewResources = new Properties();
        viewResources.put("decorateNonHtml", "true");

        configuration = new MostUsefulConfiguration()
                .usePendingStepStrategy(new FailingUponPendingStep())
                .useStoryLoader(new LoadFromClasspath(embedderClass.getClassLoader()))
                .useStoryReporterBuilder(new StoryReporterBuilder()
                        .withDefaultFormats().withPathResolver(new FilePrintStreamFactory.ResolveToPackagedName())
                        .withViewResources(viewResources)
                        .withFormats(CONSOLE, HTML, HTML_TEMPLATE, XML, STATS)

// chose prefered implementation
//                        .withReporters(new AllureReporter())    // my allure reporter implementation
//                        .withReporters(new AllureJbehave())     // basic reporter from allure team
                        .withReporters(new ABeReporter())     // myBeta

                        .withFailureTrace(true)
                        .withCrossReference(new CrossReference())
                )
                .useStoryControls(new StoryControls().doIgnoreMetaFiltersIfGivenStory(true))
                .useParameterConverters(new ParameterConverters()
                        .addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")))) // use custom date pattern
                .useStepMonitor(new SilentStepMonitor())
                .useStepCollector(new MarkUnmatchedStepsAsPending());

    }

    @Override
    public EmbedderControls embedderControls() {
        return new EmbedderControls()
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(false)
                .useThreads(_threads)
                .useStoryTimeoutInSecs(_timeout)
                .doVerboseFailures(true);
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }
}
