package com.framework.steps;

import com.framework.driver.DriverFactory;
import com.framework.utils.ScreenshotUtil;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);

    @Before
    public void setUp(Scenario scenario) {
        log.info("▶ Starting: {}", scenario.getName());
        // Only init driver for UI tests (tagged @ui)
        if (scenario.getSourceTagNames().contains("@ui")) {
            DriverFactory.init();
        }
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed() && DriverFactory.getDriver() != null) {
            ScreenshotUtil.capture("FAILED_" + scenario.getName().replaceAll("\\s+", "_"));
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        log.info("{} ◀ {}", scenario.isFailed() ? "✗" : "✓", scenario.getName());
        DriverFactory.quit();
    }
}