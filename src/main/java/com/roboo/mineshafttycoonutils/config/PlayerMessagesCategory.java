package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.chat.GlyphCategory;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerMessagesCategory {

    @Expose
    @ConfigOption(name = "Enable Chat Formatting", desc = "Reformat player chat messages using the settings below")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Part Order", desc = "Drag to reorder the parts of a chat message. Remove a part to hide it entirely.")
    @ConfigEditorDraggableList
    public List<Part> partOrder = new ArrayList<>(Arrays.asList(Part.values()));

    @Expose
    @ConfigOption(name = "Player Rank Hider", desc = "Hide player ranks (e.g. [VIP], [MVP+]) in all chat messages")
    @ConfigEditorBoolean
    public boolean rankHider = false;

    @Expose
    @ConfigOption(name = "Same Chat Color", desc = "Make all chat messages (including party chat) white regardless of rank")
    @ConfigEditorBoolean
    public boolean sameChatColor = false;

    @Expose
    @ConfigOption(name = "Custom Name Color", desc = "Color every player's name with a custom color instead of their rank color")
    @ConfigEditorBoolean
    public boolean customNameColor = false;

    @Expose
    @ConfigOption(name = "Name Color", desc = "Custom color used for player names when Custom Name Color is enabled")
    @ConfigEditorColour
    public ChromaColour nameColor = ChromaColour.fromStaticRGB(255, 255, 255, 255);

    @Expose
    @Category(name = "Glyph Ranks", desc = "Use custom rank images")
    public GlyphCategory glyph = new GlyphCategory();

    @Expose
    public double savedChatLineSpacing = -1.0;

    public enum Part {
        TIER("Tier"),
        RANK("Rank"),
        PLAYER_NAME("Player Name"),
        MESSAGE("Message");

        private final String displayName;

        Part(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}