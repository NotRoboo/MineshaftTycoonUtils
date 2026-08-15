package com.roboo.mineshafttycoonutils.features.chat;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.PlayerMessagesCategory;
import com.roboo.mineshafttycoonutils.utils.RankTierData;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerMessageFormatter {

    private static final Pattern PLAYER_CHAT_PATTERN = Pattern.compile(
            "^(?:(?:§.)*\\s*\\+\\s*)?§(.)§\\1\\[([^]]+)]\\s§r(§.(?:\\[[^]]*])?)\\s?([^\\s§]+)(§.): (.*)$"
    );

    private static final Pattern SELF_CHAT_PATTERN = Pattern.compile(
            "^(?:(?:§.)*\\s*\\+\\s*)?§(.)\\[([^]]+)]\\s(§.(?:\\[[^]]*])?)\\s?([^\\s§]+)(§.): (.*)$"
    );

    private static final Pattern PARTY_CHAT_MESSAGE_PATTERN = Pattern.compile(
            "^(§9Party §8> .*)(§.): (.*)$"
    );

    private PlayerMessageFormatter() {}

    public static Component format(String rawWithCodes, Style interactiveStyle) {
        PlayerMessagesCategory cfg = ConfigManager.config.playerMessages;
        if (rawWithCodes == null) return null;

        Matcher playerMatch = PLAYER_CHAT_PATTERN.matcher(rawWithCodes);
        if (!playerMatch.matches()) {
            playerMatch = SELF_CHAT_PATTERN.matcher(rawWithCodes);
        }
        if (playerMatch.matches()) {
            if (cfg.enabled) {
                return rebuild(cfg, playerMatch, interactiveStyle);
            }
            if (cfg.sameChatColor) {
                MutableComponent result = Component.empty();
                Component prefix = Component.literal(rawWithCodes.substring(0, playerMatch.start(5)));
                result.append(withInteractivity(prefix, interactiveStyle));
                result.append(Component.literal("§f"));
                result.append(Component.literal(rawWithCodes.substring(playerMatch.end(5))));
                return result;
            }
            return null;
        }

        if (cfg.sameChatColor) {
            Matcher partyMatch = PARTY_CHAT_MESSAGE_PATTERN.matcher(rawWithCodes);
            if (partyMatch.matches()) {
                return Component.literal(partyMatch.group(1) + "§f: " + partyMatch.group(3));
            }
        }

        return null;
    }

    private static Component rebuild(PlayerMessagesCategory cfg, Matcher m, Style interactiveStyle) {
        char tierColorChar = m.group(1).charAt(0);
        String rankSegment = m.group(3);
        String name = m.group(4);
        String colonColor = m.group(5);
        String message = m.group(6);

        String tierTag = RankTierData.resolveTag(m.group(2));
        String glyphKey = RankTierData.glyphKeyFor(tierColorChar, tierTag);

        char rankColor = rankSegment.charAt(1);
        boolean hasRankTag = rankSegment.contains("[");
        String messageColor = cfg.sameChatColor ? "§f" : colonColor;

        MutableComponent result = Component.empty();
        boolean first = true;

        for (PlayerMessagesCategory.Part part : cfg.partOrder) {
            Component segment = switch (part) {
                case TIER -> {
                    String glyph = ConfigManager.config.glyph.playerMessageGlyphs ? RankTierData.glyphFor(glyphKey) : null;
                    if (glyph != null) {
                        yield Component.literal("§f" + glyph);
                    }
                    yield Component.literal("§" + tierColorChar + "[" + tierTag + "]");
                }
                case RANK -> (!cfg.rankHider && hasRankTag) ? Component.literal(rankSegment) : null;
                case PLAYER_NAME -> cfg.customNameColor
                        ? coloredHex(name, chromaToHex(cfg.nameColor))
                        : Component.literal("§" + rankColor + name);
                case MESSAGE -> Component.literal(messageColor + ": " + message);
            };

            if (segment == null) continue;

            if (part != PlayerMessagesCategory.Part.MESSAGE && !first) {
                result.append(Component.literal(" "));
            }
            if (part != PlayerMessagesCategory.Part.MESSAGE) {
                segment = withInteractivity(segment, interactiveStyle);
            }
            result.append(segment);
            first = false;
        }

        return result;
    }

    private static Component withInteractivity(Component comp, Style interactiveStyle) {
        if (interactiveStyle == null) return comp;
        if (interactiveStyle.getClickEvent() == null && interactiveStyle.getHoverEvent() == null) return comp;

        MutableComponent copy = comp.copy();
        Style merged = copy.getStyle();
        if (interactiveStyle.getClickEvent() != null) merged = merged.withClickEvent(interactiveStyle.getClickEvent());
        if (interactiveStyle.getHoverEvent() != null) merged = merged.withHoverEvent(interactiveStyle.getHoverEvent());
        return copy.setStyle(merged);
    }

    private static Component coloredHex(String text, int hex) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(hex)));
    }

    private static int chromaToHex(ChromaColour color) {
        return color.getEffectiveColourRGB() & 0x00FFFFFF;
    }
}