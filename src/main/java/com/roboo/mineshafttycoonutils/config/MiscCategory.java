package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.glfw.GLFW;

public class MiscCategory {

    @Expose
    @ConfigOption(name = "Toggle Attack Keybind", desc = "Press to flip Minecraft's 'Toggle Attack' option on/off")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int toggleAttackKeybind = GLFW.GLFW_KEY_UNKNOWN;

    @Expose
    @ConfigOption(name = "Toggle Use Keybind", desc = "Press to flip Minecraft's 'Toggle Use' option on/off")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int toggleUseKeybind = GLFW.GLFW_KEY_UNKNOWN;

    @Expose
    @ConfigOption(name = "Night Vision Blocker", desc = "Automatically removes the Night Vision effect the moment it's applied")
    @ConfigEditorBoolean
    public boolean nightVisionBlockerEnabled = false;

    @Expose
    @ConfigOption(name = "Force Tab List Sort", desc = "Reorders the tab list: staff rank, then tier, then Hypixel rank, then A-Z")
    @ConfigEditorBoolean
    public boolean forceTabListSort = false;

}