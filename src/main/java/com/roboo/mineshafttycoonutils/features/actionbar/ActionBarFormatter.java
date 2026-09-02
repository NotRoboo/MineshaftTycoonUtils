package com.roboo.mineshafttycoonutils.features.actionbar;

import com.roboo.mineshafttycoonutils.config.categories.ActionBarCategory;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.ComponentTextUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ActionBarFormatter {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final String SEGMENT_SEPARATOR = "§8┃";
    private static final String MINOR_SEPARATOR = "§8|";
    private static final String REBUILD_SEPARATOR = " §8┃ ";

    private ActionBarFormatter() {}

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            if (!overlay) return true;
            return handle(msg);
        });
    }

    private static boolean handle(Component msg) {
        if (!ConfigManager.config.actionBar.enabled) return true;

        String raw = ComponentTextUtils.formattedText(msg);
        if (raw.isEmpty()) return true;

        if (raw.contains("Ship HP")) return rebuildOcean(raw);
        if (raw.contains("Oxygen")) return rebuildSpace(raw);
        if (raw.contains("Goal")) return rebuildMining(raw);

        return true;
    }

    private static boolean rebuildOcean(String raw) {
        ActionBarCategory cfg = ConfigManager.config.actionBar;
        Map<ActionBarCategory.OceanBar, String> parts = new LinkedHashMap<>();

        for (String chunk : splitIntoChunks(raw)) {
            if (chunk.contains("Ship HP")) parts.put(ActionBarCategory.OceanBar.SHIP_HP, chunk);
            else if (chunk.contains("Ship DMG")) parts.put(ActionBarCategory.OceanBar.SHIP_DMG, chunk);
            else if (chunk.contains("Fishing Speed")) parts.put(ActionBarCategory.OceanBar.FISHING_SPEED, chunk);
            else if (chunk.contains("SCC")) parts.put(ActionBarCategory.OceanBar.SCC, chunk);
            else if (chunk.contains("DHC")) parts.put(ActionBarCategory.OceanBar.DHC, chunk);
        }

        return applyRebuilt(cfg.oceanOrder, parts);
    }

    private static boolean rebuildMining(String raw) {
        ActionBarCategory cfg = ConfigManager.config.actionBar;
        Map<ActionBarCategory.DefaultBar, String> parts = new LinkedHashMap<>();

        for (String chunk : splitIntoChunks(raw)) {
            if (chunk.contains("ID")) {
                parts.put(ActionBarCategory.DefaultBar.ID, chunk);
            } else if (chunk.contains("XP") && chunk.contains("/")) {
                parts.put(ActionBarCategory.DefaultBar.XP_PROGRESS, chunk);
            } else if (chunk.contains("Goal")) {
                parts.put(ActionBarCategory.DefaultBar.GOAL, chunk);
            } else if (chunk.contains(MINOR_SEPARATOR)) {
                String[] minor = chunk.split(Pattern.quote(MINOR_SEPARATOR), 2);
                if (minor.length == 2) {
                    parts.put(ActionBarCategory.DefaultBar.XP, minor[0].trim());
                    parts.put(ActionBarCategory.DefaultBar.FORTUNE, minor[1].trim());
                }
            }
        }

        return applyRebuilt(cfg.defaultOrder, parts);
    }

    private static boolean rebuildSpace(String raw) {
        ActionBarCategory cfg = ConfigManager.config.actionBar;
        Map<ActionBarCategory.SpaceBar, String> parts = new LinkedHashMap<>();

        for (String chunk : splitIntoChunks(raw)) {
            if (chunk.contains("ID")) {
                parts.put(ActionBarCategory.SpaceBar.ID, chunk);
            } else if (chunk.contains("XP") && chunk.contains("/")) {
                parts.put(ActionBarCategory.SpaceBar.XP_PROGRESS, chunk);
            } else if (chunk.contains("Oxygen")) {
                parts.put(ActionBarCategory.SpaceBar.OXYGEN, chunk);
            } else if (chunk.contains(MINOR_SEPARATOR)) {
                String[] minor = chunk.split(Pattern.quote(MINOR_SEPARATOR), 2);
                if (minor.length == 2) {
                    parts.put(ActionBarCategory.SpaceBar.XP, minor[0].trim());
                    parts.put(ActionBarCategory.SpaceBar.FORTUNE, minor[1].trim());
                }
            }
        }

        return applyRebuilt(cfg.spaceOrder, parts);
    }

    private static <T> boolean applyRebuilt(List<T> order, Map<T, String> parts) {
        if (parts.isEmpty()) return true;

        StringBuilder rebuilt = new StringBuilder();
        boolean first = true;

        for (T part : order) {
            String chunk = parts.get(part);
            if (chunk == null) continue;

            if (!first) rebuilt.append(REBUILD_SEPARATOR);
            rebuilt.append(chunk);
            first = false;
        }

        mc.gui.setOverlayMessage(legacyToComponent(rebuilt.toString()), false);
        return false;
    }

    private static String[] splitIntoChunks(String raw) {
        String[] chunks = raw.split(Pattern.quote(SEGMENT_SEPARATOR));
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = chunks[i].trim();
        }
        return chunks;
    }

    private static Component legacyToComponent(String legacyText) {
        MutableComponent result = Component.empty();
        Style style = Style.EMPTY;

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < legacyText.length(); i++) {
            char c = legacyText.charAt(i);
            if (c == '§' && i + 1 < legacyText.length()) {
                if (!buffer.isEmpty()) {
                    result.append(Component.literal(buffer.toString()).setStyle(style));
                    buffer.setLength(0);
                }
                ChatFormatting formatting = ChatFormatting.getByCode(legacyText.charAt(i + 1));
                if (formatting != null) {
                    style = formatting.isColor() ? Style.EMPTY.withColor(formatting) : style.applyFormat(formatting);
                }
                i++;
            } else {
                buffer.append(c);
            }
        }
        if (!buffer.isEmpty()) {
            result.append(Component.literal(buffer.toString()).setStyle(style));
        }

        return result;
    }
}