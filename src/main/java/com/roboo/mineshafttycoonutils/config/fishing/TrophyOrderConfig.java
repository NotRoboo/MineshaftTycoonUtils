package com.roboo.mineshafttycoonutils.config.fishing;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrophyOrderConfig {

    @Expose
    @ConfigOption(name = "Breakdown", desc = "Show the per-item breakdown list for trophy fish on the HUD")
    @ConfigEditorBoolean
    public boolean breakdownEnabled = true;

    @Expose
    @ConfigOption(
            name = "Order",
            desc = "Drag to reorder trophy fish shown on the HUD. Remove an entry to hide it; it will still be tracked.")
    @ConfigEditorDraggableList
    public List<Entry> order = new ArrayList<>(Arrays.asList(Entry.values()));

    public enum Entry {
        SMALL_FISH("Small Fish"),
        CLOWN_FISH("Clown Fish"),
        PUFFER_FISH("Puffer Fish"),
        ROYAL_FISH("Royal Fish");

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