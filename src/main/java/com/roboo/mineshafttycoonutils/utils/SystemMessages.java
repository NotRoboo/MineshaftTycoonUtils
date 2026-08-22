package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class SystemMessages {

    private static final String PREFIX = "[MST Utils]";
    private static final int GRADIENT_START = 0xFF0084;
    private static final int GRADIENT_END = 0x9A003D;

    public static MutableComponent buildPrefix() {
        return buildPrefix("");
    }

    public static MutableComponent buildPrefix(String message) {
        String full = message.isEmpty() ? PREFIX : PREFIX + " " + message;
        MutableComponent result = Component.empty();

        for (int i = 0; i < full.length(); i++) {
            float t = (float) i / (full.length() - 1);
            int color = blendColor(t);
            boolean bold = i < PREFIX.length();
            result.append(colored(String.valueOf(full.charAt(i)), color, bold));
        }

        return result;
    }

    private static int blendColor(float t) {
        int r = (int) (((GRADIENT_START >> 16) & 0xFF) * (1 - t) + ((GRADIENT_END >> 16) & 0xFF) * t);
        int g = (int) (((GRADIENT_START >> 8) & 0xFF) * (1 - t) + ((GRADIENT_END >> 8) & 0xFF) * t);
        int b = (int) ((GRADIENT_START & 0xFF) * (1 - t) + (GRADIENT_END & 0xFF) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static MutableComponent colored(String text, int rgb, boolean bold) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(bold));
    }
}