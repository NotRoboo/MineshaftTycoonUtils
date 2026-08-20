package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreboardUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("MineshaftTycoonUtils");
    private static final Minecraft mc = Minecraft.getInstance();

    private ScoreboardUtils() {}

    public static void dumpSidebar() {
        if (mc.player == null || mc.level == null) {
            sendPlain("§cNo active world to read a scoreboard from.");
            return;
        }

        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            sendPlain("§cNo objective is currently displayed in the sidebar.");
            return;
        }

        List<PlayerScoreEntry> entries = new ArrayList<>(scoreboard.listPlayerScores(objective));
        entries.sort(Comparator.comparingInt(PlayerScoreEntry::value).reversed());

        String title = objective.getDisplayName().getString();

        sendHeader("§e§lScoreboard Dump §7(" + title + ")");
        LOGGER.info("Scoreboard Dump ({})", title);

        if (entries.isEmpty()) {
            sendPlain("§7(no entries)");
            LOGGER.info("  (no entries)");
            return;
        }

        for (PlayerScoreEntry entry : entries) {
            String line = resolveLineText(scoreboard, entry);

            sendPlain("§7- " + line + " §8[" + entry.value() + "]");
            LOGGER.info("  \"{}\" [{}]", line, entry.value());
        }
    }

    private static String resolveLineText(Scoreboard scoreboard, PlayerScoreEntry entry) {
        PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());

        String middle = entry.display() != null ? entry.display().getString() : "";

        if (team != null) {
            String prefix = ComponentTextUtils.formattedText(team.getPlayerPrefix());
            String suffix = ComponentTextUtils.formattedText(team.getPlayerSuffix());
            String combined = prefix + middle + suffix;
            if (!combined.isBlank()) return combined;
        }

        if (!middle.isBlank()) return middle;

        return entry.owner();
    }

    private static void sendHeader(String message) {
        if (mc.player == null) return;
        mc.player.displayClientMessage(SystemMessages.get().append(colored(" " + message)), false);
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
                ChatFormatting formatting = ChatFormatting.getByCode(legacyText.charAt(i + 1));
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