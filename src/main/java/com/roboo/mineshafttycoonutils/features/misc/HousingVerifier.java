package com.roboo.mineshafttycoonutils.features.misc;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.util.Locale;

public class HousingVerifier {

    private static final String VERIFICATION_TOKEN = "e5d589ea-f9c3-4033-b068-b66a9f5ef4a6";
    private static final String PLAYING_ON_TEXT = "you are currently playing on";
    private static final String UNKNOWN_COMMAND_TEXT = "unknown command";
    private static final String COMMAND_NAME_TEXT = "wtfmap";
    private static final int SEND_DELAY_TICKS = 20;

    private static boolean verified = false;
    private static int ticksUntilSend = -1;

    private HousingVerifier() {}

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            verified = false;
            ticksUntilSend = SEND_DELAY_TICKS;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            verified = false;
            ticksUntilSend = -1;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksUntilSend > 0 && --ticksUntilSend == 0) {
                sendCommand();
            }
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> processMessage(msg.getString()));

        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> processMessage(msg.getString()));
    }

    private static void sendCommand() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.connection.send(new ServerboundChatCommandPacket("wtfmap"));
    }

    private static boolean processMessage(String msg) {
        if (msg == null) return true;

        String stripped = ChatFormatting.stripFormatting(msg).trim();
        if (stripped.isEmpty()) return true;

        if (stripped.contains(VERIFICATION_TOKEN)) {
            verified = true;
        }

        return !isVerificationNoise(stripped);
    }

    private static boolean isVerificationNoise(String stripped) {
        String lower = stripped.toLowerCase(Locale.ROOT);

        if (lower.contains(PLAYING_ON_TEXT)) return true;

        return lower.contains(UNKNOWN_COMMAND_TEXT) && lower.contains(COMMAND_NAME_TEXT);
    }

    public static boolean isVerified() {
        return verified;
    }
}