package com.roboo.mineshafttycoonutils.features.misc;

import com.google.gson.JsonObject;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.migration.ConfigJsonUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

public class NightVisionBlocker {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (!ConfigManager.config.misc.nightVisionBlocker) return;
        if (mc.player == null) return;

        if (mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public static void onConfigFix(JsonObject savedConfig) {
        ConfigJsonUtils.renameSetting(savedConfig, "misc.nightVisionBlockerEnabled", "nightVisionBlocker");
    }
}