package com.roboo.mineshafttycoonutils.utils;

import com.roboo.mineshafttycoonutils.config.GlyphCategory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlyphTextUtils {

    private static final Pattern BRACKET_TAG = Pattern.compile("§(.)(?:§l)?\\[([^]]+)]");

    private GlyphTextUtils() {}

    public static String substituteTierTags(String rawWithCodes, GlyphCategory.GlyphMode mode) {
        if (rawWithCodes == null || mode == null || !mode.isEnabled()) {
            return rawWithCodes;
        }

        Matcher m = BRACKET_TAG.matcher(rawWithCodes);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (m.find()) {
            char colorChar = m.group(1).charAt(0);
            String resolvedTag = RankTierData.resolveTag(m.group(2));
            String glyphKey = RankTierData.glyphKeyFor(colorChar, resolvedTag);
            String glyph = RankTierData.glyphFor(glyphKey, mode);
            if (glyph == null) continue;

            out.append(rawWithCodes, last, m.start())
                    .append("§f").append(glyph)
                    .append("§").append(colorChar);
            last = m.end();
        }
        out.append(rawWithCodes.substring(last));
        return out.toString();
    }
}