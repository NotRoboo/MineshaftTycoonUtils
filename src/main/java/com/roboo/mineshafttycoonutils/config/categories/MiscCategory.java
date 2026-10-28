package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.migration.ConfigJsonUtils;
import com.roboo.mineshafttycoonutils.config.misc.KeybindsConfig;
import com.roboo.mineshafttycoonutils.config.misc.SlotLockingConfig;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
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
            desc = "Reorders the tab list when its broken. §eNote: Will only reorder visible players, " +
                    "those not on tab list will still be hidden")
    @ConfigEditorBoolean
    public boolean forceTabListSort = false;

    @Expose
    @ConfigOption(
            name = "WorldEdit Style Cmds",
            desc = "Adds /thru, /top, /jumpto, /ceil, /ascend and /descend, which teleport you using /tp. " +
                    "§eWhen disabled these are sent to the server unchanged - §cRequires /tp permission")
    @ConfigEditorBoolean
    public boolean worldEditCommands = false;

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
