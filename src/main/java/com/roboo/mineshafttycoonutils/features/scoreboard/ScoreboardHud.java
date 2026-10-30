package com.roboo.mineshafttycoonutils.features.scoreboard;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.ScoreboardCategory;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.HudScale;
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
            return resolveLeft(getWidth());
        }

        @Override
        public int getY() {
            return resolveTop(getHeight());
        }

        @Override
        public int getWidth() {
            return Math.round(calcWidth(CustomScoreboardManager.formatDisplayLines()) * HudScale.normalize(ConfigManager.config.scoreboard.scale));
        }

        @Override
        public int getHeight() {
            return Math.round(calcTotalHeight(CustomScoreboardManager.formatDisplayLines()) * HudScale.normalize(ConfigManager.config.scoreboard.scale));
        }

        @Override
        public float getScale() {
            return ConfigManager.config.scoreboard.scale;
        }

        @Override
        public void setScale(float scale) {
            ConfigManager.config.scoreboard.scale = HudScale.clamp(scale);
        }

        @Override
        public void setPosition(int x, int y) {
            ScoreboardCategory cfg = ConfigManager.config.scoreboard;
            int width = getWidth();
            int height = getHeight();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int left = HudTextUtils.isRightAligned(x) ? x - width : x;

            cfg.hudX = x;
            cfg.hudY = y;
            cfg.hudXFraction = Math.clamp(left / (float) Math.max(1, screenWidth - width), 0f, 1f);
            cfg.hudYFraction = Math.clamp(y / (float) Math.max(1, screenHeight - height), 0f, 1f);
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
        ScoreboardCategory cfg = ConfigManager.config.scoreboard;
        float scale = HudScale.normalize(cfg.scale);
        int unscaledWidth = calcWidth(lines);
        int unscaledHeight = calcTotalHeight(lines);
        int width = Math.round(unscaledWidth * scale);
        int totalHeight = Math.round(unscaledHeight * scale);
        int left = resolveLeft(width);
        int top = resolveTop(totalHeight);
        int right = left + width;

        graphics.fill(left, top - Math.round(2 * scale), right, top + totalHeight, 0x4E000000);

        graphics.pose().pushMatrix();
        graphics.pose().translate(left, top);
        graphics.pose().scale(scale, scale);

        int y = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.isEmpty()) {
                int x = i == 0
                        ? (unscaledWidth - mc.font.width(line)) / 2
                        : 3;
                graphics.drawString(mc.font, line, x, y, 0xFFFFFFFF, true);
            }
            y += LINE_HEIGHT;
        }

        graphics.pose().popMatrix();
    }

    private static int resolveLeft(int width) {
        ScoreboardCategory cfg = ConfigManager.config.scoreboard;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int maxLeft = Math.max(0, screenWidth - width);
        int left;

        if (cfg.hudX == ScoreboardCategory.AUTO_POSITION) {
            left = screenWidth - 1 - width;
        } else if (cfg.hudXFraction >= 0f) {
            left = Math.round(cfg.hudXFraction * maxLeft);
        } else {
            left = HudTextUtils.isRightAligned(cfg.hudX) ? cfg.hudX - width : cfg.hudX;
        }

        return Math.clamp(left, 0, maxLeft);
    }

    private static int resolveTop(int totalHeight) {
        ScoreboardCategory cfg = ConfigManager.config.scoreboard;
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int maxTop = Math.max(0, screenHeight - totalHeight);
        int top;

        if (cfg.hudY == ScoreboardCategory.AUTO_POSITION) {
            top = (screenHeight - totalHeight) / 2;
        } else if (cfg.hudYFraction >= 0f) {
            top = Math.round(cfg.hudYFraction * maxTop);
        } else {
            top = cfg.hudY;
        }

        return Math.clamp(top, 0, maxTop);
    }

    private static int calcWidth(List<String> lines) {
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, mc.font.width(line));
        }
        return width + 6;
    }

    private static int calcTotalHeight(List<String> lines) {
        return lines.size() * LINE_HEIGHT + 2;
    }
}