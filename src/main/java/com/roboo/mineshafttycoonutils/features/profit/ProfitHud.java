package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.profit.ProfitCategory;
import com.roboo.mineshafttycoonutils.utils.FishingZones;
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

                    int x = cfg.hudPosition.ProfitHudX;
                    int y = cfg.hudPosition.ProfitHudY;
                    int lineHeight = 10;
                    int line = 0;

                    graphics.drawString(mc.font, "§e§lProfit Tracker", x, y, 0xFFFFFFFF, true);
                    line++;

                    if (ProfitTracker.needsRamLevel()) {
                        graphics.drawString(mc.font, "§7Boosts: §c(Open refinery & /pets)", x, y + (lineHeight * line++), 0xFFFFFFFF, true);
                    }

                    graphics.drawString(mc.font, "§7$/Hour: §e$" + format(ProfitTracker.getProfitPerHour(), cfg.shortenNumbers) + "/hr",
                            x, y + (lineHeight * line++), 0xFFFFFFFF, true);
                    graphics.drawString(mc.font, "§7Total: §e$" + format(ProfitTracker.getTotalProfit(), cfg.shortenNumbers),
                            x, y + (lineHeight * line++), 0xFFFFFFFF, true);

                    if (cfg.showOreDrops) {
                        graphics.drawString(mc.font, "§e§lOres", x, y + (lineHeight * line++), 0xFFFFFFFF, true);

                        LinkedHashMap<String, Integer> breakdown = OreDropTracker.getBreakdown();
                        if (breakdown.isEmpty()) {
                            graphics.drawString(mc.font, "§7- None", x, y + (lineHeight * line++), 0xFFFFFFFF, true);
                        } else {
                            for (var entry : breakdown.entrySet()) {
                                graphics.drawString(mc.font, "§7- " + entry.getKey() + " §7(§e" + entry.getValue() + "§7)",
                                        x, y + (lineHeight * line++), 0xFFFFFFFF, true);
                            }
                        }
                    }
                }
        );
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