package com.roboo.mineshafttycoonutils.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiTextUtils {

    private static final Pattern SHORTCODE_PATTERN = Pattern.compile(":([a-z0-9_]+):", Pattern.CASE_INSENSITIVE);
    private static final Pattern DISCORD_EMOJI_PATTERN = Pattern.compile("<a?:([a-z0-9_]+):\\d+>", Pattern.CASE_INSENSITIVE);
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

    public static String stripDiscordEmojiWrapper(String rawWithCodes) {
        if (rawWithCodes == null || rawWithCodes.isEmpty()) return rawWithCodes;
        return DISCORD_EMOJI_PATTERN.matcher(rawWithCodes).replaceAll(":$1:");
    }
}