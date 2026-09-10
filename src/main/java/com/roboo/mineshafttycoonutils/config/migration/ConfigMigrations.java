package com.roboo.mineshafttycoonutils.config.migration;

import com.roboo.mineshafttycoonutils.config.categories.ItemLoreCategory;
import com.roboo.mineshafttycoonutils.config.categories.PlayerMessagesCategory;
import com.roboo.mineshafttycoonutils.features.misc.NightVisionBlocker;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class ConfigMigrations {

    private static final Logger LOGGER = LoggerFactory.getLogger("MineshaftTycoonUtils");

    public static final int CURRENT_VERSION = 5;

    private static final Map<Integer, Consumer<JsonObject>> UPGRADE_STEPS = new TreeMap<>();

    static {
        registerUpgradeStep(2, NightVisionBlocker::onConfigFix);
        registerUpgradeStep(3, ItemLoreCategory::onConfigFix);
        registerUpgradeStep(4, ConfigMigrations::renameZoneSettings);
        registerUpgradeStep(5, PlayerMessagesCategory::onConfigFix);
    }

    private static void renameZoneSettings(JsonObject savedConfig) {
        ConfigJsonUtils.renameSetting(savedConfig, "fishing.onlyShowWhenFishing", "hideInOtherZones");
        ConfigJsonUtils.renameSetting(savedConfig, "profit.tracker.onlyShowWhenMining", "hideInOtherZones");
    }

    private ConfigMigrations() {}

    private static void registerUpgradeStep(int targetVersion, Consumer<JsonObject> upgrade) {
        UPGRADE_STEPS.put(targetVersion, upgrade);
    }

    public static void upgradeSavedConfig(JsonObject savedConfig, int savedVersion) {
        for (Map.Entry<Integer, Consumer<JsonObject>> step : UPGRADE_STEPS.entrySet()) {
            if (step.getKey() <= savedVersion) continue;

            try {
                step.getValue().accept(savedConfig);
            } catch (Exception e) {
                LOGGER.warn("Failed to upgrade config to version {}", step.getKey(), e);
            }
        }
    }
}