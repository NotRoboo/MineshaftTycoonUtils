package com.roboo.mineshafttycoonutils.features.chat;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.PlayerMessagesCategory;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
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

    private static final Map<String, String> TAG_DISPLAY_ALIASES = new LinkedHashMap<>();
    static {
        TAG_DISPLAY_ALIASES.put("ADMN", "ADMIN");
    }

    private static final Map<Character, Character> GREEK_HOMOGLYPHS = new LinkedHashMap<>();
    static {
        GREEK_HOMOGLYPHS.put('Α', 'A');
        GREEK_HOMOGLYPHS.put('Β', 'B');
        GREEK_HOMOGLYPHS.put('Ε', 'E');
        GREEK_HOMOGLYPHS.put('Ζ', 'Z');
        GREEK_HOMOGLYPHS.put('Η', 'H');
        GREEK_HOMOGLYPHS.put('Ι', 'I');
        GREEK_HOMOGLYPHS.put('Κ', 'K');
        GREEK_HOMOGLYPHS.put('Μ', 'M');
        GREEK_HOMOGLYPHS.put('Ν', 'N');
        GREEK_HOMOGLYPHS.put('Ο', 'O');
        GREEK_HOMOGLYPHS.put('Ρ', 'P');
        GREEK_HOMOGLYPHS.put('Τ', 'T');
        GREEK_HOMOGLYPHS.put('Υ', 'Y');
        GREEK_HOMOGLYPHS.put('Χ', 'X');
    }

    private record StaffRank(String display, int hex) {}

    private static final Map<String, StaffRank> STAFF_RANKS = new LinkedHashMap<>();
    static {
        STAFF_RANKS.put("MANAGER", new StaffRank("Manager", 0x660000));
        STAFF_RANKS.put("DEV", new StaffRank("Dev", 0xFA4241));
        STAFF_RANKS.put("ADMIN", new StaffRank("Admin", 0x890000));
        STAFF_RANKS.put("BUILD", new StaffRank("Builder", 0xFDE047));
        STAFF_RANKS.put("MOD", new StaffRank("Mod", 0x5B1771));
        STAFF_RANKS.put("HELPER", new StaffRank("Helper", 0x15803D));
    }

    private static final Map<String, String> TIER_GLYPHS = new LinkedHashMap<>();
    static {
        TIER_GLYPHS.put("a" + "T1", "\uE001");
        TIER_GLYPHS.put("e" + "T2", "\uE002");
        TIER_GLYPHS.put("c" + "T3", "\uE003");
        TIER_GLYPHS.put("9" + "T4", "\uE004");
        TIER_GLYPHS.put("3" + "T4", "\uE005");
        TIER_GLYPHS.put("7" + "T5", "\uE006");
        TIER_GLYPHS.put("6" + "T5", "\uE007");
        TIER_GLYPHS.put("4" + "T5", "\uE008");

        TIER_GLYPHS.put("Manager", "\uE010");
        TIER_GLYPHS.put("Dev", "\uE011");
        TIER_GLYPHS.put("Admin", "\uE012");
        TIER_GLYPHS.put("Builder", "\uE013");
        TIER_GLYPHS.put("Mod", "\uE014");
        TIER_GLYPHS.put("Helper", "\uE015");
    }

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
        String tierRaw = normalizeGreekHomoglyphs(normalizeFullwidth(m.group(2)).toUpperCase(Locale.ROOT).trim());
        String rankSegment = m.group(3);
        String name = m.group(4);
        String colonColor = m.group(5);
        String message = m.group(6);

        String tierTag = TAG_DISPLAY_ALIASES.getOrDefault(tierRaw, tierRaw);

        StaffRank staffRank = STAFF_RANKS.get(tierTag);
        boolean isStaff = staffRank != null;
        String glyphKey = isStaff ? staffRank.display() : (tierColorChar + tierTag);

        char rankColor = rankSegment.charAt(1);
        boolean hasRankTag = rankSegment.contains("[");
        String messageColor = cfg.sameChatColor ? "§f" : colonColor;

        MutableComponent result = Component.empty();
        boolean first = true;

        for (PlayerMessagesCategory.Part part : cfg.partOrder) {
            Component segment = switch (part) {
                case TIER -> {
                    String glyph = cfg.pixelArtTags ? TIER_GLYPHS.get(glyphKey) : null;
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

    private static String normalizeGreekHomoglyphs(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sb.append(GREEK_HOMOGLYPHS.getOrDefault(c, c));
        }
        return sb.toString();
    }

    private static String normalizeFullwidth(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 0xFF01 && c <= 0xFF5E) {
                sb.append((char) (c - 0xFEE0));
            } else if (c == 0x3000) {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}