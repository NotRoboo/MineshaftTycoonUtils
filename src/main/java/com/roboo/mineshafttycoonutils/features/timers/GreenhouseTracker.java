package com.roboo.mineshafttycoonutils.features.timers;

import com.roboo.mineshafttycoonutils.utils.HologramUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class GreenhouseTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double NEARBY_RANGE = 40.0;
    private static final double HOLOGRAM_SEARCH_RADIUS = 5.0;
    private static final int SCAN_INTERVAL_TICKS = 10;

    public enum State {
        UNKNOWN("§cUnknown"),
        GROWING("§eGrowing"),
        GROWN("§aReady"),
        EMPTY("§cEmpty");

        private final String label;

        State(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum Plot {
        PLOT_1(1, -29, 107, 39),
        PLOT_2(2, -43, 107, 39),
        PLOT_3(3, -36, 113, 39);

        public final int number;
        public final double x;
        public final double y;
        public final double z;

        Plot(int number, double x, double y, double z) {
            this.number = number;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public static Plot fromNumber(int number) {
            for (Plot plot : values()) {
                if (plot.number == number) return plot;
            }
            return null;
        }
    }

    private static final Map<Plot, State> states = new EnumMap<>(Plot.class);
    static {
        for (Plot plot : Plot.values()) states.put(plot, State.UNKNOWN);
    }

    private static int tickCounter = 0;

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            handleMessage(msg.getString());
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> {
            handleMessage(msg.getString());
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void handleMessage(String msg) {
        if (msg == null) return;
        String lower = ChatFormatting.stripFormatting(msg).trim().toLowerCase(Locale.ROOT);
        if (lower.isEmpty()) return;

        if (lower.contains("successfully planted ")) {
            setPlotState(numberAfter(lower, "in plot "), State.GROWING);
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
        if (plot != null) states.put(plot, state);
    }

    private static void onTick() {
        if (mc.player == null) return;
        if (++tickCounter < SCAN_INTERVAL_TICKS) return;
        tickCounter = 0;

        for (Plot plot : Plot.values()) {
            double distSq = mc.player.position().distanceToSqr(plot.x, plot.y, plot.z);
            if (distSq > NEARBY_RANGE * NEARBY_RANGE) continue;

            for (String line : HologramUtils.findNearbyHologramLines(plot.x, plot.y, plot.z, HOLOGRAM_SEARCH_RADIUS)) {
                State state = stateFromHologram(line);
                if (state != null) {
                    states.put(plot, state);
                    break;
                }
            }
        }
    }

    private static State stateFromHologram(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        int at = lower.indexOf("status:");
        if (at < 0) return null;

        String status = lower.substring(at + "status:".length()).trim();
        if (status.startsWith("idle")) return State.EMPTY;
        if (status.startsWith("growing")) return State.GROWING;
        if (status.startsWith("ready")) return State.GROWN;
        return null;
    }

    public static State getState(Plot plot) {
        return states.getOrDefault(plot, State.UNKNOWN);
    }

    public static void reset() {
        for (Plot plot : Plot.values()) states.put(plot, State.UNKNOWN);
        tickCounter = 0;
    }

    static void applyPercent(int number, double percent) {
        Plot plot = Plot.fromNumber(number);
        if (plot == null) return;

        State state = percent <= 0 ? State.EMPTY
                : percent >= 100 ? State.GROWN
                  : State.GROWING;
        states.put(plot, state);
    }
}