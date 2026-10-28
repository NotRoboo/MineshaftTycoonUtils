package com.roboo.mineshafttycoonutils.config.misc;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashSet;
import java.util.Set;

public class SlotLockingConfig {

    @Expose
    @ConfigOption(
            name = "Slot Locking",
            desc = "Lock inventory slots so their items can't be moved")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(
            name = "Lock Keybind",
            desc = "Press while hovering a slot inside an inventory to lock or unlock it")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_L)
    public int lockKeybind = GLFW.GLFW_KEY_L;

    @Expose
    @ConfigOption(
            name = "Icon Type",
            desc = "Change how slots are marked while an inventory is open")
    @ConfigEditorDropdown
    public IconType iconType = IconType.ICON;

    @Expose
    public Set<Integer> lockedSlots = new LinkedHashSet<>();

    public enum IconType {
        NONE("None"),
        ICON("Icon"),
        SLOT("Slot");

        private final String displayName;

        IconType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
