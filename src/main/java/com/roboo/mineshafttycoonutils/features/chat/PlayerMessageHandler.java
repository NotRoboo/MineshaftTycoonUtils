package com.roboo.mineshafttycoonutils.features.chat;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.GlyphCategory;
import com.roboo.mineshafttycoonutils.config.PlayerMessagesCategory;
import com.roboo.mineshafttycoonutils.utils.ComponentTextUtils;
import com.roboo.mineshafttycoonutils.utils.EmojiTextUtils;
import com.roboo.mineshafttycoonutils.utils.GlyphTextUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
        GlyphCategory.GlyphMode playerGlyphMode = ConfigManager.config.glyph.playerMessageGlyphs;
        GlyphCategory.GlyphMode otherGlyphMode = ConfigManager.config.glyph.otherMessageGlyphs;
        boolean emojiEnabled = ConfigManager.config.emoji.emojisEnabled;

        if (!cfg.enabled && !cfg.sameChatColor && !playerGlyphMode.isEnabled() && !otherGlyphMode.isEnabled() && !emojiEnabled) return true;
        if (message == null) return true;

        Style interactiveStyle = ComponentTextUtils.findInteractiveStyle(message);
        String originalRaw = ComponentTextUtils.stripHypixelMessage(ComponentTextUtils.formattedText(message));
        if (originalRaw.isEmpty()) return true;

        if (MessageHider.shouldHide(originalRaw)) return true;

        String raw;
        if (PlayerMessageFormatter.isSystemBridgeMessage(originalRaw)) {
            String stripped = EmojiTextUtils.stripDiscordEmojiWrapper(originalRaw);
            raw = emojiEnabled ? EmojiTextUtils.substitute(stripped, true) : stripped;
        } else {
            raw = emojiEnabled ? EmojiTextUtils.substitute(originalRaw, true) : originalRaw;
        }

        if (cfg.enabled || cfg.sameChatColor || playerGlyphMode.isEnabled()) {
            Component formatted = PlayerMessageFormatter.format(raw, interactiveStyle);
            if (formatted != null) {
                mc.gui.getChat().addMessage(formatted);
                return false;
            }
        }

        String replaced = raw;
        if (otherGlyphMode.isEnabled()) {
            replaced = GlyphTextUtils.substituteTierTags(replaced, otherGlyphMode);
        }

        if (!replaced.equals(originalRaw)) {
            mc.gui.getChat().addMessage(preserveClickAndHover(Component.literal(replaced), interactiveStyle));
            return false;
        }

        return true;
    }

    private static Component preserveClickAndHover(Component comp, Style interactiveStyle) {
        if (interactiveStyle == null) return comp;
        if (interactiveStyle.getClickEvent() == null && interactiveStyle.getHoverEvent() == null) return comp;

        MutableComponent copy = comp.copy();
        Style merged = copy.getStyle();
        if (interactiveStyle.getClickEvent() != null) merged = merged.withClickEvent(interactiveStyle.getClickEvent());
        if (interactiveStyle.getHoverEvent() != null) merged = merged.withHoverEvent(interactiveStyle.getHoverEvent());
        return copy.setStyle(merged);
    }
}