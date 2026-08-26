package com.roboo.mineshafttycoonutils.features.profit;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class OreDropTracker {

    private static final LinkedHashMap<String, String> ORE_CHAT_PATTERNS = new LinkedHashMap<>();
    static {
        ORE_CHAT_PATTERNS.put("rare drop! lunar fragment", "Lunar Fragment");
        ORE_CHAT_PATTERNS.put("rare drop! basalt shard", "Basalt Shard");
        ORE_CHAT_PATTERNS.put("rare drop! chisilite shard", "Chisilite Shard");
        ORE_CHAT_PATTERNS.put("rare drop! brecca powder", "Brecca Powder");
        ORE_CHAT_PATTERNS.put("rare drop! regolith soil", "Regolith Soil");
        ORE_CHAT_PATTERNS.put("rare drop! amber fragment", "Amber Fragment");
        ORE_CHAT_PATTERNS.put("rare drop! martian dust", "Martian Dust");
        ORE_CHAT_PATTERNS.put("rare drop! ametrine", "Ametrine");
        ORE_CHAT_PATTERNS.put("rare drop! rhodnite", "Rhodnite");
        ORE_CHAT_PATTERNS.put("rare drop! sun fragment", "Sun Fragment");
        ORE_CHAT_PATTERNS.put("epic drop! plasma shard", "Plasma Shard");
        ORE_CHAT_PATTERNS.put("epic drop! fiber", "Fiber");
    }

    private static final Map<String, Integer> counts = new LinkedHashMap<>();

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
        String lower = stripped.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, String> entry : ORE_CHAT_PATTERNS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                counts.merge(entry.getValue(), 1, Integer::sum);
                return;
            }
        }
    }

    public static LinkedHashMap<String, Integer> getBreakdown() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (String name : ORE_CHAT_PATTERNS.values()) {
            Integer count = counts.get(name);
            if (count != null && count > 0) result.put(name, count);
        }
        return result;
    }

    public static void reset() {
        counts.clear();
    }
}
