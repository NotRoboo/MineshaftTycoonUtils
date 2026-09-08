package com.roboo.mineshafttycoonutils.features.misc;

import com.roboo.mineshafttycoonutils.utils.TimeFormatUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoreTimes {

    private static final String START_GUARD = "(?<!§)(?:(?<!\\d)|(?<=§\\d))";

    private static final Pattern SECONDS_PATTERN = Pattern.compile(
            START_GUARD + "([\\d,]+)s(?![A-Za-z0-9])"
    );

    private LoreTimes() {}

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
            result.append(TimeFormatUtils.formatDuration(totalSeconds, true));
            last = matcher.end();
            changed = true;
        }

        if (!changed) return text;

        result.append(text.substring(last));
        return result.toString();
    }
}