package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GeneralCategory {

    @Expose
    @ConfigOption(name = "Warp Helper", desc = "Use at own risk - Typing /warp <name> opens the warp menu and automatically clicks the matching warp")
    @ConfigEditorBoolean
    public boolean warpHelperEnabled = false;

    @Expose
    @ConfigOption(name = "Pets Helper", desc = "Use at own risk - Typing /pets <name> opens the pets menu and automatically clicks the matching pet")
    @ConfigEditorBoolean
    public boolean petsHelperEnabled = false;
}