package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.migration.ConfigJsonUtils;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ItemLoreCategory {

    @Expose
    @ConfigOption(
            name = "Shorten Large Lore Numbers",
            desc = "Shortens numbers over 1,000,000,000 in item lore, e.g. 1,500,000,000 becomes 1.50B")
    @ConfigEditorBoolean
    public boolean shortenLoreNumbers = true;

    @Expose
    @ConfigOption(
            name = "Shorten Lore Time",
            desc = "Shortens `Xs (X Seconds)` style durations in item lore into Xd Xh Xm Xs format")
    @ConfigEditorBoolean
    public boolean shortenLoreTime = true;

    public static void onConfigFix(JsonObject savedConfig) {
        if (!savedConfig.has("itemLore") || !savedConfig.get("itemLore").isJsonObject()) {
            savedConfig.add("itemLore", new JsonObject());
        }

        ConfigJsonUtils.moveSetting(savedConfig, "misc.shortenLoreNumbers", "itemLore.shortenLoreNumbersEnabled");
    }
}