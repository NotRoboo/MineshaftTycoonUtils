package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.hud.HudEditScreen;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GeneralCategory {

    @ConfigOption(
            name = "Edit HUD Locations",
            desc = "Opens the Position Editor, allows changing the position of the mod's HUD overlays."
    )
    @ConfigEditorButton(buttonText = "Edit")
    public Runnable editHudPositions = HudEditScreen::open;

    @Expose
    @ConfigOption(
            name = "Check for Updates",
            desc = "Automatically check for new releases on GitHub each time you join a world or server")
    @ConfigEditorBoolean
    public boolean checkForUpdates = true;

    @Expose
    @ConfigOption(
            name = "Warp Helper",
            desc = "Use at own risk - Typing /warp <name> opens the warp menu and automatically clicks the matching warp")
    @ConfigEditorBoolean
    public boolean warpHelperEnabled = false;

    @Expose
    @ConfigOption(
            name = "Pets Helper",
            desc = "Use at own risk - Typing /pets <name> opens the pets menu and automatically clicks the matching pet")
    @ConfigEditorBoolean
    public boolean petsHelperEnabled = false;
}