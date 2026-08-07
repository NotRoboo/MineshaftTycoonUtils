package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfitTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double FORTUNE_PER_DROP = 100.0;
    private static final long WINDOW_MS = 10_000;
    private static final long DISPLAY_UPDATE_INTERVAL_MS = 2_000;

    private static final Pattern FORTUNE_PATTERN = Pattern.compile("([0-9,]+)✥");

    private enum TrackedOre {
        BASALT(Blocks.GRAY_WOOL, "Basalt", 75_000, () -> ConfigManager.config.profit.state.basaltLevel, 1_875, true),
        BRECCA(Blocks.WHITE_STAINED_GLASS, "Brecca", 40_000, () -> ConfigManager.config.profit.state.breccaLevel, 1_000, true),
        REGOLITH(Blocks.ACACIA_PLANKS, "Regolith", 180_000, () -> ConfigManager.config.profit.state.regolithLevel, 4_500, true),
        AMBER_ROCK(Blocks.ORANGE_WOOL, "Amber Rock", 88_000, () -> ConfigManager.config.profit.state.amberRockLevel, 2_200, true),
        AMBER_CRYSTAL(Blocks.ORANGE_STAINED_GLASS, "Amber Crystal", 46_000, () -> ConfigManager.config.profit.state.amberCrystalLevel, 1_150, true),
        COSMIC_FIBER(Blocks.RED_STAINED_GLASS, "Cosmic Fiber", 68_000, () -> ConfigManager.config.profit.state.cosmicFiberLevel, 2_375, true),
        CRIMSON_PLASMA(Blocks.RED_WOOL, "Crimson Plasma", 95_000, () -> ConfigManager.config.profit.state.crimsonPlasmaLevel, 1_700, true),

        LIGHT_BLUE_STAINED_GLASS(Blocks.LIGHT_BLUE_STAINED_GLASS, "Light Blue Stained Glass", 10_000, () -> 0, 0, false),
        COAL_BLOCK(Blocks.COAL_BLOCK, "Coal Block", 11_000, () -> 0, 0, false),
        IRON_BLOCK(Blocks.IRON_BLOCK, "Iron Block", 12_500, () -> 0, 0, false),
        GOLD_BLOCK(Blocks.GOLD_BLOCK, "Gold Block", 14_000, () -> 0, 0, false),
        EMERALD_BLOCK(Blocks.EMERALD_BLOCK, "Emerald Block", 17_000, () -> 0, 0, false),
        DIAMOND_BLOCK(Blocks.DIAMOND_BLOCK, "Diamond Block", 20_000, () -> 0, 0, false);

        private final Block block;
        private final String displayName;
        private final long baseValue;
        private final IntSupplier levelSupplier;
        private final long perLevel;
        private final boolean refineryBased;

        TrackedOre(Block block, String displayName, long baseValue, IntSupplier levelSupplier, long perLevel, boolean refineryBased) {
            this.block = block;
            this.displayName = displayName;
            this.baseValue = baseValue;
            this.levelSupplier = levelSupplier;
            this.perLevel = perLevel;
            this.refineryBased = refineryBased;
        }

        String displayName() { return displayName; }

        long getDropValue() {
            long total;

            if (refineryBased) {
                int refineryLevel = levelSupplier.getAsInt();
                if (refineryLevel < 0) return -1;

                long refineryBonus = perLevel * refineryLevel;
                int ramLevel = ConfigManager.config.profit.noDuneRamPet
                        ? 0
                        : Math.max(ConfigManager.config.profit.state.duneRamLevel, 0);
                long ramBonus = Math.round(refineryBonus * (ramLevel * 0.02));

                total = baseValue + refineryBonus + ramBonus;
            } else {
                total = baseValue;
            }

            if (ConfigManager.config.profit.cashRegisterEnabled) {
                total = Math.round(total * 1.03);
            }

            return total;
        }

        static TrackedOre fromBlock(Block block) {
            for (TrackedOre ore : values()) {
                if (ore.block == block) return ore;
            }
            return null;
        }
    }

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
        if (!ConfigManager.config.profit.profitTrackerEnabled) return;
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
            cachedProfitPerHour = computeProfitPerHour();
        }
        return cachedProfitPerHour;
    }

    private static long computeProfitPerHour() {
        long now = System.currentTimeMillis();
        while (!recentGains.isEmpty() && now - recentGains.peekFirst().time() > WINDOW_MS) {
            recentGains.pollFirst();
        }

        long sum = 0;
        for (Gain g : recentGains) sum += g.amount();

        return Math.round(sum / (WINDOW_MS / 1000.0) * 3600.0);
    }

    public static List<String> getMissingOreNames() {
        List<String> missing = new ArrayList<>();
        for (TrackedOre ore : TrackedOre.values()) {
            if (ore.getDropValue() < 0) missing.add(ore.displayName());
        }
        return missing;
    }

    public static boolean needsRamLevel() {
        return !ConfigManager.config.profit.noDuneRamPet && ConfigManager.config.profit.state.duneRamLevel < 0;
    }

    public static void clear() {
        totalProfit = 0;
        recentGains.clear();
        cachedProfitPerHour = 0;
        lastDisplayUpdate = 0;
    }
}