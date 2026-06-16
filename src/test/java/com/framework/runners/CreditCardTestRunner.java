package com.framework.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features/credit_card_payment.feature",
        glue     = {"com.framework.steps"},
        tags     = "@ui and @payments",
        plugin   = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "json:target/cucumber-reports/creditcard-results.json",
                "html:target/cucumber-reports/creditcard-report.html"
        },
        monochrome = true
)
public class CreditCardTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}