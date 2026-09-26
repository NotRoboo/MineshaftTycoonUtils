package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlasmaCategory {

    @Expose
    @ConfigOption(name = "Plasma Generator HUDs", desc = "Show the Plasma Forge, Ore Bag Flare Value, and Solar Flare HUD overlays")
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Title Color", desc = "Color used for the Plasma Generator HUD titles")
    @ConfigEditorColour
    public ChromaColour titleColor = ChromaColour.fromStaticRGB(255, 255, 85, 255);

    @Expose
    @ConfigOption(
            name = "Disable Right-Align Flip",
            desc = "Keep these HUDs' text left-aligned even when positioned past the middle of the screen, " +
                    "instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = true;

    @Expose
    @ConfigOption(
            name = "Smithery Line Order",
            desc = "Drag to reorder the lines shown on the Plasma Smithery (forge) HUD. Remove an entry to hide it entirely.")
    @ConfigEditorDraggableList
    public List<SmitheryLine> smitheryOrder = new ArrayList<>(Arrays.asList(SmitheryLine.values()));

    @Expose
    public int smitheryHudX = 240;

    @Expose
    public int smitheryHudY = 190;

    @Expose
    public float smitheryHudScale = HudScale.DEFAULT;

    public enum SmitheryLine {
        GEN1_TIME("Gen 1 Time"),
        GEN1_HEAT("Gen 1 Heat"),
        GEN2_TIME("Gen 2 Time"),
        GEN2_HEAT("Gen 2 Heat");

        private final String displayName;

        SmitheryLine(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(
            name = "Flare Value Order",
            desc = "Drag to reorder flares shown on the HUD. Remove an entry to hide it and exclude it from the total.")
    @ConfigEditorDraggableList
    public List<FlareEntry> flareValueOrder = new ArrayList<>(Arrays.asList(FlareEntry.values()));

    @Expose
    public int flareValueHudX = 605;

    @Expose
    public int flareValueHudY = 160;

    @Expose
    public float flareValueHudScale = HudScale.DEFAULT;

    public enum FlareEntry {
        SOLAR_EMBER("Solar Ember Flare", "Solar Flare", 100),
        CORONA_SURGE("Corona Surge Flare", "Corona Flare", 120),
        STELLAR_PULSE("Stellar Pulse Flare", "Stellar Flare", 150),
        RADIANT_NOVA("Radiant Nova Flare", "Radiant Flare", 200),
        ETERNAL_SINGULARITY("Eternal Singularity Flare", "Eternal Flare", 500);

        private final String fullName;
        private final String shortName;
        private final int baseFuelValue;

        FlareEntry(String fullName, String shortName, int baseFuelValue) {
            this.fullName = fullName;
            this.shortName = shortName;
            this.baseFuelValue = baseFuelValue;
        }

        public String getShortName() {
            return shortName;
        }

        public int getBaseFuelValue() {
            return baseFuelValue;
        }

        public static FlareEntry fromFullName(String name) {
            for (FlareEntry entry : values()) {
                if (entry.fullName.equalsIgnoreCase(name)) return entry;
            }
            return null;
        }

        @Override
        public String toString() {
            return shortName;
        }
    }

    @Expose
    @ConfigOption(
            name = "Solar Flare Line Order",
            desc = "Drag to reorder the lines shown on the Solar Flare HUD. Remove an entry to hide it entirely.")
    @ConfigEditorDraggableList
    public List<SolarFlareLine> solarFlareOrder = new ArrayList<>(Arrays.asList(SolarFlareLine.values()));

    @Expose
    public int solarFlareHudX = 250;

    @Expose
    public int solarFlareHudY = 170;

    @Expose
    public float solarFlareHudScale = HudScale.DEFAULT;

    public enum SolarFlareLine {
        GEN1_HEAT("Gen 1 Heat"),
        GEN2_HEAT("Gen 2 Heat"),
        INVENTORY_FUEL("Inventory Fuel"),
        TARGET("Target"),
        DONATE("Donate List");

        private final String displayName;

        SolarFlareLine(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    public int collectorEfficiencyPercent = 100;
}