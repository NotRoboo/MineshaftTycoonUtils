package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.ProfitCategory;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.MovableHud;
import com.roboo.mineshafttycoonutils.utils.FishingZones;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;

public class ProfitHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;

    private static final String[] SUFFIXES = {
            "", "K", "M", "B", "T", "Qd", "Qn", "Sx", "Sp", "Oc", "No", "Dc"
    };

    private static final MovableHud MOVABLE = new MovableHud() {
        @Override
        public String getDisplayName() {
            return "Profit Tracker";
        }

        @Override
        public boolean isMasterEnabled() {
            return ConfigManager.config.profit.profitTrackerEnabled;
        }

        @Override
        public int getX() {
            ProfitCategory cfg = ConfigManager.config.profit;
            int anchorX = cfg.profitHudX;
            return HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip) ? anchorX - getWidth() : anchorX;
        }

        @Override
        public int getY() {
            return ConfigManager.config.profit.profitHudY;
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
            ConfigManager.config.profit.profitHudX = x;
            ConfigManager.config.profit.profitHudY = y;
        }

        @Override
        public void render(GuiGraphics graphics) {
            ProfitCategory cfg = ConfigManager.config.profit;
            drawContent(graphics, cfg.profitHudX, cfg.profitHudY);
        }
    };

    public static void init() {
        HudEditorRegistry.register(MOVABLE);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "profit_hud"),
                (graphics, tickCounter) -> {
                    ProfitCategory cfg = ConfigManager.config.profit;
                    if (mc.player == null || !cfg.profitTrackerEnabled) return;
                    if (cfg.onlyShowWhenMining && FishingZones.isInZone(mc.player.blockPosition())) return;

                    int totalHeight = computeHeight();
                    int x = HudTextUtils.clampX(cfg.profitHudX);
                    int y = HudTextUtils.clampY(cfg.profitHudY, totalHeight);

                    drawContent(graphics, x, y);
                }
        );
    }

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        ProfitCategory cfg = ConfigManager.config.profit;
        LinkedHashMap<String, Integer> breakdown = OreDropTracker.getBreakdown();
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);

        HudTextUtils.drawLine(graphics, "§e§lProfit Tracker", anchorX, y, rightAligned);
        int line = 1;

        if (ProfitTracker.needsRamLevel()) {
            HudTextUtils.drawLine(graphics, "§7Boosts: §c(Open refinery & /pets)", anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        }

        HudTextUtils.drawLine(graphics, "§7$/Hour: §e$" + format(ProfitTracker.getProfitPerHour(), cfg.shortenNumbers) + "/hr",
                anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        HudTextUtils.drawLine(graphics, "§7Total: §e$" + format(ProfitTracker.getTotalProfit(), cfg.shortenNumbers),
                anchorX, y + (LINE_HEIGHT * line++), rightAligned);

        if (cfg.showOreDrops) {
            HudTextUtils.drawLine(graphics, "§e§lOres", anchorX, y + (LINE_HEIGHT * line++), rightAligned);

            if (breakdown.isEmpty()) {
                HudTextUtils.drawLine(graphics, "§7- None", anchorX, y + (LINE_HEIGHT * line), rightAligned);
            } else {
                for (var entry : breakdown.entrySet()) {
                    HudTextUtils.drawLine(graphics, "§7- " + entry.getKey() + " §7(§e" + entry.getValue() + "§7)",
                            anchorX, y + (LINE_HEIGHT * line++), rightAligned);
                }
            }
        }
    }

    private static int countTotalLines(ProfitCategory cfg, LinkedHashMap<String, Integer> breakdown) {
        int total = 1; // header
        if (ProfitTracker.needsRamLevel()) total++;
        total += 2; // $/Hour, Total

        if (cfg.showOreDrops) {
            total++; // "Ores" header
            total += breakdown.isEmpty() ? 1 : breakdown.size();
        }

        return total;
    }

    private static int computeWidth() {
        ProfitCategory cfg = ConfigManager.config.profit;
        LinkedHashMap<String, Integer> breakdown = OreDropTracker.getBreakdown();
        int width = mc.font.width("§e§lProfit Tracker");

        if (ProfitTracker.needsRamLevel()) {
            width = Math.max(width, mc.font.width("§7Boosts: §c(Open refinery & /pets)"));
        }
        width = Math.max(width, mc.font.width("§7$/Hour: §e$" + format(ProfitTracker.getProfitPerHour(), cfg.shortenNumbers) + "/hr"));
        width = Math.max(width, mc.font.width("§7Total: §e$" + format(ProfitTracker.getTotalProfit(), cfg.shortenNumbers)));

        if (cfg.showOreDrops) {
            width = Math.max(width, mc.font.width("§e§lOres"));
            if (breakdown.isEmpty()) {
                width = Math.max(width, mc.font.width("§7- None"));
            } else {
                for (var entry : breakdown.entrySet()) {
                    width = Math.max(width, mc.font.width("§7- " + entry.getKey() + " §7(§e" + entry.getValue() + "§7)"));
                }
            }
        }

        return width;
    }

    private static int computeHeight() {
        ProfitCategory cfg = ConfigManager.config.profit;
        return countTotalLines(cfg, OreDropTracker.getBreakdown()) * LINE_HEIGHT;
    }

    private static String format(long value, boolean shorten) {
        if (!shorten || value < 1000) {
            return String.format("%,d", value);
        }

        int tier = 0;
        double reduced = value;
        while (reduced >= 1000 && tier < SUFFIXES.length - 1) {
            reduced /= 1000.0;
            tier++;
        }

        return String.format("%.2f%s", reduced, SUFFIXES[tier]);
    }
}