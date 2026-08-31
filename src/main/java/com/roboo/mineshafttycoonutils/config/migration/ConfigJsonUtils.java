package com.roboo.mineshafttycoonutils.config.migration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class ConfigJsonUtils {

    private ConfigJsonUtils() {}

    private static JsonObject findContainingObject(JsonObject root, String settingPath) {
        String[] parts = settingPath.split("\\.");
        JsonObject current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            if (current == null || !current.has(parts[i]) || !current.get(parts[i]).isJsonObject()) return null;
            current = current.getAsJsonObject(parts[i]);
        }
        return current;
    }

    private static String getSettingName(String settingPath) {
        String[] parts = settingPath.split("\\.");
        return parts[parts.length - 1];
    }

    public static boolean settingExists(JsonObject root, String settingPath) {
        JsonObject container = findContainingObject(root, settingPath);
        return container != null && container.has(getSettingName(settingPath));
    }

    public static JsonElement getSettingValue(JsonObject root, String settingPath) {
        JsonObject container = findContainingObject(root, settingPath);
        if (container == null) return null;
        return container.get(getSettingName(settingPath));
    }

    public static void renameSetting(JsonObject root, String oldSettingPath, String newSettingName) {
        JsonObject container = findContainingObject(root, oldSettingPath);
        String oldSettingName = getSettingName(oldSettingPath);
        if (container == null || !container.has(oldSettingName)) return;

        JsonElement value = container.remove(oldSettingName);
        container.add(newSettingName, value);
    }

    public static void moveSetting(JsonObject root, String oldSettingPath, String newSettingPath) {
        JsonObject oldContainer = findContainingObject(root, oldSettingPath);
        String oldSettingName = getSettingName(oldSettingPath);
        if (oldContainer == null || !oldContainer.has(oldSettingName)) return;

        JsonObject newContainer = findContainingObject(root, newSettingPath);
        if (newContainer == null) return;

        JsonElement value = oldContainer.remove(oldSettingName);
        newContainer.add(getSettingName(newSettingPath), value);
    }

    public static void deleteSetting(JsonObject root, String settingPath) {
        JsonObject container = findContainingObject(root, settingPath);
        if (container == null) return;
        container.remove(getSettingName(settingPath));
    }

    public static void updateSettingValue(JsonObject root, String settingPath, Function<JsonElement, JsonElement> updater) {
        JsonObject container = findContainingObject(root, settingPath);
        String settingName = getSettingName(settingPath);
        if (container == null || !container.has(settingName)) return;

        JsonElement updatedValue = updater.apply(container.get(settingName));
        if (updatedValue == null) {
            container.remove(settingName);
        } else {
            container.add(settingName, updatedValue);
        }
    }
}