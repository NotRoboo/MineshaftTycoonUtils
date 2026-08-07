package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.core.BlockPos;

import java.util.List;

public class FishingZones {

    private record Zone(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        static Zone of(int x1, int y1, int z1, int x2, int y2, int z2) {
            return new Zone(
                    Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                    Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
            );
        }

        boolean contains(int x, int y, int z) {
            return x >= minX && x <= maxX
                    && y >= minY && y <= maxY
                    && z >= minZ && z <= maxZ;
        }
    }

    private static final List<Zone> ZONES = List.of(
            Zone.of(25, 80, -109, 52, 97, -57),
            Zone.of(52, 255, -20, 124, 203, -109),
            Zone.of(69, 108, -84, 94, 77, -109),
            Zone.of(68, 98, -40, 94, 77, -83)
    );

    public static boolean isInZone(BlockPos pos) {
        for (Zone zone : ZONES) {
            if (zone.contains(pos.getX(), pos.getY(), pos.getZ())) return true;
        }
        return false;
    }
}