package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.profit.BagValueConfig;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.lwjgl.glfw.GLFW;

public class BagHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;
    private static final int BOX_PADDING = 3;
    private static final int BORDER_COLOR_HOVERED = 0xFF55FF55;

    private static final String[] SUFFIXES = {
            "", "K", "M", "B", "T", "Qd", "Qn", "Sx", "Sp", "Oc", "No", "Dc"
    };

    private static boolean editing = false;
    private static boolean dragging = false;
    private static double dragOffsetX;
    private static double dragOffsetY;
    private static boolean lastEditKeyDown = false;

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!isBagScreen(screen)) return;

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> render(graphics));
            ScreenEvents.remove(screen).register(s -> {
                editing = false;
                dragging = false;
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static boolean isBagScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;
        return "Bag".equalsIgnoreCase(screen.getTitle().getString().trim());
    }

    private static void onTick() {
        if (!isBagScreen(mc.screen)) {
            editing = false;
            dragging = false;
            lastEditKeyDown = false;
            return;
        }

        boolean keyDown = isKeyDown(ConfigManager.config.general.editBagHudKeybind);
        if (keyDown && !lastEditKeyDown) {
            editing = !editing;
            dragging = false;
        }
        lastEditKeyDown = keyDown;

        if (editing) handleDrag(ConfigManager.config.profit.bagValue);
    }

    private static void handleDrag(BagValueConfig cfg) {
        long handle = GLFW.glfwGetCurrentContext();
        if (handle == 0L) return;

        boolean mouseDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        int mouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int mouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

        int left = cfg.bagValueHudX - BOX_PADDING;
        int top = cfg.bagValueHudY - BOX_PADDING;
        int right = cfg.bagValueHudX + computeWidth() + BOX_PADDING;
        int bottom = cfg.bagValueHudY + computeHeight() + BOX_PADDING;

        if (mouseDown && !dragging) {
            if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
                dragging = true;
                dragOffsetX = mouseX - cfg.bagValueHudX;
                dragOffsetY = mouseY - cfg.bagValueHudY;
            }
        } else if (!mouseDown && dragging) {
            dragging = false;
            MineshaftTycoonUtils.configManager.saveConfig();
        }

        if (dragging) {
            cfg.bagValueHudX = (int) Math.round(mouseX - dragOffsetX);
            cfg.bagValueHudY = (int) Math.round(mouseY - dragOffsetY);
        }
    }

    private static boolean isKeyDown(int glfwKey) {
        if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) return false;
        long handle = GLFW.glfwGetCurrentContext();
        if (handle == 0L) return false;
        return GLFW.glfwGetKey(handle, glfwKey) == GLFW.GLFW_PRESS;
    }

    private static void render(GuiGraphics graphics) {
        BagValueConfig cfg = ConfigManager.config.profit.bagValue;
        if (!cfg.hudEnabled) return;

        if (editing) {
            int left = cfg.bagValueHudX - BOX_PADDING;
            int top = cfg.bagValueHudY - BOX_PADDING;
            int right = cfg.bagValueHudX + computeWidth() + BOX_PADDING;
            int bottom = cfg.bagValueHudY + computeHeight() + BOX_PADDING;
            drawBox(graphics, left, top, right, bottom);
        }

        drawContent(graphics, cfg.bagValueHudX, cfg.bagValueHudY);
    }

    private static void drawBox(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, top + 1, BORDER_COLOR_HOVERED);
        graphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR_HOVERED);
        graphics.fill(left, top, left + 1, bottom, BORDER_COLOR_HOVERED);
        graphics.fill(right - 1, top, right, bottom, BORDER_COLOR_HOVERED);
    }

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        BagValueConfig cfg = ConfigManager.config.profit.bagValue;
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);

        HudTextUtils.drawLine(graphics, "§e§lBag Value", anchorX, y, rightAligned);
        int line = 1;

        for (TrackedOre ore : TrackedOre.values()) {
            long value = BagValueTracker.getValue(ore);
            if (value <= 0) continue;

            HudTextUtils.drawLine(graphics, "§7" + ore.getBagItemName() + ": §e$" + format(value),
                    anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        }

        HudTextUtils.drawLine(graphics, "§e§lTotal: §e$" + format(BagValueTracker.getTotalValue()),
                anchorX, y + (LINE_HEIGHT * line), rightAligned);
    }

    private static int computeWidth() {
        int width = mc.font.width("§e§lBag Value");

        for (TrackedOre ore : TrackedOre.values()) {
            long value = BagValueTracker.getValue(ore);
            if (value <= 0) continue;

            width = Math.max(width, mc.font.width("§7" + ore.getBagItemName() + ": §e$" + format(value)));
        }

        width = Math.max(width, mc.font.width("§7Total: §e$" + format(BagValueTracker.getTotalValue())));
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

    private static String format(long value) {
        boolean shorten = ConfigManager.config.profit.shortenNumbers;
        if (!shorten || value < 1000) {
            return String.format("%,d", value);
        }

        int tier = 0;
        double reduced = value;
        while (reduced >= 1000 && tier < SUFFIXES.length - 1) {
            reduced /= 1000.0;
            tier++;
        }

        return String.format("%.2f%s", reduced, SUFFIXES[tier]);
    }
}