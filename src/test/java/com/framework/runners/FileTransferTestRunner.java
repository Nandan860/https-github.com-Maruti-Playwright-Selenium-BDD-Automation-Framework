package com.framework.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features/file_transfer.feature",
        glue     = {"com.framework.steps"},
        tags     = "@ui and @filetransfer",
        plugin   = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "json:target/cucumber-reports/filetransfer-results.json",
                "html:target/cucumber-reports/filetransfer-report.html"
        },
        monochrome = true
)
public class FileTransferTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}