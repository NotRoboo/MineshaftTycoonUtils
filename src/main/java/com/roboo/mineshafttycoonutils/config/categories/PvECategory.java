package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PvECategory {

    @Expose
    @ConfigOption(name = "PvE Tracker HUD", desc = "Show the PvE kill tracker HUD overlay")
    @ConfigEditorBoolean
    public boolean hudEnabled = true;

    @Expose
    @ConfigOption(name = "Title Color", desc = "Color used for the PvE Tracker HUD title")
    @ConfigEditorColour
    public ChromaColour titleColor = ChromaColour.fromStaticRGB(255, 255, 85, 255);

    @Expose
    @ConfigOption(name = "Hide in Other Zones", desc = "Hide the PvE tracker HUD while outside of the designated PvE area")
    @ConfigEditorBoolean
    public boolean hideInOtherZones = true;

    @Expose
    @ConfigOption(
            name = "Disable Right-Align Flip",
            desc = "Keep this HUD's text left-aligned even when positioned past the middle of the screen, " +
                    "instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = false;

    @Expose
    @ConfigOption(
            name = "HUD Line Order",
            desc = "Drag to reorder the lines shown on the PvE HUD. Remove an entry to hide it; it will still be tracked.")
    @ConfigEditorDraggableList
    public List<LineEntry> hudLineOrder = new ArrayList<>(List.of(
            LineEntry.EMPEROR,
            LineEntry.TERROR,
            LineEntry.PIRATE,
            LineEntry.WIZARD,
            LineEntry.CORRUPTONAUT,
            LineEntry.KILLS_PER_HOUR
    ));

    @Expose
    public int hudX = 10;

    @Expose
    public int hudY = 100;

    @Expose
    public float scale = HudScale.DEFAULT;

    @Expose
    public Map<String, Integer> kills = new LinkedHashMap<>();

    public enum LineEntry {
        KILLS_PER_HOUR("Kills/Hour"),
        EMPEROR("Emperor"),
        TERROR("Terror"),
        PIRATE("Pirate"),
        WIZARD("Wizard"),
        CORRUPTONAUT("Corruptonaut");

        private final String displayName;

        LineEntry(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}