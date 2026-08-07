package com.roboo.mineshafttycoonutils.config.chat;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class JoinMessages {
    @Expose
    @ConfigOption(name = "Hide T1 Join Messages", desc = "Hide T1 join messages (Mainly Bots)")
    @ConfigEditorBoolean
    public boolean hideT1JoinMessages = true;

    @Expose
    @ConfigOption(name = "Hide T2 Join Messages", desc = "Hide T2 join messages")
    @ConfigEditorBoolean
    public boolean hideT2JoinMessages = false;

    @Expose
    @ConfigOption(name = "Hide T3 Join Messages", desc = "Hide T3 join messages")
    @ConfigEditorBoolean
    public boolean hideT3JoinMessages = false;

    @Expose
    @ConfigOption(name = "Hide T4 Join Messages", desc = "Hide T4 join messages")
    @ConfigEditorBoolean
    public boolean hideT4JoinMessages = false;
}
