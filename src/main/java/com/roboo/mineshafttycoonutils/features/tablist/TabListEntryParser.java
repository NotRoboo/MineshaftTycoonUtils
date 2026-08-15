package com.roboo.mineshafttycoonutils.features.tablist;

import com.roboo.mineshafttycoonutils.utils.HypixelRank;
import com.roboo.mineshafttycoonutils.utils.RankTierData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabListEntryParser {

    private static final Pattern TAB_ENTRY_PATTERN = Pattern.compile(
            "^(?:(§.)(?:§l)?\\[([^]]+)]\\s*)?(§.)?([^\\s§]+)(?:\\s*(§.)(?:§l)?\\[([^]]+)])?$"
    );

    public record Parsed(
            char rankColor,
            String rankTag,
            char tierColor,
            String tierTag,
            boolean tierIsStaff,
            String username,
            String usernameColorCode
    ) {}

    private TabListEntryParser() {}

    public static Parsed parse(String rawFormattedText) {
        if (rawFormattedText == null) return null;
        Matcher m = TAB_ENTRY_PATTERN.matcher(rawFormattedText.trim());
        if (!m.matches()) return null;

        char rankColor = m.group(1) != null ? m.group(1).charAt(1) : ' ';
        String rankTag = m.group(2);
        String usernameColor = m.group(3);
        String username = m.group(4);
        char tierColor = m.group(5) != null ? m.group(5).charAt(1) : ' ';
        String tierRawTag = m.group(6);

        String resolvedTier = tierRawTag != null ? RankTierData.resolveTag(tierRawTag) : null;
        boolean isStaff = resolvedTier != null && RankTierData.isStaff(resolvedTier);

        return new Parsed(rankColor, rankTag, tierColor, resolvedTier, isStaff, username, usernameColor);
    }

    public static HypixelRank hypixelRank(Parsed parsed) {
        return parsed == null ? HypixelRank.NONE : HypixelRank.parse(parsed.rankTag());
    }

    public static int tierSortIndex(Parsed parsed) {
        if (parsed == null) return Integer.MAX_VALUE;
        return RankTierData.tierSortIndex(parsed.tierColor(), parsed.tierTag());
    }
}