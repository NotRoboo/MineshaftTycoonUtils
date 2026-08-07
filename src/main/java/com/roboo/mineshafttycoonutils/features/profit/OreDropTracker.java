package com.roboo.mineshafttycoonutils.features.profit;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class OreDropTracker {

    private static final LinkedHashMap<String, String> PATTERNS = new LinkedHashMap<>();
    static {
        PATTERNS.put("rare drop! lunar fragment", "Lunar Fragment");
        PATTERNS.put("rare drop! basalt shard", "Basalt Shard");
        PATTERNS.put("rare drop! chisilite shard", "Chisilite Shard");
        PATTERNS.put("rare drop! brecca powder", "Brecca Powder");
        PATTERNS.put("rare drop! regolith soil", "Regolith Soil");
        PATTERNS.put("rare drop! amber fragment", "Amber Fragment");
        PATTERNS.put("rare drop! martian dust", "Martian Dust");
        PATTERNS.put("rare drop! ametrine", "Ametrine");
        PATTERNS.put("rare drop! rhodnite", "Rhodnite");
        PATTERNS.put("rare drop! sun fragment", "Sun Fragment");
        PATTERNS.put("rare drop! plasma shard", "Plasma Shard");
        PATTERNS.put("rare drop! fiber", "Fiber");
    }

    private static final Map<String, Integer> counts = new LinkedHashMap<>();

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> handleMessage(msg.getString()));
        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, timestamp) -> handleMessage(msg.getString()));
    }

    private static void handleMessage(String msg) {
        if (msg == null) return;
        String stripped = msg.replaceAll("§.", "").trim();
        if (stripped.isEmpty()) return;
        String lower = stripped.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, String> entry : PATTERNS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                counts.merge(entry.getValue(), 1, Integer::sum);
                return;
            }
        }
    }

    public static LinkedHashMap<String, Integer> getBreakdown() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (String name : PATTERNS.values()) {
            Integer count = counts.get(name);
            if (count != null && count > 0) result.put(name, count);
        }
        return result;
    }

    public static void reset() {
        counts.clear();
    }
}