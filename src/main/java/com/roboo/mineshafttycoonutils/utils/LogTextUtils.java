package com.roboo.mineshafttycoonutils.utils;

public class LogTextUtils {

    private LogTextUtils() {}

    public static String restore(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder out = new StringBuilder(text.length());
        boolean changed = false;

        for (int i = 0; i < text.length(); ) {
            int codepoint = text.codePointAt(i);
            i += Character.charCount(codepoint);

            String replacement = null;
            if (codepoint <= 0xFFFF) {
                String tag = RankTierData.tagForGlyph((char) codepoint);
                if (tag != null) replacement = "[" + tag + "]";
            } else {
                String shortcode = EmojiData.shortcodeFor(codepoint);
                if (shortcode != null) replacement = ":" + shortcode + ":";
            }

            if (replacement != null) {
                out.append(replacement);
                changed = true;
            } else {
                out.appendCodePoint(codepoint);
            }
        }

        return changed ? out.toString() : text;
    }
}