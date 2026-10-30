package com.roboo.mineshafttycoonutils.features.eggvalue;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.misc.HousingVerifier;
import com.roboo.mineshafttycoonutils.utils.HudZones;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EggValueTracker {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int SCAN_INTERVAL_TICKS = 10;

    private static final Pattern COINS_PATTERN = Pattern.compile("(?i)\\+\\s*([0-9,]+)\\s*coins");
    private static final Pattern EGG_WORD_PATTERN = Pattern.compile("(?i)\\begg\\b");

    public record Entry(String displayName, long count, long valueEach) {
        public long total() {
            return count * valueEach;
        }
    }

    private static List<Entry> invEntries = List.of();
    private static long invTotal = 0;
    private static List<Entry> chestEntries = List.of();
    private static long chestTotal = 0;
    private static boolean inChest = false;
    private static boolean active = false;
    private static int ticks = 0;

    private EggValueTracker() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (!ConfigManager.config.misc.eggValue.hudEnabled || mc.player == null || mc.level == null) {
            clear();
            return;
        }
        if (!HousingVerifier.isVerified()) {
            clear();
            return;
        }
        if (++ticks < SCAN_INTERVAL_TICKS) return;
        ticks = 0;

        if (!HudZones.isInEggZone(mc.player.blockPosition())) {
            clear();
            return;
        }

        Inventory inventory = mc.player.getInventory();

        Map<String, long[]> invAcc = new TreeMap<>();
        boolean invHasEgg = false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (addStack(invAcc, inventory.getItem(i))) invHasEgg = true;
        }

        boolean chestOpen = mc.screen instanceof AbstractContainerScreen<?> screen
                && !(screen.getMenu() instanceof InventoryMenu);

        Map<String, long[]> chestAcc = new TreeMap<>();
        boolean chestHasEgg = false;
        if (chestOpen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
            for (Slot slot : screen.getMenu().slots) {
                if (slot.container == inventory) continue;
                if (addStack(chestAcc, slot.getItem())) chestHasEgg = true;
            }
        }

        if (!invHasEgg && !chestHasEgg) {
            clear();
            return;
        }

        invEntries = toEntries(invAcc);
        invTotal = sum(invEntries);
        chestEntries = toEntries(chestAcc);
        chestTotal = sum(chestEntries);
        inChest = chestOpen;
        active = true;
    }

    private static List<Entry> toEntries(Map<String, long[]> acc) {
        List<Entry> result = new ArrayList<>(acc.size());
        for (Map.Entry<String, long[]> e : acc.entrySet()) {
            String key = e.getKey();
            result.add(new Entry(key.substring(0, key.lastIndexOf('\0')), e.getValue()[0], e.getValue()[1]));
        }
        return result;
    }

    private static long sum(List<Entry> list) {
        long total = 0;
        for (Entry entry : list) total += entry.total();
        return total;
    }

    private static boolean addStack(Map<String, long[]> acc, ItemStack stack) {
        if (stack.isEmpty()) return false;

        String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
        if (!name.toLowerCase(Locale.ROOT).contains("egg")) return false;

        long value = readValue(stack);
        if (value < 0) return false;

        String shortName = EGG_WORD_PATTERN.matcher(name).replaceAll("").replaceAll("\\s+", " ").trim();
        if (shortName.isEmpty()) shortName = name;

        long[] slot = acc.computeIfAbsent(shortName + '\0' + value, k -> new long[]{0, value});
        slot[0] += stack.getCount();
        return true;
    }

    private static long readValue(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return -1;

        for (Component line : lore.lines()) {
            String text = ChatFormatting.stripFormatting(line.getString());
            Matcher m = COINS_PATTERN.matcher(text);
            if (!m.find()) continue;
            try {
                return Long.parseLong(m.group(1).replace(",", ""));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private static void clear() {
        if (!active && invEntries.isEmpty() && chestEntries.isEmpty()) return;
        active = false;
        inChest = false;
        invEntries = List.of();
        chestEntries = List.of();
        invTotal = 0;
        chestTotal = 0;
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isInChest() {
        return inChest;
    }

    public static List<Entry> getInvEntries() {
        return invEntries;
    }

    public static long getInvTotal() {
        return invTotal;
    }

    public static List<Entry> getChestEntries() {
        return chestEntries;
    }

    public static long getChestTotal() {
        return chestTotal;
    }
}