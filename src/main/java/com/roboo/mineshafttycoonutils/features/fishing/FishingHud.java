package com.roboo.mineshafttycoonutils.features.fishing;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.FishingCategory;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.MovableHud;
import com.roboo.mineshafttycoonutils.utils.FishingZones;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FishingHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;

    private static final MovableHud MOVABLE = new MovableHud() {
        @Override
        public String getDisplayName() {
            return "Fishing Tracker";
        }

        @Override
        public boolean isMasterEnabled() {
            return cfg().hudEnabled;
        }

        @Override
        public int getX() {
            FishingCategory cfg = cfg();
            int anchorX = cfg.hudX;
            return HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip) ? anchorX - getWidth() : anchorX;
        }

        @Override
        public int getY() {
            return cfg().hudY;
        }

        @Override
        public int getWidth() {
            return computeWidth();
        }

        @Override
        public int getHeight() {
            return computeHeight();
        }

        @Override
        public void setPosition(int x, int y) {
            cfg().hudX = x;
            cfg().hudY = y;
        }

        @Override
        public void render(GuiGraphics graphics) {
            FishingCategory cfg = cfg();
            drawContent(graphics, cfg.hudX, cfg.hudY);
        }
    };

    public static void init() {
        HudEditorRegistry.register(MOVABLE);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "fishing_hud"),
                (graphics, tickCounter) -> {
                    FishingCategory cfg = cfg();
                    if (mc.player == null || !cfg.hudEnabled) return;
                    if (cfg.onlyShowWhenFishing && !FishingZones.isInZone(mc.player.blockPosition())) return;

                    int totalHeight = computeHeight();
                    int x = HudTextUtils.clampX(cfg.hudX);
                    int y = HudTextUtils.clampY(cfg.hudY, totalHeight);

                    drawContent(graphics, x, y);
                }
        );
    }

    private static FishingCategory cfg() {
        return ConfigManager.config.fishing;
    }

    private static final String TITLE_TEXT = "§lFishing Tracker";

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        FishingCategory cfg = cfg();
        List<String> lines = computeLines(cfg);
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);

        HudTextUtils.drawLine(graphics, TITLE_TEXT, anchorX, y, rightAligned, HudTextUtils.chromaToArgb(cfg.titleColor));
        int line = 1;
        for (String l : lines) {
            HudTextUtils.drawLine(graphics, l, anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        }
    }

    private static List<String> computeLines(FishingCategory cfg) {
        List<String> lines = new ArrayList<>();

        for (FishingCategory.LineEntry entry : cfg.hudLineOrder) {
            switch (entry) {
                case TOTAL -> {
                    int doubleHooks = FishingTracker.getDoubleHookCount();
                    int displayTotal = FishingTracker.getTotalCount() - doubleHooks;
                    String totalLine = "§7Total: §e" + displayTotal;
                    if (cfg.showDoubleHookCount && doubleHooks > 0) {
                        totalLine += " §7(§e" + doubleHooks + "dh§7)";
                    }
                    lines.add(totalLine);
                }
                case TREASURE_DROPS -> {
                    int combinedCount = FishingTracker.getTreasureDropCount() + FishingTracker.getPlateDropCount();
                    lines.add("§7Treasure Drops: §e" + combinedCount);

                    if (cfg.treasure.breakdownEnabled) {
                        Map<String, Integer> combinedBreakdown = new LinkedHashMap<>(FishingTracker.getTreasureBreakdown());
                        combinedBreakdown.putAll(FishingTracker.getPlateBreakdown());
                        lines.addAll(breakdownLines(cfg, combinedBreakdown, names(cfg.treasure.order)));
                    }
                }
                case TROPHY_FISH -> {
                    lines.add("§7Trophy Fish: §e" + FishingTracker.getTrophyCount());
                    if (cfg.trophyFish.breakdownEnabled) {
                        lines.addAll(breakdownLines(cfg, FishingTracker.getTrophyBreakdown(), names(cfg.trophyFish.order)));
                    }
                }
                case SEA_CREATURES -> {
                    lines.add("§7Sea Creatures: §e" + FishingTracker.getSeaCreatureCount());
                    if (cfg.seaCreatures.breakdownEnabled) {
                        lines.addAll(breakdownLines(cfg, FishingTracker.getSeaCreatureBreakdown(), names(cfg.seaCreatures.order)));
                    }
                }
                case CRATES -> {
                    lines.add("§7Crates: §e" + FishingTracker.getCrateCount());
                    if (cfg.crates.breakdownEnabled) {
                        lines.addAll(breakdownLines(cfg, FishingTracker.getCrateBreakdown(), names(cfg.crates.order)));
                    }
                }
            }
        }

        return lines;
    }

    private static List<String> breakdownLines(FishingCategory cfg, Map<String, Integer> breakdown, List<String> orderList) {
        List<String> result = new ArrayList<>();

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

                result.add("  §7- " + colorCode + displayName + "§7: §e" + valueText);
            }
        }

        return result;
    }

    private static List<String> names(List<?> orderEntries) {
        List<String> result = new ArrayList<>(orderEntries.size());
        for (Object entry : orderEntries) {
            result.add(entry.toString());
        }
        return result;
    }

    private static int computeWidth() {
        FishingCategory cfg = cfg();
        int width = mc.font.width(TITLE_TEXT);
        for (String line : computeLines(cfg)) {
            width = Math.max(width, mc.font.width(line));
        }
        return width;
    }

    private static int computeHeight() {
        List<String> lines = computeLines(cfg());
        return (lines.size() + 1) * LINE_HEIGHT;
    }

    private static String getColorForName(String name) {
        return switch (name.toLowerCase()) {
            case "emerald" -> "§2";

            case "abyssal crate", "small fish", "red herring", "clownfish" -> "§3";

            case "phantom fisher", "captain barbossa", "epic crate", "kraken scale", "puffer fish" -> "§5";

            case "zephyr", "locked lsc", "legendary crate", "golden crate", "fortune fragments", "golden coral",
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