package com.framework.exception;

public class TestDataException extends FrameworkException {
    public TestDataException(String filePath, Throwable cause) {
        super("Failed to load test data from: " + filePath, cause);
    }
}
