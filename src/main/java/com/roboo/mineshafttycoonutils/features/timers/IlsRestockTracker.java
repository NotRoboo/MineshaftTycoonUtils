package com.roboo.mineshafttycoonutils.features.timers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IlsRestockTracker {

    private static final String CONTAINER_TITLE = "Il's Wares";
    private static final String ITEM_NAME = "Il's Restock Timer";
    private static final int READ_DELAY_TICKS = 5;

    private static final Pattern TIME_PATTERN =
            Pattern.compile("(?i)time until restock:\\s*(?:(\\d+)h\\s*)?(?:(\\d+)m\\s*)?(?:(\\d+)s)?");

    private static boolean known = false;
    private static long endTime = -1;

    private static Screen pendingScreen = null;
    private static int pendingTicks = -1;

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
            String title = containerScreen.getTitle().getString().trim();
            if (CONTAINER_TITLE.equalsIgnoreCase(title)) {
                pendingScreen = screen;
                pendingTicks = READ_DELAY_TICKS;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingTicks > 0 && --pendingTicks == 0) {
                if (pendingScreen == client.screen
                        && pendingScreen instanceof AbstractContainerScreen<?> containerScreen) {
                    readContainer(containerScreen.getMenu());
                }
                pendingScreen = null;
            }
        });
    }

    private static void readContainer(AbstractContainerMenu menu) {
        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            if (!ITEM_NAME.equalsIgnoreCase(name)) continue;

            ItemLore lore = stack.get(DataComponents.LORE);
            List<Component> loreLines = lore != null ? lore.lines() : List.of();

            for (Component loreLine : loreLines) {
                String text = ChatFormatting.stripFormatting(loreLine.getString()).trim();
                Matcher m = TIME_PATTERN.matcher(text);
                if (m.find() && (m.group(1) != null || m.group(2) != null || m.group(3) != null)) {
                    int hours = m.group(1) != null ? Integer.parseInt(m.group(1)) : 0;
                    int minutes = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
                    int seconds = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
                    long totalSeconds = hours * 3600L + minutes * 60L + seconds;

                    known = true;
                    endTime = System.currentTimeMillis() + totalSeconds * 1000L;
                    return;
                }
            }
        }
    }

    public static boolean isKnown() {
        return known;
    }

    public static long getSecondsLeft() {
        if (!known || endTime < 0) return -1;
        return Math.max(0, (endTime - System.currentTimeMillis()) / 1000);
    }

    public static void reset() {
        known = false;
        endTime = -1;
        pendingScreen = null;
        pendingTicks = -1;
    }
}