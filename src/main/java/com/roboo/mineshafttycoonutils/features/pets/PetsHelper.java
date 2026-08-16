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
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
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
import net.minecraft.world.inventory.Slot;
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

    private static final String PETS_CONTAINER_NAME = "Pets";
    private static final int HOTBAR_PET_SLOT = 8;

    private static final String MARKER_SELECTED = "§3§lUN§6§lLOCKED";
    private static final String MARKER_ON_ADVENTURE = "§2§lUN§6§lLOCKED";

    private static final String CHAT_DENY_1 = "HAHA! No.";
    private static final String CHAT_DENY_2 = "You have not unlocked";

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

    private enum Phase {
        NONE,
        WAIT_TO_SELECT,
        WAIT_TO_VERIFY,
        WAIT_TO_CONFIRM_CLOSE
    }

    private static final int OPEN_DELAY_TICKS = 4;
    private static final int SLOT_RETRY_DELAY_TICKS = 5;
    private static final int MAX_SELECT_RETRIES = 3;
    private static final int VERIFY_POLL_DELAY_TICKS = 4;
    private static final int MAX_VERIFY_POLLS = 4;
    private static final int CLOSE_CONFIRM_DELAY_TICKS = 2;
    private static final int MAX_CLOSE_RETRIES = 3;

    private static PetEntry pendingEntry = null;
    private static Phase phase = Phase.NONE;
    private static int ticksRemaining = -1;
    private static int selectRetriesLeft = 0;
    private static int verifyPollsLeft = 0;
    private static int closeRetriesLeft = 0;

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
            if (pendingEntry != null && phase == Phase.NONE && isPetsMenu(screen)) {
                schedule(Phase.WAIT_TO_SELECT, OPEN_DELAY_TICKS);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksRemaining > 0 && --ticksRemaining == 0) {
                runPhase();
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (phase == Phase.NONE || !isPetsMenu(mc.screen)) return;

            String text = message.getString();
            if (text.contains(CHAT_DENY_1) || text.contains(CHAT_DENY_2)) {
                reset();
                closeContainerNow();
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
        return PETS_CONTAINER_NAME.equalsIgnoreCase(screen.getTitle().getString().trim());
    }

    private static void runPets(String target) {
        boolean enabled = ConfigManager.config.general.petsHelperEnabled;
        String key = target == null ? null : target.toLowerCase(Locale.ROOT).trim();

        PetEntry entry = (enabled && key != null) ? PET_ITEMS.get(key) : null;

        reset();

        if (entry != null) {
            if (isAlreadyEquipped(entry)) {
                return;
            }
            pendingEntry = entry;
            selectRetriesLeft = MAX_SELECT_RETRIES;
            closeRetriesLeft = MAX_CLOSE_RETRIES;
        }

        sendRawCommand();
    }

    private static void sendRawCommand() {
        if (mc.player == null) return;
        mc.player.connection.send(new ServerboundChatCommandPacket("pets"));
    }

    private static void schedule(Phase newPhase, int delayTicks) {
        phase = newPhase;
        ticksRemaining = delayTicks;
    }

    private static void runPhase() {
        switch (phase) {
            case WAIT_TO_SELECT -> trySelectPet();
            case WAIT_TO_VERIFY -> verifySelection();
            case WAIT_TO_CONFIRM_CLOSE -> confirmClose();
            default -> {}
        }
    }

    private static void trySelectPet() {
        if (pendingEntry == null || mc.player == null || !isPetsMenu(mc.screen)) {
            reset();
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        Slot targetSlot = findSlot(menu, pendingEntry.pattern());

        if (targetSlot != null) {
            ItemStack stack = targetSlot.getItem();

            if (loreContains(stack, MARKER_ON_ADVENTURE)) {
                reset();
                closeContainerNow();
                return;
            }

            if (loreContains(stack, MARKER_SELECTED)) {
                requestClose();
                return;
            }

            clickSlot(targetSlot.index, ClickType.PICKUP, 0);
            selectRetriesLeft = 0;
            verifyPollsLeft = MAX_VERIFY_POLLS;
            schedule(Phase.WAIT_TO_VERIFY, VERIFY_POLL_DELAY_TICKS);
            return;
        }

        if (selectRetriesLeft-- > 0) {
            schedule(Phase.WAIT_TO_SELECT, SLOT_RETRY_DELAY_TICKS);
        } else {
            reset();
        }
    }

    private static void verifySelection() {
        if (pendingEntry == null || mc.player == null) {
            reset();
            return;
        }

        if (isAlreadyEquipped(pendingEntry)) {
            requestClose();
            return;
        }

        if (!isPetsMenu(mc.screen)) {
            reset();
            return;
        }

        if (verifyPollsLeft-- > 0) {
            schedule(Phase.WAIT_TO_VERIFY, VERIFY_POLL_DELAY_TICKS);
        } else {
            reset();
        }
    }

    private static void requestClose() {
        pendingEntry = null;
        closeContainerNow();
        schedule(Phase.WAIT_TO_CONFIRM_CLOSE, CLOSE_CONFIRM_DELAY_TICKS);
    }

    private static void confirmClose() {
        if (!isPetsMenu(mc.screen)) {
            reset();
            return;
        }

        if (closeRetriesLeft-- > 0) {
            closeContainerNow();
            schedule(Phase.WAIT_TO_CONFIRM_CLOSE, CLOSE_CONFIRM_DELAY_TICKS);
        } else {
            reset();
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

    private static boolean loreContains(ItemStack stack, String marker) {
        if (stack.isEmpty()) return false;
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return false;

        for (Component line : lore.lines()) {
            if (line.getString().contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private static void closeContainerNow() {
        var player = mc.player;
        if (player != null) {
            player.closeContainer();
        } else {
            mc.setScreen(null);
        }
    }

    private static void reset() {
        pendingEntry = null;
        phase = Phase.NONE;
        ticksRemaining = -1;
    }

    private static Slot findSlot(AbstractContainerMenu menu, Pattern pattern) {
        if (menu == null) return null;
        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            if (pattern.matcher(name).matches()) {
                return slot;
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