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

    // other players msgs
    private static final Pattern MST_PLAYER_PATTERN = Pattern.compile(
            "^(?:(?:§.)*\\s*\\+\\s*)?§(.)§\\1\\[([^]]+)]\\s§r(§.(?:\\[[^]]*])?)\\s?([^\\s§]+)(§.): (.*)$"
    );

    // detect the mod user msgs since hyp does weird stuff
    private static final Pattern MOD_USERS_PATTERN = Pattern.compile(
            "^(?:(?:§.)*\\s*\\+\\s*)?§(.)\\[([^]]+)]\\s(§.(?:\\[[^]]*])?)\\s?([^\\s§]+)(§.): (.*)$"
    );

    private PlayerMessageFormatter() {}

    public static Component format(String rawWithCodes, Style interactiveStyle) {
        PlayerMessagesCategory cfg = ConfigManager.config.playerMessages;
        if (rawWithCodes == null) return null;

        Matcher messageMatch = MST_PLAYER_PATTERN.matcher(rawWithCodes);
        if (!messageMatch.matches()) {
            messageMatch = MOD_USERS_PATTERN.matcher(rawWithCodes);
        }
        if (!messageMatch.matches()) return null;

        if (cfg.enabled) {
            return assembleMessage(cfg, messageMatch, interactiveStyle);
        }

        if (cfg.sameChatColor || ConfigManager.config.glyph.playerMessageGlyphs) {
            return assembleUnformattedMessage(cfg, rawWithCodes, messageMatch, interactiveStyle);
        }

        return null;
    }

    private static Component assembleMessage(PlayerMessagesCategory cfg, Matcher chatMatch, Style interactiveStyle) {
        char tierColorChar = chatMatch.group(1).charAt(0);
        String rankSegment = chatMatch.group(3);
        String name = chatMatch.group(4);
        String colonColor = chatMatch.group(5);
        String message = chatMatch.group(6);

        String tierTag = RankTierData.resolveTag(chatMatch.group(2));
        String glyphKey = RankTierData.glyphKeyFor(tierColorChar, tierTag);

        char rankColor = rankSegment.charAt(1);
        boolean hasRankTag = rankSegment.contains("[");
        String messageColor = cfg.sameChatColor ? "§f" : colonColor;

        MutableComponent result = Component.empty();
        boolean isFirstPart = true;

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
                        ? styledWithColor(name, chromaToHex(cfg.nameColor))
                        : Component.literal("§" + rankColor + name);
                case MESSAGE -> Component.literal(messageColor + ": " + message);
            };

            if (segment == null) continue;

            if (part != PlayerMessagesCategory.Part.MESSAGE && !isFirstPart) {
                result.append(Component.literal(" "));
            }
            if (part != PlayerMessagesCategory.Part.MESSAGE) {
                segment = preserveClickAndHover(segment, interactiveStyle);
            }
            result.append(segment);
            isFirstPart = false;
        }

        return result;
    }

    private static Component assembleUnformattedMessage(PlayerMessagesCategory cfg, String rawWithCodes, Matcher chatMatch, Style interactiveStyle) {
        String prefixText = ConfigManager.config.glyph.playerMessageGlyphs
                ? substituteTierGlyph(rawWithCodes, chatMatch)
                : rawWithCodes.substring(0, chatMatch.start(5));

        MutableComponent result = Component.empty();
        result.append(preserveClickAndHover(Component.literal(prefixText), interactiveStyle));

        String messageColor = cfg.sameChatColor ? "§f" : chatMatch.group(5);
        result.append(Component.literal(messageColor));
        result.append(Component.literal(rawWithCodes.substring(chatMatch.end(5))));

        return result;
    }

    private static String substituteTierGlyph(String rawWithCodes, Matcher chatMatch) {
        char tierColorChar = chatMatch.group(1).charAt(0);
        String tierTag = RankTierData.resolveTag(chatMatch.group(2));
        String glyphKey = RankTierData.glyphKeyFor(tierColorChar, tierTag);
        String glyph = RankTierData.glyphFor(glyphKey);
        if (glyph == null) return rawWithCodes.substring(0, chatMatch.start(5));

        int tierStart = chatMatch.start(1) - 1;
        int tierEnd = chatMatch.end(2) + 1;

        return rawWithCodes.substring(0, tierStart)
                + "§f" + glyph
                + rawWithCodes.substring(tierEnd, chatMatch.start(5));
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

    private static Component styledWithColor(String text, int hex) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(hex)));
    }

    private static int chromaToHex(ChromaColour color) {
        return color.getEffectiveColourRGB() & 0x00FFFFFF;
    }
}