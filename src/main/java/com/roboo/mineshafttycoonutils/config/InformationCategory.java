package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InformationCategory {

    @Expose
    @ConfigOption(name = "Show Information HUD", desc = "Show the information HUD overlay")
    @ConfigEditorBoolean
    public boolean hudEnabled = true;

    @Expose
    @ConfigOption(name = "Disable Right-Align Flip", desc = "Keep this HUD's text left-aligned even when positioned past the middle of the screen, instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = false;

    @Expose
    @ConfigOption(name = "Line Order", desc = "Drag to reorder the lines shown on the information HUD. Remove an entry to hide it entirely.")
    @ConfigEditorDraggableList
    public List<Entry> order = new ArrayList<>(Arrays.asList(Entry.values()));

    @Expose
    public int informationHudX = 10;

    @Expose
    public int informationHudY = 10;

    public enum Entry {
        SPRINT("Sprint"),
        LEFT_CLICK("Left Click"),
        RIGHT_CLICK("Right Click"),
        PET("Pet");

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