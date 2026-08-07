package com.roboo.mineshafttycoonutils.config.timers;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class TimersHudPositionConfig {

    @Expose
    @ConfigOption(name = "Timers HUD X Position", desc = "Horizontal position of the timers HUD")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1920f, minStep = 1f)
    public int timersHudX = 70;

    @Expose
    @ConfigOption(name = "Timers HUD Y Position", desc = "Vertical position of the timers HUD")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1080f, minStep = 1f)
    public int timersHudY = 20;
}