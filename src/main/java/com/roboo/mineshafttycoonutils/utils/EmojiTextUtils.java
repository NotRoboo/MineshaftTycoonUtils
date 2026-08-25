package com.roboo.mineshafttycoonutils.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiTextUtils {

    private static final Pattern SHORTCODE_PATTERN = Pattern.compile(":([a-z0-9_]+):");
    private static final String COLOR_CODES = "0123456789abcdef";

    private EmojiTextUtils() {}

    public static String substitute(String rawWithCodes, boolean enabled) {
        if (rawWithCodes == null || !enabled) {
            return rawWithCodes;
        }
        if (!rawWithCodes.contains(":")) {
            return rawWithCodes;
        }

        Matcher m = SHORTCODE_PATTERN.matcher(rawWithCodes);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (m.find()) {
            Integer codepoint = EmojiData.codepointFor(m.group(1).toLowerCase(Locale.ROOT));
            if (codepoint == null) continue;

            String restoreColor = findPrecedingColorCode(rawWithCodes, m.start());
            out.append(rawWithCodes, last, m.start())
                    .append("§f")
                    .appendCodePoint(codepoint)
                    .append(restoreColor);
            last = m.end();
        }
        out.append(rawWithCodes.substring(last));
        return out.toString();
    }

    private static String findPrecedingColorCode(String text, int index) {
        for (int i = index - 1; i >= 1; i--) {
            if (text.charAt(i - 1) == '§' && COLOR_CODES.indexOf(Character.toLowerCase(text.charAt(i))) >= 0) {
                return "§" + text.charAt(i);
            }
        }
        return "§f";
    }
}