package com.roboo.mineshafttycoonutils.features.fishing;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.FishingCategory;
import com.roboo.mineshafttycoonutils.utils.FishingZones;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FishingHud {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "fishing_hud"),
                (graphics, tickCounter) -> {
                    FishingCategory cfg = cfg();
                    if (mc.player == null || !cfg.hudEnabled) return;
                    if (cfg.onlyShowWhenFishing && !FishingZones.isInZone(mc.player.blockPosition())) return;

                    int x = cfg.hudPosition.hudX;
                    int y = cfg.hudPosition.hudY;
                    int lineHeight = 10;
                    int line = 0;

                    graphics.drawString(mc.font, "§e§lFishing Tracker", x, y, 0xFFFFFFFF, true);
                    line++;

                    for (FishingCategory.LineEntry entry : cfg.hudLineOrder) {
                        switch (entry) {
                            case TOTAL -> {
                                int doubleHooks = FishingTracker.getDoubleHookCount();
                                int displayTotal = FishingTracker.getTotalCount() - doubleHooks;
                                String totalLine = "§7Total: §e" + displayTotal;
                                if (cfg.showDoubleHookCount && doubleHooks > 0) {
                                    totalLine += " §7(§e" + doubleHooks + "dh§7)";
                                }
                                graphics.drawString(mc.font, totalLine, x, y + (lineHeight * line++), 0xFFFFFFFF, true);
                            }
                            case TREASURE_DROPS -> {
                                int combinedCount = FishingTracker.getTreasureDropCount() + FishingTracker.getPlateDropCount();
                                graphics.drawString(mc.font, "§7Treasure Drops: §e" + combinedCount,
                                        x, y + (lineHeight * line++), 0xFFFFFFFF, true);

                                if (cfg.treasure.breakdownEnabled) {
                                    Map<String, Integer> combinedBreakdown = new LinkedHashMap<>(FishingTracker.getTreasureBreakdown());
                                    combinedBreakdown.putAll(FishingTracker.getPlateBreakdown());

                                    line += drawBreakdown(graphics, combinedBreakdown, names(cfg.treasure.order), x, y, lineHeight, line);
                                }
                            }
                            case TROPHY_FISH -> {
                                graphics.drawString(mc.font, "§7Trophy Fish: §e" + FishingTracker.getTrophyCount(),
                                        x, y + (lineHeight * line++), 0xFFFFFFFF, true);
                                if (cfg.trophyFish.breakdownEnabled) {
                                    line += drawBreakdown(graphics, FishingTracker.getTrophyBreakdown(),
                                            names(cfg.trophyFish.order), x, y, lineHeight, line);
                                }
                            }
                            case SEA_CREATURES -> {
                                graphics.drawString(mc.font, "§7Sea Creatures: §e" + FishingTracker.getSeaCreatureCount(),
                                        x, y + (lineHeight * line++), 0xFFFFFFFF, true);
                                if (cfg.seaCreatures.breakdownEnabled) {
                                    line += drawBreakdown(graphics, FishingTracker.getSeaCreatureBreakdown(),
                                            names(cfg.seaCreatures.order), x, y, lineHeight, line);
                                }
                            }
                            case CRATES -> {
                                graphics.drawString(mc.font, "§7Crates: §e" + FishingTracker.getCrateCount(),
                                        x, y + (lineHeight * line++), 0xFFFFFFFF, true);
                                if (cfg.crates.breakdownEnabled) {
                                    line += drawBreakdown(graphics, FishingTracker.getCrateBreakdown(),
                                            names(cfg.crates.order), x, y, lineHeight, line);
                                }
                            }
                        }
                    }
                }
        );
    }

    private static FishingCategory cfg() {
        return ConfigManager.config.fishing;
    }

    private static List<String> names(List<?> orderEntries) {
        List<String> result = new ArrayList<>(orderEntries.size());
        for (Object entry : orderEntries) {
            result.add(entry.toString());
        }
        return result;
    }

    private static int drawBreakdown(net.minecraft.client.gui.GuiGraphics graphics,
                                     Map<String, Integer> breakdown,
                                     List<String> orderList,
                                     int x, int y, int lineHeight, int startLine) {
        int line = startLine;
        FishingCategory cfg = cfg();

        for (String orderedName : orderList) {
            Integer count = breakdown.get(orderedName);
            if (count != null && count > 0) {
                String colorCode = cfg.coloredNamesEnabled
                        ? getColorForName(orderedName)
                        : "§f";

                String displayName = orderedName;
                String valueText = String.valueOf(count);

                if (orderedName.equals("Fortune Fragments")) {
                    displayName = "Fortune Frags";
                    valueText = String.valueOf(FishingTracker.getFortuneFragmentsCatches());
                    if (cfg.treasure.showFortuneFragmentsTotal) {
                        valueText += " (" + count + ")";
                    }
                }

                if (FishingTracker.PLATE_ORDER.contains(orderedName)) {
                    displayName = orderedName + " Plate";
                    valueText = String.valueOf(FishingTracker.getPlateCatches(orderedName));
                    if (cfg.treasure.showPlateTotal) {
                        valueText += " (" + count + ")";
                    }
                }

                graphics.drawString(mc.font,
                        "  §7- " + colorCode + displayName + "§7: §e" + valueText,
                        x, y + (lineHeight * line++), 0xFFFFFFFF, true);
            }
        }

        for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            if (!orderList.contains(entry.getKey()) && entry.getValue() > 0
                    && !FishingTracker.PLATE_ORDER.contains(entry.getKey())) {
                String colorCode = getColorForName(entry.getKey());
                graphics.drawString(mc.font,
                        "  §7- " + colorCode + entry.getKey() + "§7: §e" + entry.getValue(),
                        x, y + (lineHeight * line++), 0xFFFFFFFF, true);
            }
        }

        return line - startLine;
    }

    private static String getColorForName(String name) {
        return switch (name.toLowerCase()) {
            case "emerald" -> "§2";

            case "abyssal crate", "small fish", "red herring", "clownfish" -> "§3";

            case "phantom fisher", "captain barbossa", "epic crate", "kraken scale", "puffer fish" -> "§5";

            case "zephyr", "legendary crate", "golden crate", "fortune fragments", "golden coral",
                 "gold", "royal fish" -> "§6";

            case "common crate", "coal" -> "§7";

            case "scarfion", "cthulhu", "attack squid", "rare crate" -> "§9";

            case "supreme leech", "deep sea scientist", "fafnir", "uncommon crate", "pufferfish",
                 "anglerfish bulb", "sea turtle scute", "octopus", "clown fish" -> "§a";

            case "pet crate", "ancient trident", "diamond" -> "§b";

            case "mythic crate", "mythical crate" -> "§c";

            case "mermaid's pearl", "fused" -> "§d";

            case "trash squid", "iron", "lost trainer", "diver", "wood", "steel" -> "§f";

            default -> "§8";
        };
    }
}