package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionBarCategory {

    @Expose
    @ConfigOption(name = "Action Bar Reorder", desc = "Reorder or hide parts of the action bar")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(
            name = "Default Order",
            desc = "Drag to reorder parts of the default action bar. Remove a part to hide it.")
    @ConfigEditorDraggableList
    public List<DefaultBar> defaultOrder = new ArrayList<>(Arrays.asList(DefaultBar.values()));

    public enum DefaultBar {
        ID("ID"),
        XP_PROGRESS("XP Progress"),
        XP("XP"),
        FORTUNE("Fortune"),
        GOAL("Cookie Goal");

        private final String displayName;

        DefaultBar(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(
            name = "Ocean Order",
            desc = "Drag to reorder parts of the Ocean action bar. Remove a part to hide it.")
    @ConfigEditorDraggableList
    public List<OceanBar> oceanOrder = new ArrayList<>(Arrays.asList(OceanBar.values()));

    public enum OceanBar {
        SHIP_HP("Ship HP"),
        SHIP_DMG("Ship DMG"),
        FISHING_SPEED("Fishing Speed"),
        SCC("SCC"),
        DHC("DHC");

        private final String displayName;

        OceanBar(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }


        @Expose
        @ConfigOption(
                name = "Space Order",
                desc = "Drag to reorder parts of the Space action bar. Remove a part to hide it.")
        @ConfigEditorDraggableList
        public List<SpaceBar> spaceOrder = new ArrayList<>(Arrays.asList(SpaceBar.values()));

        public enum SpaceBar {
            ID("ID"),
            XP_PROGRESS("XP Progress"),
            XP("XP"),
            FORTUNE("Fortune"),
            OXYGEN("Oxygen");

            private final String displayName;

            SpaceBar(String displayName) {
                this.displayName = displayName;
            }

            @Override
            public String toString() {
                return displayName;
            }
        }
    }
