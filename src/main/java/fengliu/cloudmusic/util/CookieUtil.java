package fengliu.cloudmusic.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Cookie header helpers that preserve values containing '='. */
public final class CookieUtil {
    private CookieUtil() {
    }

    public static String getValue(String cookie, String name) {
        if (cookie == null || name == null) {
            return null;
        }

        for (String item : cookie.split(";")) {
            int separator = item.indexOf('=');
            if (separator < 0) {
                continue;
            }
            String key = item.substring(0, separator).trim();
            if (key.equalsIgnoreCase(name)) {
                return item.substring(separator + 1).trim();
            }
        }
        return null;
    }

    public static String putValue(String cookie, String name, String value) {
        Map<String, String> values = new LinkedHashMap<>();
        if (cookie != null) {
            for (String item : cookie.split(";")) {
                int separator = item.indexOf('=');
                if (separator < 0) {
                    continue;
                }
                String key = item.substring(0, separator).trim();
                if (!key.isEmpty() && !key.equalsIgnoreCase(name)) {
                    values.put(key, item.substring(separator + 1).trim());
                }
            }
        }

        if (value != null && !value.isBlank()) {
            values.put(name, value.trim());
        }

        StringBuilder result = new StringBuilder();
        values.forEach((key, entryValue) -> result.append(key).append('=').append(entryValue).append("; "));
        return result.toString();
    }

    /** Accepts a raw token, MUSIC_U=token, or a complete Cookie header. */
    public static String normalizeMusicU(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        String cookieValue = getValue(trimmed, "MUSIC_U");
        String value = cookieValue == null ? trimmed : cookieValue;
        if (cookieValue == null && trimmed.regionMatches(true, 0, "MUSIC_U=", 0, 8)) {
            value = trimmed.substring(8).trim();
        }

        if (value.isBlank() || value.indexOf(';') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            return null;
        }
        return value;
    }
}
