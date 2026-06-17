package com.framework.exception;

/**
 * Root unchecked exception for all framework-level failures.
 *
 * Hierarchy:
 *   FrameworkException
 *     └── DriverInitException    — browser/engine failed to start
 *     └── ConfigException        — required property missing or invalid
 *     └── TestDataException      — Excel/JSON data file issue
 *     └── PageException          — page object interaction failed beyond retries
 *
 * Why not use RuntimeException directly?
 *   Custom root exception lets CI/CD pipelines and log aggregators distinguish
 *   framework failures from application-under-test failures in failure reports.
 */
public class FrameworkException extends RuntimeException {
    public FrameworkException(String message)                   { super(message); }
    public FrameworkException(String message, Throwable cause)  { super(message, cause); }
}
