package com.framework.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features/broken_links.feature",
        glue     = {"com.framework.steps"},
        tags     = "@ui and @brokenlinks",
        plugin   = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "json:target/cucumber-reports/brokenlinks-results.json",
                "html:target/cucumber-reports/brokenlinks-report.html"
        },
        monochrome = true
)
public class BrokenLinksTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)   // broken link scans are sequential — one page at a time
    public Object[][] scenarios() {
        return super.scenarios();
    }
}