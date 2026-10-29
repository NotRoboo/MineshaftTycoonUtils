package com.roboo.mineshafttycoonutils;

import com.roboo.mineshafttycoonutils.config.*;
import com.roboo.mineshafttycoonutils.features.actionbar.*;
import com.roboo.mineshafttycoonutils.features.chat.*;
import com.roboo.mineshafttycoonutils.features.fishing.*;
import com.roboo.mineshafttycoonutils.features.informationhud.*;
import com.roboo.mineshafttycoonutils.features.misc.*;
import com.roboo.mineshafttycoonutils.features.pets.*;
import com.roboo.mineshafttycoonutils.features.plasma.*;
import com.roboo.mineshafttycoonutils.features.profit.*;
import com.roboo.mineshafttycoonutils.features.pve.*;
import com.roboo.mineshafttycoonutils.features.scoreboard.*;
import com.roboo.mineshafttycoonutils.features.timers.*;
import com.roboo.mineshafttycoonutils.features.update.*;
import com.roboo.mineshafttycoonutils.features.warp.*;
import com.roboo.mineshafttycoonutils.features.wiki.*;
import com.roboo.mineshafttycoonutils.utils.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class MineshaftTycoonUtils implements ClientModInitializer {

    public static ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        LogNoiseFilter.install();
        configManager = new ConfigManager();
        configManager.firstLoad();

        HousingVerifier.init();
        FishingTracker.init();
        PvETracker.init();
        OreDropTracker.init();
        PetAdTracker.init();
        CustomScoreboardManager.init();
        ScoreboardHud.init();
        ActionBarDebugUtils.init();
        MessageHider.init();
        PlayerMessageHandler.init();
        ProfitTracker.init();
        ActionBarFormatter.init();

        CommandAliases.init();
        FishingHud.init();
        PvEHud.init();
        InformationHud.init();
        PetsHelper.init();
        ProfitHud.init();
        RefineryPetTracker.init();
        BagValueTracker.init();
        CookieBlocker.init();
        BagHud.init();
        MagmaValueTracker.init();
        MagmaHud.init();
        WarpHelper.init();
        BuffTracker.init();
        CompactorTracker.init();
        GreenhouseTracker.init();
        TimersHud.init();
        PlasmaSmitheryTracker.init();
        PlasmaSmitheryHud.init();
        FlareValueTracker.init();
        FlareValueHud.init();
        SlotLocking.init();
        SolarFlareTracker.init();
        SolarFlareHud.init();
        HotkeyUtils.init();
        NightVisionBlocker.init();
        PotionToggleHelper.init();
        PotDurTracker.init();
        UpdateChecker.init();
        WikiExporter.init();
        WorldEditCommands.init();
        ConfigGuiManager.init();
        Commands.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configManager.saveConfig());
    }
}