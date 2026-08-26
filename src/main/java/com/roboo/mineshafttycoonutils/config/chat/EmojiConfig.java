package com.roboo.mineshafttycoonutils.config.chat;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EmojiConfig {

    @Expose
    @ConfigOption(name = "Enable Emojis", desc = "Replace :shortcode: text like :mpreg: with emoji images in chat messages")
    @ConfigEditorBoolean
    public boolean emojisEnabled = true;

    @Expose
    @ConfigOption(name = "Suggestions", desc = "Show a suggestion popup while typing an emoji shortcode in chat")
    @ConfigEditorBoolean
    public boolean suggestionsEnabled = true;
}