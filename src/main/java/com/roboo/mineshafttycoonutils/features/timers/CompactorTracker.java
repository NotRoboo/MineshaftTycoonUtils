package com.roboo.mineshafttycoonutils.features.timers;

import com.roboo.mineshafttycoonutils.utils.TimeParseUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.EnumMap;
import java.util.Map;

public class CompactorTracker {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final String CONTAINER_TITLE = "Sun Compactor";
    private static final String TIME_MARKER = "➠";

    public enum Compactor {
        FIRST("Compactor #1"),
        SECOND("Compactor #2");

        private final String itemName;

        Compactor(String itemName) {
            this.itemName = itemName;
        }

        static Compactor fromItemName(String name) {
            for (Compactor compactor : values()) {
                if (compactor.itemName.equalsIgnoreCase(name)) return compactor;
            }
            return null;
        }
    }

    private record Reading(long secondsAtRead, long readAtMillis) {}

    private static final Map<Compactor, Reading> readings = new EnumMap<>(Compactor.class);

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
        long now = System.currentTimeMillis();

        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            Compactor compactor = Compactor.fromItemName(name);
            if (compactor == null) continue;

            long seconds = readDuration(stack);
            if (seconds >= 0) {
                readings.put(compactor, new Reading(seconds, now));
            } else {
                readings.remove(compactor);
            }
        }
    }

    private static long readDuration(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return -1;

        for (Component line : lore.lines()) {
            String text = ChatFormatting.stripFormatting(line.getString()).trim();
            int marker = text.indexOf(TIME_MARKER);
            if (marker < 0) continue;

            long parsed = TimeParseUtils.parseSeconds(text.substring(marker + TIME_MARKER.length()));
            if (parsed >= 0) return parsed;
        }

        return -1;
    }

    public static long getSecondsLeft(Compactor compactor) {
        Reading reading = readings.get(compactor);
        if (reading == null) return -1;

        long elapsed = (System.currentTimeMillis() - reading.readAtMillis()) / 1000;
        return Math.max(0, reading.secondsAtRead() - elapsed);
    }

    public static void reset() {
        readings.clear();
    }
}