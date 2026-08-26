package com.roboo.mineshafttycoonutils;

import com.roboo.mineshafttycoonutils.config.ConfigGuiManager;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.actionbar.ActionBarFormatter;
import com.roboo.mineshafttycoonutils.features.chat.MessageHider;
import com.roboo.mineshafttycoonutils.features.chat.PlayerMessageHandler;
import com.roboo.mineshafttycoonutils.features.fishing.FishingHud;
import com.roboo.mineshafttycoonutils.features.fishing.FishingTracker;
import com.roboo.mineshafttycoonutils.features.informationhud.InformationHud;
import com.roboo.mineshafttycoonutils.features.misc.NightVisionBlocker;
import com.roboo.mineshafttycoonutils.features.pets.PetsHelper;
import com.roboo.mineshafttycoonutils.features.profit.BagHud;
import com.roboo.mineshafttycoonutils.features.profit.BagValueTracker;
import com.roboo.mineshafttycoonutils.features.profit.MagmaHud;
import com.roboo.mineshafttycoonutils.features.profit.MagmaValueTracker;
import com.roboo.mineshafttycoonutils.features.profit.OreDropTracker;
import com.roboo.mineshafttycoonutils.features.profit.ProfitHud;
import com.roboo.mineshafttycoonutils.features.profit.ProfitTracker;
import com.roboo.mineshafttycoonutils.features.profit.RefineryPetTracker;
import com.roboo.mineshafttycoonutils.features.scoreboard.CustomScoreboardManager;
import com.roboo.mineshafttycoonutils.features.scoreboard.ScoreboardHud;
import com.roboo.mineshafttycoonutils.features.timers.BuffTracker;
import com.roboo.mineshafttycoonutils.features.timers.GreenhouseTracker;
import com.roboo.mineshafttycoonutils.features.timers.IlsRestockTracker;
import com.roboo.mineshafttycoonutils.features.timers.PetAdTracker;
import com.roboo.mineshafttycoonutils.features.timers.TimersHud;
import com.roboo.mineshafttycoonutils.features.update.UpdateChecker;
import com.roboo.mineshafttycoonutils.features.warp.WarpHelper;
import com.roboo.mineshafttycoonutils.utils.ActionBarDebugUtils;
import com.roboo.mineshafttycoonutils.utils.MinecraftOptionUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class MineshaftTycoonUtils implements ClientModInitializer {

    public static ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        configManager = new ConfigManager();
        configManager.firstLoad();

        FishingTracker.init();
        OreDropTracker.init();
        PetAdTracker.init();
        CustomScoreboardManager.init();
        ScoreboardHud.init();
        ActionBarDebugUtils.init();
        MessageHider.init();
        PlayerMessageHandler.init();
        ActionBarFormatter.init();

        FishingHud.init();
        InformationHud.init();
        PetsHelper.init();
        ProfitHud.init();
        ProfitTracker.init();
        RefineryPetTracker.init();
        BagValueTracker.init();
        BagHud.init();
        MagmaValueTracker.init();
        MagmaHud.init();
        WarpHelper.init();
        BuffTracker.init();
        IlsRestockTracker.init();
        GreenhouseTracker.init();
        TimersHud.init();
        MinecraftOptionUtils.init();
        NightVisionBlocker.init();
        UpdateChecker.init();
        ConfigGuiManager.init();
        Commands.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configManager.saveConfig());
    }
}