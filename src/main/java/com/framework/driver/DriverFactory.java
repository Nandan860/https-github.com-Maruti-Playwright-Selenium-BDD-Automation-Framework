package com.framework.driver;

import com.framework.config.ConfigManager;
import com.microsoft.playwright.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;
import java.util.List;

/**
 * Thread-local WebDriver + Playwright manager.
 *
 * Browsers: chrome | firefox | edge  (Selenium)
 *           chromium | webkit | firefox-pw  (Playwright)
 *
 * Engine is picked from config: engine=selenium (default) | playwright
 */
public class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);

    // Selenium thread locals
    private static final ThreadLocal<WebDriver> driverTL = new ThreadLocal<>();

    // Playwright thread locals
    private static final ThreadLocal<Playwright> playwrightTL = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserTL = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextTL = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageTL = new ThreadLocal<>();

    private DriverFactory() {}

    // ── Public API ──────────────────────────────────────────────────────────

    public static void init() {
        String engine = ConfigManager.get("engine", "selenium");
        String browser = ConfigManager.get("browser", "chrome");
        boolean headless = ConfigManager.getBoolean("headless", false);

        log.info("Initialising {} driver | browser={} | headless={}", engine, browser, headless);

        if ("playwright".equalsIgnoreCase(engine)) {
            initPlaywright(browser, headless);
        } else {
            initSelenium(browser, headless);
        }
    }

    /** Selenium WebDriver – null when engine=playwright */
    public static WebDriver getDriver() { return driverTL.get(); }

    /** Playwright Page – null when engine=selenium */
    public static Page getPage() { return pageTL.get(); }

    public static boolean isPlaywright() { return pageTL.get() != null; }

    public static void quit() {
        try {
            if (driverTL.get() != null) { driverTL.get().quit(); driverTL.remove(); }
            if (pageTL.get() != null)   { pageTL.get().close(); pageTL.remove(); }
            if (contextTL.get() != null){ contextTL.get().close(); contextTL.remove(); }
            if (browserTL.get() != null){ browserTL.get().close(); browserTL.remove(); }
            if (playwrightTL.get() != null){ playwrightTL.get().close(); playwrightTL.remove(); }
        } catch (Exception e) {
            log.warn("Error during driver teardown: {}", e.getMessage());
        }
    }

    // ── Selenium setup ──────────────────────────────────────────────────────

    private static void initSelenium(String browser, boolean headless) {
        String gridUrl = ConfigManager.get("grid.url", "");
        WebDriver driver;

        if (!gridUrl.isBlank()) {
            driver = createRemoteDriver(browser, headless, gridUrl);
        } else {
            driver = switch (browser.toLowerCase()) {
                case "firefox" -> createFirefox(headless);
                case "edge"    -> createEdge(headless);
                default        -> createChrome(headless);
            };
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(
                ConfigManager.getInt("implicit.wait", 0)));
        driver.manage().window().maximize();
        driverTL.set(driver);
    }

    private static WebDriver createChrome(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        opts.addArguments("--remote-allow-origins=*");
        if (headless) opts.addArguments("--headless=new");
        return new ChromeDriver(opts);
    }

    private static WebDriver createFirefox(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions opts = new FirefoxOptions();
        if (headless) opts.addArguments("-headless");
        return new FirefoxDriver(opts);
    }

    private static WebDriver createEdge(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions opts = new EdgeOptions();
        if (headless) opts.addArguments("--headless=new");
        return new EdgeDriver(opts);
    }

    private static WebDriver createRemoteDriver(String browser, boolean headless, String gridUrl) {
        try {
            var caps = switch (browser.toLowerCase()) {
                case "firefox" -> { var o = new FirefoxOptions(); if (headless) o.addArguments("-headless"); yield o; }
                case "edge"    -> { var o = new EdgeOptions();   if (headless) o.addArguments("--headless=new"); yield o; }
                default        -> { var o = new ChromeOptions(); if (headless) o.addArguments("--headless=new"); yield o; }
            };
            return new RemoteWebDriver(new URL(gridUrl), caps);
        } catch (Exception e) {
            throw new RuntimeException("Could not connect to Selenium Grid at " + gridUrl, e);
        }
    }

    // ── Playwright setup ────────────────────────────────────────────────────

    private static void initPlaywright(String browser, boolean headless) {
        Playwright pw = Playwright.create();
        playwrightTL.set(pw);

        BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(ConfigManager.getInt("playwright.slowmo", 0));

        Browser b = switch (browser.toLowerCase()) {
            case "webkit", "safari" -> pw.webkit().launch(opts);
            case "firefox-pw"       -> pw.firefox().launch(opts);
            default                 -> pw.chromium().launch(opts);
        };
        browserTL.set(b);

        BrowserContext ctx = b.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setAcceptDownloads(true));
        contextTL.set(ctx);
        pageTL.set(ctx.newPage());
    }
}
