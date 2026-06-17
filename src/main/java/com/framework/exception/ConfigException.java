package com.framework.exception;

public class ConfigException extends FrameworkException {
    public ConfigException(String key) {
        super("Required config key is missing or blank: " + key);
    }
}
