package com.roboo.mineshafttycoonutils.config.fishing;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrateOrderConfig {

    @Expose
    @ConfigOption(name = "Breakdown", desc = "Show the per-crate breakdown list for crates on the HUD")
    @ConfigEditorBoolean
    public boolean breakdownEnabled = true;

    @Expose
    @ConfigOption(name = "Order", desc = "Drag to reorder crates shown on the HUD. Remove an entry to hide it; it will still be tracked.")
    @ConfigEditorDraggableList
    public List<Entry> order = new ArrayList<>(Arrays.asList(Entry.values()));

    public enum Entry {
        COMMON("Common Crate"),
        UNCOMMON("Uncommon Crate"),
        RARE("Rare Crate"),
        EPIC("Epic Crate"),
        LEGENDARY("Legendary Crate"),
        MYTHIC("Mythic Crate"),
        MYTHICAL("Mythical Crate"),
        GOLDEN("Golden Crate"),
        ABYSSAL("Abyssal Crate"),
        PET("Pet Crate");

        private final String displayName;

        Entry(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}