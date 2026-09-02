package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.chat.JoinMessages;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ChatCategory {

    @Expose
    @Accordion
    @ConfigOption(name = "Join Messages", desc = "Filters for tier welcome/join back messages")
    public JoinMessages joinMessages = new JoinMessages();

    @Expose
    @ConfigOption(name = "Hide Pet XP Messages", desc = "Hide '+1 XP for your ...' pet leveling messages")
    @ConfigEditorBoolean
    public boolean hidePetXP = false;

    @Expose
    @ConfigOption(name = "Hide Pet Messages", desc = "Hide chat messages related to pets (if it starts with 'PETS!')")
    @ConfigEditorBoolean
    public boolean hidePetMessages = false;

    @Expose
    @ConfigOption(name = "Hide Fishing Messages", desc = "Hide common fishing catch spam, Ultra rare drops will still show.")
    @ConfigEditorBoolean
    public boolean hideFishingMessages = false;

    @Expose
    @ConfigOption(name = "Hide Fortune Fragment Spam", desc = "Hide mining up fortune frags")
    @ConfigEditorBoolean
    public boolean hidePotFrags = false;

    @Expose
    @ConfigOption(
            name = "Hide Discord Join Messages",
            desc = "Hide Discord advert, you should already be in it.")
    @ConfigEditorBoolean
    public boolean hideDiscordJoin = false;

    @Expose
    @ConfigOption(name = "Hide PvE Messages", desc = "Hide PvE defeat messages and CPS limit warnings")
    @ConfigEditorBoolean
    public boolean hidePvE = false;
}