package com.roboo.mineshafttycoonutils.features.plasma;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.PlasmaCategory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlareValueTracker {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final String CONTAINER_TITLE = "Space Ores Bag";

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?i)amount in bag:\\s*([0-9,]*)");

    private static final Map<PlasmaCategory.FlareEntry, Long> quantities =
            new EnumMap<>(PlasmaCategory.FlareEntry.class);

    private static final Map<PlasmaCategory.FlareEntry, Long> inventoryQuantities =
            new EnumMap<>(PlasmaCategory.FlareEntry.class);

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (mc.player == null) return;

        Inventory playerInventory = mc.player.getInventory();

        Map<PlasmaCategory.FlareEntry, Long> invCounts = new EnumMap<>(PlasmaCategory.FlareEntry.class);
        countInventory(playerInventory, invCounts);
        inventoryQuantities.clear();
        inventoryQuantities.putAll(invCounts);

        if (mc.screen instanceof AbstractContainerScreen<?> containerScreen) {
            String title = containerScreen.getTitle().getString().trim();
            if (CONTAINER_TITLE.equalsIgnoreCase(title)) {
                readContainer(containerScreen.getMenu(), playerInventory);
            }
        }
    }

    private static void readContainer(AbstractContainerMenu menu, Inventory playerInventory) {
        for (Slot slot : menu.slots) {
            if (slot.container == playerInventory) continue;

            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            PlasmaCategory.FlareEntry entry = PlasmaCategory.FlareEntry.fromFullName(name);
            if (entry == null) continue;

            ItemLore lore = stack.get(DataComponents.LORE);
            List<Component> loreLines = lore != null ? lore.lines() : List.of();

            long quantity = -1;
            for (Component loreLine : loreLines) {
                String text = ChatFormatting.stripFormatting(loreLine.getString()).trim();
                Matcher m = AMOUNT_PATTERN.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).replace(",", "");
                    quantity = raw.isEmpty() ? 0 : Long.parseLong(raw);
                    break;
                }
            }

            if (quantity > 0) {
                quantities.put(entry, quantity);
            } else {
                quantities.remove(entry);
            }
        }
    }

    private static void countInventory(Inventory inventory, Map<PlasmaCategory.FlareEntry, Long> counts) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            PlasmaCategory.FlareEntry entry = PlasmaCategory.FlareEntry.fromFullName(name);
            if (entry == null) continue;

            counts.merge(entry, (long) stack.getCount(), Long::sum);
        }
    }

    public static long getQuantity(PlasmaCategory.FlareEntry entry) {
        Long quantity = quantities.get(entry);
        return quantity != null ? quantity : 0;
    }

    public static long getInventoryQuantity(PlasmaCategory.FlareEntry entry) {
        Long quantity = inventoryQuantities.get(entry);
        return quantity != null ? quantity : 0;
    }

    public static long getEffectiveFuelValue(PlasmaCategory.FlareEntry entry) {
        int efficiencyPercent = ConfigManager.config.plasma.collectorEfficiencyPercent;
        return Math.round(entry.getBaseFuelValue() * (efficiencyPercent / 100.0));
    }

    public static long getValue(PlasmaCategory.FlareEntry entry) {
        return getQuantity(entry) * getEffectiveFuelValue(entry);
    }

    public static long getInventoryValue(PlasmaCategory.FlareEntry entry) {
        return getInventoryQuantity(entry) * getEffectiveFuelValue(entry);
    }

    public static long getTotalValue() {
        long total = 0;
        for (PlasmaCategory.FlareEntry entry : ConfigManager.config.plasma.flareValueOrder) {
            total += getValue(entry);
        }
        return total;
    }

    public static long getTotalInventoryValue() {
        long total = 0;
        for (PlasmaCategory.FlareEntry entry : PlasmaCategory.FlareEntry.values()) {
            total += getInventoryValue(entry);
        }
        return total;
    }

    public static void reset() {
        quantities.clear();
        inventoryQuantities.clear();
    }
}