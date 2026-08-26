package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.scoreboard.CustomScoreboardManager;
import com.roboo.mineshafttycoonutils.features.scoreboard.ScoreboardHud;
import com.roboo.mineshafttycoonutils.utils.ComponentTextUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(Gui.class)
public class ScoreboardMixin {

    @Unique
    private static final long mineshaftUtils$UPDATE_INTERVAL_MS = 250L;

    @Unique
    private static long mineshaftUtils$lastUpdate = 0L;
    @Unique
    private static boolean mineshaftUtils$active = false;
    @Unique
    private static List<String> mineshaftUtils$cachedLines = List.of();

    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void mineshaftUtils$overrideScoreboard(GuiGraphics guiGraphics, Objective objective, CallbackInfo ci) {
        Scoreboard scoreboard = objective.getScoreboard();

        long now = System.currentTimeMillis();
        if (now - mineshaftUtils$lastUpdate >= mineshaftUtils$UPDATE_INTERVAL_MS) {
            mineshaftUtils$lastUpdate = now;

            List<String> rawLines = mineshaftUtils$extractRawLines(scoreboard, objective);
            if (!rawLines.isEmpty()) {
                CustomScoreboardManager.observe(rawLines);

                mineshaftUtils$active = ConfigManager.config.scoreboard.enabled
                        && CustomScoreboardManager.isMineshaftTycoonBoard(rawLines);

                if (mineshaftUtils$active) {
                    mineshaftUtils$cachedLines = CustomScoreboardManager.formatDisplayLines();
                }
            }
        }

        if (!mineshaftUtils$active) return;

        ScoreboardHud.renderLines(guiGraphics, mineshaftUtils$cachedLines);
        ci.cancel();
    }

    @Unique
    private List<String> mineshaftUtils$extractRawLines(Scoreboard scoreboard, Objective objective) {
        List<String> lines = new ArrayList<>();
        Collection<PlayerScoreEntry> entries = scoreboard.listPlayerScores(objective);

        for (PlayerScoreEntry entry : entries) {
            if (entry.isHidden()) continue;

            PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
            Component formatted = PlayerTeam.formatNameForTeam(team, entry.ownerName());
            lines.add(ComponentTextUtils.formattedText(formatted));
        }

        return lines;
    }
}