package jbehave.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.Steps;

import java.util.logging.Logger;

public class BasicSteps extends Steps {
  private static final Logger LOG = Logger.getLogger("basicSteps");

  @Given("a Calculator")
  public void givenACalculator() {
    // PENDING todo: need any impl here
    LOG.info("a CALCULATOR instance has been initiated...");

  }

  @When("I increment 4 with 1")
  public void whenIIncrement4With1() {
    // PENDING  todo: need any impl here
    LOG.info(" 4 incremented with 1....");
  }

  @Then("Calculator returns 5")
  public void thenCalculatorReturns5() {
    // PENDING  todo: need any impl here
    LOG.info(" ... and got 5.");
  }

}
