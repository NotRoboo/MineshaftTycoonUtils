package com.roboo.mineshafttycoonutils.config.fishing;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HudPositionConfig {

    @Expose
    @ConfigOption(name = "HUD X Position", desc = "Horizontal position of the fishing HUD")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1920f, minStep = 1f)
    public int hudX = 10;

    @Expose
    @ConfigOption(name = "HUD Y Position", desc = "Vertical position of the fishing HUD")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1080f, minStep = 1f)
    public int hudY = 50;
}