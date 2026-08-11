package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RefineryPetTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final String REFINERY_TITLE = "Refinery";
    private static final String PETS_TITLE = "Pets";

    private static final Pattern REFINERY_ITEM_PATTERN =
            Pattern.compile("(?i)^Refined (.+?)\\s*\\[(\\d{1,2})/10]$");

    private static final Pattern DUNE_RAM_PATTERN =
            Pattern.compile("(?i)^\\[Lvl (\\d{1,3})]\\s*Dune Ram$");

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;

            String title = containerScreen.getTitle().getString().trim();

            if (REFINERY_TITLE.equalsIgnoreCase(title)) {
                ScreenEvents.remove(screen).register(s -> readRefinery(containerScreen.getMenu()));
            } else if (PETS_TITLE.equalsIgnoreCase(title)) {
                ScreenEvents.remove(screen).register(s -> readPets(containerScreen.getMenu()));
            }
        });
    }

    private static void readRefinery(AbstractContainerMenu menu) {
        var state = ConfigManager.config.profit.state;
        boolean changed = false;

        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            Matcher m = REFINERY_ITEM_PATTERN.matcher(name);
            if (!m.matches()) continue;

            String oreKey = m.group(1).trim().toLowerCase(Locale.ROOT).replace(" ", "");
            int level = Integer.parseInt(m.group(2));

            switch (oreKey) {
                case "basalt" -> changed |= apply(level, state.basaltLevel, v -> state.basaltLevel = v);
                case "brecca" -> changed |= apply(level, state.breccaLevel, v -> state.breccaLevel = v);
                case "regolith" -> changed |= apply(level, state.regolithLevel, v -> state.regolithLevel = v);
                case "amberrock" -> changed |= apply(level, state.amberRockLevel, v -> state.amberRockLevel = v);
                case "ambercrystal" -> changed |= apply(level, state.amberCrystalLevel, v -> state.amberCrystalLevel = v);
                case "cosmicfiber" -> changed |= apply(level, state.cosmicFiberLevel, v -> state.cosmicFiberLevel = v);
                case "crimsonplasma" -> changed |= apply(level, state.crimsonPlasmaLevel, v -> state.crimsonPlasmaLevel = v);
                default -> {}
            }
        }

        if (changed) {
            MineshaftTycoonUtils.configManager.saveConfig();
            msg("Refinery level updated.");
        }
    }

    private static void readPets(AbstractContainerMenu menu) {
        var state = ConfigManager.config.profit.state;

        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            Matcher m = DUNE_RAM_PATTERN.matcher(name);
            if (!m.matches()) continue;

            int level = Integer.parseInt(m.group(1));
            if (level != state.duneRamLevel) {
                state.duneRamLevel = level;
                MineshaftTycoonUtils.configManager.saveConfig();
                msg("Ram level updated.");
            }
            return;
        }
    }

    private static boolean apply(int newValue, int oldValue, java.util.function.IntConsumer setter) {
        if (newValue == oldValue) return false;
        setter.accept(newValue);
        return true;
    }

    private static void msg(String text) {
        if (mc.player == null) return;
        mc.execute(() -> mc.player.displayClientMessage(SystemMessages.get(text), false));
    }
}