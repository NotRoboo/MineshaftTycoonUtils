package com.roboo.mineshafttycoonutils.features.timers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;
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

public class BuffTracker {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final String CONTAINER_TITLE = "Buff Duration Menu";
    private static final String MENU_BUTTON_MARKER = "Menu Button";

    private static final Pattern TIME_LEFT_PATTERN =
            Pattern.compile("(?i)time left:\\s*([0-9,]+)s");

    public enum Buff {
        T4_POTION("T4 Fortune Potion"),
        T3_POTION("T3 Fortune Potion"),
        T2_POTION("T2 Fortune Potion"),
        T1_POTION("T1 Fortune Potion"),
        IRONVINE("Ironvine"),
        REDROOT("Redroot"),
        AURORA_FRUIT("Aurora Fruit"),
        SQUASH("Andromeda Squash"),
        DUSTGRAIN("Dustgrain"),
        SUNFLOWER("Sunflower Seed"),
        FISHING_BUFF("Active Fishing Buff");

        final String itemName;

        Buff(String itemName) {
            this.itemName = itemName;
        }
    }

    private static final List<Buff> POTION_PRIORITY = List.of(
            Buff.T4_POTION, Buff.T3_POTION, Buff.T2_POTION, Buff.T1_POTION
    );

    private record Reading(boolean enabled, long secondsAtRead, long readAt) {}

    private static final Map<Buff, Reading> readings = new LinkedHashMap<>();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) return;

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
            Buff buff = fromItemName(name);
            if (buff == null) continue;

            ItemLore lore = stack.get(DataComponents.LORE);
            List<Component> loreLines = lore != null ? lore.lines() : List.of();

            boolean isMenuButton = false;
            boolean enabledFound = false;
            boolean enabled = false;
            boolean timeFound = false;
            long secondsLeft = 0;

            for (Component loreLine : loreLines) {
                String text = ChatFormatting.stripFormatting(loreLine.getString()).trim();

                if (text.contains(MENU_BUTTON_MARKER)) {
                    isMenuButton = true;
                }

                if (text.contains("ENABLED!")) {
                    enabled = true;
                    enabledFound = true;
                } else if (text.contains("DISABLED!")) {
                    enabled = false;
                    enabledFound = true;
                }

                Matcher timeLeft = TIME_LEFT_PATTERN.matcher(text);
                if (timeLeft.find()) {
                    try {
                        secondsLeft = Long.parseLong(timeLeft.group(1).replace(",", ""));
                        timeFound = true;
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (!isMenuButton) continue;

            if (timeFound) {
                readings.put(buff, new Reading(enabledFound && enabled, secondsLeft, now));
            } else {
                readings.remove(buff);
            }
        }
    }

    private static Buff fromItemName(String name) {
        for (Buff buff : Buff.values()) {
            if (buff.itemName.equalsIgnoreCase(name)) return buff;
        }
        return null;
    }

    public static boolean isEnabled(Buff buff) {
        Reading r = readings.get(buff);
        return r != null && r.enabled();
    }

    public static long getSecondsLeft(Buff buff) {
        Reading r = readings.get(buff);
        if (r == null) return 0;
        if (!r.enabled()) return r.secondsAtRead();
        long elapsed = (System.currentTimeMillis() - r.readAt()) / 1000;
        return Math.max(0, r.secondsAtRead() - elapsed);
    }

    public static long getPotionSecondsLeft(Buff buff) {
        Reading target = readings.get(buff);
        if (target == null) return 0;
        if (!target.enabled()) return target.secondsAtRead();

        long now = System.currentTimeMillis();
        long cumulativeStart = -1;

        for (Buff tier : POTION_PRIORITY) {
            Reading r = readings.get(tier);
            if (r == null || !r.enabled() || r.secondsAtRead() <= 0) continue;

            if (cumulativeStart < 0) cumulativeStart = r.readAt();
            long expiry = cumulativeStart + r.secondsAtRead() * 1000L;

            if (tier == buff) {
                if (now < cumulativeStart) return r.secondsAtRead();
                return Math.max(0, (expiry - now) / 1000);
            }

            cumulativeStart = expiry;
        }

        return target.secondsAtRead();
    }

    public static boolean isPotionActive(Buff buff) {
        Reading target = readings.get(buff);
        if (target == null || !target.enabled()) return false;

        long now = System.currentTimeMillis();
        long cumulativeStart = -1;

        for (Buff tier : POTION_PRIORITY) {
            Reading r = readings.get(tier);
            if (r == null || !r.enabled() || r.secondsAtRead() <= 0) continue;

            if (cumulativeStart < 0) cumulativeStart = r.readAt();
            long expiry = cumulativeStart + r.secondsAtRead() * 1000L;

            if (tier == buff) {
                return now >= cumulativeStart && now < expiry;
            }

            cumulativeStart = expiry;
        }

        return false;
    }

    public static void reset() {
        readings.clear();
    }
}