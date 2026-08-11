package com.roboo.mineshafttycoonutils.features.pets;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class PetsHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final String PETS_TITLE = "Pets";
    private static final int HOTBAR_PET_SLOT = 8;

    public record PetEntry(String displayName, Pattern pattern) {
        static PetEntry of(String displayName) {

            return new PetEntry(displayName, Pattern.compile("(?i)^\\[Lvl \\d{1,3}]\\s*" + Pattern.quote(displayName) + "$"));
        }
    }

    private static final Map<String, PetEntry> PET_ITEMS = new LinkedHashMap<>();
    static {
        PET_ITEMS.put("slime", PetEntry.of("Slime"));
        PET_ITEMS.put("bee", PetEntry.of("Bee"));
        PET_ITEMS.put("parrot", PetEntry.of("Parrot"));
        PET_ITEMS.put("ocelot", PetEntry.of("Ocelot"));
        PET_ITEMS.put("cyclops", PetEntry.of("Cyclops"));
        PetEntry caspianTiger = PetEntry.of("Caspian Tiger");
        PET_ITEMS.put("capspiantiger", caspianTiger);
        PET_ITEMS.put("tiger", caspianTiger);
        PetEntry fireHydra = PetEntry.of("Fire Hydra");
        PET_ITEMS.put("firehydra", fireHydra);
        PET_ITEMS.put("hydra", fireHydra);
        PET_ITEMS.put("piglin", PetEntry.of("Piglin"));
        PET_ITEMS.put("bonedragon", PetEntry.of("Bone Dragon"));
        PetEntry silverSerpent = PetEntry.of("Silver Serpent");
        PET_ITEMS.put("silverserpent", silverSerpent);
        PET_ITEMS.put("serpent", silverSerpent);
        PET_ITEMS.put("goat", PetEntry.of("Goat"));
        PET_ITEMS.put("seadragon", PetEntry.of("Sea Dragon"));
        PetEntry goblinShark = PetEntry.of("Goblin Shark");
        PET_ITEMS.put("goblinshark", goblinShark);
        PET_ITEMS.put("shark", goblinShark);
        PetEntry duneRam = PetEntry.of("Dune Ram");
        PET_ITEMS.put("duneram", duneRam);
        PET_ITEMS.put("ram", duneRam);
        PetEntry spaceWarrior = PetEntry.of("Space Warrior");
        PET_ITEMS.put("spacewarrior", spaceWarrior);
        PET_ITEMS.put("warrior", spaceWarrior);
        PET_ITEMS.put("raven", PetEntry.of("Raven"));
        PetEntry solarScorpion = PetEntry.of("Solar Scorpion");
        PET_ITEMS.put("solarscorpion", solarScorpion);
        PET_ITEMS.put("scorpion", solarScorpion);
    }

    private static final int RETRY_DELAY_TICKS = 5;
    private static final int MAX_RETRIES = 3;
    private static final int CLOSE_DELAY_TICKS = 3;

    private static PetEntry pendingEntry = null;
    private static int retryTicks = -1;
    private static int retriesLeft = 0;
    private static int closeTicks = -1;

    private static final SuggestionProvider<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> SUGGESTIONS =
            (ctx, builder) -> {
                if (ConfigManager.config.general.petsHelperEnabled) {
                    PET_ITEMS.forEach((shortArg, entry) ->
                            builder.suggest(shortArg, new LiteralMessage(entry.displayName())));
                }
                return builder.buildFuture();
            };

    public static List<String> matchingArgs(String startsWith) {
        List<String> result = new ArrayList<>();
        PET_ITEMS.keySet().forEach(key -> {
            if (key.startsWith(startsWith)) result.add(key);
        });
        return result;
    }

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommand(dispatcher, "pets");
            registerCommand(dispatcher, "pet");
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (pendingEntry != null && isPetsMenu(screen)) {
                retryTicks = -1;
                attemptClick();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (retryTicks > 0 && --retryTicks == 0) {
                attemptClick();
            }
            if (closeTicks > 0 && --closeTicks == 0) {
                if (isPetsMenu(mc.screen)) closeMenu();
            }
        });
    }

    private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, String name) {
        dispatcher.register(ClientCommandManager.literal(name)
                .executes(ctx -> {
                    runPets(null);
                    return 1;
                })
                .then(ClientCommandManager.argument("target", StringArgumentType.greedyString())
                        .suggests(SUGGESTIONS)
                        .executes(ctx -> {
                            runPets(StringArgumentType.getString(ctx, "target"));
                            return 1;
                        })
                )
        );
    }

    private static boolean isPetsMenu(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;
        return PETS_TITLE.equalsIgnoreCase(screen.getTitle().getString().trim());
    }

    private static void runPets(String target) {
        boolean enabled = ConfigManager.config.general.petsHelperEnabled;
        String key = target == null ? null : target.toLowerCase(Locale.ROOT).trim();

        PetEntry entry = (enabled && key != null) ? PET_ITEMS.get(key) : null;

        if (entry != null && isAlreadyEquipped(entry)) {
            pendingEntry = null;
            retriesLeft = 0;
            retryTicks = -1;
            closeTicks = -1;
            return;
        }

        pendingEntry = entry;
        retriesLeft = MAX_RETRIES;
        retryTicks = -1;
        closeTicks = -1;

        sendRawCommand();
    }

    private static void sendRawCommand() {
        if (mc.player == null) return;
        mc.player.connection.send(new ServerboundChatCommandPacket("pets"));
    }

    private static void attemptClick() {
        if (pendingEntry == null || mc.player == null || !isPetsMenu(mc.screen)) {
            pendingEntry = null;
            return;
        }

        if (isAlreadyEquipped(pendingEntry)) {
            pendingEntry = null;
            scheduleClose();
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;

        Integer targetSlot = findSlot(menu, pendingEntry.pattern());
        if (targetSlot != null) {
            clickSlot(targetSlot, ClickType.PICKUP, 0);
            pendingEntry = null;
            scheduleClose();
            return;
        }

        if (retriesLeft-- > 0) {
            retryTicks = RETRY_DELAY_TICKS;
        } else {
            pendingEntry = null;
        }
    }

    private static boolean isAlreadyEquipped(PetEntry entry) {
        if (mc.player == null) return false;
        ItemStack stack = mc.player.getInventory().getItem(HOTBAR_PET_SLOT);
        if (stack.isEmpty()) return false;

        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return false;

        for (Component line : lore.lines()) {
            String text = ChatFormatting.stripFormatting(line.getString());
            if (text.toLowerCase(Locale.ROOT).contains(entry.displayName().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static void scheduleClose() {
        closeTicks = CLOSE_DELAY_TICKS;
    }

    private static void closeMenu() {
        mc.setScreen(null);
    }

    private static Integer findSlot(AbstractContainerMenu menu, Pattern pattern) {
        if (menu == null) return null;
        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            if (pattern.matcher(name).matches()) {
                return slot.index;
            }
        }
        return null;
    }

    public static void clickSlot(int slotId, ClickType actionType, int button) {
        if (mc.player == null || mc.gameMode == null) return;
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                slotId,
                button,
                actionType,
                mc.player
        );
    }
}