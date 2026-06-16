package com.framework.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features",
        glue     = {"com.framework.steps"},
        tags     = "@db",
        plugin   = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "json:target/cucumber-reports/db-results.json",
                "html:target/cucumber-reports/db-report.html"
        },
        monochrome = true
)
public class DbTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)   // ETL comparisons are sequential to avoid pool exhaustion
    public Object[][] scenarios() {
        return super.scenarios();
    }
}