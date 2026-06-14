package com.framework.steps;

import com.framework.config.ConfigManager;
import com.framework.pages.LoginPage;
import io.cucumber.java.en.*;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    private final LoginPage loginPage = new LoginPage();

    @Given("the user navigates to the banking portal")
    public void navigateToPortal() {
        loginPage.navigateTo(ConfigManager.get("app.url"));
    }

    @When("the user logs in with username {string} and password {string}")
    public void login(String user, String pass) {
        loginPage.login(user, pass);
    }

    @Then("the login should fail with error {string}")
    public void verifyLoginError(String expectedError) {
        assertThat(loginPage.isErrorDisplayed()).isTrue();
        assertThat(loginPage.getErrorText()).containsIgnoringCase(expectedError);
    }
}