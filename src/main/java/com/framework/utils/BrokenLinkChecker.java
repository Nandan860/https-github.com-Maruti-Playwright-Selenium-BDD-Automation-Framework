package com.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Finds broken links and broken images on a page.
 *
 * Usage (in a step def or test):
 *   BrokenLinkChecker checker = new BrokenLinkChecker(driver);
 *   List<String> broken = checker.findBrokenLinks();
 *   assertThat(broken).isEmpty();
 */
public class BrokenLinkChecker {

    private static final Logger log = LogManager.getLogger(BrokenLinkChecker.class);
    private static final int TIMEOUT_MS = 5000;
    private static final int THREAD_POOL = 10;

    private final WebDriver driver;

    public BrokenLinkChecker(WebDriver driver) { this.driver = driver; }

    /** Returns list of broken link URLs (HTTP >= 400 or connection error). */
    public List<String> findBrokenLinks() {
        List<WebElement> anchors = driver.findElements(By.tagName("a"));
        List<String> urls = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(href -> href != null && (href.startsWith("http://") || href.startsWith("https://")))
                .distinct()
                .toList();
        return checkUrls(urls, "link");
    }

    /** Returns list of broken image src URLs. */
    public List<String> findBrokenImages() {
        List<WebElement> images = driver.findElements(By.tagName("img"));
        List<String> urls = images.stream()
                .map(img -> img.getAttribute("src"))
                .filter(src -> src != null && (src.startsWith("http://") || src.startsWith("https://")))
                .distinct()
                .toList();
        return checkUrls(urls, "image");
    }

    private List<String> checkUrls(List<String> urls, String type) {
        List<String> broken = new ArrayList<>();
        ExecutorService exec = Executors.newFixedThreadPool(THREAD_POOL);
        List<Future<String>> futures = new ArrayList<>();

        for (String url : urls) {
            futures.add(exec.submit(() -> {
                int code = getStatusCode(url);
                if (code >= 400 || code == -1) {
                    log.warn("Broken {} [{}]: {}", type, code, url);
                    return url + " [HTTP " + code + "]";
                }
                return null;
            }));
        }

        for (Future<String> f : futures) {
            try { String result = f.get(); if (result != null) broken.add(result); }
            catch (Exception ignored) {}
        }
        exec.shutdown();
        log.info("Checked {} {}s | {} broken", urls.size(), type, broken.size());
        return broken;
    }

    private int getStatusCode(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            return conn.getResponseCode();
        } catch (Exception e) {
            return -1;
        }
    }
}