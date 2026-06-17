package com.framework.exception;

public class DriverInitException extends FrameworkException {
    public DriverInitException(String browser, Throwable cause) {
        super("Failed to initialise driver for browser: " + browser, cause);
    }
}
