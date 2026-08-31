package com.roboo.mineshafttycoonutils.features.scoreboard;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.ScoreboardCategory;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomScoreboardManager {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}/\\d{2}/\\d{2}\\b");
    private static final String TITLE_TEXT = "§4§lMINESHAFT §c§lTYCOON";

    private static final Pattern PET_MAGMA_PATTERN =
            Pattern.compile("(?i)^\\*?\\s*pets!\\s*your\\s+.+?\\s+pet mined up\\s+(\\d+)\\s+magma for you!$");

    private static final Pattern MAGMA_OVERFLOW_PATTERN =
            Pattern.compile("(?i)^\\*?\\s*magma!\\s*you had too much magma and it bubbled out of your storage!$");

    private static final Pattern SCOREBOARD_MAGMA_PATTERN = Pattern.compile("(?<!§)([0-9][0-9,]*)");

    private CustomScoreboardManager() {}

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            handlePetMagmaMessage(msg.getString());
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> {
            handlePetMagmaMessage(msg.getString());
            return true;
        });
    }

    private static void handlePetMagmaMessage(String msg) {
        if (msg == null) return;
        String stripped = ChatFormatting.stripFormatting(msg).trim();
        if (stripped.isEmpty()) return;

        if (MAGMA_OVERFLOW_PATTERN.matcher(stripped).matches()) {
            addMagma(-1);
            return;
        }

        Matcher m = PET_MAGMA_PATTERN.matcher(stripped);
        if (!m.matches()) return;

        long amount;
        try {
            amount = Long.parseLong(m.group(1));
        } catch (NumberFormatException e) {
            return;
        }

        addMagma(amount);
    }

    private static void addMagma(long amount) {
        ScoreboardCategory cfg = ConfigManager.config.scoreboard;
        String known = cfg.lastKnownLines.get(ScoreboardCategory.Line.MAGMA);
        if (known == null) return;

        Matcher numberMatch = SCOREBOARD_MAGMA_PATTERN.matcher(known);
        if (!numberMatch.find()) return;

        long current;
        try {
            current = Long.parseLong(numberMatch.group(1).replace(",", ""));
        } catch (NumberFormatException e) {
            return;
        }

        long updated = current + amount;
        String updatedLine = known.substring(0, numberMatch.start())
                + String.format("%,d", updated)
                + known.substring(numberMatch.end());

        cfg.lastKnownLines.put(ScoreboardCategory.Line.MAGMA, updatedLine);
        MineshaftTycoonUtils.configManager.saveConfig();
    }

    public static void observe(List<String> rawFormattedLines) {
        ScoreboardCategory cfg = ConfigManager.config.scoreboard;
        boolean changed = false;

        EnumSet<ScoreboardCategory.Line> seen = EnumSet.noneOf(ScoreboardCategory.Line.class);

        for (String raw : rawFormattedLines) {
            if (raw == null) continue;
            String stripped = ChatFormatting.stripFormatting(raw).trim();
            if (stripped.isEmpty()) continue;

            ScoreboardCategory.Line line = matchScoreboardLine(stripped);
            if (line == null) continue;

            seen.add(line);

            String previous = cfg.lastKnownLines.get(line);
            if (!raw.equals(previous)) {
                cfg.lastKnownLines.put(line, raw);
                changed = true;
            }
        }

        for (ScoreboardCategory.Line line : ScoreboardCategory.Line.values()) {
            if (line.onlyShowWhenKnown() && !seen.contains(line) && cfg.lastKnownLines.remove(line) != null) {
                changed = true;
            }
        }

        if (changed) {
            MineshaftTycoonUtils.configManager.saveConfig();
        }
    }

    public static boolean isMineshaftTycoonBoard(List<String> rawFormattedLines) {
        for (String raw : rawFormattedLines) {
            if (raw == null) continue;
            String stripped = ChatFormatting.stripFormatting(raw).trim().toUpperCase(Locale.ROOT);
            if (stripped.contains("MINESHAFT") && stripped.contains("TYCOON")) return true;
        }
        return false;
    }

    public static List<String> formatDisplayLines() {
        ScoreboardCategory cfg = ConfigManager.config.scoreboard;
        List<String> result = new ArrayList<>(cfg.lineOrder.size() + 1);
        result.add(TITLE_TEXT);

        for (ScoreboardCategory.Line line : cfg.lineOrder) {
            if (line == null) continue;

            if (line.isBlank()) {
                result.add("");
                continue;
            }

            String known = cfg.lastKnownLines.get(line);
            if (known != null) {
                result.add(known);
            } else if (!line.onlyShowWhenKnown()) {
                result.add("§7" + line.placeholderLabel() + ": §f0");
            }
        }

        return result;
    }

    private static ScoreboardCategory.Line matchScoreboardLine(String stripped) {
        String lower = stripped.toLowerCase(Locale.ROOT);

        if (lower.contains("itv")) return ScoreboardCategory.Line.BY_ITV;
        if (lower.contains("level »")) return ScoreboardCategory.Line.LEVEL;
        if (lower.contains("coins »")) return ScoreboardCategory.Line.COINS;
        if (lower.contains("ash »")) return ScoreboardCategory.Line.ASH;
        if (lower.contains("magma »")) return ScoreboardCategory.Line.MAGMA;
        if (lower.contains("ice »")) return ScoreboardCategory.Line.ICE;
        if (lower.contains("asc »")) return ScoreboardCategory.Line.ASC;
        if (lower.contains("shards »")) return ScoreboardCategory.Line.SHARDS;
        if (lower.contains("candy »") || lower.contains("skin »")) return ScoreboardCategory.Line.EVENT;
        if (lower.contains("⌚ time »")) return ScoreboardCategory.Line.TIME;
        if (lower.contains("time »")) return ScoreboardCategory.Line.FISHING_TIME;
        if (lower.contains("www.hypixel")) return ScoreboardCategory.Line.HYPIXEL_NET;
        if (DATE_PATTERN.matcher(stripped).find()) return ScoreboardCategory.Line.DATE;

        return null;
    }
}