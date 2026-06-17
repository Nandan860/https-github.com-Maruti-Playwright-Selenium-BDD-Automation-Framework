package com.framework.pages;

import com.framework.driver.BasePage;
import org.openqa.selenium.By;

public class CreditCardPage extends BasePage {

    private static final By CARD_NUMBER      = By.id("card-number");
    private static final By AMOUNT           = By.id("amount");
    private static final By PAYEE            = By.id("payee");
    private static final By SUBMIT_BTN       = By.id("submit-payment");
    private static final By CONFIRMATION     = By.cssSelector(".confirmation-banner");
    private static final By TXN_REF          = By.id("txn-reference");
    private static final By PAYMENT_ERROR    = By.cssSelector(".payment-error-message");
    private static final By FORM_VALIDATION  = By.cssSelector(".field-validation-error");

    public void enterCardNumber(String number)  { type(CARD_NUMBER, number); }
    public void enterAmount(String amount)      { type(AMOUNT, amount); }
    public void enterPayee(String payee)        { type(PAYEE, payee); }
    public void submitPayment()                 { click(SUBMIT_BTN); }

    public boolean isConfirmationVisible()      { return isDisplayed(CONFIRMATION); }
    public boolean isPaymentErrorVisible()      { return isDisplayed(PAYMENT_ERROR); }
    public String  getTransactionReference()    { return getText(TXN_REF); }
    public String  getPaymentErrorText()        { return getText(PAYMENT_ERROR); }
    public String  getFormValidationError()     { return getText(FORM_VALIDATION); }
}
