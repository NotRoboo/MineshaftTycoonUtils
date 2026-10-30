package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimersCategory {

    @Expose
    @ConfigOption(
            name = "Timers HUD",
            desc = "Show the timers HUD overlay. \n" +
                    "§eNOTE: Over long periods timer may go slightly quicker")
    @ConfigEditorBoolean
    public boolean hudEnabled = true;

    @Expose
    @ConfigOption(name = "Title Color", desc = "Color used for the Timers HUD title")
    @ConfigEditorColour
    public ChromaColour titleColor = ChromaColour.fromStaticRGB(255, 255, 85, 255);

    @Expose
    @ConfigOption(
            name = "Hide Seconds Until Final Minute",
            desc = "Only show seconds once a timer drops under a minute, so timers with hours/minutes left aren't ticking down on screen")
    @ConfigEditorBoolean
    public boolean hideSecondsUntilFinalMinute = false;

    @Expose
    @ConfigOption(
            name = "Disable Right-Align Flip",
            desc = "Keep this HUD's text left-aligned even when positioned past the middle of the screen, " +
                    "instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = false;

    @Expose
    @ConfigOption(
            name = "Timer Order",
            desc = "Drag to reorder timers shown on the HUD. Remove an entry to hide it; it will still be tracked.")
    @ConfigEditorDraggableList
    public List<Entry> order = new ArrayList<>(Arrays.asList(Entry.values()));

    @Expose
    public int timersHudX = 180;

    @Expose
    public int timersHudY = 20;

    @Expose
    public float scale = HudScale.DEFAULT;

    public enum Entry {
        POTION("Fortune Pots"),
        PETAD("Petad"),
        EVENT("Event"),
        COMPACTOR_1("Compactor 1"),
        COMPACTOR_2("Compactor 2"),
        PLASMA_GEN_1("Plasma Gen 1"),
        PLASMA_GEN_2("Plasma Gen 2"),
        REFUEL("Refuel"),
        IRONVINE("Ironvine"),
        REDROOT("Redroot"),
        AURORA_FRUIT("Aurora Fruit"),
        SQUASH("Squash"),
        DUSTGRAIN("Dustgrain"),
        SUNFLOWER("Sunflower"),
        FISHING_BUFF("Fishing Buff"),
        GREENHOUSE("Greenhouse");

        private final String displayName;

        Entry(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static void onConfigFix(JsonObject savedConfig) {
        addMissingOrderEntries(savedConfig,
                List.of("COMPACTOR_1", "COMPACTOR_2", "PLASMA_GEN_1", "PLASMA_GEN_2", "REFUEL"));
    }

    public static void onEventFix(JsonObject savedConfig) {
        addMissingOrderEntries(savedConfig, List.of("EVENT"));
    }

    private static void addMissingOrderEntries(JsonObject savedConfig, List<String> names) {
        if (!savedConfig.has("timers") || !savedConfig.get("timers").isJsonObject()) return;

        JsonObject timers = savedConfig.getAsJsonObject("timers");
        if (!timers.has("order") || !timers.get("order").isJsonArray()) return;

        JsonArray order = timers.getAsJsonArray("order");
        for (String name : names) {
            boolean present = false;
            for (JsonElement element : order) {
                if (element.isJsonPrimitive() && name.equals(element.getAsString())) {
                    present = true;
                    break;
                }
            }
            if (!present) order.add(name);
        }
    }

    public static void onPotionMergeFix(JsonObject savedConfig) {
        if (!savedConfig.has("timers") || !savedConfig.get("timers").isJsonObject()) return;

        JsonObject timers = savedConfig.getAsJsonObject("timers");
        if (!timers.has("order") || !timers.get("order").isJsonArray()) return;

        List<String> potionTiers = List.of("T4_POTION", "T3_POTION", "T2_POTION", "T1_POTION");
        JsonArray updated = new JsonArray();
        boolean potionAdded = false;

        for (JsonElement element : timers.getAsJsonArray("order")) {
            if (!element.isJsonPrimitive()) continue;
            String name = element.getAsString();

            if ("ILS_RESTOCK".equals(name)) continue;

            if (potionTiers.contains(name)) {
                if (!potionAdded) {
                    updated.add("POTION");
                    potionAdded = true;
                }
                continue;
            }

            updated.add(name);
        }

        timers.add("order", updated);
    }
}