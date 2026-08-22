package com.roboo.mineshafttycoonutils.utils;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionBarDebugUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("MineshaftTycoonUtils");
    private static final Minecraft mc = Minecraft.getInstance();

    private static String lastRaw = null;

    private ActionBarDebugUtils() {}

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> {
            if (!overlay) return;
            lastRaw = msg.getString();
        });
    }

    public static void dumpActionBar() {
        if (lastRaw == null) {
            sendPlain("§cNo action bar message has been seen yet.");
            return;
        }

        sendHeader();
        LOGGER.info("Action Bar Dump");

        sendPlain("§7- " + lastRaw);
        LOGGER.info("  \"{}\"", lastRaw);
    }

    private static void sendHeader() {
        if (mc.player == null) return;
        mc.player.displayClientMessage(SystemMessages.buildPrefix().append(colored(" §e§lAction Bar Dump")), false);
    }

    private static void sendPlain(String message) {
        if (mc.player == null) return;
        mc.player.displayClientMessage(colored(message), false);
    }

    private static MutableComponent colored(String legacyText) {
        MutableComponent result = Component.empty();
        Style style = Style.EMPTY;

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < legacyText.length(); i++) {
            char c = legacyText.charAt(i);
            if (c == '§' && i + 1 < legacyText.length()) {
                if (!buffer.isEmpty()) {
                    result.append(Component.literal(buffer.toString()).setStyle(style));
                    buffer.setLength(0);
                }
                net.minecraft.ChatFormatting formatting = net.minecraft.ChatFormatting.getByCode(legacyText.charAt(i + 1));
                if (formatting != null) {
                    style = formatting.isColor() ? Style.EMPTY.withColor(formatting) : style.applyFormat(formatting);
                }
                i++;
            } else {
                buffer.append(c);
            }
        }
        if (!buffer.isEmpty()) {
            result.append(Component.literal(buffer.toString()).setStyle(style));
        }

        return result;
    }
}