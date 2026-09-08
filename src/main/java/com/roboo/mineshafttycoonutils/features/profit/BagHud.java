package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.profit.BagValueConfig;
import com.roboo.mineshafttycoonutils.hud.ContainerHudDragHandler;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import com.roboo.mineshafttycoonutils.utils.NumberFormatUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class BagHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;
    private static final String CONTAINER_TITLE = "Bag";

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
                () -> ConfigManager.config.profit.bagValue.bagValueHudX,
                () -> ConfigManager.config.profit.bagValue.bagValueHudY,
                BagHud::calcWidth,
                BagHud::calcHeight,
                () -> ConfigManager.config.profit.bagValue.bagValueHudScale,
                scale -> ConfigManager.config.profit.bagValue.bagValueHudScale = scale
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

        BagValueConfig cfg = ConfigManager.config.profit.bagValue;

        dragHandler.tick(
                ConfigManager.config.general.editBagHudKeybind,
                cfg.bagValueHudX,
                cfg.bagValueHudY,
                calcWidth(),
                calcHeight(),
                (x, y) -> {
                    cfg.bagValueHudX = x;
                    cfg.bagValueHudY = y;
                },
                () -> MineshaftTycoonUtils.configManager.saveConfig()
        );
    }

    private static void render(GuiGraphics graphics) {
        BagValueConfig cfg = ConfigManager.config.profit.bagValue;
        if (!cfg.hudEnabled) return;

        dragHandler.drawBoxIfEditing(graphics, cfg.bagValueHudX, cfg.bagValueHudY, calcWidth(), calcHeight());

        drawContent(graphics, cfg.bagValueHudX, cfg.bagValueHudY);
    }

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        BagValueConfig cfg = ConfigManager.config.profit.bagValue;
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);
        int titleColor = HudTextUtils.chromaToArgb(cfg.titleColor);
        float scale = HudScale.normalize(cfg.bagValueHudScale);
        int unscaledWidth = calcUnscaledWidth();
        int screenLeft = rightAligned ? anchorX - Math.round(unscaledWidth * scale) : anchorX;
        int localAnchorX = rightAligned ? unscaledWidth : 0;

        graphics.pose().pushMatrix();
        graphics.pose().translate(screenLeft, y);
        graphics.pose().scale(scale, scale);

        HudTextUtils.drawLine(graphics, "§lBag Value", localAnchorX, 0, rightAligned, titleColor);
        int line = 1;

        for (TrackedOre ore : TrackedOre.values()) {
            long value = BagValueTracker.getValue(ore);
            if (value <= 0) continue;

            HudTextUtils.drawLine(graphics, "§7 - " + ore.getDisplayName() + ": §e$" + NumberFormatUtils.formatShortened(value, true),
                    localAnchorX, LINE_HEIGHT * line++, rightAligned);
        }

        HudTextUtils.drawLine(graphics, "§lTotal: §e$" + NumberFormatUtils.formatShortened(BagValueTracker.getTotalValue(), true),
                localAnchorX, LINE_HEIGHT * line, rightAligned, titleColor);

        graphics.pose().popMatrix();
    }

    private static int calcUnscaledWidth() {
        int width = mc.font.width("§lBag Value");

        for (TrackedOre ore : TrackedOre.values()) {
            long value = BagValueTracker.getValue(ore);
            if (value <= 0) continue;

            width = Math.max(width, mc.font.width("§7" + ore.getDisplayName() + ": §e$" + NumberFormatUtils.formatShortened(value, true)));
        }

        width = Math.max(width, mc.font.width("§lTotal: §e$" + NumberFormatUtils.formatShortened(BagValueTracker.getTotalValue(), true)));
        return width;
    }

    private static int calcUnscaledHeight() {
        int lines = 1;

        for (TrackedOre ore : TrackedOre.values()) {
            if (BagValueTracker.getValue(ore) <= 0) continue;
            lines++;
        }

        lines++;
        return lines * LINE_HEIGHT;
    }

    private static int calcWidth() {
        float scale = HudScale.normalize(ConfigManager.config.profit.bagValue.bagValueHudScale);
        return Math.round(calcUnscaledWidth() * scale);
    }

    private static int calcHeight() {
        float scale = HudScale.normalize(ConfigManager.config.profit.bagValue.bagValueHudScale);
        return Math.round(calcUnscaledHeight() * scale);
    }
}