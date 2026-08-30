package com.roboo.mineshafttycoonutils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoreNumberUtils {

    private static final String START_GUARD = "(?<!§)(?:(?<!\\d)|(?<=§\\d))";

    private static final Pattern LARGE_NUMBER_PATTERN = Pattern.compile(
            START_GUARD + "\\d{1,3}(?:,\\d{3})+(?!\\d)"
                    + "|" + START_GUARD + "\\d{1,3}(?:\\.\\d{3})+(?!\\d)"
                    + "|" + START_GUARD + "\\d{10,}(?!\\d)"
    );

    private static final long THRESHOLD = 1_000_000L;

    private LoreNumberUtils() {}

    public static String shortenLargeNumbers(String text) {
        if (text == null) return null;

        Matcher matcher = LARGE_NUMBER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        int last = 0;
        boolean changed = false;

        while (matcher.find()) {
            long value;
            try {
                value = Long.parseLong(matcher.group().replaceAll("[,.]", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            if (value <= THRESHOLD) continue;

            result.append(text, last, matcher.start());
            result.append(formatShortened(value));
            last = matcher.end();
            changed = true;
        }

        if (!changed) return text;

        result.append(text.substring(last));
        return result.toString();
    }

    private static String formatShortened(long value) {
        String formatted = NumberFormatUtils.formatShortened(value, true);
        return formatted.replaceAll("\\.00([A-Za-z]*)$", "$1");
    }
}