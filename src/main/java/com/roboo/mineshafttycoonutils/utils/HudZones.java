package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.core.BlockPos;

import java.util.List;

public class HudZones {

    public record Zone(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public static Zone of(int x1, int y1, int z1, int x2, int y2, int z2) {
            return new Zone(
                    Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                    Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
            );
        }

        public boolean contains(int x, int y, int z) {
            return x >= minX && x <= maxX
                    && y >= minY && y <= maxY
                    && z >= minZ && z <= maxZ;
        }
    }

    private static final List<Zone> FISHING_ZONES = List.of(
            Zone.of(25, 80, -109, 52, 97, -57),
            Zone.of(52, 255, -20, 124, 203, -109),
            Zone.of(69, 108, -84, 94, 77, -109),
            Zone.of(68, 98, -40, 94, 77, -83)
    );

    private static final List<Zone> PVE_ZONES = List.of(
            Zone.of(-21, 204, 102, -61, 240, 136)
    );

    private static final List<Zone> LAVA_FISHING_ZONES = List.of(
            Zone.of(-108, 100, 38, -122, 140, 80)
    );

    public static final Zone CRATE_ZONE = Zone.of(67, 99, -37, 33, 79, 21);
    public static final Zone TRADE_ZONE = Zone.of(95, 245, 72, 77, 232, 120);
    public static final Zone T5_TRADE_ZONE = Zone.of(56, 153, -3, 117, 134, -63);
    public static final Zone PV_ZONE = Zone.of(
            Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE,
            Integer.MAX_VALUE, 9, Integer.MAX_VALUE);

    public static boolean isInFishingZone(BlockPos pos) {
        return isInZone(FISHING_ZONES, pos);
    }

    public static boolean isInPveZone(BlockPos pos) {
        return isInZone(PVE_ZONES, pos);
    }

    public static boolean isInLavaFishingZone(BlockPos pos) {
        return isInZone(LAVA_FISHING_ZONES, pos);
    }

    public static boolean isInCrateZone(BlockPos pos) {
        return CRATE_ZONE.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isInTradeZone(BlockPos pos) {
        return TRADE_ZONE.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isInT5TradeZone(BlockPos pos) {
        return T5_TRADE_ZONE.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isInPvZone(BlockPos pos) {
        return PV_ZONE.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isInEggZone(BlockPos pos) {
        return isInCrateZone(pos) || isInTradeZone(pos) || isInT5TradeZone(pos) || isInPvZone(pos);
    }

    private static boolean isInZone(List<Zone> zones, BlockPos pos) {
        for (Zone zone : zones) {
            if (zone.contains(pos.getX(), pos.getY(), pos.getZ())) return true;
        }
        return false;
    }
}