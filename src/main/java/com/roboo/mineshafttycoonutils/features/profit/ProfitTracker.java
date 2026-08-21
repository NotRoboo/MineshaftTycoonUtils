package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfitTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double FORTUNE_PER_DROP = 100.0;
    private static final long IDLE_TIMEOUT_MS = 10_000;
    private static final long DISPLAY_UPDATE_INTERVAL_MS = 500;

    private static final Pattern FORTUNE_PATTERN = Pattern.compile("([0-9,]+)✥");

    private static long fortune = 0;
    private static long totalProfit = 0;

    private static long lastMinedMillis = -1;
    private static long lastSecondMarkMillis = 0;
    private static long uptimeSeconds = 0;
    private static boolean paused = false;

    private static long cachedProfitPerHour = 0;
    private static long lastDisplayUpdate = 0;

    private static BlockPos watchedPos = null;
    private static TrackedOre watchedOre = null;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> {
            if (overlay) onActionBar(msg.getString());
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetSession());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetSession());
    }

    public static void onActionBar(String text) {
        if (text == null) return;
        String stripped = text.replaceAll("§.", "");
        Matcher m = FORTUNE_PATTERN.matcher(stripped);
        if (m.find()) {
            try {
                fortune = Long.parseLong(m.group(1).replace(",", ""));
            } catch (NumberFormatException ignored) {}
        }
    }

    private static void onTick() {
        if (!ConfigManager.config.profit.tracker.profitTrackerEnabled) return;
        if (mc.player == null || mc.level == null) return;

        updateUptime();

        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bhr.getBlockPos();
            TrackedOre ore = TrackedOre.fromBlock(mc.level.getBlockState(pos).getBlock());
            if (ore != null) {
                watchedPos = pos;
                watchedOre = ore;
            }
        }

        if (watchedPos != null) {
            TrackedOre current = TrackedOre.fromBlock(mc.level.getBlockState(watchedPos).getBlock());
            if (current != watchedOre) {
                registerBreak(watchedOre);
                watchedPos = null;
                watchedOre = null;
            }
        }
    }

    private static void updateUptime() {
        if (lastMinedMillis < 0) return;

        long now = System.currentTimeMillis();
        if (now - lastSecondMarkMillis < 1000) return;
        lastSecondMarkMillis = now;

        if (now - lastMinedMillis > IDLE_TIMEOUT_MS) {
            paused = true;
            return;
        }

        uptimeSeconds++;
    }

    private static void registerBreak(TrackedOre ore) {
        long value = ore.getDropValue();
        if (value < 0) return;

        long drops = Math.round(fortune / FORTUNE_PER_DROP);
        long gained = drops * value;
        totalProfit += gained;

        long now = System.currentTimeMillis();
        if (lastMinedMillis < 0) lastSecondMarkMillis = now;
        lastMinedMillis = now;
        paused = false;
    }

    public static long getTotalProfit() {
        return totalProfit;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static long getProfitPerHour() {
        long now = System.currentTimeMillis();
        if (now - lastDisplayUpdate >= DISPLAY_UPDATE_INTERVAL_MS) {
            lastDisplayUpdate = now;
            cachedProfitPerHour = calcProfitPerHour();
        }
        return cachedProfitPerHour;
    }

    private static long calcProfitPerHour() {
        if (uptimeSeconds <= 0) return 0;
        return Math.round(totalProfit / (double) uptimeSeconds * 3600.0);
    }

    public static boolean needsRamLevel() {
        return ConfigManager.config.profit.tracker.state.duneRamLevel < 0;
    }

    private static void resetSession() {
        fortune = 0;
        watchedPos = null;
        watchedOre = null;
        cachedProfitPerHour = 0;
        lastDisplayUpdate = 0;
    }

    public static void resetProfit() {
        totalProfit = 0;
        lastMinedMillis = -1;
        lastSecondMarkMillis = 0;
        uptimeSeconds = 0;
        paused = false;
        cachedProfitPerHour = 0;
        lastDisplayUpdate = 0;
    }
}