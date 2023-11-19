//import io.cucumber.junit.platform.engine.Cucumber;
//import org.junit.platform.suite.api.*;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

//@Suite
//@SelectClasspathResource("features")
//@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "stepDefinitions")
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features")
public class RunnerUnitTests {
}
