package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.profit.MagmaValueConfig;
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

public class MagmaValueTracker {

    private static final String CONTAINER_TITLE = "Space Ores Bag";

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?i)amount in bag:\\s*([0-9,]*)");

    private static final Map<MagmaValueConfig.Entry, Long> quantities =
            new EnumMap<>(MagmaValueConfig.Entry.class);

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        String title = containerScreen.getTitle().getString().trim();
        if (!CONTAINER_TITLE.equalsIgnoreCase(title)) {
            return;
        }

        readContainer(containerScreen.getMenu());
    }

    private static void readContainer(AbstractContainerMenu menu) {
        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            MagmaValueConfig.Entry entry = fromDisplayName(name);
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

    private static MagmaValueConfig.Entry fromDisplayName(String name) {
        for (MagmaValueConfig.Entry entry : MagmaValueConfig.Entry.values()) {
            if (entry.getDisplayName().equalsIgnoreCase(name)) return entry;
        }
        return null;
    }

    public static long getQuantity(MagmaValueConfig.Entry entry) {
        Long quantity = quantities.get(entry);
        return quantity != null ? quantity : 0;
    }

    public static long getMagmaValue(MagmaValueConfig.Entry entry) {
        return getQuantity(entry) * entry.getMagmaValue();
    }

    public static long getTotalMagma() {
        long total = 0;
        for (MagmaValueConfig.Entry entry : ConfigManager.config.profit.magma.order) {
            total += getMagmaValue(entry);
        }
        return total;
    }

    public static void reset() {
        quantities.clear();
    }
}