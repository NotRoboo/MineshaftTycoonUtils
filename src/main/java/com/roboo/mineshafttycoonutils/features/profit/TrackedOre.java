package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.IntSupplier;

public enum TrackedOre {

    // T1 Ores
    GRAVEL(List.of(Blocks.GRAVEL), "GRAVEL", 1, () -> 0, 0, false),
    COAL_ORE(List.of(Blocks.COAL_ORE), "COAL ORE", 5, () -> 0, 0, false),
    IRON_ORE(List.of(Blocks.IRON_ORE), "IRON ORE", 10, () -> 0, 0, false),
    GOLD_ORE(List.of(Blocks.GOLD_ORE), "GOLD ORE", 20, () -> 0, 0, false),
    LAPIS_ORE(List.of(Blocks.LAPIS_ORE), "LAPIS LAZULI ORE", 50, () -> 0, 0, false),
    EMERALD_ORE(List.of(Blocks.EMERALD_ORE), "EMERALD ORE", 100, () -> 0, 0, false),
    DIAMOND_ORE(List.of(Blocks.DIAMOND_ORE), "DIAMOND ORE", 200, () -> 0, 0, false),

    // T2 Ores
    SAND(List.of(Blocks.SAND), "SAND", 500, () -> 0, 0, false),
    GRANITE(List.of(Blocks.GRANITE), "GRANITE", 750, () -> 0, 0, false),
    COMPACT_GRANITE(List.of(Blocks.POLISHED_GRANITE), "COMPACT GRANITE", 1_000,  () -> 0, 0, false),
    BRONITE(List.of(Blocks.LIGHT_GRAY_TERRACOTTA), "BRONITE", 1_500, () -> 0, 0, false),
    HEMATITE(List.of(Blocks.ORANGE_TERRACOTTA), "HEMATITE", 2_250, () -> 0, 0, false),

    // T3 Ores
    SOUL_SAND(List.of(Blocks.SOUL_SAND), "SOUL SAND", 6_000, () -> 0, 0, false),
    GLOWSTONE(List.of(Blocks.GLOWSTONE), "GLOWSTONE", 4_800, () -> 0, 0, false),
    QUARTZ_ORE(List.of(Blocks.NETHER_QUARTZ_ORE), "QUARTZ ORE", 3_600, () -> 0, 0, false),
    QUARTZ_BLOCK(List.of(Blocks.QUARTZ_BLOCK), "QUARTZ BLOCK", 4_800, () -> 0, 0, false),
    ERYTHRITE(List.of(Blocks.REDSTONE_BLOCK), "ERYTHRITE", 7_000, () -> 0, 0, false),

    // T4 Ores
    ICE(List.of(Blocks.LIGHT_BLUE_STAINED_GLASS), "COMPACT ICE", 10_000, () -> 0, 0, false),
    COMPACT_COAL(List.of(Blocks.COAL_BLOCK), "COMPACT COAL", 11_000, () -> 0, 0, false),
    COMPACT_IRON(List.of(Blocks.IRON_BLOCK), "COMPACT IRON", 12_500, () -> 0, 0, false),
    COMPACT_GOLD(List.of(Blocks.GOLD_BLOCK), "COMPACT GOLD", 14_000, () -> 0, 0, false),
    COMPACT_EMERALD(List.of(Blocks.EMERALD_BLOCK), "COMPACT EMERALD", 17_000, () -> 0, 0, false),
    COMPACT_DIAMOND(List.of(Blocks.DIAMOND_BLOCK), "COMPACT DIAMOND", 20_000, () -> 0, 0, false),

    // T5 Ores
    BASALT(List.of(Blocks.GRAY_WOOL), "BASALT", 75_000, () -> ConfigManager.config.profit.tracker.state.basaltLevel, 1_875, true),
    BRECCA(List.of(Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE), "BRECCA CRYSTAL", 40_000, () -> ConfigManager.config.profit.tracker.state.breccaLevel, 1_000, true),
    REGOLITH(List.of(Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, Blocks.ACACIA_STAIRS), "REGOLITH", 180_000, () -> ConfigManager.config.profit.tracker.state.regolithLevel, 4_500, true),
    AMBER_ROCK(List.of(Blocks.ORANGE_WOOL), "AMBER ROCK", 88_000, () -> ConfigManager.config.profit.tracker.state.amberRockLevel, 2_200, true),
    AMBER_CRYSTAL(List.of(Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE), "AMBER CRYSTAL", 46_000, () -> ConfigManager.config.profit.tracker.state.amberCrystalLevel, 1_150, true),
    COSMIC_FIBER(List.of(Blocks.RED_STAINED_GLASS), "COSMIC FIBER", 68_000, () -> ConfigManager.config.profit.tracker.state.cosmicFiberLevel, 2_375, true),
    CRIMSON_PLASMA(List.of(Blocks.RED_WOOL), "CRIMSON PLASMA", 95_000, () -> ConfigManager.config.profit.tracker.state.crimsonPlasmaLevel, 1_700, true);

    private final List<Block> blocks;
    private final String bagItemName;
    private final long baseValue;
    private final IntSupplier levelSupplier;
    private final long perLevel;
    private final boolean refineryBased;

    TrackedOre(List<Block> blocks, String bagItemName, long baseValue, IntSupplier levelSupplier, long perLevel, boolean refineryBased) {
        this.blocks = blocks;
        this.bagItemName = bagItemName;
        this.baseValue = baseValue;
        this.levelSupplier = levelSupplier;
        this.perLevel = perLevel;
        this.refineryBased = refineryBased;
    }

    public long getDropValue() {
        long total;

        if (refineryBased) {
            int refineryLevel = levelSupplier.getAsInt();
            if (refineryLevel < 0) return -1;

            long refineryBonus = perLevel * refineryLevel;
            int ramLevel = ConfigManager.config.profit.noDuneRamPet
                    ? 0
                    : Math.max(ConfigManager.config.profit.tracker.state.duneRamLevel, 0);
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

    public String getBagItemName() {
        return bagItemName;
    }

    public static TrackedOre fromBlock(Block block) {
        for (TrackedOre ore : values()) {
            if (ore.blocks.contains(block)) return ore;
        }
        return null;
    }

    public static TrackedOre fromBagItemName(String name) {
        for (TrackedOre ore : values()) {
            if (ore.bagItemName.equalsIgnoreCase(name)) return ore;
        }
        return null;
    }
}