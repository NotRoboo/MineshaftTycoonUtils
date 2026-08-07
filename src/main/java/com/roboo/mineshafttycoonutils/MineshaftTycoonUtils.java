package com.roboo.mineshafttycoonutils;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.chat.MessageHider;
import com.roboo.mineshafttycoonutils.features.fishing.FishingHud;
import com.roboo.mineshafttycoonutils.features.fishing.FishingTracker;
import com.roboo.mineshafttycoonutils.features.pets.PetsHelper;
import com.roboo.mineshafttycoonutils.features.profit.OreDropTracker;
import com.roboo.mineshafttycoonutils.features.profit.ProfitHud;
import com.roboo.mineshafttycoonutils.features.profit.ProfitTracker;
import com.roboo.mineshafttycoonutils.features.profit.RefineryPetTracker;
import com.roboo.mineshafttycoonutils.features.warp.WarpHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class MineshaftTycoonUtils implements ClientModInitializer {

    public static ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        configManager = new ConfigManager();
        configManager.firstLoad();

        FishingTracker.init();
        FishingHud.init();
        MessageHider.init();
        OreDropTracker.init();
        PetsHelper.init();
        ProfitHud.init();
        ProfitTracker.init();
        RefineryPetTracker.init();
        WarpHelper.init();
        Commands.init();


        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configManager.saveConfig());
    }
}