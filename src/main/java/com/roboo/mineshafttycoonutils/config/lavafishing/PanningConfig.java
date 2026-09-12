package com.roboo.mineshafttycoonutils.config.lavafishing;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PanningConfig {

    @Expose
    @ConfigOption(name = "Panning Helper", desc = "Helps you pan without sound on")
    @ConfigEditorBoolean
    public boolean panningHelperEnabled = false;
}