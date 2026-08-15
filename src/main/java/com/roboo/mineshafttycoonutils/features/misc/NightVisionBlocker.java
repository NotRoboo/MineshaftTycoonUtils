package com.roboo.mineshafttycoonutils.features.misc;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

public class NightVisionBlocker {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (!ConfigManager.config.misc.nightVisionBlockerEnabled) return;
        if (mc.player == null) return;

        if (mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}