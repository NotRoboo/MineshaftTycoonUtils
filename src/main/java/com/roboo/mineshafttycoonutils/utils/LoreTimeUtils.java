package com.roboo.mineshafttycoonutils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoreTimeUtils {

    private static final String START_GUARD = "(?<!§)(?:(?<!\\d)|(?<=§\\d))";

    private static final Pattern SECONDS_PATTERN = Pattern.compile(
            START_GUARD + "([\\d,]+)s(?![A-Za-z0-9])"
    );

    private LoreTimeUtils() {}

    public static String shortenSeconds(String text) {
        if (text == null) return null;

        Matcher matcher = SECONDS_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        int last = 0;
        boolean changed = false;

        while (matcher.find()) {
            long totalSeconds;
            try {
                totalSeconds = Long.parseLong(matcher.group(1).replace(",", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            result.append(text, last, matcher.start());
            result.append(formatDuration(totalSeconds));
            last = matcher.end();
            changed = true;
        }

        if (!changed) return text;

        result.append(text.substring(last));
        return result.toString();
    }

    private static String formatDuration(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (days > 0 || hours > 0) sb.append(hours).append("h ");
        if (days > 0 || hours > 0 || minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString();
    }
}