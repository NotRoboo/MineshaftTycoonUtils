package com.roboo.mineshafttycoonutils.utils;

import com.roboo.mineshafttycoonutils.config.categories.GlyphCategory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared staff/tier tag data used by chat formatting, the tab list overlay,
 * and nametag rendering so all three stay in sync.
 */
public class RankTierData {

    private RankTierData() {}

    public record StaffRank(String display, int hex) {}

    public static final Map<String, StaffRank> STAFF_RANKS = new LinkedHashMap<>();
    static {
        STAFF_RANKS.put("OWNER", new StaffRank("Owner", 0xA80000));
        STAFF_RANKS.put("MANAGER", new StaffRank("Manager", 0xA80000));
        STAFF_RANKS.put("ADMIN", new StaffRank("Admin", 0xA80000));
        STAFF_RANKS.put("DEV", new StaffRank("Dev", 0xEC4F4F));
        STAFF_RANKS.put("BUILD", new StaffRank("Builder", 0xFAFA53));
        STAFF_RANKS.put("MOD", new StaffRank("Mod", 0x960096));
        STAFF_RANKS.put("HELPER", new StaffRank("Helper", 0x00A800));
        STAFF_RANKS.put("SYSTEM", new StaffRank("System", 0x545454));
    }

    public static final Map<String, String> TAG_DISPLAY_ALIASES = new LinkedHashMap<>();
    static {
        TAG_DISPLAY_ALIASES.put("ADMN", "ADMIN");
        TAG_DISPLAY_ALIASES.put("BUILDER", "BUILD");
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
        TIER_GLYPHS.put("System", "\uE016");
    }

    // just offsetting og glyphs by 100
    public static final Map<String, String> TIER_GLYPHS_TEXTURED = new LinkedHashMap<>();
    static {
        for (Map.Entry<String, String> entry : TIER_GLYPHS.entrySet()) {
            char shifted = (char) (entry.getValue().charAt(0) + 0x100);
            TIER_GLYPHS_TEXTURED.put(entry.getKey(), String.valueOf(shifted));
        }
    }

    public static final List<String> TIER_SORT_ORDER = List.of(
            "OWNER", "MANAGER", "ADMIN", "DEV", "BUILD", "MOD", "HELPER", "SYSTEM",
            "4T5", "6T5", "7T5",
            "9T4", "3T4",
            "cT3", "eT2", "aT1"
    );
    private static final Map<Character, Character> GREEK_HOMOGLYPHS = new LinkedHashMap<>();
    static {
        GREEK_HOMOGLYPHS.put('Α', 'A');
        GREEK_HOMOGLYPHS.put('Β', 'B');
        GREEK_HOMOGLYPHS.put('Ε', 'E');
        GREEK_HOMOGLYPHS.put('Ζ', 'Z');
        GREEK_HOMOGLYPHS.put('Η', 'H');
        GREEK_HOMOGLYPHS.put('Ι', 'I');
        GREEK_HOMOGLYPHS.put('Κ', 'K');
        GREEK_HOMOGLYPHS.put('Μ', 'M');
        GREEK_HOMOGLYPHS.put('Ν', 'N');
        GREEK_HOMOGLYPHS.put('Ο', 'O');
        GREEK_HOMOGLYPHS.put('Ρ', 'P');
        GREEK_HOMOGLYPHS.put('Τ', 'T');
        GREEK_HOMOGLYPHS.put('Υ', 'Y');
        GREEK_HOMOGLYPHS.put('Χ', 'X');
    }

    public static String resolveTag(String rawTag) {
        if (rawTag == null) return null;
        String normalized = normalizeFullwidth(normalizeGreekHomoglyphs(rawTag));
        String upper = normalized.toUpperCase(Locale.ROOT).trim();
        return TAG_DISPLAY_ALIASES.getOrDefault(upper, upper);
    }

    public static boolean isStaff(String resolvedTag) {
        return STAFF_RANKS.containsKey(resolvedTag);
    }

    public static String glyphKeyFor(char colorChar, String resolvedTag) {
        StaffRank staff = STAFF_RANKS.get(resolvedTag);
        return staff != null ? staff.display() : (colorChar + resolvedTag);
    }

    public static String glyphFor(String glyphKey, GlyphCategory.GlyphMode mode) {
        if (mode == null) return null;
        return switch (mode) {
            case CLASSIC -> TIER_GLYPHS.get(glyphKey);
            case THEMED -> TIER_GLYPHS_TEXTURED.get(glyphKey);
            case OFF -> null;
        };
    }

    public static int tierSortIndex(char colorChar, String resolvedTag) {
        if (resolvedTag == null) return Integer.MAX_VALUE;
        String key = isStaff(resolvedTag) ? resolvedTag : (colorChar + resolvedTag);
        int idx = TIER_SORT_ORDER.indexOf(key);
        return idx == -1 ? Integer.MAX_VALUE : idx;
    }

    private static String normalizeGreekHomoglyphs(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sb.append(GREEK_HOMOGLYPHS.getOrDefault(c, c));
        }
        return sb.toString();
    }

    private static String normalizeFullwidth(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 0xFF01 && c <= 0xFF5E) {
                sb.append((char) (c - 0xFEE0));
            } else if (c == 0x3000) {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}