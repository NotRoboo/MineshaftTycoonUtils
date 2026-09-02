package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.profit.BagValueConfig;
import com.roboo.mineshafttycoonutils.config.profit.MagmaValueConfig;
import com.roboo.mineshafttycoonutils.config.profit.ProfitTrackerConfig;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ProfitCategory {

    @Expose
    @ConfigOption(name = "Shorten Numbers", desc = "Abbreviate large profit numbers, e.g. 125B instead of 125,000,000,000")
    @ConfigEditorBoolean
    public boolean shortenNumbers = true;

    @Expose
    @ConfigOption(name = "Cash Register Boost", desc = "Apply Cash Register's +3% coin boost to profit calculations")
    @ConfigEditorBoolean
    public boolean cashRegisterEnabled = false;

    @Expose
    @Category(name = "Profit Tracker", desc = "Coins/hour and ore drop tracking settings")
    public ProfitTrackerConfig tracker = new ProfitTrackerConfig();

    @Expose
    @Category(name = "Bag Value", desc = "Shows the total value of items inside the /bag")
    public BagValueConfig bagValue = new BagValueConfig();

    @Expose
    @Category(name = "Magma Value", desc = "Shows the total magma value of ores inside the Space Ores Bag")
    public MagmaValueConfig magma = new MagmaValueConfig();
}