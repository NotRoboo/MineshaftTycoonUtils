package com.roboo.mineshafttycoonutils.config.chat;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlyphCategory {

    @Expose
    @ConfigOption(
            name = "Glyphs in Player Messages",
            desc = "Replace MST tags with pixel art glyphs in formatted player chat lines. " +
                    "THIS REQUIRES PLAYER MESSAGING FORMATTING TO BE ENABLED")
    @ConfigEditorBoolean
    public boolean playerMessageGlyphs = false;

    @Expose
    @ConfigOption(
            name = "Glyphs in Other Chat Messages",
            desc = "Replace MST tags with pixel art glyphs anywhere they appear in non-player chat messages")
    @ConfigEditorBoolean
    public boolean otherMessageGlyphs = false;

    @Expose
    @ConfigOption(name = "Glyphs in Tab List", desc = "Replace MST tags with pixel art glyphs in the tab list")
    @ConfigEditorBoolean
    public boolean tablistGlyphs = false;

    @Expose
    @ConfigOption(
            name = "Glyphs in Nametags/Holograms",
            desc = "Replace MST tags with pixel art glyphs in player nametags")
    @ConfigEditorBoolean
    public boolean nametagGlyphs = false;

    @Expose
    @ConfigOption(name = "Tab List Part Order", desc = "Drag to reorder tab list name parts, left to right. Remove a part to hide it entirely.")
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
}