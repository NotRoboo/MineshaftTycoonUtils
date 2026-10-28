package com.roboo.mineshafttycoonutils.config.misc;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.glfw.GLFW;

public class KeybindsConfig {

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
            name = "Visibility 0",
            desc = "Press to run §e/visibility 0")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int visibilityZeroKeybind = GLFW.GLFW_KEY_UNKNOWN;

    @Expose
    @ConfigOption(
            name = "Visibility 1000",
            desc = "Press to run §e/visibility 1000")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int visibilityThousandKeybind = GLFW.GLFW_KEY_UNKNOWN;
}
