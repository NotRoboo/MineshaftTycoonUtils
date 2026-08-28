package com.roboo.mineshafttycoonutils.config.profit;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MagmaValueConfig {

    @Expose
    @ConfigOption(name = "Magma Value HUD", desc = "Show the magma value HUD overlay while inside the Space Ores Bag " +
            "§eUse your `Edit Container HUD Locations Keybind` to edit location")
    @ConfigEditorBoolean
    public boolean hudEnabled = true;

    @Expose
    @ConfigOption(name = "Title Color", desc = "Color used for the Magma Value HUD title and total")
    @ConfigEditorColour
    public ChromaColour titleColor = ChromaColour.fromStaticRGB(255, 255, 85, 255);

    @Expose
    @ConfigOption(
            name = "Disable Right-Align Flip",
            desc = "Keep this HUD's text left-aligned even when positioned past the middle of the screen, " +
                    "instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = false;

    @Expose
    @ConfigOption(
            name = "Magma Price (Billions)",
            desc = "Coin value of a single Magma, in billions. E.g. 200 means 1 Magma = 200b coins. §eDefault: 200")
    @ConfigEditorSlider(minValue = 1, maxValue = 2000, minStep = 1)
    public int magmaPriceBillions = 200;

    @Expose
    @ConfigOption(
            name = "Order",
            desc = "Drag to reorder ores shown on the HUD. Remove an entry to hide it and exclude it from the total.")
    @ConfigEditorDraggableList
    public List<Entry> order = new ArrayList<>(Arrays.asList(Entry.values()));

    @Expose
    public int magmaHudX = 200;

    @Expose
    public int magmaHudY = 160;

    public enum Entry {
        LUNAR_FRAGMENT("Lunar Fragment", 1),
        CHISILITE_SHARD("Chisilite Shard", 5),
        BASALT_SHARD("Basalt Shard", 10),
        BRECCA_POWDER("Brecca Powder", 15),
        AMBER_FRAGMENT("Amber Fragment", 20),
        AMETRINE("Ametrine", 25),
        MARTIAN_DUST("Martian Dust", 30),
        RHODNITE("Rhodnite", 35),
        SUN_FRAGMENT("Sun Fragment", 30),
        FIBER("Fiber", 40),
        PLASMA_SHARD("Plasma Shard", 50);

        private final String displayName;
        private final int magmaValue;

        Entry(String displayName, int magmaValue) {
            this.displayName = displayName;
            this.magmaValue = magmaValue;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getMagmaValue() {
            return magmaValue;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}