package com.roboo.mineshafttycoonutils.config.migration;

import com.roboo.mineshafttycoonutils.config.ItemLoreCategory;
import com.roboo.mineshafttycoonutils.features.misc.NightVisionBlocker;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class ConfigMigrations {

    private static final Logger LOGGER = LoggerFactory.getLogger("MineshaftTycoonUtils");

    public static final int CURRENT_VERSION = 3;

    private static final Map<Integer, Consumer<JsonObject>> UPGRADE_STEPS = new TreeMap<>();

    static {
        registerUpgradeStep(2, NightVisionBlocker::onConfigFix);
        registerUpgradeStep(3, ItemLoreCategory::onConfigFix);
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