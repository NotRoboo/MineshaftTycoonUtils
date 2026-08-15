package com.roboo.mineshafttycoonutils.config.chat;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GlyphCategory {

    @Expose
    @ConfigOption(name = "Glyphs in Player Messages", desc = "Replace [T#]/[Staff] tags with pixel art glyphs in formatted player chat lines")
    @ConfigEditorBoolean
    public boolean playerMessageGlyphs = false;

    @Expose
    @ConfigOption(name = "Glyphs in Other Chat Messages", desc = "Replace [T#]/[Staff] tags with pixel art glyphs anywhere they appear in non-player chat messages")
    @ConfigEditorBoolean
    public boolean otherMessageGlyphs = false;

    @Expose
    @ConfigOption(name = "Glyphs in Tab List", desc = "Replace [T#]/[Staff] tags with pixel art glyphs in the tab list")
    @ConfigEditorBoolean
    public boolean tablistGlyphs = false;

    @Expose
    @ConfigOption(name = "Glyphs in Nametags", desc = "Replace [T#]/[Staff] tags with pixel art glyphs in player nametags")
    @ConfigEditorBoolean
    public boolean nametagGlyphs = false;

}