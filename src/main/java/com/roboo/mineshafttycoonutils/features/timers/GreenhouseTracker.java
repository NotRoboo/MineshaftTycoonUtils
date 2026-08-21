package com.roboo.mineshafttycoonutils.features.timers;

import com.roboo.mineshafttycoonutils.utils.HologramUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreenhouseTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double NEARBY_RANGE = 40.0;
    private static final double HOLOGRAM_SEARCH_RADIUS = 5.0;
    private static final int SCAN_INTERVAL_TICKS = 10;

    private static final Pattern STATUS_PATTERN =
            Pattern.compile("(?i)status:\\s*(idle|growing|ready)");

    private static final Pattern PLANTED_PATTERN =
            Pattern.compile("(?i)successfully planted .+? in plot (\\d+)!");

    private static final Pattern READY_PATTERN =
            Pattern.compile("(?i)your .+? in plot (\\d+) is ready to be claimed");

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
        String stripped = ChatFormatting.stripFormatting(msg).trim();
        if (stripped.isEmpty()) return;

        Matcher planted = PLANTED_PATTERN.matcher(stripped);
        if (planted.find()) {
            setPlotState(planted.group(1), State.GROWING);
            return;
        }

        Matcher ready = READY_PATTERN.matcher(stripped);
        if (ready.find()) {
            setPlotState(ready.group(1), State.GROWN);
        }
    }

    private static void setPlotState(String numberText, State state) {
        try {
            int number = Integer.parseInt(numberText);
            Plot plot = Plot.fromNumber(number);
            if (plot != null) states.put(plot, state);
        } catch (NumberFormatException ignored) {}
    }

    private static void onTick() {
        if (mc.player == null) return;
        if (++tickCounter < SCAN_INTERVAL_TICKS) return;
        tickCounter = 0;

        for (Plot plot : Plot.values()) {
            double distSq = mc.player.position().distanceToSqr(plot.x, plot.y, plot.z);
            if (distSq > NEARBY_RANGE * NEARBY_RANGE) continue;

            for (String line : HologramUtils.findNearbyHologramLines(plot.x, plot.y, plot.z, HOLOGRAM_SEARCH_RADIUS)) {
                Matcher m = STATUS_PATTERN.matcher(line);
                if (m.find()) {
                    states.put(plot, stateFromStatus(m.group(1)));
                    break;
                }
            }
        }
    }

    private static State stateFromStatus(String status) {
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "idle" -> State.EMPTY;
            case "growing" -> State.GROWING;
            case "ready" -> State.GROWN;
            default -> State.UNKNOWN;
        };
    }

    public static State getState(Plot plot) {
        return states.getOrDefault(plot, State.UNKNOWN);
    }

    public static void reset() {
        for (Plot plot : Plot.values()) states.put(plot, State.UNKNOWN);
        tickCounter = 0;
    }
}