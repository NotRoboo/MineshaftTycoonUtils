package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;

public class MSTUConfig extends Config {

    @Override
    public StructuredText getTitle() {
        return StructuredText.of("MineshaftTycoon Utils");
    }

    @Expose
    @Category(name = "General", desc = "General settings")
    public GeneralCategory general = new GeneralCategory();

    @Expose
    @Category(name = "Profit Tracker", desc = "Refinery profit tracking settings")
    public ProfitCategory profit = new ProfitCategory();

    @Expose
    @Category(name = "Fishing", desc = "Fishing tracker and HUD settings")
    public FishingCategory fishing = new FishingCategory();

    @Expose
    @Category(name = "Chat Filters", desc = "Chat message filtering settings")
    public ChatCategory chat = new ChatCategory();

    @Expose
    @Category(name = "Player Messages", desc = "Change formatting of player messages")
    public PlayerMessagesCategory playerMessages = new PlayerMessagesCategory();

    @Expose
    @Category(name = "Timers", desc = "Buff, Petad, and Il's Restock timer settings")
    public TimersCategory timers = new TimersCategory();

    @Expose
    @Category(name = "Information Hud", desc = "Information HUD settings")
    public InformationCategory information = new InformationCategory();

    @Expose
    @Category(name = "Misc", desc = "Miscellaneous quality-of-life settings")
    public MiscCategory misc = new MiscCategory();
}