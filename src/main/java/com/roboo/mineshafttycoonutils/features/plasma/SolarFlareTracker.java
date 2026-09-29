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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolarFlareTracker {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final String CONTAINER_TITLE = "Solar Flare";
    private static final String COLLECTOR_ITEM = "Flare Collector";
    private static final int DP_LIMIT = 60_000;
    private static final long ABSORB_WINDOW_MS = 10_000L;

    private static final Pattern STORED_FUEL_PATTERN =
            Pattern.compile("(?i)stored fuel:\\s*([0-9,]+)\\s*/\\s*([0-9,]+)\\s*fuel");
    private static final Pattern EFFICIENCY_PATTERN =
            Pattern.compile("(?i)collector effici?ency:\\s*([0-9,]+)%");

    private static long collectorStoredFuel = -1;
    private static long collectorMaxFuel = -1;
    private static long lastReadStoredFuel = -1;
    private static long lastInventoryValue = -1;
    private static long absorbedFuel = 0;
    private static long openedAtMillis = 0;
    private static boolean open = false;

    private static long manualHeatGoalOverride = -1;

    private static String withdrawalCacheKey = null;
    private static Map<PlasmaCategory.FlareEntry, Long> withdrawalCache = new LinkedHashMap<>();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static AbstractContainerScreen<?> currentScreen() {
        if (mc.player == null || !(mc.screen instanceof AbstractContainerScreen<?> screen)) return null;
        return CONTAINER_TITLE.equalsIgnoreCase(screen.getTitle().getString().trim()) ? screen : null;
    }

    private static void onTick() {
        AbstractContainerScreen<?> screen = currentScreen();
        if (screen == null) {
            open = false;
            lastReadStoredFuel = -1;
            lastInventoryValue = -1;
            return;
        }

        if (!open) {
            open = true;
            openedAtMillis = System.currentTimeMillis();
        }

        boolean refreshed = readContainer(screen.getMenu());
        trackDeposits(refreshed);
    }

    private static void trackDeposits(boolean refreshed) {
        long inventory = FlareValueTracker.getTotalInventoryValue();

        if (lastInventoryValue >= 0 && !refreshed && isCollectorKnown()) {
            long delta = lastInventoryValue - inventory;
            if (delta != 0) {
                collectorStoredFuel = Math.clamp(collectorStoredFuel + delta, 0, collectorMaxFuel);
            }
        }

        lastInventoryValue = inventory;
    }

    private static boolean readContainer(AbstractContainerMenu menu) {
        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            if (!COLLECTOR_ITEM.equalsIgnoreCase(name)) continue;

            return readCollector(stack);
        }
        return false;
    }

    private static boolean readCollector(ItemStack stack) {
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

        collectorMaxFuel = max;

        boolean refreshed = stored != lastReadStoredFuel;
        if (refreshed) {
            long previousStored = collectorStoredFuel;
            boolean withinWindow = System.currentTimeMillis() - openedAtMillis <= ABSORB_WINDOW_MS;
            if (withinWindow && stored >= 0 && stored < previousStored) {
                absorbedFuel += previousStored - stored;
            }

            lastReadStoredFuel = stored;
            collectorStoredFuel = stored;
        }

        if (efficiency != null && efficiency != ConfigManager.config.plasma.collectorEfficiencyPercent) {
            ConfigManager.config.plasma.collectorEfficiencyPercent = efficiency;
            MineshaftTycoonUtils.configManager.saveConfig();
        }

        return refreshed;
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

    public static void clearAbsorbedFuel() {
        absorbedFuel = 0;
    }

    public static boolean isCollectorKnown() {
        return collectorStoredFuel >= 0 && collectorMaxFuel >= 0;
    }

    private static long getActualStored() {
        return Math.max(0, collectorStoredFuel - 1);
    }

    public static long getCollectorRoom() {
        if (!isCollectorKnown()) return -1;
        return Math.max(0, collectorMaxFuel - getActualStored());
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

        return Math.max(0, (maxHeat1 + maxHeat2) - (heat1 + heat2) - absorbedFuel);
    }

    public static long computeTarget() {
        long heatGoal = computeHeatGoal();
        if (heatGoal < 0) return -1;

        if (!isCollectorKnown()) return heatGoal;

        long needed = Math.max(0, heatGoal - getActualStored());
        return Math.min(needed, getCollectorRoom());
    }

    public static long getRefuelSecondsLeft() {
        long first = PlasmaSmitheryTracker.getGen1DepletionMillis();
        long second = PlasmaSmitheryTracker.getGen2DepletionMillis();
        if (first < 0 || second < 0) return -1;

        long stored = isCollectorKnown() ? getActualStored() : 0;
        long runOutAt = Math.min(first, second) + (stored + absorbedFuel) * PlasmaSmitheryTracker.MILLIS_PER_FUEL;

        return Math.max(0, (runOutAt - System.currentTimeMillis()) / 1000);
    }

    private static boolean isSuggestable(PlasmaCategory.FlareEntry entry) {
        return entry != PlasmaCategory.FlareEntry.ETERNAL_SINGULARITY;
    }

    public static Map<PlasmaCategory.FlareEntry, Long> computeFlareDonation(long target) {
        Map<PlasmaCategory.FlareEntry, Long> result = new LinkedHashMap<>();
        if (target <= 0) return result;

        long remaining = target;
        for (PlasmaCategory.FlareEntry entry : PlasmaCategory.FlareEntry.values()) {
            if (!isSuggestable(entry)) continue;

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

    public static Map<PlasmaCategory.FlareEntry, Long> computeWithdrawal(long amount) {
        PlasmaCategory.FlareEntry[] entries = PlasmaCategory.FlareEntry.values();
        long[] values = new long[entries.length];
        long[] available = new long[entries.length];

        for (int i = 0; i < entries.length; i++) {
            values[i] = FlareValueTracker.getEffectiveFuelValue(entries[i]);
            available[i] = isSuggestable(entries[i]) ? FlareValueTracker.getQuantity(entries[i]) : 0;
        }

        String key = amount + ":" + Arrays.toString(values) + ":" + Arrays.toString(available);
        if (key.equals(withdrawalCacheKey)) return withdrawalCache;

        Map<PlasmaCategory.FlareEntry, Long> result = solveWithdrawal(entries, values, available, amount);
        withdrawalCacheKey = key;
        withdrawalCache = result;
        return result;
    }

    private static Map<PlasmaCategory.FlareEntry, Long> solveWithdrawal(
            PlasmaCategory.FlareEntry[] entries, long[] values, long[] available, long amount) {
        Map<PlasmaCategory.FlareEntry, Long> result = new LinkedHashMap<>();
        if (amount <= 0) return result;

        long totalValue = 0;
        for (int i = 0; i < entries.length; i++) {
            if (values[i] > 0) totalValue += values[i] * available[i];
        }

        if (totalValue <= amount) {
            for (int i = 0; i < entries.length; i++) {
                if (values[i] > 0 && available[i] > 0) result.put(entries[i], available[i]);
            }
            return result;
        }

        if (amount > DP_LIMIT) {
            long remaining = amount;
            for (int i = entries.length - 1; i >= 0; i--) {
                if (values[i] <= 0) continue;
                long take = Math.min(available[i], remaining / values[i]);
                if (take > 0) {
                    result.put(entries[i], take);
                    remaining -= take * values[i];
                }
            }
            return result;
        }

        int cap = (int) amount;
        List<int[]> pieces = new ArrayList<>();
        for (int i = 0; i < entries.length; i++) {
            if (values[i] <= 0 || available[i] <= 0) continue;

            long left = Math.min(available[i], cap / values[i]);
            for (long chunk = 1; left > 0; chunk <<= 1) {
                long take = Math.min(chunk, left);
                pieces.add(new int[]{i, (int) take, (int) (take * values[i])});
                left -= take;
            }
        }

        int unreachable = Integer.MAX_VALUE;
        int[] minCount = new int[cap + 1];
        Arrays.fill(minCount, unreachable);
        minCount[0] = 0;
        boolean[][] chosen = new boolean[pieces.size()][cap + 1];

        for (int p = 0; p < pieces.size(); p++) {
            int count = pieces.get(p)[1];
            int weight = pieces.get(p)[2];
            for (int c = cap; c >= weight; c--) {
                int previous = minCount[c - weight];
                if (previous != unreachable && previous + count < minCount[c]) {
                    minCount[c] = previous + count;
                    chosen[p][c] = true;
                }
            }
        }

        int best = cap;
        while (best > 0 && minCount[best] == unreachable) best--;

        long[] taken = new long[entries.length];
        int c = best;
        for (int p = pieces.size() - 1; p >= 0; p--) {
            if (c > 0 && chosen[p][c]) {
                taken[pieces.get(p)[0]] += pieces.get(p)[1];
                c -= pieces.get(p)[2];
            }
        }

        for (int i = 0; i < entries.length; i++) {
            if (taken[i] > 0) result.put(entries[i], taken[i]);
        }

        return result;
    }

    public static void reset() {
        collectorStoredFuel = -1;
        collectorMaxFuel = -1;
        lastReadStoredFuel = -1;
        lastInventoryValue = -1;
        absorbedFuel = 0;
        openedAtMillis = 0;
        open = false;
        manualHeatGoalOverride = -1;
        withdrawalCacheKey = null;
        withdrawalCache = new LinkedHashMap<>();
    }
}