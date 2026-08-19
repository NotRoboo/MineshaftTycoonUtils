package com.roboo.mineshafttycoonutils.utils;

public class NumberFormatUtils {

    private static final String[] SUFFIXES = {
            "", "K", "M", "B", "T", "Qd", "Qn", "Sx", "Sp", "Oc", "No", "Dc"
    };

    private NumberFormatUtils() {}

    public static String formatShortened(long value, boolean shorten) {
        if (!shorten || value < 1000) {
            return String.format("%,d", value);
        }

        int tier = 0;
        double reduced = value;
        while (reduced >= 1000 && tier < SUFFIXES.length - 1) {
            reduced /= 1000.0;
            tier++;
        }

        return String.format("%.2f%s", reduced, SUFFIXES[tier]);
    }
}