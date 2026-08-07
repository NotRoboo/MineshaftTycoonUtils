package com.roboo.mineshafttycoonutils.config.profit;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ProfitHudPositionConfig {

    @Expose
    @ConfigOption(name = "Profit HUD X Position", desc = "Horizontal position of the profit tracker HUD")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1920f, minStep = 1f)
    public int ProfitHudX = 10;

    @Expose
    @ConfigOption(name = "Profit HUD Y Position", desc = "Vertical position of the profit tracker HUD")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1080f, minStep = 1f)
    public int ProfitHudY = 50;
}