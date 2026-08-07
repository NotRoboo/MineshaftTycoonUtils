package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.profit.ProfitCategory;
import com.roboo.mineshafttycoonutils.utils.FishingZones;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;

public class ProfitHud {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final String[] SUFFIXES = {
            "", "K", "M", "B", "T", "Qd", "Qn", "Sx", "Sp", "Oc", "No", "Dc"
    };

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "profit_hud"),
                (graphics, tickCounter) -> {
                    ProfitCategory cfg = ConfigManager.config.profit;
                    if (mc.player == null || !cfg.profitTrackerEnabled) return;
                    if (cfg.onlyShowWhenMining && FishingZones.isInZone(mc.player.blockPosition())) return;

                    int lineHeight = 10;
                    LinkedHashMap<String, Integer> breakdown = OreDropTracker.getBreakdown();
                    int totalLines = countTotalLines(cfg, breakdown);
                    int totalHeight = totalLines * lineHeight;

                    int x = HudTextUtils.clampX(cfg.hudPosition.ProfitHudX);
                    int y = HudTextUtils.clampY(cfg.hudPosition.ProfitHudY, totalHeight);
                    int line = 0;
                    boolean rightAligned = HudTextUtils.isRightAligned(x);

                    HudTextUtils.drawLine(graphics, "§e§lProfit Tracker", x, y, rightAligned);
                    line++;

                    if (ProfitTracker.needsRamLevel()) {
                        HudTextUtils.drawLine(graphics, "§7Boosts: §c(Open refinery & /pets)", x, y + (lineHeight * line++), rightAligned);
                    }

                    HudTextUtils.drawLine(graphics, "§7$/Hour: §e$" + format(ProfitTracker.getProfitPerHour(), cfg.shortenNumbers) + "/hr",
                            x, y + (lineHeight * line++), rightAligned);
                    HudTextUtils.drawLine(graphics, "§7Total: §e$" + format(ProfitTracker.getTotalProfit(), cfg.shortenNumbers),
                            x, y + (lineHeight * line++), rightAligned);

                    if (cfg.showOreDrops) {
                        HudTextUtils.drawLine(graphics, "§e§lOres", x, y + (lineHeight * line++), rightAligned);

                        if (breakdown.isEmpty()) {
                            HudTextUtils.drawLine(graphics, "§7- None", x, y + (lineHeight * line++), rightAligned);
                        } else {
                            for (var entry : breakdown.entrySet()) {
                                HudTextUtils.drawLine(graphics, "§7- " + entry.getKey() + " §7(§e" + entry.getValue() + "§7)",
                                        x, y + (lineHeight * line++), rightAligned);
                            }
                        }
                    }
                }
        );
    }

    private static int countTotalLines(ProfitCategory cfg, LinkedHashMap<String, Integer> breakdown) {
        int total = 1; // header
        if (ProfitTracker.needsRamLevel()) total++;
        total += 2; // $/Hour, Total

        if (cfg.showOreDrops) {
            total++; // "Ores" header
            total += breakdown.isEmpty() ? 1 : breakdown.size();
        }

        return total;
    }

    private static String format(long value, boolean shorten) {
        if (!shorten || value < 1000) {
            return String.format("%,d", value);
        }

        int tier = 0;
        double reduced = value;
        while (reduced >= 1000 && tier < SUFFIXES.length - 1) {
            reduced /= 1000.0;
            tier++;
        }

        return String.format("%.2f%s", reduced, SUFFIXES[tier]);
    }
}