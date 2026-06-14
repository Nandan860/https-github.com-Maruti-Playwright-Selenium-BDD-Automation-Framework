package com.framework.steps;

import com.framework.config.ConfigManager;
import com.framework.driver.DriverFactory;
import com.framework.pages.FileTransferPage;
import com.framework.utils.BrokenLinkChecker;
import io.cucumber.java.en.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileTransferSteps {

    private final FileTransferPage page = new FileTransferPage();
    private List<String> brokenLinks;
    private List<String> brokenImages;

    // ── File Upload ─────────────────────────────────────────────────────────

    @When("the user uploads the file {string}")
    public void uploadFile(String fileName) {
        Path filePath = Paths.get("src/test/resources/testdata", fileName);
        assertThat(filePath).exists();
        page.uploadFile(filePath);
    }

    @Then("the upload should succeed with status {string}")
    public void verifyUpload(String expectedStatus) {
        assertThat(page.getUploadStatus()).containsIgnoringCase(expectedStatus);
    }

    // ── File Download ───────────────────────────────────────────────────────

    @When("the user clicks the download button")
    public void clickDownload() {
        page.clickDownload();
    }

    @Then("a file should be downloaded to the downloads folder")
    public void verifyDownload() throws Exception {
        String downloadDir = ConfigManager.get("download.dir", System.getProperty("user.home") + "/Downloads");
        Thread.sleep(2000); // brief wait for download to complete
        File[] files = new File(downloadDir).listFiles();
        assertThat(files).isNotEmpty();
    }

    // ── Broken Links ────────────────────────────────────────────────────────

    @When("the user scans the page for broken links")
    public void scanBrokenLinks() {
        BrokenLinkChecker checker = new BrokenLinkChecker(DriverFactory.getDriver());
        brokenLinks = checker.findBrokenLinks();
    }

    @Then("no broken links should be found")
    public void noBrokenLinks() {
        assertThat(brokenLinks)
                .as("Broken links found on page")
                .isEmpty();
    }

    @Then("the broken links report should be logged")
    public void logBrokenLinks() {
        // Non-failing step — just reports
        brokenLinks.forEach(link -> System.out.println("BROKEN LINK: " + link));
    }

    // ── Broken Images ───────────────────────────────────────────────────────

    @When("the user scans the page for broken images")
    public void scanBrokenImages() {
        BrokenLinkChecker checker = new BrokenLinkChecker(DriverFactory.getDriver());
        brokenImages = checker.findBrokenImages();
    }

    @Then("no broken images should be found")
    public void noBrokenImages() {
        assertThat(brokenImages)
                .as("Broken images found on page")
                .isEmpty();
    }
}