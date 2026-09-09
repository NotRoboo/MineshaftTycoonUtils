package com.roboo.mineshafttycoonutils.features.pve;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.PvECategory;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import com.roboo.mineshafttycoonutils.hud.MovableHud;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import com.roboo.mineshafttycoonutils.utils.HudZones;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PvEHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;

    private static final MovableHud PVE_BOX = new MovableHud() {
        @Override
        public String getDisplayName() {
            return "PvE Tracker";
        }

        @Override
        public boolean isMasterEnabled() {
            return cfg().hudEnabled;
        }

        @Override
        public int getX() {
            PvECategory cfg = cfg();
            int anchorX = cfg.hudX;
            return HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip) ? anchorX - getWidth() : anchorX;
        }

        @Override
        public int getY() {
            return cfg().hudY;
        }

        @Override
        public int getWidth() {
            return Math.round(calcWidth() * HudScale.normalize(cfg().scale));
        }

        @Override
        public int getHeight() {
            return Math.round(calcHeight() * HudScale.normalize(cfg().scale));
        }

        @Override
        public float getScale() {
            return cfg().scale;
        }

        @Override
        public void setScale(float scale) {
            cfg().scale = HudScale.clamp(scale);
        }

        @Override
        public void setPosition(int x, int y) {
            cfg().hudX = x;
            cfg().hudY = y;
        }

        @Override
        public void render(GuiGraphics graphics) {
            PvECategory cfg = cfg();
            drawContent(graphics, cfg.hudX, cfg.hudY);
        }
    };

    public static void init() {
        HudEditorRegistry.register(PVE_BOX);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "pve_hud"),
                (graphics, tickCounter) -> {
                    PvECategory cfg = cfg();
                    if (mc.player == null || !cfg.hudEnabled) return;
                    if (cfg.hideInOtherZones && !HudZones.isInPveZone(mc.player.blockPosition())) return;

                    int totalHeight = Math.round(calcHeight() * HudScale.normalize(cfg.scale));
                    int x = HudTextUtils.clampX(cfg.hudX);
                    int y = HudTextUtils.clampY(cfg.hudY, totalHeight);

                    drawContent(graphics, x, y);
                }
        );
    }

    private static PvECategory cfg() {
        return ConfigManager.config.pve;
    }

    private static final String TITLE_TEXT = "§lPvE Tracker";

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        PvECategory cfg = cfg();
        List<String> lines = calcLines(cfg);
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);
        float scale = HudScale.normalize(cfg.scale);
        int unscaledWidth = calcWidth();
        int screenLeft = rightAligned ? anchorX - Math.round(unscaledWidth * scale) : anchorX;
        int localAnchorX = rightAligned ? unscaledWidth : 0;

        graphics.pose().pushMatrix();
        graphics.pose().translate(screenLeft, y);
        graphics.pose().scale(scale, scale);

        HudTextUtils.drawLine(graphics, TITLE_TEXT, localAnchorX, 0, rightAligned, HudTextUtils.chromaToArgb(cfg.titleColor));
        int line = 1;
        for (String l : lines) {
            HudTextUtils.drawLine(graphics, l, localAnchorX, LINE_HEIGHT * line++, rightAligned);
        }

        graphics.pose().popMatrix();
    }

    private static List<String> calcLines(PvECategory cfg) {
        List<String> lines = new ArrayList<>();
        for (PvECategory.LineEntry entry : cfg.hudLineOrder) {
            String rendered = renderEntry(entry);
            if (rendered != null) lines.add(rendered);
        }
        return lines;
    }

    private static String renderEntry(PvECategory.LineEntry entry) {
        if (entry == PvECategory.LineEntry.KILLS_PER_HOUR) {
            String color = PvETracker.isPaused() ? "§c" : "§e";
            return "§7Kills/Hour: " + color + PvETracker.getKillsPerHour();
        }

        int kills = PvETracker.getKills(entry.toString());
        if (kills <= 0) return null;

        return "§7" + entry + ": §e" + kills;
    }

    private static int calcWidth() {
        int width = mc.font.width(TITLE_TEXT);
        for (String line : calcLines(cfg())) {
            width = Math.max(width, mc.font.width(line));
        }
        return width;
    }

    private static int calcHeight() {
        List<String> lines = calcLines(cfg());
        return (lines.size() + 1) * LINE_HEIGHT;
    }
}