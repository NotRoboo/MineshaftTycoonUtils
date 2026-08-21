package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.profit.MagmaValueConfig;
import com.roboo.mineshafttycoonutils.hud.ContainerHudDragHandler;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import com.roboo.mineshafttycoonutils.utils.NumberFormatUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class MagmaHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;
    private static final long COINS_PER_BILLION = 1_000_000_000L;
    private static final String CONTAINER_TITLE = "Space Ores Bag";
    private static final String LARGE_CHEST_TITLE = "Large Chest";

    private static final ContainerHudDragHandler dragHandler = new ContainerHudDragHandler();

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (isNotRelevantScreen(screen)) return;

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> render(graphics));
            ScreenEvents.remove(screen).register(s -> dragHandler.reset());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static boolean isNotRelevantScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return true;
        String title = screen.getTitle().getString().trim();
        return !CONTAINER_TITLE.equalsIgnoreCase(title) && !LARGE_CHEST_TITLE.equalsIgnoreCase(title);
    }

    private static void onTick() {
        if (isNotRelevantScreen(mc.screen)) {
            dragHandler.reset();
            return;
        }

        MagmaValueConfig cfg = ConfigManager.config.profit.magma;

        dragHandler.tick(
                ConfigManager.config.general.editBagHudKeybind,
                cfg.magmaHudX,
                cfg.magmaHudY,
                computeWidth(),
                computeHeight(),
                (x, y) -> {
                    cfg.magmaHudX = x;
                    cfg.magmaHudY = y;
                },
                () -> MineshaftTycoonUtils.configManager.saveConfig()
        );
    }

    private static void render(GuiGraphics graphics) {
        MagmaValueConfig cfg = ConfigManager.config.profit.magma;
        if (!cfg.hudEnabled) return;

        dragHandler.drawBoxIfEditing(graphics, cfg.magmaHudX, cfg.magmaHudY, computeWidth(), computeHeight());

        drawContent(graphics, cfg.magmaHudX, cfg.magmaHudY);
    }

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        MagmaValueConfig cfg = ConfigManager.config.profit.magma;
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);
        int titleColor = HudTextUtils.chromaToArgb(cfg.titleColor);

        HudTextUtils.drawLine(graphics, "§lMagma Value:", anchorX, y, rightAligned, titleColor);
        int line = 1;

        for (MagmaValueConfig.Entry entry : cfg.order) {
            long quantity = MagmaValueTracker.getQuantity(entry);
            if (quantity <= 0) continue;

            long value = MagmaValueTracker.getMagmaValue(entry);
            HudTextUtils.drawLine(graphics, lineText(entry, quantity, value), anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        }

        HudTextUtils.drawLine(graphics, totalText(cfg), anchorX, y + (LINE_HEIGHT * line++), rightAligned, titleColor);

        line++;

        HudTextUtils.drawLine(graphics, "§lInv Magma:", anchorX, y + (LINE_HEIGHT * line++), rightAligned, titleColor);

        for (MagmaValueConfig.Entry entry : cfg.order) {
            long quantity = MagmaValueTracker.getInventoryQuantity(entry);
            if (quantity <= 0) continue;

            long value = MagmaValueTracker.getInventoryMagmaValue(entry);
            HudTextUtils.drawLine(graphics, lineText(entry, quantity, value), anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        }

        HudTextUtils.drawLine(graphics, inventoryTotalText(cfg), anchorX, y + (LINE_HEIGHT * line), rightAligned, titleColor);
    }

    private static String lineText(MagmaValueConfig.Entry entry, long quantity, long value) {
        return "§7 - " + quantity + "x " + entry.getDisplayName() + ": §c"
                + String.format("%,d", value);
    }

    private static String totalText(MagmaValueConfig cfg) {
        long totalMagma = MagmaValueTracker.getTotalMagma();
        long totalCoins = totalMagma * cfg.magmaPriceBillions * COINS_PER_BILLION;

        return "§lTotal: §c" + String.format("%,d", totalMagma)
                + " Magma ($" + NumberFormatUtils.formatShortened(totalCoins, ConfigManager.config.profit.shortenNumbers) + ")";
    }

    private static String inventoryTotalText(MagmaValueConfig cfg) {
        long totalMagma = MagmaValueTracker.getTotalInventoryMagma();
        long totalCoins = totalMagma * cfg.magmaPriceBillions * COINS_PER_BILLION;

        return "§lInventory Total: §c" + String.format("%,d", totalMagma)
                + " Magma ($" + NumberFormatUtils.formatShortened(totalCoins, ConfigManager.config.profit.shortenNumbers) + ")";
    }

    private static int computeWidth() {
        MagmaValueConfig cfg = ConfigManager.config.profit.magma;
        int width = mc.font.width("§lMagma Value:");

        for (MagmaValueConfig.Entry entry : cfg.order) {
            long quantity = MagmaValueTracker.getQuantity(entry);
            if (quantity <= 0) continue;

            long value = MagmaValueTracker.getMagmaValue(entry);
            width = Math.max(width, mc.font.width(lineText(entry, quantity, value)));
        }

        width = Math.max(width, mc.font.width(totalText(cfg)));

        width = Math.max(width, mc.font.width("§lInventory Magma:"));

        for (MagmaValueConfig.Entry entry : cfg.order) {
            long quantity = MagmaValueTracker.getInventoryQuantity(entry);
            if (quantity <= 0) continue;

            long value = MagmaValueTracker.getInventoryMagmaValue(entry);
            width = Math.max(width, mc.font.width(lineText(entry, quantity, value)));
        }

        width = Math.max(width, mc.font.width(inventoryTotalText(cfg)));

        return width;
    }

    private static int computeHeight() {
        MagmaValueConfig cfg = ConfigManager.config.profit.magma;
        int lines = 1;

        for (MagmaValueConfig.Entry entry : cfg.order) {
            if (MagmaValueTracker.getQuantity(entry) <= 0) continue;
            lines++;
        }
        lines++;

        lines++;
        lines++;

        for (MagmaValueConfig.Entry entry : cfg.order) {
            if (MagmaValueTracker.getInventoryQuantity(entry) <= 0) continue;
            lines++;
        }
        lines++;

        return lines * LINE_HEIGHT;
    }
}