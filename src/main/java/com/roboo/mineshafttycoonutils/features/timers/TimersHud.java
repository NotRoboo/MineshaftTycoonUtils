package com.roboo.mineshafttycoonutils.features.timers;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.TimersCategory;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class TimersHud {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "timers_hud"),
                (graphics, tickCounter) -> {
                    TimersCategory cfg = ConfigManager.config.timers;
                    if (mc.player == null || !cfg.hudEnabled) return;

                    List<String> lines = new ArrayList<>();
                    for (TimersCategory.Entry entry : cfg.order) {
                        String rendered = renderEntry(entry, cfg);
                        if (rendered != null) lines.add(rendered);
                    }
                    if (lines.isEmpty()) return;

                    int lineHeight = 10;
                    int totalLines = lines.size() + 1; // +1 for header
                    int totalHeight = totalLines * lineHeight;

                    int x = HudTextUtils.clampX(cfg.hudPosition.timersHudX);
                    int y = HudTextUtils.clampY(cfg.hudPosition.timersHudY, totalHeight);
                    int line = 0;

                    boolean rightAligned = HudTextUtils.isRightAligned(x);

                    HudTextUtils.drawLine(graphics, "§e§lTimers", x, y + (lineHeight * line++), rightAligned);
                    for (String l : lines) {
                        HudTextUtils.drawLine(graphics, l, x, y + (lineHeight * line++), rightAligned);
                    }
                }
        );
    }

    private static String renderEntry(TimersCategory.Entry entry, TimersCategory cfg) {
        return switch (entry) {
            case T4_POTION -> renderBuff("T4 Potion: ", BuffTracker.Buff.T4_POTION, cfg);
            case T3_POTION -> renderBuff("T3 Potion: ", BuffTracker.Buff.T3_POTION, cfg);
            case T2_POTION -> renderBuff("T2 Potion: ", BuffTracker.Buff.T2_POTION, cfg);
            case T1_POTION -> renderBuff("T1 Potion: ", BuffTracker.Buff.T1_POTION, cfg);
            case IRONVINE -> renderBuff("Ironvine: ", BuffTracker.Buff.IRONVINE, cfg);
            case REDROOT -> renderBuff("Redroot: ", BuffTracker.Buff.REDROOT, cfg);
            case AURORA_FRUIT -> renderBuff("Aurora Fruit: ", BuffTracker.Buff.AURORA_FRUIT, cfg);
            case SQUASH -> renderBuff("Squash: ", BuffTracker.Buff.SQUASH, cfg);
            case DUSTGRAIN -> renderBuff("Dustgrain: ", BuffTracker.Buff.DUSTGRAIN, cfg);
            case SUNFLOWER -> renderBuff("Sunflower: ", BuffTracker.Buff.SUNFLOWER, cfg);
            case FISHING_BUFF -> renderBuff("Fishing Buff: ", BuffTracker.Buff.FISHING_BUFF, cfg);
            case PETAD -> renderPetad(cfg);
            case ILS_RESTOCK -> renderIlsRestock(cfg);
        };
    }

    private static String renderBuff(String label, BuffTracker.Buff buff, TimersCategory cfg) {
        long secondsLeft = BuffTracker.getSecondsLeft(buff);
        if (secondsLeft <= 0) return null;

        String time = formatDuration(secondsLeft, cfg);
        boolean enabled = BuffTracker.isEnabled(buff);
        return "§7" + label + "§e" + time + (enabled ? "" : " §c(Disabled)");
    }

    private static String renderPetad(TimersCategory cfg) {
        if (!PetAdTracker.isKnown()) return null;

        long secondsLeft = PetAdTracker.getSecondsLeft();
        if (secondsLeft <= 0) return "§7Petad: §cPetad Ready!";

        return "§7Petad: §e" + formatDuration(secondsLeft, cfg);
    }

    private static String renderIlsRestock(TimersCategory cfg) {
        if (!IlsRestockTracker.isKnown()) return "§7Il's Restock: §c(Talk to Il to enable timer)";

        long secondsLeft = IlsRestockTracker.getSecondsLeft();
        return "§7Il's Restock: §e" + formatDuration(secondsLeft, cfg);
    }

    private static String formatDuration(long totalSeconds, TimersCategory cfg) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        boolean hideSeconds = cfg.hideSecondsUntilFinalMinute && totalSeconds >= 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (days > 0 || hours > 0) sb.append(hours).append("h ");
        if (days > 0 || hours > 0 || minutes > 0) sb.append(minutes).append("m");
        if (!hideSeconds) {
            if (days > 0 || hours > 0 || minutes > 0) sb.append(" ");
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }
}