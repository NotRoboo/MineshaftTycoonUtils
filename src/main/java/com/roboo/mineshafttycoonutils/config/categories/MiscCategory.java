package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.migration.ConfigJsonUtils;
import com.roboo.mineshafttycoonutils.config.misc.KeybindsConfig;
import com.roboo.mineshafttycoonutils.config.misc.SlotLockingConfig;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MiscCategory {

    @Expose
    @Category(name = "Keybinds", desc = "Keyboard shortcuts for toggles and commands")
    public KeybindsConfig keybinds = new KeybindsConfig();

    @Expose
    @Category(name = "Slot Locking", desc = "Lock inventory slots")
    public SlotLockingConfig slotLocking = new SlotLockingConfig();

    @Expose
    @ConfigOption(
            name = "Cookie Blocker",
            desc = "Blocks right clicking while holding cookies unless they are in hotbar slot 5")
    @ConfigEditorBoolean
    public boolean cookieBlocker = true;

    @Expose
    @ConfigOption(
            name = "Night Vision Blocker",
            desc = "Removes the Night Vision effect")
    @ConfigEditorBoolean
    public boolean nightVisionBlocker = false;

    @Expose
    @ConfigOption(
            name = "Force Tab List Sort",
            desc = "Reorders the tab list when its broken.")
    @ConfigEditorBoolean
    public boolean forceTabListSort = false;

    @Expose
    @ConfigOption(
            name = "Copy Chat Messages",
            desc = "Copy a chat message to your clipboard while the chat is open. " +
                    "Formatting codes are removed and emotes are copied as their :shortcode:")
    @ConfigEditorBoolean
    public boolean copyChatMessages = false;

    @Expose
    @ConfigOption(
            name = "Copy Method",
            desc = "How to copy a hovered chat message. §eCtrl is Cmd on macOS")
    @ConfigEditorDropdown
    public CopyMethod copyMethod = CopyMethod.CTRL_CLICK;

    @Expose
    @ConfigOption(
            name = "WorldEdit Style Cmds",
            desc = "Adds /thru, /top, /jumpto, /ceil, /ascend and /descend, which teleport you using /tp. " +
                    "§eWhen disabled these are sent to the server unchanged - §cRequires /tp permission")
    @ConfigEditorBoolean
    public boolean worldEditCommands = false;

    public enum CopyMethod {
        CTRL_CLICK("Ctrl + Click", 0, true),
        CTRL_RIGHT_CLICK("Ctrl + Right Click", 1, true),
        SHIFT_CLICK("Shift + Click", 0, false),
        SHIFT_RIGHT_CLICK("Shift + Right Click", 1, false);

        private final String displayName;
        private final int button;
        private final boolean control;

        CopyMethod(String displayName, int button, boolean control) {
            this.displayName = displayName;
            this.button = button;
            this.control = control;
        }

        public int button() {
            return button;
        }

        public boolean usesControl() {
            return control;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static void onConfigFix(JsonObject savedConfig) {
        if (!savedConfig.has("misc") || !savedConfig.get("misc").isJsonObject()) return;

        JsonObject misc = savedConfig.getAsJsonObject("misc");
        if (!misc.has("keybinds") || !misc.get("keybinds").isJsonObject()) {
            misc.add("keybinds", new JsonObject());
        }

        ConfigJsonUtils.moveSetting(savedConfig, "misc.toggleAttackKeybind", "misc.keybinds.toggleAttackKeybind");
        ConfigJsonUtils.moveSetting(savedConfig, "misc.toggleUseKeybind", "misc.keybinds.toggleUseKeybind");
    }
}