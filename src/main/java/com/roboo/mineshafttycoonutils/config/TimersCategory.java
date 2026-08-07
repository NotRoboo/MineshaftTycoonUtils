package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.timers.TimersHudPositionConfig;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimersCategory {

    @Expose
    @ConfigOption(name = "Show Timers HUD", desc = "Show the timers HUD overlay")
    @ConfigEditorBoolean
    public boolean hudEnabled = true;

    @Expose
    @ConfigOption(name = "Hide Seconds Until Final Minute", desc = "Only show seconds once a timer drops under a minute, so timers with hours/minutes left aren't ticking down on screen")
    @ConfigEditorBoolean
    public boolean hideSecondsUntilFinalMinute = false;

    @Expose
    @ConfigOption(name = "Timer Order", desc = "Drag to reorder timers shown on the HUD. Remove an entry to hide it; it will still be tracked.")
    @ConfigEditorDraggableList
    public List<Entry> order = new ArrayList<>(Arrays.asList(Entry.values()));

    @Expose
    @Category(name = "HUD Position", desc = "Position of the timers HUD overlay")
    public TimersHudPositionConfig hudPosition = new TimersHudPositionConfig();

    public enum Entry {
        T4_POTION("T4 Potion"),
        T3_POTION("T3 Potion"),
        T2_POTION("T2 Potion"),
        T1_POTION("T1 Potion"),
        PETAD("Petad"),
        ILS_RESTOCK("Il's Restock"),
        IRONVINE("Ironvine"),
        REDROOT("Redroot"),
        AURORA_FRUIT("Aurora Fruit"),
        SQUASH("Squash"),
        DUSTGRAIN("Dustgrain"),
        SUNFLOWER("Sunflower"),
        FISHING_BUFF("Fishing Buff");

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