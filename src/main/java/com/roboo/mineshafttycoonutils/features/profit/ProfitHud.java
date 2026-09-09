package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.ProfitCategory;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import com.roboo.mineshafttycoonutils.hud.MovableHud;
import com.roboo.mineshafttycoonutils.utils.HudZones;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import com.roboo.mineshafttycoonutils.utils.NumberFormatUtils;
import com.roboo.mineshafttycoonutils.utils.TimeFormatUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;

public class ProfitHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;

    private static final MovableHud PROFIT_BOX = new MovableHud() {
        @Override
        public String getDisplayName() {
            return "Profit Tracker";
        }

        @Override
        public boolean isMasterEnabled() {
            return ConfigManager.config.profit.tracker.profitTrackerEnabled;
        }

        @Override
        public int getX() {
            ProfitCategory cfg = ConfigManager.config.profit;
            int anchorX = cfg.tracker.profitHudX;
            return HudTextUtils.isRightAligned(anchorX, cfg.tracker.disableRightAlignFlip) ? anchorX - getWidth() : anchorX;
        }

        @Override
        public int getY() {
            return ConfigManager.config.profit.tracker.profitHudY;
        }

        @Override
        public int getWidth() {
            return Math.round(calcWidth() * HudScale.normalize(ConfigManager.config.profit.tracker.profitHudScale));
        }

        @Override
        public int getHeight() {
            return Math.round(calcHeight() * HudScale.normalize(ConfigManager.config.profit.tracker.profitHudScale));
        }

        @Override
        public float getScale() {
            return ConfigManager.config.profit.tracker.profitHudScale;
        }

        @Override
        public void setScale(float scale) {
            ConfigManager.config.profit.tracker.profitHudScale = HudScale.clamp(scale);
        }

        @Override
        public void setPosition(int x, int y) {
            ConfigManager.config.profit.tracker.profitHudX = x;
            ConfigManager.config.profit.tracker.profitHudY = y;
        }

        @Override
        public void render(GuiGraphics graphics) {
            ProfitCategory cfg = ConfigManager.config.profit;
            drawContent(graphics, cfg.tracker.profitHudX, cfg.tracker.profitHudY);
        }
    };

    public static void init() {
        HudEditorRegistry.register(PROFIT_BOX);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "profit_hud"),
                (graphics, tickCounter) -> {
                    ProfitCategory cfg = ConfigManager.config.profit;
                    if (mc.player == null || !cfg.tracker.profitTrackerEnabled) return;
                    if (cfg.tracker.hideInOtherZones
                            && (HudZones.isInFishingZone(mc.player.blockPosition()) || HudZones.isInPveZone(mc.player.blockPosition())))
                        return;

                    int totalHeight = Math.round(calcHeight() * HudScale.normalize(cfg.tracker.profitHudScale));
                    int x = HudTextUtils.clampX(cfg.tracker.profitHudX);
                    int y = HudTextUtils.clampY(cfg.tracker.profitHudY, totalHeight);

                    drawContent(graphics, x, y);
                }
        );
    }

    private static final String TITLE_TEXT = "§lProfit Tracker";
    private static final String ORES_HEADER_TEXT = "§lOres";

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        ProfitCategory cfg = ConfigManager.config.profit;
        LinkedHashMap<String, Integer> breakdown = OreDropTracker.getBreakdown();
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.tracker.disableRightAlignFlip);
        int titleColor = HudTextUtils.chromaToArgb(cfg.tracker.titleColor);
        float scale = HudScale.normalize(cfg.tracker.profitHudScale);
        int unscaledWidth = calcWidth();
        int screenLeft = rightAligned ? anchorX - Math.round(unscaledWidth * scale) : anchorX;
        int localAnchorX = rightAligned ? unscaledWidth : 0;

        graphics.pose().pushMatrix();
        graphics.pose().translate(screenLeft, y);
        graphics.pose().scale(scale, scale);

        HudTextUtils.drawLine(graphics, TITLE_TEXT, localAnchorX, 0, rightAligned, titleColor);
        int line = 1;

        if (ProfitTracker.needsRamLevel()) {
            HudTextUtils.drawLine(graphics, "§7Boosts: §c(Open refinery & /pets)", localAnchorX, LINE_HEIGHT * line++, rightAligned);
        }

        if (cfg.tracker.showMiningTime) {
            HudTextUtils.drawLine(graphics, miningTimeText(cfg), localAnchorX, LINE_HEIGHT * line++, rightAligned);
        }

        HudTextUtils.drawLine(graphics, profitPerHourText(cfg), localAnchorX, LINE_HEIGHT * line++, rightAligned);
        HudTextUtils.drawLine(graphics, "§7Total: §e$" + NumberFormatUtils.formatShortened(ProfitTracker.getTotalProfit(), cfg.shortenNumbers),
                localAnchorX, LINE_HEIGHT * line++, rightAligned);

        if (cfg.tracker.showOreDrops) {
            HudTextUtils.drawLine(graphics, ORES_HEADER_TEXT, localAnchorX, LINE_HEIGHT * line++, rightAligned, titleColor);

            if (breakdown.isEmpty()) {
                HudTextUtils.drawLine(graphics, "§7- None", localAnchorX, LINE_HEIGHT * line, rightAligned);
            } else {
                for (var entry : breakdown.entrySet()) {
                    HudTextUtils.drawLine(graphics, "§7- " + entry.getKey() + " §7(§e" + entry.getValue() + "§7)",
                            localAnchorX, LINE_HEIGHT * line++, rightAligned);
                }
            }
        }

        graphics.pose().popMatrix();
    }

    private static String profitPerHourText(ProfitCategory cfg) {
        String color = ProfitTracker.isPaused() ? "§c" : "§e";
        return "§7$/Hour: " + color + "$" + NumberFormatUtils.formatShortened(ProfitTracker.getProfitPerHour(), cfg.shortenNumbers) + "/hr";
    }

    private static String miningTimeText(ProfitCategory cfg) {
        String color = ProfitTracker.isPaused() ? "§c" : "§e";
        return "§7Mining Time: " + color + TimeFormatUtils.formatDuration(ProfitTracker.getUptimeSeconds(), cfg.tracker.showMiningTimeSeconds);
    }

    private static int countTotalLines(ProfitCategory cfg, LinkedHashMap<String, Integer> breakdown) {
        int total = 1;
        if (ProfitTracker.needsRamLevel()) total++;
        if (cfg.tracker.showMiningTime) total++;
        total += 2;

        if (cfg.tracker.showOreDrops) {
            total++;
            total += breakdown.isEmpty() ? 1 : breakdown.size();
        }

        return total;
    }

    private static int calcWidth() {
        ProfitCategory cfg = ConfigManager.config.profit;
        LinkedHashMap<String, Integer> breakdown = OreDropTracker.getBreakdown();
        int width = mc.font.width(TITLE_TEXT);

        if (ProfitTracker.needsRamLevel()) {
            width = Math.max(width, mc.font.width("§7Boosts: §c(Open refinery & /pets)"));
        }
        if (cfg.tracker.showMiningTime) {
            width = Math.max(width, mc.font.width(miningTimeText(cfg)));
        }
        width = Math.max(width, mc.font.width(profitPerHourText(cfg)));
        width = Math.max(width, mc.font.width("§7Total: §e$" + NumberFormatUtils.formatShortened(ProfitTracker.getTotalProfit(), cfg.shortenNumbers)));

        if (cfg.tracker.showOreDrops) {
            width = Math.max(width, mc.font.width(ORES_HEADER_TEXT));
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

    private static int calcHeight() {
        ProfitCategory cfg = ConfigManager.config.profit;
        return countTotalLines(cfg, OreDropTracker.getBreakdown()) * LINE_HEIGHT;
    }
}