package com.framework.pages;

import com.framework.driver.BasePage;
import org.openqa.selenium.By;

import java.nio.file.Path;

public class FileTransferPage extends BasePage {

    private static final By FILE_INPUT     = By.cssSelector("input[type='file']");
    private static final By UPLOAD_BTN     = By.id("upload-btn");
    private static final By UPLOAD_STATUS  = By.id("upload-status");
    private static final By UPLOAD_ERROR   = By.cssSelector(".upload-error");
    private static final By DOWNLOAD_BTN   = By.id("download-btn");

    public void uploadFile(Path filePath) {
        // sendKeys on a file input works headlessly without AutoIT
        waitForVisible(FILE_INPUT).sendKeys(filePath.toAbsolutePath().toString());
        click(UPLOAD_BTN);
    }

    public void clickDownload()           { click(DOWNLOAD_BTN); }
    public String getUploadStatus()       { return getText(UPLOAD_STATUS); }
    public String getUploadError()        { return getText(UPLOAD_ERROR); }
    public boolean isUploadSuccessful()   { return isDisplayed(UPLOAD_STATUS); }
    public boolean isUploadErrorShown()   { return isDisplayed(UPLOAD_ERROR); }
}
