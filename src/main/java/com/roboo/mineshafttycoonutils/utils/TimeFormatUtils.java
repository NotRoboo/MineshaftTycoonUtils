package com.roboo.mineshafttycoonutils.utils;

public class TimeFormatUtils {

    private TimeFormatUtils() {}

    public static String formatDuration(long totalSeconds, boolean showSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (days > 0 || hours > 0) sb.append(hours).append("h ");
        if (days > 0 || hours > 0 || minutes > 0 || !showSeconds) sb.append(minutes).append("m");
        if (showSeconds) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }
}