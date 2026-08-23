package com.roboo.mineshafttycoonutils.features.timers;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.TimersCategory;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.MovableHud;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class TimersHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;

    private static final MovableHud TIMER_BOX = new MovableHud() {
        @Override
        public String getDisplayName() {
            return "Timers";
        }

        @Override
        public boolean isMasterEnabled() {
            return ConfigManager.config.timers.hudEnabled;
        }

        @Override
        public int getX() {
            TimersCategory cfg = ConfigManager.config.timers;
            int anchorX = cfg.timersHudX;
            return HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip) ? anchorX - getWidth() : anchorX;
        }

        @Override
        public int getY() {
            return ConfigManager.config.timers.timersHudY;
        }

        @Override
        public int getWidth() {
            return calcWidth();
        }

        @Override
        public int getHeight() {
            return calcHeight();
        }

        @Override
        public void setPosition(int x, int y) {
            ConfigManager.config.timers.timersHudX = x;
            ConfigManager.config.timers.timersHudY = y;
        }

        @Override
        public void render(GuiGraphics graphics) {
            TimersCategory cfg = ConfigManager.config.timers;
            drawContent(graphics, cfg.timersHudX, cfg.timersHudY);
        }
    };

    public static void init() {
        HudEditorRegistry.register(TIMER_BOX);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "timers_hud"),
                (graphics, tickCounter) -> {
                    TimersCategory cfg = ConfigManager.config.timers;
                    if (mc.player == null || !cfg.hudEnabled) return;

                    List<String> lines = calcLines(cfg);
                    if (lines.isEmpty()) return;

                    int totalHeight = (lines.size() + 1) * LINE_HEIGHT;

                    int x = HudTextUtils.clampX(cfg.timersHudX);
                    int y = HudTextUtils.clampY(cfg.timersHudY, totalHeight);

                    drawContent(graphics, x, y);
                }
        );
    }

    private static final String TITLE_TEXT = "§lTimers";

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        TimersCategory cfg = ConfigManager.config.timers;
        List<String> lines = calcLines(cfg);
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);

        HudTextUtils.drawLine(graphics, TITLE_TEXT, anchorX, y, rightAligned, HudTextUtils.chromaToArgb(cfg.titleColor));
        int line = 1;
        for (String l : lines) {
            HudTextUtils.drawLine(graphics, l, anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        }
    }

    private static List<String> calcLines(TimersCategory cfg) {
        List<String> lines = new ArrayList<>();
        for (TimersCategory.Entry entry : cfg.order) {
            if (entry == TimersCategory.Entry.GREENHOUSE) {
                lines.addAll(renderGreenhouseLines());
                continue;
            }

            String rendered = renderEntry(entry, cfg);
            if (rendered != null) lines.add(rendered);
        }
        return lines;
    }

    private static int calcWidth() {
        TimersCategory cfg = ConfigManager.config.timers;
        int width = mc.font.width(TITLE_TEXT);
        for (String line : calcLines(cfg)) {
            width = Math.max(width, mc.font.width(line));
        }
        return width;
    }

    private static int calcHeight() {
        List<String> lines = calcLines(ConfigManager.config.timers);
        if (lines.isEmpty()) return LINE_HEIGHT;
        return (lines.size() + 1) * LINE_HEIGHT;
    }

    private static String renderEntry(TimersCategory.Entry entry, TimersCategory cfg) {
        return switch (entry) {
            case T4_POTION -> renderPotion("T4 Pot: ", BuffTracker.Buff.T4_POTION, cfg);
            case T3_POTION -> renderPotion("T3 Pot: ", BuffTracker.Buff.T3_POTION, cfg);
            case T2_POTION -> renderPotion("T2 Pot: ", BuffTracker.Buff.T2_POTION, cfg);
            case T1_POTION -> renderPotion("T1 Pot: ", BuffTracker.Buff.T1_POTION, cfg);
            case IRONVINE -> renderBuff("Ironvine: ", BuffTracker.Buff.IRONVINE, cfg);
            case REDROOT -> renderBuff("Redroot: ", BuffTracker.Buff.REDROOT, cfg);
            case AURORA_FRUIT -> renderBuff("Aurora Fruit: ", BuffTracker.Buff.AURORA_FRUIT, cfg);
            case SQUASH -> renderBuff("Squash: ", BuffTracker.Buff.SQUASH, cfg);
            case DUSTGRAIN -> renderBuff("Dustgrain: ", BuffTracker.Buff.DUSTGRAIN, cfg);
            case SUNFLOWER -> renderBuff("Sunflower: ", BuffTracker.Buff.SUNFLOWER, cfg);
            case FISHING_BUFF -> renderBuff("Fishing Buff: ", BuffTracker.Buff.FISHING_BUFF, cfg);
            case PETAD -> renderPetad(cfg);
            case ILS_RESTOCK -> renderIlsRestock(cfg);
            case GREENHOUSE -> null;
        };
    }

    private static String renderPotion(String label, BuffTracker.Buff buff, TimersCategory cfg) {
        long secondsLeft = BuffTracker.getPotionSecondsLeft(buff);
        if (secondsLeft <= 0) return null;

        String time = formatDuration(secondsLeft, cfg);
        boolean active = BuffTracker.isPotionActive(buff);
        String color = active ? "§e" : "§c";
        return "§7" + label + color + time;
    }

    private static List<String> renderGreenhouseLines() {
        List<String> lines = new ArrayList<>();
        for (GreenhouseTracker.Plot plot : GreenhouseTracker.Plot.values()) {
            GreenhouseTracker.State state = GreenhouseTracker.getState(plot);
            lines.add("§7Plot " + plot.number + ": " + state.label());
        }
        return lines;
    }

    private static String renderBuff(String label, BuffTracker.Buff buff, TimersCategory cfg) {
        long secondsLeft = BuffTracker.getSecondsLeft(buff);
        if (secondsLeft <= 0) return null;

        String time = formatDuration(secondsLeft, cfg);
        boolean enabled = BuffTracker.isEnabled(buff);
        String color = enabled ? "§e" : "§c";
        return "§7" + label + color + time;
    }

    private static String renderPetad(TimersCategory cfg) {
        if (!PetAdTracker.isKnown()) return null;

        long secondsLeft = PetAdTracker.getSecondsLeft();
        if (secondsLeft <= 0) return "§7Petad: §cPetad Ready!";

        return "§7Petad: §e" + formatDuration(secondsLeft, cfg);
    }

    private static String renderIlsRestock(TimersCategory cfg) {
        if (!IlsRestockTracker.isKnown()) return "§7Il's Restock: §c(Talk to Il)";

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