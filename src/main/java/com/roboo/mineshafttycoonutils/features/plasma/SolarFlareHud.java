package com.roboo.mineshafttycoonutils.features.plasma;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.PlasmaCategory;
import com.roboo.mineshafttycoonutils.hud.ContainerHudDragHandler;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolarFlareHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;
    private static final String CONTAINER_TITLE = "Solar Flare";

    private static final ContainerHudDragHandler dragHandler = new ContainerHudDragHandler();

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (isNotSolarFlareScreen(screen)) return;

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> render(graphics));
            ScreenEvents.remove(screen).register(s -> dragHandler.reset());

            registerScroll(screen);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void registerScroll(Screen screen) {
        dragHandler.registerScroll(
                screen,
                () -> ConfigManager.config.plasma.solarFlareHudX,
                () -> ConfigManager.config.plasma.solarFlareHudY,
                SolarFlareHud::calcWidth,
                SolarFlareHud::calcHeight,
                () -> ConfigManager.config.plasma.solarFlareHudScale,
                scale -> ConfigManager.config.plasma.solarFlareHudScale = scale
        );
    }

    private static boolean isNotSolarFlareScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return true;
        return !CONTAINER_TITLE.equalsIgnoreCase(screen.getTitle().getString().trim());
    }

    private static void onTick() {
        if (isNotSolarFlareScreen(mc.screen)) {
            dragHandler.reset();
            return;
        }

        PlasmaCategory cfg = ConfigManager.config.plasma;

        dragHandler.tick(
                ConfigManager.config.general.editBagHudKeybind,
                cfg.solarFlareHudX,
                cfg.solarFlareHudY,
                calcWidth(),
                calcHeight(),
                (x, y) -> {
                    cfg.solarFlareHudX = x;
                    cfg.solarFlareHudY = y;
                },
                () -> MineshaftTycoonUtils.configManager.saveConfig()
        );
    }

    private static void render(GuiGraphics graphics) {
        PlasmaCategory cfg = ConfigManager.config.plasma;
        if (!cfg.enabled) return;

        dragHandler.drawBoxIfEditing(graphics, cfg.solarFlareHudX, cfg.solarFlareHudY, calcWidth(), calcHeight());

        drawContent(graphics, cfg.solarFlareHudX, cfg.solarFlareHudY);
    }

    private static List<String> calcLines(PlasmaCategory cfg) {
        List<String> lines = new ArrayList<>();
        long target = SolarFlareTracker.computeTarget();

        for (PlasmaCategory.SolarFlareLine entry : cfg.solarFlareOrder) {
            switch (entry) {
                case GEN1_HEAT -> lines.add("§7Gen 1 Heat: §e" + heatText(PlasmaSmitheryTracker.getHeat1(), PlasmaSmitheryTracker.getMaxHeat1()));
                case GEN2_HEAT -> lines.add("§7Gen 2 Heat: §e" + heatText(PlasmaSmitheryTracker.getHeat2(), PlasmaSmitheryTracker.getMaxHeat2()));
                case INVENTORY_FUEL -> lines.add("§7Inventory Fuel: §e" + String.format("%,d", FlareValueTracker.getTotalInventoryValue()));
                case TARGET -> lines.add(target < 0
                        ? "§7Target: §cUnknown"
                        : "§7Target: §e" + String.format("%,d", target) + (SolarFlareTracker.isManualHeatGoalActive() ? " §7(forced)" : ""));
                case DONATE -> lines.addAll(donateLines(target));
            }
        }

        return lines;
    }

    private static List<String> donateLines(long target) {
        List<String> lines = new ArrayList<>();
        if (target < 0) return lines;

        lines.add("§lDonate:");

        if (target == 0) {
            lines.add("§7- §aGenerators Full");
            return lines;
        }

        Map<PlasmaCategory.FlareEntry, Long> donation = SolarFlareTracker.computeFlareDonation(target);
        if (donation.isEmpty()) {
            lines.add("§7- §cNot enough flares");
            return lines;
        }

        for (Map.Entry<PlasmaCategory.FlareEntry, Long> entry : donation.entrySet()) {
            lines.add("§7- x" + entry.getValue() + " " + entry.getKey().getShortName());
        }

        return lines;
    }

    private static String heatText(long heat, long maxHeat) {
        if (heat < 0 || maxHeat < 0) return "Unknown";
        return String.format("%,d", heat) + "/" + String.format("%,d", maxHeat) + "°C";
    }

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        PlasmaCategory cfg = ConfigManager.config.plasma;
        List<String> lines = calcLines(cfg);
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);
        int titleColor = HudTextUtils.chromaToArgb(cfg.titleColor);
        float scale = HudScale.normalize(cfg.solarFlareHudScale);
        int unscaledWidth = calcUnscaledWidth(lines);
        int screenLeft = rightAligned ? anchorX - Math.round(unscaledWidth * scale) : anchorX;
        int localAnchorX = rightAligned ? unscaledWidth : 0;

        graphics.pose().pushMatrix();
        graphics.pose().translate(screenLeft, y);
        graphics.pose().scale(scale, scale);

        HudTextUtils.drawLine(graphics, "§lSolar Flare", localAnchorX, 0, rightAligned, titleColor);
        int line = 1;
        for (String l : lines) {
            HudTextUtils.drawLine(graphics, l, localAnchorX, LINE_HEIGHT * line++, rightAligned);
        }

        graphics.pose().popMatrix();
    }

    private static int calcUnscaledWidth(List<String> lines) {
        int width = mc.font.width("§lSolar Flare");
        for (String line : lines) {
            width = Math.max(width, mc.font.width(line));
        }
        return width;
    }

    private static int calcUnscaledHeight(List<String> lines) {
        return (lines.size() + 1) * LINE_HEIGHT;
    }

    private static int calcWidth() {
        float scale = HudScale.normalize(ConfigManager.config.plasma.solarFlareHudScale);
        return Math.round(calcUnscaledWidth(calcLines(ConfigManager.config.plasma)) * scale);
    }

    private static int calcHeight() {
        float scale = HudScale.normalize(ConfigManager.config.plasma.solarFlareHudScale);
        return Math.round(calcUnscaledHeight(calcLines(ConfigManager.config.plasma)) * scale);
    }
}