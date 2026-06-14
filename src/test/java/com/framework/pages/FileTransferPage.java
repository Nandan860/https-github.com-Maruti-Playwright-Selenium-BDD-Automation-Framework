package com.framework.pages;

import com.framework.driver.BasePage;
import org.openqa.selenium.By;

import java.nio.file.Path;

public class FileTransferPage extends BasePage {

    private static final By FILE_INPUT    = By.cssSelector("input[type='file']");
    private static final By UPLOAD_BTN    = By.id("upload-btn");
    private static final By UPLOAD_STATUS = By.id("upload-status");
    private static final By DOWNLOAD_BTN  = By.id("download-btn");

    /** Sends file path directly to file input (works headless too). */
    public void uploadFile(Path filePath) {
        waitForVisible(FILE_INPUT).sendKeys(filePath.toAbsolutePath().toString());
        click(UPLOAD_BTN);
    }

    public String getUploadStatus() { return getText(UPLOAD_STATUS); }

    public void clickDownload() { click(DOWNLOAD_BTN); }
}