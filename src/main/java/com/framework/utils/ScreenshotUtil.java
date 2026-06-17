package com.framework.utils;

import com.framework.driver.DriverFactory;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    private static final Logger log = LogManager.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_DIR = "target/screenshots/";

    private ScreenshotUtil() {}

    /** Captures screenshot and attaches it to Allure report. */
    public static void capture(String label) {
        try {
            byte[] bytes;
            if (DriverFactory.isPlaywright()) {
                bytes = DriverFactory.getPage().screenshot(
                        new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(true));
            } else if (DriverFactory.getDriver() != null) {
                bytes = ((TakesScreenshot) DriverFactory.getDriver())
                        .getScreenshotAs(OutputType.BYTES);
            } else {
                log.warn("No active driver — screenshot skipped.");
                return;
            }

            // Attach to Allure
            Allure.addAttachment(label, new ByteArrayInputStream(bytes));

            // Save to disk
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            Path dir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(dir);
            Files.write(dir.resolve(label + "_" + timestamp + ".png"), bytes);

        } catch (Exception e) {
            log.error("Screenshot failed: {}", e.getMessage());
        }
    }
}
