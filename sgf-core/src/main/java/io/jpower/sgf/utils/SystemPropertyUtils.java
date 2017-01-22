package io.jpower.sgf.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zheng.sun
 */
public class SystemPropertyUtils {

    private static final Logger log = LoggerFactory.getLogger(SystemPropertyUtils.class);

    public static boolean contains(String key) {
        return get(key) != null;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String def) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException("key must not be empty.");
        }

        String value = null;
        try {
            value = System.getProperty(key);
        } catch (Exception e) {
            log.warn("Unable to retrieve a system property '" + key
                    + "'; default values will be used.", e);
        }

        if (value == null) {
            return def;
        }

        return value;
    }

    public static boolean getBoolean(String key, boolean def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (value.length() == 0) {
            return true;
        }

        if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
            return false;
        }

        log.warn("Unable to parse the boolean system property '" + key + "':" + value + " - "
                + "using the default value: " + def);

        return def;
    }

    public static int getInt(String key, int def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            log.warn("Unable to parse the integer system property '" + key + "':" + value + " - "
                    + "using the default value: " + def);
        }

        return def;
    }

    public static long getLong(String key, long def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            log.warn("Unable to parse the long integer system property '" + key + "':" + value
                    + " - " + "using the default value: " + def);
        }

        return def;
    }

}
