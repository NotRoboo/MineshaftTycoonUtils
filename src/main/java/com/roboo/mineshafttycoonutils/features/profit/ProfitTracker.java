package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfitTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double FORTUNE_PER_DROP = 100.0;
    private static final long WINDOW_MS = 10_000;
    private static final long DISPLAY_UPDATE_INTERVAL_MS = 500;

    private static final Pattern FORTUNE_PATTERN = Pattern.compile("([0-9,]+)✥");

    private record Gain(long time, long amount) {}

    private static long fortune = 0;
    private static long totalProfit = 0;
    private static final Deque<Gain> recentGains = new ArrayDeque<>();

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

    private static void registerBreak(TrackedOre ore) {
        long value = ore.getDropValue();
        if (value < 0) return; // refinery level unknown for this ore - open Refinery to detect it

        // 1 drop per 100 fortune
        long drops = Math.round(fortune / FORTUNE_PER_DROP);
        long gained = drops * value;
        totalProfit += gained;
        recentGains.addLast(new Gain(System.currentTimeMillis(), gained));
    }

    public static long getTotalProfit() {
        return totalProfit;
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
        long now = System.currentTimeMillis();
        while (!recentGains.isEmpty() && now - recentGains.peekFirst().time() > WINDOW_MS) {
            recentGains.pollFirst();
        }

        long sum = 0;
        for (Gain g : recentGains) sum += g.amount();

        return Math.round(sum / (WINDOW_MS / 1000.0) * 3600.0);
    }

    public static boolean needsRamLevel() {
        return !ConfigManager.config.profit.noDuneRamPet && ConfigManager.config.profit.tracker.state.duneRamLevel < 0;
    }

    private static void resetSession() {
        fortune = 0;
        watchedPos = null;
        watchedOre = null;
        recentGains.clear();
        cachedProfitPerHour = 0;
        lastDisplayUpdate = 0;
    }

    public static void resetProfit() {
        totalProfit = 0;
        recentGains.clear();
        cachedProfitPerHour = 0;
        lastDisplayUpdate = 0;
    }
}