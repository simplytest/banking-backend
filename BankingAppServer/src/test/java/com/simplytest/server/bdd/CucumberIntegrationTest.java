package com.simplytest.server.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("com.simplytest.server.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,junit:target/cucumber-reports/CucumberIntegrationTest.xml,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.simplytest.server.bdd")
public class CucumberIntegrationTest
{
}
