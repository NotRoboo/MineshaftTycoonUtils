package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.profit.BagValueConfig;
import com.roboo.mineshafttycoonutils.config.profit.ProfitTrackerConfig;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ProfitCategory {

    @Expose
    @ConfigOption(name = "Shorten Numbers", desc = "Abbreviate large profit numbers, e.g. 489.39T instead of 489,390,000,000,000")
    @ConfigEditorBoolean
    public boolean shortenNumbers = false;

    @Expose
    @ConfigOption(name = "Cash Register Boost", desc = "Apply Cash Register's +3% coin boost to profit calculations")
    @ConfigEditorBoolean
    public boolean cashRegisterEnabled = false;

    @Expose
    @ConfigOption(name = "No Dune Ram Pet", desc = "Turn on if you don't own the Dune Ram pet, so profit calculations stop asking you to open /pets to detect its level")
    @ConfigEditorBoolean
    public boolean noDuneRamPet = false;

    @Expose
    @Category(name = "Profit Tracker", desc = "Coins/hour and ore drop tracking settings")
    public ProfitTrackerConfig tracker = new ProfitTrackerConfig();

    @Expose
    @Category(name = "Bag Value", desc = "Shows the total value of items inside the /bag")
    public BagValueConfig bagValue = new BagValueConfig();
}