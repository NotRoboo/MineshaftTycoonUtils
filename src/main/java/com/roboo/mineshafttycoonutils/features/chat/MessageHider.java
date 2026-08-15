package com.roboo.mineshafttycoonutils.features.chat;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.fishing.FishingTracker;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;

import java.util.Locale;

public class MessageHider {

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> !shouldHide(msg.getString()));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> !shouldHide(msg.getString()));
    }

    private static boolean shouldHide(String rawMessage) {
        if (rawMessage == null) return false;

        String stripped = ChatFormatting.stripFormatting(rawMessage).trim();
        if (stripped.isEmpty()) return false;

        String lower = stripped.toLowerCase(Locale.ROOT);
        var cfg = ConfigManager.config.chat;

        if (cfg.joinMessages.hideT1JoinMessages && (lower.contains("welcome back [t1]")
                || lower.contains("welcome! [t1]") || lower.contains("welcome back" + "\uE001")
                || lower.contains("welcome!" + "\uE001"))) {
            return true;
        }
        if (cfg.joinMessages.hideT2JoinMessages && lower.contains("welcome back [t2]")
                || lower.contains("welcome back" + "\uE002")) {
            return true;
        }
        if (cfg.joinMessages.hideT3JoinMessages && lower.contains("welcome back [t3]")
                || lower.contains("welcome back" + "\uE003")) {
            return true;
        }
        if (cfg.joinMessages.hideT4JoinMessages && lower.contains("welcome back [t4]")
                || lower.contains("welcome back" + "\uE004")) {
            return true;
        }
        if (cfg.hidePetXP && lower.contains("+1 xp for your")) {
            return true;
        }
        if (cfg.hidePotFrags && lower.contains("rare drop! fortune fragment!")) {
            return true;
        }
        if (cfg.hidePetMessages && lower.contains("pets!")) {
            return true;
        }
        if (cfg.hideDiscordJoin && lower.contains("=╶-----------------------------╶=") ||
                lower.contains("join our discord") || lower.contains("announcing updates") ||
                lower.contains("-=+=-> https://discord.gg/itv <-=+=-") || lower.contains("✌)")) {
            return true;
        }
        if (cfg.hidePvE && lower.contains("[system]: you defeated") ||
                lower.contains("you're currently above the cps limit")) {
            return true;
        }
        if (cfg.hideFishingMessages
                && !FishingTracker.isUltraRareDrop(stripped)
                && FishingTracker.isTrackableFishingMessage(stripped)) {
            return true;
        }
        if (cfg.hideFishingMessages && lower.contains("your boat to fish") ||
                lower.contains("victory! you have defeated")) {
            return true;
        }


        return false;
    }
}