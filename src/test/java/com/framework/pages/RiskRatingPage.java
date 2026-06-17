package com.framework.pages;

import com.framework.driver.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

public class RiskRatingPage extends BasePage {

    private static final By CUSTOMER_ID       = By.id("customer-id");
    private static final By ANNUAL_INCOME     = By.id("annual-income");
    private static final By CREDIT_SCORE      = By.id("credit-score");
    private static final By EMPLOYMENT        = By.id("employment-type");
    private static final By CALCULATE_BTN     = By.id("calculate-rating");
    private static final By RISK_RESULT       = By.id("risk-rating-result");
    private static final By VALIDATION_ERROR  = By.cssSelector(".field-error");

    public void enterCustomerId(String id)       { type(CUSTOMER_ID, id); }
    public void enterAnnualIncome(String income) { type(ANNUAL_INCOME, income); }
    public void enterCreditScore(String score)   { type(CREDIT_SCORE, score); }
    public void calculateRating()                { click(CALCULATE_BTN); }

    public void selectEmployment(String type) {
        new Select(waitForVisible(EMPLOYMENT)).selectByVisibleText(type);
    }

    public String  getRiskRating()       { return getText(RISK_RESULT); }
    public String  getValidationError()  { return getText(VALIDATION_ERROR); }
    public boolean isResultVisible()     { return isDisplayed(RISK_RESULT); }
}
