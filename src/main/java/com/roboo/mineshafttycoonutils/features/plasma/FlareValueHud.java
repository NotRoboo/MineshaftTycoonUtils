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

public class FlareValueHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;
    private static final String CONTAINER_TITLE = "Space Ores Bag";

    private static final ContainerHudDragHandler dragHandler = new ContainerHudDragHandler();

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (isNotBagScreen(screen)) return;

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> render(graphics));
            ScreenEvents.remove(screen).register(s -> dragHandler.reset());

            registerScroll(screen);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void registerScroll(Screen screen) {
        dragHandler.registerScroll(
                screen,
                () -> ConfigManager.config.plasma.flareValueHudX,
                () -> ConfigManager.config.plasma.flareValueHudY,
                FlareValueHud::calcWidth,
                FlareValueHud::calcHeight,
                () -> ConfigManager.config.plasma.flareValueHudScale,
                scale -> ConfigManager.config.plasma.flareValueHudScale = scale
        );
    }

    private static boolean isNotBagScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return true;
        return !CONTAINER_TITLE.equalsIgnoreCase(screen.getTitle().getString().trim());
    }

    private static void onTick() {
        if (isNotBagScreen(mc.screen)) {
            dragHandler.reset();
            return;
        }

        PlasmaCategory cfg = ConfigManager.config.plasma;

        dragHandler.tick(
                ConfigManager.config.general.editBagHudKeybind,
                cfg.flareValueHudX,
                cfg.flareValueHudY,
                calcWidth(),
                calcHeight(),
                (x, y) -> {
                    cfg.flareValueHudX = x;
                    cfg.flareValueHudY = y;
                },
                () -> MineshaftTycoonUtils.configManager.saveConfig()
        );
    }

    private static void render(GuiGraphics graphics) {
        PlasmaCategory cfg = ConfigManager.config.plasma;
        if (!cfg.enabled) return;

        dragHandler.drawBoxIfEditing(graphics, cfg.flareValueHudX, cfg.flareValueHudY, calcWidth(), calcHeight());

        drawContent(graphics, cfg.flareValueHudX, cfg.flareValueHudY);
    }

    private static List<String> targetLines() {
        List<String> lines = new ArrayList<>();
        long target = SolarFlareTracker.computeTarget();

        if (target < 0) {
            lines.add("§lTarget: §cUnknown");
            return lines;
        }

        lines.add("§lTarget: §e" + String.format("%,d", target)
                + (SolarFlareTracker.isManualHeatGoalActive() ? " §7(forced)" : ""));

        if (target == 0) {
            lines.add("§7- §aGenerators Full");
            return lines;
        }

        lines.add("§lWithdraw:");

        long deficit = target - FlareValueTracker.getTotalInventoryValue();
        if (deficit <= 0) {
            lines.add("§7- §aInventory has enough");
            return lines;
        }

        Map<PlasmaCategory.FlareEntry, Long> withdrawal = SolarFlareTracker.computeWithdrawal(deficit);
        if (withdrawal.isEmpty()) {
            lines.add("§7- §cNot enough flares");
            return lines;
        }

        for (Map.Entry<PlasmaCategory.FlareEntry, Long> entry : withdrawal.entrySet()) {
            lines.add("§7- x" + entry.getValue() + " " + entry.getKey().getShortName());
        }

        return lines;
    }

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        PlasmaCategory cfg = ConfigManager.config.plasma;
        List<String> targetLines = targetLines();
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);
        int titleColor = HudTextUtils.chromaToArgb(cfg.titleColor);
        float scale = HudScale.normalize(cfg.flareValueHudScale);
        int unscaledWidth = calcUnscaledWidth(targetLines);
        int screenLeft = rightAligned ? anchorX - Math.round(unscaledWidth * scale) : anchorX;
        int localAnchorX = rightAligned ? unscaledWidth : 0;

        graphics.pose().pushMatrix();
        graphics.pose().translate(screenLeft, y);
        graphics.pose().scale(scale, scale);

        HudTextUtils.drawLine(graphics, "§lFlare Value:", localAnchorX, 0, rightAligned, titleColor);
        int line = 1;

        for (PlasmaCategory.FlareEntry entry : cfg.flareValueOrder) {
            long quantity = FlareValueTracker.getQuantity(entry);
            if (quantity <= 0) continue;

            long value = FlareValueTracker.getValue(entry);
            HudTextUtils.drawLine(graphics, lineText(entry, quantity, value), localAnchorX, LINE_HEIGHT * line++, rightAligned);
        }

        HudTextUtils.drawLine(graphics, "§lTotal: §e" + String.format("%,d", FlareValueTracker.getTotalValue()),
                localAnchorX, LINE_HEIGHT * line++, rightAligned, titleColor);

        HudTextUtils.drawLine(graphics, "§lInventory Fuel: §e" + String.format("%,d", FlareValueTracker.getTotalInventoryValue()),
                localAnchorX, LINE_HEIGHT * line++, rightAligned, titleColor);

        for (String targetLine : targetLines) {
            HudTextUtils.drawLine(graphics, targetLine, localAnchorX, LINE_HEIGHT * line++, rightAligned);
        }

        graphics.pose().popMatrix();
    }

    private static String lineText(PlasmaCategory.FlareEntry entry, long quantity, long value) {
        return "§7- x" + quantity + " " + entry.getShortName() + " = §e" + String.format("%,d", value);
    }

    private static int calcUnscaledWidth(List<String> targetLines) {
        PlasmaCategory cfg = ConfigManager.config.plasma;
        int width = mc.font.width("§lFlare Value:");

        for (PlasmaCategory.FlareEntry entry : cfg.flareValueOrder) {
            long quantity = FlareValueTracker.getQuantity(entry);
            if (quantity <= 0) continue;

            long value = FlareValueTracker.getValue(entry);
            width = Math.max(width, mc.font.width(lineText(entry, quantity, value)));
        }

        width = Math.max(width, mc.font.width("§lTotal: §e" + String.format("%,d", FlareValueTracker.getTotalValue())));
        width = Math.max(width, mc.font.width("§lInventory Fuel: §e" + String.format("%,d", FlareValueTracker.getTotalInventoryValue())));

        for (String targetLine : targetLines) {
            width = Math.max(width, mc.font.width(targetLine));
        }

        return width;
    }

    private static int calcUnscaledHeight(List<String> targetLines) {
        PlasmaCategory cfg = ConfigManager.config.plasma;
        int lines = 1;

        for (PlasmaCategory.FlareEntry entry : cfg.flareValueOrder) {
            if (FlareValueTracker.getQuantity(entry) <= 0) continue;
            lines++;
        }

        lines += 2;
        lines += targetLines.size();
        return lines * LINE_HEIGHT;
    }

    private static int calcWidth() {
        float scale = HudScale.normalize(ConfigManager.config.plasma.flareValueHudScale);
        return Math.round(calcUnscaledWidth(targetLines()) * scale);
    }

    private static int calcHeight() {
        float scale = HudScale.normalize(ConfigManager.config.plasma.flareValueHudScale);
        return Math.round(calcUnscaledHeight(targetLines()) * scale);
    }
}