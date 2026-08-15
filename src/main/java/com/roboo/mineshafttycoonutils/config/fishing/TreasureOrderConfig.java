package com.roboo.mineshafttycoonutils.config.fishing;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreasureOrderConfig {

    @Expose
    @ConfigOption(name = "Breakdown", desc = "Show the per-item breakdown list for treasure and plate drops on the HUD")
    @ConfigEditorBoolean
    public boolean breakdownEnabled = true;

    @Expose
    @ConfigOption(
            name = "Order",
            desc = "Drag to reorder treasure and plate drops shown on the HUD. " +
                    "Remove an entry to hide it; it will still be tracked.")
    @ConfigEditorDraggableList
    public List<Entry> order = new ArrayList<>(Arrays.asList(Entry.values()));

    @Expose
    @ConfigOption(
            name = "Show Fortune Frags Total",
            desc = "Show the total Fortune Fragments obtained next to the catch count, e.g. Fortune Frags: 4 (495)")
    @ConfigEditorBoolean
    public boolean showFortuneFragmentsTotal = true;

    @Expose
    @ConfigOption(
            name = "Show Plate Drops Total",
            desc = "Show the total plates obtained next to the catch count, e.g. Coal: 5 (7)")
    @ConfigEditorBoolean
    public boolean showPlateTotal = true;

    public enum Entry {
        WOOD("Wood"),
        STEEL("Steel"),
        SMALL_FISH("Small Fish"),
        RED_HERRING("Red Herring"),
        CLOWNFISH("Clownfish"),
        PUFFERFISH("Pufferfish"),
        ANGLERFISH_BULB("Anglerfish Bulb"),
        SEA_TURTLE_SCUTE("Sea Turtle Scute"),
        OCTOPUS("Octopus"),
        FORTUNE_FRAGMENTS("Fortune Fragments"),
        GOLDEN_CORAL("Golden Coral"),
        MERMAIDS_PEARL("Mermaid's Pearl"),
        ANCIENT_TRIDENT("Ancient Trident"),
        KRAKEN_SCALE("Kraken Scale"),
        COAL("Coal"),
        IRON("Iron"),
        GOLD("Gold"),
        EMERALD("Emerald"),
        DIAMOND("Diamond"),
        FUSED("Fused");

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