package com.roboo.mineshafttycoonutils.features.timers;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PotionToggleHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final String CONTAINER_TITLE = "Buff Duration Menu";
    private static final int OPEN_DELAY_TICKS = 4;
    private static final int SLOT_RETRY_DELAY_TICKS = 5;
    private static final int MAX_SLOT_RETRIES = 3;
    private static final int VERIFY_DELAY_TICKS = 4;
    private static final int CLOSE_DELAY_TICKS = 2;

    private static final Map<String, BuffTracker.Buff> BUFF_ITEMS = new LinkedHashMap<>();
    static {
        BUFF_ITEMS.put("t4", BuffTracker.Buff.T4_POTION);
        BUFF_ITEMS.put("t3", BuffTracker.Buff.T3_POTION);
        BUFF_ITEMS.put("t2", BuffTracker.Buff.T2_POTION);
        BUFF_ITEMS.put("t1", BuffTracker.Buff.T1_POTION);
        BUFF_ITEMS.put("ironvine", BuffTracker.Buff.IRONVINE);
        BUFF_ITEMS.put("redroot", BuffTracker.Buff.REDROOT);
        BUFF_ITEMS.put("aurora", BuffTracker.Buff.AURORA_FRUIT);
        BUFF_ITEMS.put("squash", BuffTracker.Buff.SQUASH);
        BUFF_ITEMS.put("dustgrain", BuffTracker.Buff.DUSTGRAIN);
        BUFF_ITEMS.put("sunflower", BuffTracker.Buff.SUNFLOWER);
        BUFF_ITEMS.put("fishing", BuffTracker.Buff.FISHING_BUFF);
    }

    private enum Phase {
        NONE,
        WAIT_TO_CLICK,
        WAIT_TO_VERIFY,
        WAIT_TO_CLOSE
    }

    private static BuffTracker.Buff pendingBuff = null;
    private static Phase phase = Phase.NONE;
    private static int ticksRemaining = -1;
    private static int slotRetriesLeft = 0;

    private static final SuggestionProvider<FabricClientCommandSource> SUGGESTIONS =
            (ctx, builder) -> {
                if (ConfigManager.config.general.potsoffCommand) {
                    BUFF_ITEMS.forEach((shortArg, buff) ->
                            builder.suggest(shortArg, new LiteralMessage(buff.itemName)));
                }
                return builder.buildFuture();
            };

    public static List<String> matchingArgs(String startsWith) {
        List<String> result = new ArrayList<>();
        BUFF_ITEMS.keySet().forEach(key -> {
            if (key.startsWith(startsWith)) result.add(key);
        });
        return result;
    }

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("potsoff")
                        .executes(ctx -> {
                            runPotsOff(null);
                            return 1;
                        })
                        .then(ClientCommandManager.argument("type", StringArgumentType.greedyString())
                                .suggests(SUGGESTIONS)
                                .executes(ctx -> {
                                    runPotsOff(StringArgumentType.getString(ctx, "type"));
                                    return 1;
                                })
                        )
                )
        );

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (pendingBuff != null && phase == Phase.NONE && isBuffMenu(screen)) {
                schedule(Phase.WAIT_TO_CLICK, OPEN_DELAY_TICKS);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksRemaining > 0 && --ticksRemaining == 0) {
                runPhase();
            }
        });
    }

    private static boolean isBuffMenu(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;
        return CONTAINER_TITLE.equalsIgnoreCase(screen.getTitle().getString().trim());
    }

    private static void runPotsOff(String target) {
        boolean enabled = ConfigManager.config.general.potsoffCommand;
        String key = target == null ? null : target.toLowerCase(Locale.ROOT).trim();

        BuffTracker.Buff buff = (enabled && key != null) ? BUFF_ITEMS.get(key) : null;

        reset();

        if (enabled && key != null && buff == null) {
            errorMsg("Unknown potion/buff type: " + key);
            return;
        }

        if (buff != null) {
            pendingBuff = buff;
            slotRetriesLeft = MAX_SLOT_RETRIES;
        }

        sendRawCommand();
    }

    private static void sendRawCommand() {
        if (mc.player == null) return;
        mc.player.connection.send(new ServerboundChatCommandPacket("potsoff"));
    }

    private static void schedule(Phase newPhase, int delayTicks) {
        phase = newPhase;
        ticksRemaining = delayTicks;
    }

    private static void runPhase() {
        switch (phase) {
            case WAIT_TO_CLICK -> tryClickBuff();
            case WAIT_TO_VERIFY -> verifyAndReport();
            case WAIT_TO_CLOSE -> confirmClose();
            default -> {}
        }
    }

    private static void tryClickBuff() {
        if (pendingBuff == null || mc.player == null || !isBuffMenu(mc.screen)) {
            reset();
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        Integer slotIndex = findBuffSlot(menu, pendingBuff);

        if (slotIndex != null) {
            clickSlot(slotIndex);
            schedule(Phase.WAIT_TO_VERIFY, VERIFY_DELAY_TICKS);
            return;
        }

        if (slotRetriesLeft-- > 0) {
            schedule(Phase.WAIT_TO_CLICK, SLOT_RETRY_DELAY_TICKS);
        } else {
            errorMsg("Could not find " + pendingBuff.itemName + " in the menu.");
            reset();
        }
    }

    private static void verifyAndReport() {
        if (pendingBuff == null || mc.player == null || !isBuffMenu(mc.screen)) {
            reset();
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        Integer slotIndex = findBuffSlot(menu, pendingBuff);
        Boolean nowEnabled = slotIndex != null ? BuffTracker.readEnabledState(menu.slots.get(slotIndex).getItem()) : null;
        String buffName = pendingBuff.itemName;

        if (nowEnabled != null) {
            reportToggled(buffName, nowEnabled);
        } else {
            errorMsg("Could not read the new state for " + buffName + ".");
        }

        pendingBuff = null;
        closeContainerNow();
        schedule(Phase.WAIT_TO_CLOSE, CLOSE_DELAY_TICKS);
    }

    private static void confirmClose() {
        if (isBuffMenu(mc.screen)) {
            closeContainerNow();
        }
        reset();
    }

    private static Integer findBuffSlot(AbstractContainerMenu menu, BuffTracker.Buff buff) {
        if (menu == null) return null;
        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            if (!buff.itemName.equalsIgnoreCase(name)) continue;
            if (BuffTracker.readEnabledState(stack) == null) continue;

            return slot.index;
        }
        return null;
    }

    private static void closeContainerNow() {
        var player = mc.player;
        if (player != null) {
            player.closeContainer();
        } else {
            mc.setScreen(null);
        }
    }

    private static void clickSlot(int slotId) {
        if (mc.player == null || mc.gameMode == null) return;
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                slotId,
                0,
                ClickType.PICKUP,
                mc.player
        );
    }

    private static void reset() {
        pendingBuff = null;
        phase = Phase.NONE;
        ticksRemaining = -1;
        slotRetriesLeft = 0;
    }

    private static void reportToggled(String buffName, boolean enabled) {
        if (mc.player == null) return;

        MutableComponent message = Component.literal(" " + buffName + " is now ")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA));

        message.append(Component.literal(enabled ? "Enabled" : "Disabled")
                .withStyle(Style.EMPTY.withColor(enabled ? 0x55FF55 : 0xFF5555)));

        mc.execute(() -> mc.player.displayClientMessage(SystemMessages.buildPrefix().append(message), false));
    }

    private static void errorMsg(String text) {
        if (mc.player == null) return;

        MutableComponent message = Component.literal(" " + text)
                .withStyle(Style.EMPTY.withColor(0xFF5555));

        mc.execute(() -> mc.player.displayClientMessage(SystemMessages.buildPrefix().append(message), false));
    }
}