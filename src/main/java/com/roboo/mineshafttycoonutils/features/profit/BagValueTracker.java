package com.roboo.mineshafttycoonutils.features.profit;

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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BagValueTracker {

    private static final String CONTAINER_TITLE = "Bag";

    private static final Pattern QUANTITY_PATTERN =
            Pattern.compile("(?i)quantity:\\s*([0-9,]*)");

    private static final Map<TrackedOre, Long> quantities =
            new EnumMap<>(TrackedOre.class);

    private static boolean bagOpen = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            bagOpen = false;
            return;
        }

        String title = containerScreen.getTitle().getString().trim();
        if (!CONTAINER_TITLE.equalsIgnoreCase(title)) {
            bagOpen = false;
            return;
        }

        bagOpen = true;
        readContainer(containerScreen.getMenu());
    }

    private static void readContainer(AbstractContainerMenu menu) {
        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            TrackedOre ore = TrackedOre.fromBagItemName(name);
            if (ore == null) continue;

            ItemLore lore = stack.get(DataComponents.LORE);
            List<Component> loreLines = lore != null ? lore.lines() : List.of();

            long quantity = -1;
            for (Component loreLine : loreLines) {
                String text = ChatFormatting.stripFormatting(loreLine.getString()).trim();
                Matcher m = QUANTITY_PATTERN.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).replace(",", "");
                    quantity = raw.isEmpty() ? 0 : Long.parseLong(raw);
                    break;
                }
            }

            if (quantity > 0) {
                quantities.put(ore, quantity);
            } else {
                quantities.remove(ore);
            }
        }
    }

    public static boolean isBagOpen() {
        return bagOpen;
    }

    public static long getQuantity(TrackedOre ore) {
        Long quantity = quantities.get(ore);
        return quantity != null ? quantity : 0;
    }

    public static long getValue(TrackedOre ore) {
        long quantity = getQuantity(ore);
        if (quantity <= 0) return 0;

        long dropValue = ore.getDropValue();
        if (dropValue < 0) return 0;

        return quantity * dropValue;
    }

    public static long getTotalValue() {
        long total = 0;
        for (TrackedOre ore : TrackedOre.values()) {
            total += getValue(ore);
        }
        return total;
    }

    public static void reset() {
        quantities.clear();
        bagOpen = false;
    }
}