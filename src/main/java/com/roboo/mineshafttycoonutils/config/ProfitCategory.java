package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ProfitCategory {

    @Expose
    @ConfigOption(name = "Profit Tracker", desc = "Track coins/hour and ores")
    @ConfigEditorBoolean
    public boolean profitTrackerEnabled = true;

    @Expose
    @ConfigOption(name = "Hide in Ocean", desc = "Hide the profit tracker HUD while inside a designated fishing area")
    @ConfigEditorBoolean
    public boolean onlyShowWhenMining = true;

    @Expose
    @ConfigOption(name = "Show Ore Drops", desc = "Show a breakdown of rare ore drops obtained (Lunar Fragment, Basalt Shard, etc.) on the profit HUD")
    @ConfigEditorBoolean
    public boolean showOreDrops = true;

    @Expose
    @ConfigOption(name = "Cash Register Boost", desc = "Apply Cash Register's +3% coin boost to profit calculations")
    @ConfigEditorBoolean
    public boolean cashRegisterEnabled = false;

    @Expose
    @ConfigOption(name = "Shorten Numbers", desc = "Abbreviate large profit numbers, e.g. 489.39T instead of 489,390,000,000,000")
    @ConfigEditorBoolean
    public boolean shortenNumbers = false;

    @Expose
    @ConfigOption(name = "No Dune Ram Pet", desc = "Turn on if you don't own the Dune Ram pet, so the profit tracker stops asking you to open /pets to detect its level")
    @ConfigEditorBoolean
    public boolean noDuneRamPet = false;

    @Expose
    @ConfigOption(name = "Disable Right-Align Flip", desc = "Keep this HUD's text left-aligned even when positioned past the middle of the screen, instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = false;

    @Expose
    public int profitHudX = 10;

    @Expose
    public int profitHudY = 70;

    // Auto-detected by opening the Refinery
    @Expose
    public RefineryState state = new RefineryState();

    public static class RefineryState {
        @Expose public int basaltLevel = 0;
        @Expose public int breccaLevel = 0;
        @Expose public int regolithLevel = 0;
        @Expose public int amberRockLevel = 0;
        @Expose public int amberCrystalLevel = 0;
        @Expose public int cosmicFiberLevel = 0;
        @Expose public int crimsonPlasmaLevel = 0;

        // -1 = never detected. 0+ once the player has opened /pets
        @Expose public int duneRamLevel = -1;
    }
}