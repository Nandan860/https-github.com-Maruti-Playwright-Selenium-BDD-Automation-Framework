package com.framework.utils;

import com.framework.driver.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;

/**
 * Centralised wait utility.
 *
 * Why this class exists:
 *   BasePage has basic waits for common page interactions.
 *   WaitUtils handles edge cases — AJAX spinners, element staleness,
 *   custom conditions, JS execution completion — that don't belong in BasePage.
 *
 * All timeouts are in seconds. Defaults are config-driven via ConfigManager.
 */
public class WaitUtils {

    private static final Logger log = LogManager.getLogger(WaitUtils.class);
    private static final int DEFAULT_TIMEOUT  = 10;
    private static final int POLLING_INTERVAL = 500; // ms

    private WaitUtils() {}

    // ── Core wait builders ───────────────────────────────────────────────────

    public static WebDriverWait standardWait(int timeoutSeconds) {
        return new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(timeoutSeconds));
    }

    public static FluentWait<WebDriver> fluentWait(int timeoutSeconds) {
        return new FluentWait<>(DriverFactory.getDriver())
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    // ── Visibility ───────────────────────────────────────────────────────────

    public static WebElement waitForVisible(By locator) {
        return waitForVisible(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForVisible(By locator, int timeoutSeconds) {
        return standardWait(timeoutSeconds)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static boolean waitForInvisible(By locator, int timeoutSeconds) {
        try {
            return standardWait(timeoutSeconds)
                    .until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.warn("Element still visible after {}s: {}", timeoutSeconds, locator);
            return false;
        }
    }

    // ── Loading spinner wait ─────────────────────────────────────────────────

    /**
     * Waits for a spinner/loader overlay to disappear before interacting.
     * Pass the locator of your app's loading indicator.
     */
    public static void waitForSpinnerToDisappear(By spinnerLocator) {
        waitForSpinnerToDisappear(spinnerLocator, DEFAULT_TIMEOUT);
    }

    public static void waitForSpinnerToDisappear(By spinnerLocator, int timeoutSeconds) {
        log.debug("Waiting for spinner to disappear: {}", spinnerLocator);
        waitForInvisible(spinnerLocator, timeoutSeconds);
    }

    // ── JavaScript readiness ──────────────────────────────────────────────────

    /**
     * Waits until document.readyState === 'complete'.
     * Useful after page navigation before interacting with elements.
     */
    public static void waitForPageLoad() {
        waitForPageLoad(DEFAULT_TIMEOUT);
    }

    public static void waitForPageLoad(int timeoutSeconds) {
        standardWait(timeoutSeconds).until((ExpectedCondition<Boolean>) driver -> {
            assert driver != null;
            String state = ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").toString();
            return "complete".equals(state);
        });
    }

    /**
     * Waits until jQuery AJAX calls are complete.
     * Returns immediately if jQuery is not present on the page.
     */
    public static void waitForAjax() {
        waitForAjax(DEFAULT_TIMEOUT);
    }

    public static void waitForAjax(int timeoutSeconds) {
        try {
            standardWait(timeoutSeconds).until((ExpectedCondition<Boolean>) driver -> {
                assert driver != null;
                Object result = ((JavascriptExecutor) driver)
                        .executeScript("return (typeof jQuery !== 'undefined') ? jQuery.active === 0 : true");
                return Boolean.TRUE.equals(result);
            });
        } catch (TimeoutException e) {
            log.warn("AJAX did not complete within {}s", timeoutSeconds);
        }
    }

    // ── Staleness-safe wait ───────────────────────────────────────────────────

    /**
     * Waits for an element to be re-rendered after a DOM update (staleness then presence).
     */
    public static WebElement waitForRefresh(By locator) {
        WebElement stale = DriverFactory.getDriver().findElement(locator);
        standardWait(DEFAULT_TIMEOUT).until(ExpectedConditions.stalenessOf(stale));
        return standardWait(DEFAULT_TIMEOUT).until(
                ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ── Custom condition wait ─────────────────────────────────────────────────

    public static <T> T waitUntil(Function<WebDriver, T> condition, int timeoutSeconds) {
        return fluentWait(timeoutSeconds).until(condition);
    }
}
