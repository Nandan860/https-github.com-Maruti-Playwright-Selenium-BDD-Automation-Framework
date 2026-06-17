package com.framework.utils;

import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries flaky tests up to max.retry.count times (default 2).
 *
 * Attach at method level:  @Test(retryAnalyzer = RetryAnalyzer.class)
 * Or wired globally via RetryListener (recommended).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRIES = ConfigManager.getInt("max.retry.count", 2);

    private final ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(() -> 0);

    @Override
    public boolean retry(ITestResult result) {
        int count = retryCount.get();
        if (count < MAX_RETRIES) {
            retryCount.set(count + 1);
            log.warn("Retrying [{}/{}]: {}", count + 1, MAX_RETRIES,
                    result.getMethod().getMethodName());
            return true;
        }
        retryCount.remove();
        return false;
    }
}
