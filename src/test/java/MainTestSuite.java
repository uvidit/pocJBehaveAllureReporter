import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class MainTestSuite {

  @Test
  public void simpleEmptyTest(){
// empty simple test body
  }

  @Test
  public void simpleHappyTest(){
    Assert.assertTrue("happy test", true);
  }

  @Test
  @Ignore
  public void simpleIgnoredTest(){
    Assert.assertTrue("happy test", true);
  }

  @Test
  public void simpleFailedTest(){
    Assert.assertTrue("always failed test", false);
  }

}
