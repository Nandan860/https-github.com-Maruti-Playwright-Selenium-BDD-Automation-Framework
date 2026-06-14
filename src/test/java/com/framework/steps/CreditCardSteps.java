package com.framework.steps;

import com.framework.api.ApiClient;
import com.framework.pages.CreditCardPage;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class CreditCardSteps {

    private final CreditCardPage page = new CreditCardPage();
    private final ApiClient api = new ApiClient("api.base.url");
    private String transactionRef;

    @When("the user submits a payment of {string} to {string} using card {string}")
    public void submitPayment(String amount, String payee, String cardNumber) {
        page.enterCardNumber(cardNumber);
        page.enterAmount(amount);
        page.enterPayee(payee);
        page.submitPayment();
    }

    @Then("the payment confirmation should be displayed")
    public void verifyConfirmation() {
        assertThat(page.isConfirmationVisible())
                .as("Payment confirmation banner should be visible")
                .isTrue();
        transactionRef = page.getTransactionReference();
        assertThat(transactionRef).isNotBlank();
    }

    @Then("the transaction should be recorded in the API")
    public void verifyTransactionInApi() {
        Response r = api.get("/transactions/" + transactionRef);
        api.assertStatus(r, 200);
        String apiStatus = r.jsonPath().getString("status");
        assertThat(apiStatus)
                .as("API transaction status")
                .isEqualToIgnoringCase("COMPLETED");
    }
}