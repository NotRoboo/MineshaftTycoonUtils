package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlyphCategory {

    @ConfigOption(
            name = "Glyph Preview",
            desc = """
                    Thanks watergrowsifwhat for `§eThemed` §fglyphs
                    §7Off:      §a[T1] §e[T2] §c[T3]  §3[T4] §9[T4] §7[T5] §6[T5] §4[T5]
                    §7Classic: §f\uE001 \uE002 \uE003 \uE005 \uE004 \uE006 \uE007 \uE008
                    §7Themed: §f\uE101  \uE102 \uE103  \uE105  \uE104 \uE106 \uE107  \uE108"""
    )
    @ConfigEditorInfoText
    @SuppressWarnings("unused")
    public boolean glyphPreview = false;

    @Expose
    @ConfigOption(
            name = "Glyphs in Player Messages",
            desc = "Replace  with glyphs in player chat messages"
    )
    @ConfigEditorDropdown
    public GlyphMode playerMessageGlyphs = GlyphMode.OFF;

    @Expose
    @ConfigOption(
            name = "Glyphs in Other Chat Messages",
            desc = "Replace with glyphs anywhere they appear in non-player chat messages")
    @ConfigEditorDropdown
    public GlyphMode otherMessageGlyphs = GlyphMode.OFF;

    @Expose
    @ConfigOption(name = "Glyphs in Tab List", desc = "Replace with pixel art glyphs in the tab list")
    @ConfigEditorDropdown
    public GlyphMode tablistGlyphs = GlyphMode.OFF;

    @Expose
    @ConfigOption(
            name = "Glyphs in Nametags/Holograms",
            desc = "Replace with pixel art glyphs in nametags an holograms")
    @ConfigEditorDropdown
    public GlyphMode nametagGlyphs = GlyphMode.OFF;

    @Expose
    @ConfigOption(name = "Tab List Order", desc = "Drag to reorder tab list name parts, left to right in the tab list. Remove a part to hide it entirely.")
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