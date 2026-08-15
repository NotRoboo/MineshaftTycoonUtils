package com.roboo.mineshafttycoonutils.features.chat;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.PlayerMessagesCategory;
import com.roboo.mineshafttycoonutils.utils.ComponentTextUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class PlayerMessageHandler {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                handle(message));

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay) return true;
            return handle(message);
        });
    }

    private static boolean handle(Component message) {
        PlayerMessagesCategory cfg = ConfigManager.config.playerMessages;
        if (!cfg.enabled && !cfg.sameChatColor) return true;
        if (message == null) return true;

        Style interactiveStyle = ComponentTextUtils.findInteractiveStyle(message);
        String raw = ComponentTextUtils.stripHypixelMessage(ComponentTextUtils.formattedText(message));
        if (raw.isEmpty()) return true;

        Component formatted = PlayerMessageFormatter.format(raw, interactiveStyle);
        if (formatted == null) return true;

        mc.gui.getChat().addMessage(formatted);
        return false;
    }
}