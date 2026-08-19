package com.roboo.mineshafttycoonutils.config.profit;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BagValueConfig {

    @Expose
    @ConfigOption(name = "Show Bag Value HUD", desc = "Show the bag value HUD overlay while inside the /bag menu " +
            "Use `Edit Container HUD Locations Keybind` to edit location")
    @ConfigEditorBoolean
    public boolean hudEnabled = true;

    @Expose
    @ConfigOption(name = "Title Color", desc = "Color used for the Bag Value HUD title and total")
    @ConfigEditorColour
    public ChromaColour titleColor = ChromaColour.fromStaticRGB(255, 255, 85, 255);

    @Expose
    @ConfigOption(
            name = "Disable Right-Align Flip",
            desc = "Keep this HUD's text left-aligned even when positioned past the middle of the screen, " +
                    "instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = false;

    @Expose
    public int bagValueHudX = 200;

    @Expose
    public int bagValueHudY = 85;
}