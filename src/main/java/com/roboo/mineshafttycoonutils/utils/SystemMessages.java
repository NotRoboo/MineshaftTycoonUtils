package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class SystemMessages {

    private static final String PREFIX = "[MST Utils]";
    private static final int GRADIENT_START = 0xFF0084;
    private static final int GRADIENT_END = 0x9A003D;

    public static MutableComponent get() {
        return get("");
    }

    public static MutableComponent get(String message) {
        String full = message.isEmpty() ? PREFIX : PREFIX + " " + message;
        MutableComponent result = Component.empty();

        for (int i = 0; i < full.length(); i++) {
            float t = full.length() == 1 ? 0f : (float) i / (full.length() - 1);
            int color = lerp(GRADIENT_START, GRADIENT_END, t);
            boolean bold = i < PREFIX.length();
            result.append(colored(String.valueOf(full.charAt(i)), color, bold));
        }

        return result;
    }

    private static int lerp(int start, int end, float t) {
        int r = (int) (((start >> 16) & 0xFF) * (1 - t) + ((end >> 16) & 0xFF) * t);
        int g = (int) (((start >> 8) & 0xFF) * (1 - t) + ((end >> 8) & 0xFF) * t);
        int b = (int) ((start & 0xFF) * (1 - t) + (end & 0xFF) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static MutableComponent colored(String text, int rgb, boolean bold) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(bold));
    }
}