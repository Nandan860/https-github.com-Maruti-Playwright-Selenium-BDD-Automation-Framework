package com.framework.report;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * AllureManager — centralised Allure report helper.
 *
 * Provides typed attachment methods so step definitions
 * don't need to import Allure directly.
 *
 * Why centralise?
 *   If the reporting library changes (Allure → Extent → custom),
 *   only this class changes — not every step definition.
 */
public class AllureManager {

    private static final Logger log = LogManager.getLogger(AllureManager.class);

    private AllureManager() {}

    /** Attach raw bytes (screenshots, PDFs). */
    public static void attachBytes(String name, String mimeType, byte[] data) {
        try {
            Allure.addAttachment(name, mimeType, new ByteArrayInputStream(data), "");
        } catch (Exception e) {
            log.warn("Failed to attach '{}' to Allure report: {}", name, e.getMessage());
        }
    }

    /** Attach plain text (SQL results, API responses, log excerpts). */
    public static void attachText(String name, String content) {
        try {
            Allure.addAttachment(name, "text/plain", content);
        } catch (Exception e) {
            log.warn("Failed to attach text '{}' to Allure report: {}", name, e.getMessage());
        }
    }

    /** Attach JSON string with syntax highlighting in Allure UI. */
    public static void attachJson(String name, String json) {
        try {
            Allure.addAttachment(name, "application/json", json);
        } catch (Exception e) {
            log.warn("Failed to attach JSON '{}': {}", name, e.getMessage());
        }
    }

    /** Attach a file from disk. */
    public static void attachFile(String name, String filePath) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            String mimeType = Files.probeContentType(Paths.get(filePath));
            attachBytes(name, mimeType != null ? mimeType : "application/octet-stream", bytes);
        } catch (Exception e) {
            log.warn("Failed to attach file '{}': {}", filePath, e.getMessage());
        }
    }

    /** Mark current test step with a dynamic label. */
    public static void step(String stepName) {
        Allure.step(stepName);
    }

    /** Annotate the Allure report with environment metadata. */
    public static void setEnvironmentLabel(String key, String value) {
        Allure.label(key, value);
    }
}
