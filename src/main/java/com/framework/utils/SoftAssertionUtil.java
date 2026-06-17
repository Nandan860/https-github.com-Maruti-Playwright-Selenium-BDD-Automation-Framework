package com.framework.utils;

import org.assertj.core.api.SoftAssertions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * Soft Assertion Utility.
 *
 * Collects ALL assertion failures within a block before failing.
 * Essential for validation scenarios where you want to see every mismatch,
 * not just the first one (e.g. DB row comparison, form field validation).
 *
 * Usage:
 *   SoftAssertionUtil.assertAll(softly -> {
 *       softly.assertThat(actual.getName()).isEqualTo(expected.getName());
 *       softly.assertThat(actual.getAmount()).isEqualTo(expected.getAmount());
 *       softly.assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
 *   });
 *   // All three are checked — report shows ALL failures, not just the first.
 */
public class SoftAssertionUtil {

    private static final Logger log = LogManager.getLogger(SoftAssertionUtil.class);

    private SoftAssertionUtil() {}

    /**
     * Runs all assertions in the block; throws MultipleFailuresError at the end
     * if any assertion failed.
     */
    public static void assertAll(Consumer<SoftAssertions> assertionBlock) {
        SoftAssertions softly = new SoftAssertions();
        assertionBlock.accept(softly);
        try {
            softly.assertAll();
        } catch (AssertionError e) {
            log.error("Soft assertion failures:\n{}", e.getMessage());
            throw e;
        }
    }
}
