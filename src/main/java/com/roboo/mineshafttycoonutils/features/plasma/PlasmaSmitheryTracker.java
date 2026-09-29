package com.roboo.mineshafttycoonutils.features.plasma;

import com.roboo.mineshafttycoonutils.utils.TimeParseUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlasmaSmitheryTracker {

    public static final long MILLIS_PER_FUEL = 12_000L;
    private static final long DEFAULT_MIN_HEAT = 0;

    private static final Minecraft mc = Minecraft.getInstance();
    private static final String CONTAINER_TITLE = "Plasma Smithery";
    private static final String TIME_LEFT_MARKER = "time left:";

    private static final String GEN1_HEAT_ITEM = "Plasma Generator 1 Heat";
    private static final String GEN2_HEAT_ITEM = "Plasma Generator 2 Heat";
    private static final String GEN1_ITEM = "Plasma Generator 1";
    private static final String GEN2_ITEM = "Plasma Generator 2";

    private static final Pattern MAX_HEAT_PATTERN = Pattern.compile("(?i)^max heat:\\s*([0-9,]+)");
    private static final Pattern MIN_HEAT_PATTERN = Pattern.compile("(?i)^minimum heat:\\s*([0-9,]+)");
    private static final Pattern HEAT_PATTERN = Pattern.compile("(?i)^heat:\\s*([0-9,]+)");

    private record TimeReading(long secondsAtRead, long readAtMillis) {}

    private static long heat1 = -1;
    private static long maxHeat1 = -1;
    private static long minHeat1 = -1;
    private static long heat2 = -1;
    private static long maxHeat2 = -1;
    private static long minHeat2 = -1;
    private static long heat1ReadAt = -1;
    private static long heat2ReadAt = -1;

    private static TimeReading gen1Time = null;
    private static TimeReading gen2Time = null;

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;

            String title = containerScreen.getTitle().getString().trim();
            if (CONTAINER_TITLE.equalsIgnoreCase(title)) {
                SolarFlareTracker.clearManualHeatGoal();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (mc.player == null || !(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) return;

        String title = containerScreen.getTitle().getString().trim();
        if (!CONTAINER_TITLE.equalsIgnoreCase(title)) return;

        readContainer(containerScreen.getMenu());
    }

    private static void readContainer(AbstractContainerMenu menu) {
        boolean foundGen1Heat = false;
        boolean foundGen2Heat = false;
        boolean foundGen1Time = false;
        boolean foundGen2Time = false;

        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();

            if (!foundGen1Heat && GEN1_HEAT_ITEM.equalsIgnoreCase(name)) {
                long[] values = readHeatValues(stack);
                heat1 = values[0];
                maxHeat1 = values[1];
                minHeat1 = values[2];
                heat1ReadAt = System.currentTimeMillis();
                SolarFlareTracker.clearAbsorbedFuel();
                foundGen1Heat = true;
            } else if (!foundGen2Heat && GEN2_HEAT_ITEM.equalsIgnoreCase(name)) {
                long[] values = readHeatValues(stack);
                heat2 = values[0];
                maxHeat2 = values[1];
                minHeat2 = values[2];
                heat2ReadAt = System.currentTimeMillis();
                SolarFlareTracker.clearAbsorbedFuel();
                foundGen2Heat = true;
            } else if (!foundGen1Time && GEN1_ITEM.equalsIgnoreCase(name)) {
                gen1Time = readTimeLeft(stack);
                foundGen1Time = true;
            } else if (!foundGen2Time && GEN2_ITEM.equalsIgnoreCase(name)) {
                gen2Time = readTimeLeft(stack);
                foundGen2Time = true;
            }

            if (foundGen1Heat && foundGen2Heat && foundGen1Time && foundGen2Time) break;
        }
    }

    private static long[] readHeatValues(ItemStack stack) {
        long heat = -1;
        long maxHeat = -1;
        long minHeat = -1;

        for (String text : loreLines(stack)) {
            Matcher maxMatch = MAX_HEAT_PATTERN.matcher(text);
            if (maxMatch.find()) {
                maxHeat = parseNumber(maxMatch.group(1));
                continue;
            }

            Matcher minMatch = MIN_HEAT_PATTERN.matcher(text);
            if (minMatch.find()) {
                minHeat = parseNumber(minMatch.group(1));
                continue;
            }

            Matcher heatMatch = HEAT_PATTERN.matcher(text);
            if (heatMatch.find()) {
                heat = parseNumber(heatMatch.group(1));
            }
        }

        return new long[]{heat, maxHeat, minHeat};
    }

    private static TimeReading readTimeLeft(ItemStack stack) {
        for (String text : loreLines(stack)) {
            int marker = text.toLowerCase(Locale.ROOT).indexOf(TIME_LEFT_MARKER);
            if (marker < 0) continue;

            long seconds = TimeParseUtils.parseSeconds(text.substring(marker + TIME_LEFT_MARKER.length()));
            if (seconds >= 0) return new TimeReading(seconds, System.currentTimeMillis());
        }
        return null;
    }

    private static List<String> loreLines(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return List.of();

        List<String> lines = new ArrayList<>();
        for (Component line : lore.lines()) {
            lines.add(ChatFormatting.stripFormatting(line.getString()).trim());
        }
        return lines;
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

    public static long getHeat1() { return heat1; }
    public static long getMaxHeat1() { return maxHeat1; }
    public static long getHeat2() { return heat2; }
    public static long getMaxHeat2() { return maxHeat2; }

    public static long getGen1DepletionMillis() { return depletionMillis(heat1, minHeat1, heat1ReadAt); }
    public static long getGen2DepletionMillis() { return depletionMillis(heat2, minHeat2, heat2ReadAt); }

    private static long depletionMillis(long heat, long minHeat, long readAt) {
        if (heat < 0 || readAt < 0) return -1;
        long floor = minHeat >= 0 ? minHeat : DEFAULT_MIN_HEAT;
        return readAt + Math.max(0, heat - floor) * MILLIS_PER_FUEL;
    }

    public static long getGen1SecondsLeft() { return secondsLeft(gen1Time); }
    public static long getGen2SecondsLeft() { return secondsLeft(gen2Time); }

    public static boolean isGen1TimeKnown() { return gen1Time != null; }
    public static boolean isGen2TimeKnown() { return gen2Time != null; }

    private static long secondsLeft(TimeReading reading) {
        if (reading == null) return -1;
        long elapsed = (System.currentTimeMillis() - reading.readAtMillis()) / 1000;
        return Math.max(0, reading.secondsAtRead() - elapsed);
    }

    public static void reset() {
        heat1 = -1;
        maxHeat1 = -1;
        minHeat1 = -1;
        heat2 = -1;
        maxHeat2 = -1;
        minHeat2 = -1;
        heat1ReadAt = -1;
        heat2ReadAt = -1;
        gen1Time = null;
        gen2Time = null;
    }
}