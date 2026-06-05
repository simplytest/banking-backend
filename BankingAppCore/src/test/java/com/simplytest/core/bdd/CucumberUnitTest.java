package com.simplytest.core.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("com.simplytest.core.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,junit:target/cucumber-reports/CucumberUnitTest.xml")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.simplytest.core.bdd")
public class CucumberUnitTest
{
}
