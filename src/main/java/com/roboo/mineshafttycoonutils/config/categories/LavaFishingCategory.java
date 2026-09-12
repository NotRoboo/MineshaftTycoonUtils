package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.lavafishing.PanningConfig;
import io.github.notenoughupdates.moulconfig.annotations.Category;

public class LavaFishingCategory {

    @Expose
    @Category(name = "Panning", desc = "Settings related to panning")
    public PanningConfig panning = new PanningConfig();
}