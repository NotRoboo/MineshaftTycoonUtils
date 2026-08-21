package com.roboo.mineshafttycoonutils.features.profit;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.PetStatusUtils;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RefineryPetTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final String REFINERY_CONTAINER_NAME = "Refinery";
    private static final String PETS_CONTAINER_NAME = "Pets";
    private static final Pattern REFINERY_ITEM_PATTERN =
            Pattern.compile("(?i)^Refined (.+?)\\s*\\[(\\d{1,2})/10]$");

    private static final Pattern DUNE_RAM_PATTERN =
            Pattern.compile("(?i)^\\[Lvl (\\d{1,3})]\\s*Dune Ram$");

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;

            String title = containerScreen.getTitle().getString().trim();

            if (REFINERY_CONTAINER_NAME.equalsIgnoreCase(title)) {
                ScreenEvents.remove(screen).register(s -> grabRefineryLvl(containerScreen.getMenu()));
            } else if (PETS_CONTAINER_NAME.equalsIgnoreCase(title)) {
                ScreenEvents.remove(screen).register(s -> grabDuneRamLvl(containerScreen.getMenu()));
            }
        });
    }

    private static void grabRefineryLvl(AbstractContainerMenu menu) {
        var state = ConfigManager.config.profit.tracker.state;
        boolean changed = false;

        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            Matcher refineryLvl = REFINERY_ITEM_PATTERN.matcher(name);
            if (!refineryLvl.matches()) continue;

            String oreKey = refineryLvl.group(1).trim().toLowerCase(Locale.ROOT).replace(" ", "");
            int level = Integer.parseInt(refineryLvl.group(2));

            switch (oreKey) {
                case "basalt" -> changed |= updateLvl(level, state.basaltLevel, v -> state.basaltLevel = v);
                case "brecca" -> changed |= updateLvl(level, state.breccaLevel, v -> state.breccaLevel = v);
                case "regolith" -> changed |= updateLvl(level, state.regolithLevel, v -> state.regolithLevel = v);
                case "amberrock" -> changed |= updateLvl(level, state.amberRockLevel, v -> state.amberRockLevel = v);
                case "ambercrystal" -> changed |= updateLvl(level, state.amberCrystalLevel, v -> state.amberCrystalLevel = v);
                case "cosmicfiber" -> changed |= updateLvl(level, state.cosmicFiberLevel, v -> state.cosmicFiberLevel = v);
                case "crimsonplasma" -> changed |= updateLvl(level, state.crimsonPlasmaLevel, v -> state.crimsonPlasmaLevel = v);
                default -> {}
            }
        }

        if (changed) {
            MineshaftTycoonUtils.configManager.saveConfig();
            updateLvlMsg("Refinery level");
        }
    }

    private static void grabDuneRamLvl(AbstractContainerMenu menu) {
        var state = ConfigManager.config.profit.tracker.state;

        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            Matcher duneRam = DUNE_RAM_PATTERN.matcher(name);
            if (!duneRam.matches()) continue;

            int level = PetStatusUtils.resolveStatus(stack) == PetStatusUtils.Status.LOCKED
                    ? 0
                    : Integer.parseInt(duneRam.group(1));

            if (level != state.duneRamLevel) {
                state.duneRamLevel = level;
                MineshaftTycoonUtils.configManager.saveConfig();
                updateLvlMsg("Ram level");
            }
            return;
        }
    }

    private static boolean updateLvl(int newValue, int oldValue, java.util.function.IntConsumer setter) {
        if (newValue == oldValue) return false;
        setter.accept(newValue);
        return true;
    }

    private static void updateLvlMsg(String label) {
        if (mc.player == null) return;

        MutableComponent message = Component.literal(" " + label + " ")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA));

        message.append(Component.literal("Updated!")
                .withStyle(Style.EMPTY.withColor(0x55FF55)));

        mc.execute(() -> mc.player.displayClientMessage(SystemMessages.get().append(message), false));
    }
}