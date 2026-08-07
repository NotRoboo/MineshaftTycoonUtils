package com.roboo.mineshafttycoonutils.features.fishing;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FishingTracker {

    private static int seaCreatureCount = 0;
    private static int treasureDropCount = 0;
    private static int plateDropCount = 0;
    private static int crateCount = 0;
    private static int trophyCount = 0;
    private static int doubleHookCount = 0;
    private static int fortuneFragmentsCatches = 0;
    private static final Map<String, Integer> plateCatches = new LinkedHashMap<>();

    private static final Map<String, Integer> seaCreatureBreakdown = new LinkedHashMap<>();
    private static final Map<String, Integer> treasureBreakdown = new LinkedHashMap<>();
    private static final Map<String, Integer> plateBreakdown = new LinkedHashMap<>();
    private static final Map<String, Integer> crateBreakdown = new LinkedHashMap<>();
    private static final Map<String, Integer> trophyBreakdown = new LinkedHashMap<>();

    public static final List<String> PLATE_ORDER = List.of(
            "Coal",
            "Iron",
            "Gold",
            "Emerald",
            "Diamond",
            "Fused"
    );

    private static final Pattern FISH_PATTERN =
            Pattern.compile("(?i)^\\*\\s*fish!\\s*you fished up\\s*(?:\\d+x\\s*|a?\\s*)?(.+?)!\\s*\\(\\d+(?:\\.\\d+)?%\\)\\s*(?:\\[added to bag!])?$");

    private static final Pattern GREAT_CATCH_PATTERN =
            Pattern.compile("(?i)^\\*\\s*great catch!\\s*(.+?)!?\\s*\\((?:\\d+/\\d+|\\d+(?:\\.\\d+)?%)\\)\\s*(?:\\[added to bag!])?$");

    private static final Pattern MATERIAL_PATTERN =
            Pattern.compile("(?i)^\\*\\s*material!\\s*you fished up some\\s*(.+?)!\\s*\\(\\d+(?:\\.\\d+)?%\\)$");

    private static final Pattern SEA_CREATURE_PATTERN =
            Pattern.compile("(?i)^\\*\\s*sea creature!\\s*you caught (?:(?:an?|the)\\s+)?(.+?)!?$");

    private static final Pattern CRATE_PATTERN =
            Pattern.compile("(?i)^\\*\\s*\\+\\s*\\|\\s*(.+crate)!\\s*\\(\\d+%\\)\\s*(?:x\\d+)?$");

    private static final Pattern RARE_DROP_FRAGMENTS_PATTERN =
            Pattern.compile("(?i)^\\*\\s*rare drop!\\s*(\\d+)x\\s*fortune fragments!\\s*\\(\\d+(?:\\.\\d+)?%\\)\\s*(?:\\[added to bag!])?$");

    private static final Pattern RARE_DROP_ITEM_PATTERN =
            Pattern.compile("(?i)^\\*\\s*(golden coral|mermaid's pearl|ancient trident|kraken scale)\\s*\\(\\d+(?:\\.\\d+)?%\\)$");

    private static final Pattern PLATE_PATTERN =
            Pattern.compile("(?i)^\\*\\s*plates!\\s*you fished up\\s*(\\d)x (.+?) plates!\\s*\\(\\d+(?:\\.\\d+)?%\\)$");

    private static final Pattern FUSED_PLATE_PATTERN =
            Pattern.compile("(?i)^\\*\\s*(fused) plates\\s*\\(\\d+(?:\\.\\d+)?%\\)\\s*x(\\d)$");

    private static final Pattern TROPHY_PATTERN =
            Pattern.compile("(?i)^\\*\\s*trophy!\\s*(.+?)\\s*\\((?:\\d+/\\d+|\\d+(?:\\.\\d+)?%)\\)$");

    private static final Pattern DOUBLE_HOOK_PATTERN =
            Pattern.compile("(?i)^\\*\\s*double hook!$");

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            handleMessage(msg.getString());
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> {
            handleMessage(msg.getString());
            return true;
        });
    }

    private static void handleMessage(String msg) {
        if (msg == null) return;

        String stripped = msg.replaceAll("§.", "").trim();
        if (stripped.isEmpty()) return;

        String clean = stripped.toLowerCase(Locale.ROOT);

        if (DOUBLE_HOOK_PATTERN.matcher(stripped).matches()) {
            doubleHookCount++;
            return;
        }

        Matcher fragMatcher = RARE_DROP_FRAGMENTS_PATTERN.matcher(stripped);
        if (fragMatcher.matches()) {
            int quantity = Integer.parseInt(fragMatcher.group(1));
            treasureDropCount++;
            increment(treasureBreakdown, "Fortune Fragments", quantity);
            fortuneFragmentsCatches++;
            return;
        }

        String treasureName = extractTreasureName(stripped, clean);
        if (treasureName != null) {
            treasureDropCount++;
            increment(treasureBreakdown, treasureName);
            return;
        }

        Matcher plateMatcher = PLATE_PATTERN.matcher(stripped);
        if (plateMatcher.matches()) {
            int quantity = Integer.parseInt(plateMatcher.group(1));
            String plateName = toTitleCase(plateMatcher.group(2).trim());
            plateDropCount++;
            increment(plateBreakdown, plateName, quantity);
            increment(plateCatches, plateName);
            return;
        }

        Matcher fusedPlateMatcher = FUSED_PLATE_PATTERN.matcher(stripped);
        if (fusedPlateMatcher.matches()) {
            int quantity = Integer.parseInt(fusedPlateMatcher.group(2));
            String plateName = toTitleCase(fusedPlateMatcher.group(1).trim());
            plateDropCount++;
            increment(plateBreakdown, plateName, quantity);
            increment(plateCatches, plateName);
            return;
        }

        String trophyName = extractTrophyName(stripped, clean);
        if (trophyName != null) {
            trophyCount++;
            increment(trophyBreakdown, trophyName);
            return;
        }

        String seaName = extractSeaCreatureName(stripped, clean);
        if (seaName != null) {
            seaCreatureCount++;
            increment(seaCreatureBreakdown, seaName);
            return;
        }

        String crateName = extractCrateName(stripped);
        if (crateName != null) {
            crateCount++;
            increment(crateBreakdown, crateName);
        }
    }

    private static String extractTreasureName(String stripped, String clean) {
        Matcher m;

        if (clean.startsWith("* fish! you fished up")) {
            m = FISH_PATTERN.matcher(stripped);
            if (m.matches()) return m.group(1).trim();
        }

        if (clean.startsWith("* material! you fished up some")) {
            m = MATERIAL_PATTERN.matcher(stripped);
            if (m.matches()) return m.group(1).trim();
        }

        m = GREAT_CATCH_PATTERN.matcher(stripped);
        if (m.matches()) {
            return m.group(1).trim();
        }

        m = RARE_DROP_ITEM_PATTERN.matcher(stripped);
        if (m.matches()) {
            return toTitleCase(m.group(1).trim());
        }

        return null;
    }

    private static String extractTrophyName(String stripped, String clean) {
        if (clean.startsWith("* trophy!")) {
            Matcher m = TROPHY_PATTERN.matcher(stripped);
            if (m.matches()) {
                return m.group(1).trim();
            }
        }
        return null;
    }

    private static String extractSeaCreatureName(String stripped, String clean) {
        if (clean.contains("sea creature! you caught")) {
            Matcher m = SEA_CREATURE_PATTERN.matcher(stripped);
            if (m.matches()) {
                return m.group(1).trim();
            }
        }

        if (clean.contains("pulled your rod with you to it's underwater dungeon")) {
            return "Zephyr";
        }

        return null;
    }

    private static String extractCrateName(String stripped) {
        Matcher m = CRATE_PATTERN.matcher(stripped);
        if (m.matches()) {
            String rawName = m.group(1).trim();
            rawName = toTitleCase(rawName);
            rawName = rawName.replace("Creature ", "").replace("creature ", "");
            return rawName;
        }
        return null;
    }

    private static String toTitleCase(String input) {
        String[] words = input.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    private static void increment(Map<String, Integer> map, String key) {
        increment(map, key, 1);
    }

    private static void increment(Map<String, Integer> map, String key, int amount) {
        map.merge(key, amount, Integer::sum);
    }


    public static int getSeaCreatureCount()   { return seaCreatureCount; }
    public static int getTreasureDropCount()  { return treasureDropCount; }
    public static int getPlateDropCount()     { return plateDropCount; }
    public static int getCrateCount()         { return crateCount; }
    public static int getTrophyCount()        { return trophyCount; }
    public static int getDoubleHookCount()    { return doubleHookCount; }
    public static int getFortuneFragmentsCatches() { return fortuneFragmentsCatches; }
    public static int getTotalCount()         { return seaCreatureCount + treasureDropCount + plateDropCount + trophyCount; }

    public static int getPlateCatches(String plateName) { return plateCatches.get(plateName); }

    public static Map<String, Integer> getSeaCreatureBreakdown() { return seaCreatureBreakdown; }
    public static Map<String, Integer> getTreasureBreakdown()    { return treasureBreakdown; }
    public static Map<String, Integer> getPlateBreakdown()       { return plateBreakdown; }
    public static Map<String, Integer> getCrateBreakdown()       { return crateBreakdown; }
    public static Map<String, Integer> getTrophyBreakdown()      { return trophyBreakdown; }

    public static boolean isUltraRareDrop(String stripped) {
        return RARE_DROP_ITEM_PATTERN.matcher(stripped).matches()
                || FUSED_PLATE_PATTERN.matcher(stripped).matches();
    }

    public static boolean isTrackableFishingMessage(String stripped) {
        String clean = stripped.toLowerCase(Locale.ROOT);

        if (DOUBLE_HOOK_PATTERN.matcher(stripped).matches()) return true;
        if (RARE_DROP_FRAGMENTS_PATTERN.matcher(stripped).matches()) return true;
        if (extractTreasureName(stripped, clean) != null) return true;
        if (PLATE_PATTERN.matcher(stripped).matches()) return true;
        if (FUSED_PLATE_PATTERN.matcher(stripped).matches()) return true;
        if (extractTrophyName(stripped, clean) != null) return true;
        if (extractSeaCreatureName(stripped, clean) != null) return true;
        if (extractCrateName(stripped) != null) return true;

        return false;
    }

    public static void reset() {
        seaCreatureCount = 0;
        treasureDropCount = 0;
        plateDropCount = 0;
        crateCount = 0;
        trophyCount = 0;
        doubleHookCount = 0;
        fortuneFragmentsCatches = 0;
        plateCatches.clear();
        seaCreatureBreakdown.clear();
        treasureBreakdown.clear();
        plateBreakdown.clear();
        crateBreakdown.clear();
        trophyBreakdown.clear();
    }
}