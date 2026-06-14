package com.framework.driver;

import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base Page Object.
 *
 * Subclasses extend this and use the protected helper methods.
 * Keeps test steps clean — no raw Selenium boilerplate in step defs.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected WebDriver driver;
    protected WebDriverWait wait;

    protected BasePage() {
        this.driver = DriverFactory.getDriver();
        int explicitWait = ConfigManager.getInt("explicit.wait", 10);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
        PageFactory.initElements(driver, this);
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    protected void goTo(String url) {
        log.debug("Navigating to: {}", url);
        driver.get(url);
    }

    // ── Wait helpers ────────────────────────────────────────────────────────

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean waitForUrlContains(String fragment) {
        return wait.until(ExpectedConditions.urlContains(fragment));
    }

    // ── Interaction helpers ─────────────────────────────────────────────────

    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    protected boolean isDisplayed(By locator) {
        try { return driver.findElement(locator).isDisplayed(); }
        catch (NoSuchElementException e) { return false; }
    }

    // ── Screenshot ──────────────────────────────────────────────────────────

    public byte[] takeScreenshot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
}