package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.hud.HudEditScreen;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.glfw.GLFW;

public class GeneralCategory {

    @ConfigOption(
            name = "Edit HUD Locations",
            desc = "Opens the Position Editor, allows changing the position of the mod's HUD overlays."
    )
    @ConfigEditorButton(buttonText = "Edit")
    public Runnable editHudPositions = HudEditScreen::open;

    @Expose
    @ConfigOption(
            name = "Edit Container HUD Locations Keybind",
            desc = "Press while inside the Bag menu to toggle dragging HUD position")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int editBagHudKeybind = GLFW.GLFW_KEY_UNKNOWN;

    @Expose
    @ConfigOption(
            name = "Check for Updates",
            desc = "Automatically check for new releases on GitHub each time you join")
    @ConfigEditorBoolean
    public boolean checkForUpdates = true;

    @Expose
    @ConfigOption(
            name = "Warp Helper",
            desc = "Typing /warp <name> opens the warp menu and automatically clicks the matching warp - §cUse at own risk")
    @ConfigEditorBoolean
    public boolean warpHelperEnabled = false;

    @Expose
    @ConfigOption(
            name = "Pets Helper",
            desc = "Typing /pets <name> opens the pets menu and automatically clicks the matching pet - §cUse at own risk")
    @ConfigEditorBoolean
    public boolean petsHelperEnabled = false;

    @Expose
    @ConfigOption(
            name = "Potsoff Helper",
            desc = "Typing /potsoff <type> opens the buff menu and automatically toggles the matching potion/buff - §cUse at own risk")
    @ConfigEditorBoolean
    public boolean potsoffCommand = false;
}