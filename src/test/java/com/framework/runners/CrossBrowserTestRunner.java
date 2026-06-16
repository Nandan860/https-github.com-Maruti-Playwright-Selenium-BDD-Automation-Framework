package com.framework.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Cross-Browser Cucumber Runner.
 *
 * TestNG injects browser and engine from testng-crossbrowser.xml at runtime.
 * The same runner class is reused by each <test> block in the XML,
 * so the same scenarios run across Chrome, Firefox, Edge, and WebKit
 * in parallel without duplicating runner code.
 *
 * Tag filter: @ui and @smoke  (override in testng XML per <test> block)
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue     = {"com.framework.steps"},
        tags     = "@ui and @smoke",
        plugin   = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "json:target/cucumber-reports/crossbrowser-results.json",
                "html:target/cucumber-reports/crossbrowser-report.html"
        },
        monochrome  = true,
        dryRun      = false
)
public class CrossBrowserTestRunner extends AbstractTestNGCucumberTests {

    @Parameters({"browser", "engine", "headless"})
    @BeforeClass(alwaysRun = true)
    public void setUpBrowserParams(
            @Optional("chrome")   String browser,
            @Optional("selenium") String engine,
            @Optional("false")    String headless) {

        // Inject into system properties so DriverFactory and ConfigManager pick them up
        System.setProperty("browser",  browser);
        System.setProperty("engine",   engine);
        System.setProperty("headless", headless);
    }

    /**
     * Returning parallel=true makes Cucumber scenarios within this runner
     * run in parallel threads managed by TestNG's thread pool.
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}