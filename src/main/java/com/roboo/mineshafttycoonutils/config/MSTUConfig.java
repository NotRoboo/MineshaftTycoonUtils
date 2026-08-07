package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.profit.ProfitCategory;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;

public class MSTUConfig extends Config {

    @Override
    public StructuredText getTitle() {
        return StructuredText.of("MineshaftTycoon Utils");
    }

    @Expose
    @Category(name = "General", desc = "General settings")
    public GeneralCategory general = new GeneralCategory();

    @Expose
    @Category(name = "Profit Tracker", desc = "Refinery profit tracking settings")
    public ProfitCategory profit = new ProfitCategory();

    @Expose
    @Category(name = "Fishing", desc = "Fishing tracker and HUD settings")
    public FishingCategory fishing = new FishingCategory();

    @Expose
    @Category(name = "Chat", desc = "Chat message filtering settings")
    public ChatCategory chat = new ChatCategory();
}