package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.profit.BagValueConfig;
import com.roboo.mineshafttycoonutils.hud.ContainerHudDragHandler;
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

    private static final ContainerHudDragHandler dragHandler = new ContainerHudDragHandler();

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!isBagScreen(screen)) return;

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> render(graphics));
            ScreenEvents.remove(screen).register(s -> dragHandler.reset());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static boolean isBagScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;
        return "Bag".equalsIgnoreCase(screen.getTitle().getString().trim());
    }

    private static void onTick() {
        if (!isBagScreen(mc.screen)) {
            dragHandler.reset();
            return;
        }

        BagValueConfig cfg = ConfigManager.config.profit.bagValue;

        dragHandler.tick(
                ConfigManager.config.general.editBagHudKeybind,
                cfg.bagValueHudX,
                cfg.bagValueHudY,
                computeWidth(),
                computeHeight(),
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

        dragHandler.drawBoxIfEditing(graphics, cfg.bagValueHudX, cfg.bagValueHudY, computeWidth(), computeHeight());

        drawContent(graphics, cfg.bagValueHudX, cfg.bagValueHudY);
    }

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        BagValueConfig cfg = ConfigManager.config.profit.bagValue;
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);
        int titleColor = HudTextUtils.chromaToArgb(cfg.titleColor);

        HudTextUtils.drawLine(graphics, "§lBag Value", anchorX, y, rightAligned, titleColor);
        int line = 1;

        for (TrackedOre ore : TrackedOre.values()) {
            long value = BagValueTracker.getValue(ore);
            if (value <= 0) continue;

            HudTextUtils.drawLine(graphics, "§7 - " + ore.getDisplayName() + ": §e$" + NumberFormatUtils.formatShortened(value, ConfigManager.config.profit.shortenNumbers),
                    anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        }

        HudTextUtils.drawLine(graphics, "§lTotal: §e$" + NumberFormatUtils.formatShortened(BagValueTracker.getTotalValue(), ConfigManager.config.profit.shortenNumbers),
                anchorX, y + (LINE_HEIGHT * line), rightAligned, titleColor);
    }

    private static int computeWidth() {
        int width = mc.font.width("§lBag Value");

        for (TrackedOre ore : TrackedOre.values()) {
            long value = BagValueTracker.getValue(ore);
            if (value <= 0) continue;

            width = Math.max(width, mc.font.width("§7" + ore.getDisplayName() + ": §e$" + NumberFormatUtils.formatShortened(value, ConfigManager.config.profit.shortenNumbers)));
        }

        width = Math.max(width, mc.font.width("§lTotal: §e$" + NumberFormatUtils.formatShortened(BagValueTracker.getTotalValue(), ConfigManager.config.profit.shortenNumbers)));
        return width;
    }

    private static int computeHeight() {
        int lines = 1;

        for (TrackedOre ore : TrackedOre.values()) {
            if (BagValueTracker.getValue(ore) <= 0) continue;
            lines++;
        }

        lines++;
        return lines * LINE_HEIGHT;
    }
}