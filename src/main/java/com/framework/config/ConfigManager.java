package com.framework.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton config loader. Reads from config/{env}.properties.
 * System properties and env vars override file values.
 *
 * Usage:  ConfigManager.get("browser")
 *         ConfigManager.get("db.url")
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final Properties props = new Properties();

    static {
        String env = System.getProperty("env", "dev");
        String file = "config/" + env + ".properties";
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(file)) {
            if (in != null) {
                props.load(in);
                log.info("Loaded config: {}", file);
            } else {
                log.warn("Config file not found: {}. Using system properties only.", file);
            }
        } catch (IOException e) {
            log.error("Failed to load config: {}", file, e);
        }
    }

    private ConfigManager() {}

    /** Get value — system property > env var > properties file > default */
    public static String get(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null) return sysProp;

        String envVar = System.getenv(key.replace(".", "_").toUpperCase());
        if (envVar != null) return envVar;

        return props.getProperty(key, "");
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val == null || val.isBlank()) ? defaultValue : val;
    }

    public static int getInt(String key, int defaultValue) {
        try { return Integer.parseInt(get(key)); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null || val.isBlank()) return defaultValue;
        return Boolean.parseBoolean(val);
    }
}
