package com.roboo.mineshafttycoonutils.features.plasma;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.PlasmaCategory;
import com.roboo.mineshafttycoonutils.hud.ContainerHudDragHandler;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import com.roboo.mineshafttycoonutils.utils.TimeFormatUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.ArrayList;
import java.util.List;

public class PlasmaSmitheryHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;
    private static final String CONTAINER_TITLE = "Plasma Smithery";

    private static final ContainerHudDragHandler dragHandler = new ContainerHudDragHandler();

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (isNotSmitheryScreen(screen)) return;

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> render(graphics));
            ScreenEvents.remove(screen).register(s -> dragHandler.reset());

            registerScroll(screen);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void registerScroll(Screen screen) {
        dragHandler.registerScroll(
                screen,
                () -> ConfigManager.config.plasma.smitheryHudX,
                () -> ConfigManager.config.plasma.smitheryHudY,
                PlasmaSmitheryHud::calcWidth,
                PlasmaSmitheryHud::calcHeight,
                () -> ConfigManager.config.plasma.smitheryHudScale,
                scale -> ConfigManager.config.plasma.smitheryHudScale = scale
        );
    }

    private static boolean isNotSmitheryScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return true;
        return !CONTAINER_TITLE.equalsIgnoreCase(screen.getTitle().getString().trim());
    }

    private static void onTick() {
        if (isNotSmitheryScreen(mc.screen)) {
            dragHandler.reset();
            return;
        }

        PlasmaCategory cfg = ConfigManager.config.plasma;

        dragHandler.tick(
                ConfigManager.config.general.editBagHudKeybind,
                cfg.smitheryHudX,
                cfg.smitheryHudY,
                calcWidth(),
                calcHeight(),
                (x, y) -> {
                    cfg.smitheryHudX = x;
                    cfg.smitheryHudY = y;
                },
                () -> MineshaftTycoonUtils.configManager.saveConfig()
        );
    }

    private static void render(GuiGraphics graphics) {
        PlasmaCategory cfg = ConfigManager.config.plasma;
        if (!cfg.enabled) return;

        dragHandler.drawBoxIfEditing(graphics, cfg.smitheryHudX, cfg.smitheryHudY, calcWidth(), calcHeight());

        drawContent(graphics, cfg.smitheryHudX, cfg.smitheryHudY);
    }

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        PlasmaCategory cfg = ConfigManager.config.plasma;
        List<String> lines = calcLines(cfg);
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);
        int titleColor = HudTextUtils.chromaToArgb(cfg.titleColor);
        float scale = HudScale.normalize(cfg.smitheryHudScale);
        int unscaledWidth = calcUnscaledWidth(lines);
        int screenLeft = rightAligned ? anchorX - Math.round(unscaledWidth * scale) : anchorX;
        int localAnchorX = rightAligned ? unscaledWidth : 0;

        graphics.pose().pushMatrix();
        graphics.pose().translate(screenLeft, y);
        graphics.pose().scale(scale, scale);

        HudTextUtils.drawLine(graphics, "§lPlasma Smithery", localAnchorX, 0, rightAligned, titleColor);

        int line = 1;
        for (String l : lines) {
            HudTextUtils.drawLine(graphics, l, localAnchorX, LINE_HEIGHT * line++, rightAligned);
        }

        graphics.pose().popMatrix();
    }

    private static List<String> calcLines(PlasmaCategory cfg) {
        List<String> lines = new ArrayList<>();
        for (PlasmaCategory.SmitheryLine entry : cfg.smitheryOrder) {
            lines.add(renderEntry(entry));
        }
        return lines;
    }

    private static String renderEntry(PlasmaCategory.SmitheryLine entry) {
        return switch (entry) {
            case GEN1_TIME -> "§7Gen 1: §e" + timeText(PlasmaSmitheryTracker.isGen1TimeKnown(), PlasmaSmitheryTracker.getGen1SecondsLeft());
            case GEN1_HEAT -> "§7Heat 1: §e" + heatText(PlasmaSmitheryTracker.getHeat1());
            case GEN2_TIME -> "§7Gen 2: §e" + timeText(PlasmaSmitheryTracker.isGen2TimeKnown(), PlasmaSmitheryTracker.getGen2SecondsLeft());
            case GEN2_HEAT -> "§7Heat 2: §e" + heatText(PlasmaSmitheryTracker.getHeat2());
        };
    }

    private static String timeText(boolean known, long secondsLeft) {
        return known ? TimeFormatUtils.formatDuration(secondsLeft, true) : "Unknown";
    }

    private static String heatText(long heat) {
        return heat >= 0 ? String.format("%,d", heat) + "°C" : "Unknown";
    }

    private static int calcUnscaledWidth(List<String> lines) {
        int width = mc.font.width("§lPlasma Smithery");
        for (String line : lines) {
            width = Math.max(width, mc.font.width(line));
        }
        return width;
    }

    private static int calcUnscaledHeight(List<String> lines) {
        if (lines.isEmpty()) return LINE_HEIGHT;
        return (lines.size() + 1) * LINE_HEIGHT;
    }

    private static int calcWidth() {
        float scale = HudScale.normalize(ConfigManager.config.plasma.smitheryHudScale);
        return Math.round(calcUnscaledWidth(calcLines(ConfigManager.config.plasma)) * scale);
    }

    private static int calcHeight() {
        float scale = HudScale.normalize(ConfigManager.config.plasma.smitheryHudScale);
        return Math.round(calcUnscaledHeight(calcLines(ConfigManager.config.plasma)) * scale);
    }
}