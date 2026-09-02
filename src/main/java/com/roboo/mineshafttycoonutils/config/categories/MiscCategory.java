package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.glfw.GLFW;

public class MiscCategory {

    @Expose
    @ConfigOption(
            name = "Toggle Attack Keybind",
            desc = "Press to flip Minecraft's §e`Toggle Attack' §foption on/off \n" +
                    "§eThis is just the setting state, not actually clicking"
    )
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int toggleAttackKeybind = GLFW.GLFW_KEY_UNKNOWN;

    @Expose
    @ConfigOption(
            name = "Toggle Use Keybind",
            desc = "Press to flip Minecraft's §e'Toggle Use' §foption on/off \n" +
                    "§eThis is just the setting state, not actually clicking"
    )
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int toggleUseKeybind = GLFW.GLFW_KEY_UNKNOWN;

    @Expose
    @ConfigOption(
            name = "Night Vision Blocker",
            desc = "Removes the Night Vision effect")
    @ConfigEditorBoolean
    public boolean nightVisionBlocker = false;

    @Expose
    @ConfigOption(
            name = "Force Tab List Sort",
            desc = "Reorders the tab list when its broken. §eNote: Will only reorder visible players, " +
                    "those not on tab list will still be hidden")
    @ConfigEditorBoolean
    public boolean forceTabListSort = false;

}