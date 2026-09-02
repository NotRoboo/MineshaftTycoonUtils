package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerMessagesCategory {

    @Expose
    @ConfigOption(name = "Chat Formatting", desc = "Reformat player chat messages using the settings below. Also reformats [SYSTEM] chat bridge messages to strip the bot's name and show the real sender")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(
            name = "Part Order",
            desc = "Drag to reorder the parts of a chat message. Remove a part to hide it entirely.\n" +
                    "Top -> Bottom = Left -> Right"
    )
    @ConfigEditorDraggableList
    public List<Part> partOrder = new ArrayList<>(Arrays.asList(Part.values()));

    @Expose
    @ConfigOption(name = "Hypixel Rank Hider", desc = "Hide ranks ([VIP], [MVP+])")
    @ConfigEditorBoolean
    public boolean rankHider = false;

    @Expose
    @ConfigOption(name = "Nons White Chat", desc = "Make all nons chat messages white instead of grey")
    @ConfigEditorBoolean
    public boolean sameChatColor = true;

    @Expose
    @ConfigOption(
            name = "Custom Name Color",
            desc = "Color every player's name with a custom color instead of their rank color")
    @ConfigEditorBoolean
    public boolean customNameColor = false;

    @Expose
    @ConfigOption(name = "Name Color", desc = "Custom color used for player names when Custom Name Color is enabled")
    @ConfigEditorColour
    public ChromaColour nameColor = ChromaColour.fromStaticRGB(255, 255, 255, 255);

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