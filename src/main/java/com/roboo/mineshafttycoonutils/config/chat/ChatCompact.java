package com.roboo.mineshafttycoonutils.config.chat;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ChatCompact {

    @Expose
    @ConfigOption(name = "Compact Messages", desc = "Merge identical chat messages into one line with a counter, e.g. * Hello! (3)")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Compact Time", desc = "How long between identical to keep compact §eDefault: 30")
    @ConfigEditorSlider(minValue = 5, maxValue = 300, minStep = 5)
    public int windowSeconds = 30;
}