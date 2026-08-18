package com.roboo.mineshafttycoonutils.config.profit;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ProfitTrackerConfig {

    @Expose
    @ConfigOption(name = "Profit Tracker", desc = "Track coins/hour and ores")
    @ConfigEditorBoolean
    public boolean profitTrackerEnabled = true;

    @Expose
    @ConfigOption(name = "Title Color", desc = "Color used for the Profit Tracker HUD titles")
    @ConfigEditorColour
    public ChromaColour titleColor = ChromaColour.fromStaticRGB(255, 255, 85, 255);

    @Expose
    @ConfigOption(name = "Hide in Ocean", desc = "Hide the profit tracker HUD while inside a designated fishing area")
    @ConfigEditorBoolean
    public boolean onlyShowWhenMining = true;

    @Expose
    @ConfigOption(name = "Show Ore Drops", desc = "Show a breakdown of rare ore drops obtained (Lunar Fragment, Basalt Shard, etc.) on the profit HUD")
    @ConfigEditorBoolean
    public boolean showOreDrops = true;

    @Expose
    @ConfigOption(name = "Disable Right-Align Flip", desc = "Keep this HUD's text left-aligned even when positioned past the middle of the screen, instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = false;

    @Expose
    public int profitHudX = 10;

    @Expose
    public int profitHudY = 70;

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

        @Expose public int duneRamLevel = -1;
    }
}