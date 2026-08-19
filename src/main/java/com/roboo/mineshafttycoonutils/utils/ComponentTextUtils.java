package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;
import java.util.Optional;

public class ComponentTextUtils {

    private ComponentTextUtils() {}

    public static String formattedText(Component component) {
        StringBuilder builder = new StringBuilder();
        component.visit((style, text) -> {
            String codes = legacyCodesFor(style);

            if (!codes.isEmpty() && endsWithCodes(builder, codes)) {
                codes = "";
            }
            builder.append(codes).append(text);
            return Optional.<Void>empty();
        }, Style.EMPTY);
        return builder.toString();
    }

    private static boolean endsWithCodes(StringBuilder builder, String codes) {
        int len = codes.length();
        return builder.length() >= len
                && builder.substring(builder.length() - len).equals(codes);
    }

    public static Style findInteractiveStyle(Component component) {
        Style[] clickable = {null};
        Style[] hoverOnly = {null};
        component.visit((style, text) -> {
            if (clickable[0] == null && style.getClickEvent() != null) {
                clickable[0] = style;
            } else if (hoverOnly[0] == null && style.getHoverEvent() != null) {
                hoverOnly[0] = style;
            }
            return Optional.<Void>empty();
        }, Style.EMPTY);
        return clickable[0] != null ? clickable[0] : hoverOnly[0];
    }

    public static String stripHypixelMessage(String text) {
        String result = text;
        while (result.startsWith("§r")) result = result.substring(2);
        while (result.endsWith("§r")) result = result.substring(0, result.length() - 2);
        return result;
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] words = input.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    private static String legacyCodesFor(Style style) {
        StringBuilder sb = new StringBuilder();
        TextColor color = style.getColor();
        if (color != null) {
            String code = legacyColorCode(color);
            if (code != null) sb.append(code);
        }
        if (style.isBold()) sb.append("§l");
        if (style.isItalic()) sb.append("§o");
        if (style.isUnderlined()) sb.append("§n");
        if (style.isStrikethrough()) sb.append("§m");
        if (style.isObfuscated()) sb.append("§k");
        return sb.toString();
    }

    private static String legacyColorCode(TextColor color) {
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.isColor() && formatting.getColor() != null && formatting.getColor().equals(color.getValue())) {
                return "§" + formatting.getChar();
            }
        }
        return null;
    }
}