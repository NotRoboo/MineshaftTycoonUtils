package com.roboo.mineshafttycoonutils.features.scoreboard;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.ScoreboardCategory;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.MovableHud;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class ScoreboardHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 9;

    private static final MovableHud SCOREBOARD_BOX = new MovableHud() {
        @Override
        public String getDisplayName() {
            return "Scoreboard";
        }

        @Override
        public boolean isMasterEnabled() {
            return ConfigManager.config.scoreboard.enabled;
        }

        @Override
        public int getX() {
            return resolveLeft(CustomScoreboardManager.formatDisplayLines());
        }

        @Override
        public int getY() {
            return resolveTop(CustomScoreboardManager.formatDisplayLines());
        }

        @Override
        public int getWidth() {
            return computeWidth(CustomScoreboardManager.formatDisplayLines());
        }

        @Override
        public int getHeight() {
            return computeTotalHeight(CustomScoreboardManager.formatDisplayLines());
        }

        @Override
        public void setPosition(int x, int y) {
            ConfigManager.config.scoreboard.hudX = x;
            ConfigManager.config.scoreboard.hudY = y;
        }

        @Override
        public void render(GuiGraphics graphics) {
            renderLines(graphics, CustomScoreboardManager.formatDisplayLines());
        }
    };

    public static void init() {
        HudEditorRegistry.register(SCOREBOARD_BOX);
    }

    public static void renderLines(GuiGraphics graphics, List<String> lines) {
        int width = computeWidth(lines);
        int totalHeight = computeTotalHeight(lines);
        int left = resolveLeft(lines);
        int top = resolveTop(lines);
        int right = left + width;

        graphics.fill(left, top - 2, right, top + totalHeight, 0x4E000000);

        int y = top;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.isEmpty()) {
                int x = i == 0
                        ? left + (width - mc.font.width(line)) / 2
                        : left + 3;
                graphics.drawString(mc.font, line, x, y, 0xFFFFFFFF, true);
            }
            y += LINE_HEIGHT;
        }
    }

    private static int resolveLeft(List<String> lines) {
        ScoreboardCategory cfg = ConfigManager.config.scoreboard;
        int width = computeWidth(lines);

        if (cfg.hudX == ScoreboardCategory.AUTO_POSITION) {
            return mc.getWindow().getGuiScaledWidth() - 1 - width;
        }

        return HudTextUtils.isRightAligned(cfg.hudX) ? cfg.hudX - width : cfg.hudX;
    }

    private static int resolveTop(List<String> lines) {
        ScoreboardCategory cfg = ConfigManager.config.scoreboard;

        if (cfg.hudY == ScoreboardCategory.AUTO_POSITION) {
            int totalHeight = computeTotalHeight(lines);
            return (mc.getWindow().getGuiScaledHeight() - totalHeight) / 2;
        }

        return cfg.hudY;
    }

    private static int computeWidth(List<String> lines) {
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, mc.font.width(line));
        }
        return width + 6;
    }

    private static int computeTotalHeight(List<String> lines) {
        return lines.size() * LINE_HEIGHT + 2;
    }
}