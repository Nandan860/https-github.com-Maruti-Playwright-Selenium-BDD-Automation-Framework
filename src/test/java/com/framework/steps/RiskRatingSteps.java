package com.framework.steps;

import com.framework.pages.RiskRatingPage;
import io.cucumber.java.en.*;

import static org.assertj.core.api.Assertions.assertThat;

public class RiskRatingSteps {

    private final RiskRatingPage page = new RiskRatingPage();

    @When("the user enters customer id {string}, income {string}, score {string}, employment {string}")
    public void enterRiskData(String id, String income, String score, String employment) {
        page.enterCustomerId(id);
        page.enterAnnualIncome(income);
        page.enterCreditScore(score);
        page.selectEmployment(employment);
        page.calculateRating();
    }

    @Then("the risk rating should be {string}")
    public void verifyRiskRating(String expected) {
        assertThat(page.getRiskRating())
                .as("Risk rating result")
                .isEqualToIgnoringCase(expected);
    }
}