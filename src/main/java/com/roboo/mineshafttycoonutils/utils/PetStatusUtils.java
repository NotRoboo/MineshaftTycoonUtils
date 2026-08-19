package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.Optional;

public class PetStatusUtils {

    public enum Status {
        LOCKED,
        UNLOCKED,
        EQUIPPED,
        ON_ADVENTURE,
        UNKNOWN
    }

    private PetStatusUtils() {}

    public static Status resolveStatus(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return Status.UNKNOWN;

        for (Component line : lore.lines()) {
            ChatFormatting unColor = findUnColor(line);
            if (unColor == null) continue;

            return switch (unColor) {
                case BLACK -> Status.LOCKED;
                case GOLD -> Status.UNLOCKED;
                case DARK_AQUA -> Status.EQUIPPED;
                case DARK_GREEN -> Status.ON_ADVENTURE;
                default -> Status.UNKNOWN;
            };
        }

        return Status.UNKNOWN;
    }

    private static ChatFormatting findUnColor(Component component) {
        ChatFormatting[] result = {null};
        component.visit((style, text) -> {
            if (result[0] == null && "UN".equals(text)) {
                result[0] = colorOf(style);
            }
            return Optional.<Void>empty();
        }, Style.EMPTY);
        return result[0];
    }

    private static ChatFormatting colorOf(Style style) {
        var color = style.getColor();
        if (color == null) return null;
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.isColor() && formatting.getColor() != null && formatting.getColor().equals(color.getValue())) {
                return formatting;
            }
        }
        return null;
    }
}