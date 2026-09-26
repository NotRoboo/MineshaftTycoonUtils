package com.roboo.mineshafttycoonutils.features.plasma;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.PlasmaCategory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolarFlareTracker {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final String CONTAINER_TITLE = "Solar Flare";
    private static final String COLLECTOR_ITEM = "Flare Collector";

    private static final Pattern STORED_FUEL_PATTERN =
            Pattern.compile("(?i)stored fuel:\\s*([0-9,]+)\\s*/\\s*([0-9,]+)\\s*fuel");
    private static final Pattern EFFICIENCY_PATTERN =
            Pattern.compile("(?i)collector effici?ency:\\s*([0-9,]+)%");

    private static long collectorStoredFuel = -1;
    private static long collectorMaxFuel = -1;

    private static long manualHeatGoalOverride = -1;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (mc.player == null || !(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) return;

        String title = containerScreen.getTitle().getString().trim();
        if (!CONTAINER_TITLE.equalsIgnoreCase(title)) return;

        readContainer(containerScreen.getMenu());
    }

    private static void readContainer(AbstractContainerMenu menu) {
        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            if (!COLLECTOR_ITEM.equalsIgnoreCase(name)) continue;

            readCollector(stack);
            return;
        }
    }

    private static void readCollector(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        List<Component> loreLines = lore != null ? lore.lines() : List.of();

        long stored = -1;
        long max = -1;
        Integer efficiency = null;

        for (Component loreLine : loreLines) {
            String text = ChatFormatting.stripFormatting(loreLine.getString()).trim();

            Matcher storedMatch = STORED_FUEL_PATTERN.matcher(text);
            if (storedMatch.find()) {
                stored = parseNumber(storedMatch.group(1));
                max = parseNumber(storedMatch.group(2));
                continue;
            }

            Matcher efficiencyMatch = EFFICIENCY_PATTERN.matcher(text);
            if (efficiencyMatch.find()) {
                try {
                    efficiency = Integer.parseInt(efficiencyMatch.group(1).replace(",", ""));
                } catch (NumberFormatException ignored) {}
            }
        }

        collectorStoredFuel = stored;
        collectorMaxFuel = max;

        if (efficiency != null && efficiency != ConfigManager.config.plasma.collectorEfficiencyPercent) {
            ConfigManager.config.plasma.collectorEfficiencyPercent = efficiency;
            MineshaftTycoonUtils.configManager.saveConfig();
        }
    }

    private static long parseNumber(String raw) {
        String cleaned = raw.replace(",", "");
        if (cleaned.isEmpty()) return -1;
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isCollectorKnown() {
        return collectorStoredFuel >= 0 && collectorMaxFuel >= 0;
    }

    public static long getCollectorRoom() {
        if (!isCollectorKnown()) return -1;
        long actualStored = Math.max(0, collectorStoredFuel - 1);
        return Math.max(0, collectorMaxFuel - actualStored);
    }

    public static void setManualHeatGoal(long value) {
        manualHeatGoalOverride = value;
    }

    public static void clearManualHeatGoal() {
        manualHeatGoalOverride = -1;
    }

    public static boolean isManualHeatGoalActive() {
        return manualHeatGoalOverride >= 0;
    }

    public static long computeHeatGoal() {
        if (manualHeatGoalOverride >= 0) return manualHeatGoalOverride;

        long heat1 = PlasmaSmitheryTracker.getHeat1();
        long maxHeat1 = PlasmaSmitheryTracker.getMaxHeat1();
        long heat2 = PlasmaSmitheryTracker.getHeat2();
        long maxHeat2 = PlasmaSmitheryTracker.getMaxHeat2();

        if (heat1 < 0 || maxHeat1 < 0 || heat2 < 0 || maxHeat2 < 0) return -1;

        return Math.max(0, (maxHeat1 + maxHeat2) - (heat1 + heat2));
    }

    public static long computeTarget() {
        long heatGoal = computeHeatGoal();
        if (heatGoal < 0) return -1;

        long room = getCollectorRoom();
        if (room < 0) return heatGoal;

        return Math.min(heatGoal, room);
    }

    public static Map<PlasmaCategory.FlareEntry, Long> computeFlareDonation(long target) {
        Map<PlasmaCategory.FlareEntry, Long> result = new LinkedHashMap<>();
        if (target <= 0) return result;

        long remaining = target;
        for (PlasmaCategory.FlareEntry entry : PlasmaCategory.FlareEntry.values()) {
            long value = FlareValueTracker.getEffectiveFuelValue(entry);
            if (value <= 0) continue;

            long available = FlareValueTracker.getInventoryQuantity(entry);
            long take = Math.min(available, remaining / value);
            if (take > 0) {
                result.put(entry, take);
                remaining -= take * value;
            }
        }

        return result;
    }

    public static void reset() {
        collectorStoredFuel = -1;
        collectorMaxFuel = -1;
        manualHeatGoalOverride = -1;
    }
}