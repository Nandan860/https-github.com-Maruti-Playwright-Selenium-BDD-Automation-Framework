package com.framework.pages;

import com.framework.driver.BasePage;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private static final By USERNAME  = By.id("username");
    private static final By PASSWORD  = By.id("password");
    private static final By LOGIN_BTN = By.id("login-btn");
    private static final By ERROR_MSG = By.cssSelector(".error-message");

    public LoginPage navigateTo(String url) { goTo(url); return this; }

    public void login(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BTN);
    }

    public boolean isErrorDisplayed() { return isDisplayed(ERROR_MSG); }
    public String getErrorText()      { return getText(ERROR_MSG); }
}