package com.roboo.mineshafttycoonutils.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared staff/tier tag data used by chat formatting, the tab list overlay,
 * and nametag rendering so all three stay in sync. This replaces  what was in PlayerMessageFormatter.
 */
public class RankTierData {

    private RankTierData() {}

    public record StaffRank(String display, int hex) {}

    public static final Map<String, StaffRank> STAFF_RANKS = new LinkedHashMap<>();
    static {
        STAFF_RANKS.put("OWNER", new StaffRank("Owner", 0xAA0000));
        STAFF_RANKS.put("MANAGER", new StaffRank("Manager", 0x660000));
        STAFF_RANKS.put("ADMIN", new StaffRank("Admin", 0x890000));
        STAFF_RANKS.put("DEV", new StaffRank("Dev", 0xFA4241));
        STAFF_RANKS.put("BUILD", new StaffRank("Builder", 0xFDE047));
        STAFF_RANKS.put("MOD", new StaffRank("Mod", 0x5B1771));
        STAFF_RANKS.put("HELPER", new StaffRank("Helper", 0x15803D));
    }

    public static final Map<String, String> TAG_DISPLAY_ALIASES = new LinkedHashMap<>();
    static {
        TAG_DISPLAY_ALIASES.put("ADMN", "ADMIN");
    }

    public static final Map<String, String> TIER_GLYPHS = new LinkedHashMap<>();
    static {
        TIER_GLYPHS.put("a" + "T1", "\uE001");
        TIER_GLYPHS.put("e" + "T2", "\uE002");
        TIER_GLYPHS.put("c" + "T3", "\uE003");
        TIER_GLYPHS.put("9" + "T4", "\uE004");
        TIER_GLYPHS.put("3" + "T4", "\uE005");
        TIER_GLYPHS.put("7" + "T5", "\uE006");
        TIER_GLYPHS.put("6" + "T5", "\uE007");
        TIER_GLYPHS.put("4" + "T5", "\uE008");

        TIER_GLYPHS.put("Owner", "\uE009");
        TIER_GLYPHS.put("Manager", "\uE010");
        TIER_GLYPHS.put("Dev", "\uE011");
        TIER_GLYPHS.put("Admin", "\uE012");
        TIER_GLYPHS.put("Builder", "\uE013");
        TIER_GLYPHS.put("Mod", "\uE014");
        TIER_GLYPHS.put("Helper", "\uE015");
    }

    public static final List<String> TIER_SORT_ORDER = List.of(
            "OWNER", "MANAGER", "ADMIN", "DEV", "BUILD", "MOD", "HELPER",
            "4T5", "6T5", "7T5",
            "9T4", "3T4",
            "cT3", "eT2", "aT1"
    );

    public static String resolveTag(String rawTag) {
        String upper = rawTag.toUpperCase(Locale.ROOT).trim();
        return TAG_DISPLAY_ALIASES.getOrDefault(upper, upper);
    }

    public static boolean isStaff(String resolvedTag) {
        return STAFF_RANKS.containsKey(resolvedTag);
    }

    public static String glyphKeyFor(char colorChar, String resolvedTag) {
        StaffRank staff = STAFF_RANKS.get(resolvedTag);
        return staff != null ? staff.display() : (colorChar + resolvedTag);
    }

    public static String glyphFor(String glyphKey) {
        return TIER_GLYPHS.get(glyphKey);
    }

    public static int tierSortIndex(char colorChar, String resolvedTag) {
        if (resolvedTag == null) return Integer.MAX_VALUE;
        String key = isStaff(resolvedTag) ? resolvedTag : (colorChar + resolvedTag);
        int idx = TIER_SORT_ORDER.indexOf(key);
        return idx == -1 ? Integer.MAX_VALUE : idx;
    }
}