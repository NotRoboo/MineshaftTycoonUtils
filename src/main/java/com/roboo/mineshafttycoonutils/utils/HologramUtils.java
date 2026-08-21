package com.roboo.mineshafttycoonutils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class HologramUtils {

    private HologramUtils() {}

    public static List<String> findNearbyHologramLines(double x, double y, double z, double radius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return List.of();

        AABB box = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        List<String> lines = new ArrayList<>();

        for (ArmorStand stand : mc.level.getEntitiesOfClass(ArmorStand.class, box,
                s -> s.isInvisible() && s.hasCustomName())) {
            lines.add(cleanName(stand));
        }

        return lines;
    }

    private static String cleanName(ArmorStand stand) {
        var custom = stand.getCustomName();
        if (custom == null) return "";
        return custom.getString().replaceAll("§[0-9a-fk-or]", "").trim();
    }
}