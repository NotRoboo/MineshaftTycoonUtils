package com.roboo.mineshafttycoonutils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParseUtils {

    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "(?=\\d)(?:([\\d,]+)d\\s*)?(?:([\\d,]+)h\\s*)?(?:([\\d,]+)m\\s*)?(?:([\\d,]+)s)?"
    );

    private TimeParseUtils() {}

    public static long parseSeconds(String text) {
        if (text == null) return -1;

        Matcher matcher = DURATION_PATTERN.matcher(text);
        while (matcher.find()) {
            if (matcher.group(1) == null && matcher.group(2) == null
                    && matcher.group(3) == null && matcher.group(4) == null) {
                continue;
            }

            return toLong(matcher.group(1)) * 86400L
                    + toLong(matcher.group(2)) * 3600L
                    + toLong(matcher.group(3)) * 60L
                    + toLong(matcher.group(4));
        }

        return -1;
    }

    private static long toLong(String raw) {
        if (raw == null) return 0;
        try {
            return Long.parseLong(raw.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}