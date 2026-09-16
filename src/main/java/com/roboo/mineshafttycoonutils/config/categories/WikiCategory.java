package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.glfw.GLFW;

public class WikiCategory {

    @ConfigOption(
            name = "Wiki Exporter",
            desc = """
                    §7While inside any container GUI, press the keybind below to save that container's \
                    contents to a text file for easy wiki template creation.
                    §7Files are saved to §e.minecraft/config/mineshafttycoonutils/wiki/<Container Name>.txt
                    """
    )
    @ConfigEditorInfoText
    @SuppressWarnings("unused")
    public boolean wikiExporterPreview = false;

    @Expose
    @ConfigOption(
            name = "Export Container",
            desc = "Press while inside a container GUI to save its contents for the wiki")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int exportKeybind = GLFW.GLFW_KEY_UNKNOWN;

    @Expose
    @ConfigOption(
            name = "Export Item",
            desc = "Press while hovering over an item to save its contents for the wiki")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    public int exportItemKeybind = GLFW.GLFW_KEY_UNKNOWN;
}
