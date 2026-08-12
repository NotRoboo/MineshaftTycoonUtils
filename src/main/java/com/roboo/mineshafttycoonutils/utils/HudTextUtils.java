package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class HudTextUtils {

    private HudTextUtils() {}

    public static boolean isRightAligned(int x) {
        return isRightAligned(x, false);
    }

    public static boolean isRightAligned(int x, boolean disableFlip) {
        if (disableFlip) return false;
        return x > Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
    }

    public static int clampX(int x) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        return Math.clamp(x, 0, screenWidth);
    }

    public static int clampY(int y, int totalHeight) {
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int maxY = Math.max(0, screenHeight - totalHeight);
        return Math.clamp(y, 0, maxY);
    }

    public static void drawLine(GuiGraphics graphics, String text, int x, int y, boolean rightAligned) {
        Minecraft mc = Minecraft.getInstance();
        int drawX = rightAligned ? x - mc.font.width(text) : x;
        graphics.drawString(mc.font, text, drawX, y, 0xFFFFFFFF, true);
    }
}