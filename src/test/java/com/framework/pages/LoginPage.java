package com.framework.pages;

import com.framework.config.ConfigManager;
import com.framework.driver.BasePage;
import org.openqa.selenium.By;

/**
 * LoginPage — covers the banking portal login screen and top-nav.
 *
 * Fixes from audit:
 *  - Added isDashboardLoaded(), getWelcomeText(), isAccountLocked()
 *  - Added navigateToSection() for shared navigation steps
 *  - All locators private static final (were missing in original)
 */
public class LoginPage extends BasePage {

    // ── Locators ────────────────────────────────────────────────────────────
    private static final By USERNAME     = By.id("username");
    private static final By PASSWORD     = By.id("password");
    private static final By LOGIN_BTN    = By.id("login-btn");
    private static final By ERROR_MSG    = By.cssSelector(".error-message");
    private static final By LOCKED_MSG   = By.cssSelector(".account-locked-banner");
    private static final By DASHBOARD    = By.id("dashboard-container");
    private static final By WELCOME_TEXT = By.cssSelector(".welcome-message");
    private static final By NAV_MENU     = By.id("main-nav");

    // ── Actions ──────────────────────────────────────────────────────────────

    public LoginPage navigateTo(String url) {
        goTo(url);
        return this;
    }

    public void login(String username, String password) {
        log.info("Logging in as: {}", username);
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BTN);
    }

    public void navigateToSection(String section) {
        String baseUrl = ConfigManager.get("app.url");
        goTo(baseUrl + "/" + section);
    }

    // ── Assertions (state queries — no assertThat here, that belongs in steps) ──

    public boolean isErrorDisplayed()   { return isDisplayed(ERROR_MSG); }
    public boolean isAccountLocked()    { return isDisplayed(LOCKED_MSG); }
    public boolean isDashboardLoaded()  { return isDisplayed(DASHBOARD); }
    public String  getErrorText()       { return getText(ERROR_MSG); }
    public String  getWelcomeText()     { return getText(WELCOME_TEXT); }
}
