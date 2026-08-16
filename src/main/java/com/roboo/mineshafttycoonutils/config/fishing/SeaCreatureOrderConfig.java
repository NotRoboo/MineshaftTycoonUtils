package com.roboo.mineshafttycoonutils.config.fishing;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeaCreatureOrderConfig {

    @Expose
    @ConfigOption(
            name = "Breakdown",
            desc = "Show the per-creature breakdown list for sea creatures on the HUD")
    @ConfigEditorBoolean
    public boolean breakdownEnabled = true;

    @Expose
    @ConfigOption(
            name = "Order",
            desc = "Drag to reorder sea creatures shown on the HUD. Remove an entry to hide it; it will still be tracked.")
    @ConfigEditorDraggableList
    public List<Entry> order = new ArrayList<>(Arrays.asList(Entry.values()));

    public enum Entry {
        TRASH_SQUID("Trash Squid"),
        DIVER("Diver"),
        LOST_TRAINER("Lost Trainer"),
        SUPREME_LEECH("Supreme Leech"),
        FAFNIR("Fafnir"),
        DEEP_SEA_SCIENTIST("Deep Sea Scientist"),
        SCARFION("Scarfion"),
        CTHULHU("Cthulhu"),
        ATTACK_SQUID("Attack Squid"),
        PHANTOM_FISHER("Phantom Fisher"),
        CAPTAIN_BARBOSSA("Captain Barbossa"),
        LOCKED_LSC("Locked LSC"),
        ZEPHYR("Zephyr");

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