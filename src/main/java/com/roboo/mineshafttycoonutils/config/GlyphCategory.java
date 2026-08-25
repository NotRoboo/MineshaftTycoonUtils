package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlyphCategory {

    @Expose
    @ConfigOption(
            name = "Glyphs in Player Messages",
            desc = "Replace MST tags with pixel art glyphs in player chat lines")
    @ConfigEditorDropdown
    public GlyphMode playerMessageGlyphs = GlyphMode.OFF;

    @Expose
    @ConfigOption(
            name = "Glyphs in Other Chat Messages",
            desc = "Replace MST tags with pixel art glyphs anywhere they appear in non-player chat messages")
    @ConfigEditorDropdown
    public GlyphMode otherMessageGlyphs = GlyphMode.OFF;

    @Expose
    @ConfigOption(name = "Glyphs in Tab List", desc = "Replace MST tags with pixel art glyphs in the tab list")
    @ConfigEditorDropdown
    public GlyphMode tablistGlyphs = GlyphMode.OFF;

    @Expose
    @ConfigOption(
            name = "Glyphs in Nametags/Holograms",
            desc = "Replace MST tags with pixel art glyphs in player nametags")
    @ConfigEditorDropdown
    public GlyphMode nametagGlyphs = GlyphMode.OFF;

    @Expose
    @ConfigOption(name = "Tab List Part Order", desc = "Drag to reorder tab list name parts, left to right in the tab list. Remove a part to hide it entirely.")
    @ConfigEditorDraggableList
    public List<TabListPart> tabListPartOrder = new ArrayList<>(Arrays.asList(TabListPart.values()));

    public enum TabListPart {
        TIER("Tier"),
        RANK("Rank"),
        USERNAME("Username");

        private final String displayName;

        TabListPart(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum GlyphMode {
        OFF("Off"),
        CLASSIC("Classic"),
        THEMED("Themed");

        private final String displayName;

        GlyphMode(String displayName) {
            this.displayName = displayName;
        }

        public boolean isEnabled() {
            return this != OFF;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}