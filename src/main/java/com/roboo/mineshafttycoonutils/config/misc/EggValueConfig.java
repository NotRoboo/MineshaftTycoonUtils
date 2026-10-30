package com.roboo.mineshafttycoonutils.config.misc;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EggValueConfig {

    @Expose
    @ConfigOption(name = "Egg Value HUD", desc = "Show the egg value HUD " +
            "§eUse your `Edit Container HUD Locations Keybind` to edit location while a container is open")
    @ConfigEditorBoolean
    public boolean hudEnabled = true;

    @Expose
    @ConfigOption(name = "Title Color", desc = "Color used for the Egg Value HUD title")
    @ConfigEditorColour
    public ChromaColour titleColor = ChromaColour.fromStaticRGB(255, 255, 85, 255);

    @Expose
    public int eggHudX = 10;

    @Expose
    public int eggHudY = 80;

    @Expose
    public float eggHudScale = HudScale.DEFAULT;
}