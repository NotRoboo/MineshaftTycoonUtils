package com.roboo.mineshafttycoonutils.features.timers;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class GreenhouseTracker {

    private static final int PLANT_POTDUR_DELAY_TICKS = 100;

    public enum State {
        UNKNOWN,
        GROWING,
        GROWN,
        EMPTY
    }

    public enum Plot {
        PLOT_1(1),
        PLOT_2(2),
        PLOT_3(3);

        public final int number;

        Plot(int number) {
            this.number = number;
        }

        public static Plot fromNumber(int number) {
            for (Plot plot : values()) {
                if (plot.number == number) return plot;
            }
            return null;
        }
    }

    private static final Map<Plot, State> states = new EnumMap<>(Plot.class);
    private static final Map<Plot, Double> percents = new EnumMap<>(Plot.class);

    static {
        for (Plot plot : Plot.values()) states.put(plot, State.UNKNOWN);
    }

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            handleMessage(msg.getString());
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> {
            handleMessage(msg.getString());
            return true;
        });
    }

    private static void handleMessage(String msg) {
        if (msg == null) return;
        String lower = ChatFormatting.stripFormatting(msg).trim().toLowerCase(Locale.ROOT);
        if (lower.isEmpty()) return;

        if (lower.contains("successfully planted ")) {
            setPlotState(numberAfter(lower, "in plot "), State.GROWING);
            PotDurTracker.scheduleDebounced(PLANT_POTDUR_DELAY_TICKS);
            return;
        }

        if (lower.contains("your ") && lower.contains("is ready to be claimed")) {
            setPlotState(numberAfter(lower, "in plot "), State.GROWN);
        }
    }

    private static int numberAfter(String text, String marker) {
        int at = text.indexOf(marker);
        if (at < 0) return -1;

        int start = at + marker.length();
        int end = start;
        while (end < text.length() && Character.isDigit(text.charAt(end))) end++;
        if (end == start) return -1;

        try {
            return Integer.parseInt(text.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void setPlotState(int number, State state) {
        Plot plot = Plot.fromNumber(number);
        if (plot == null) return;

        states.put(plot, state);
        switch (state) {
            case GROWN -> percents.put(plot, 100.0);
            case EMPTY -> percents.put(plot, 0.0);
            default -> percents.remove(plot);
        }
    }

    static void applyPercent(int number, double percent) {
        Plot plot = Plot.fromNumber(number);
        if (plot == null) return;

        State state = percent <= 0 ? State.EMPTY
                : percent >= 100 ? State.GROWN
                  : State.GROWING;
        states.put(plot, state);
        percents.put(plot, Math.min(percent, 100.0));
    }

    public static State getState(Plot plot) {
        return states.getOrDefault(plot, State.UNKNOWN);
    }

    public static boolean anyGrowing() {
        return states.containsValue(State.GROWING);
    }

    public static String describe(Plot plot) {
        double percent = percents.getOrDefault(plot, -1.0);
        return switch (getState(plot)) {
            case EMPTY -> null;
            case GROWN -> "§a100%";
            case GROWING -> percent > 0
                    ? "§e" + formatPercent(percent) + "%"
                    : "§eGrowing";
            case UNKNOWN -> "§cUnknown";
        };
    }

    private static String formatPercent(double percent) {
        double floored = Math.floor(Math.min(percent, 99.99) * 100) / 100;
        return String.format(Locale.ROOT, "%.2f", floored);
    }

    public static void reset() {
        for (Plot plot : Plot.values()) states.put(plot, State.UNKNOWN);
        percents.clear();
    }
}