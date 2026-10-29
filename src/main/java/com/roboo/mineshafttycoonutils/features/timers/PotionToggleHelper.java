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
    private static final int CLICK_COOLDOWN_TICKS = 2;
    private static final int MAX_CLICK_WAIT_TICKS = 60;
    private static final int MAX_VERIFY_WAIT_TICKS = 40;
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
        CLICK,
        VERIFY,
        CLOSE
    }

    private static BuffTracker.Buff pendingBuff = null;
    private static Phase phase = Phase.NONE;
    private static int waitedTicks = 0;
    private static int cooldownTicks = 0;
    private static int staleContainerId = Integer.MIN_VALUE;
    private static Boolean stateBeforeClick = null;

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
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

        if (buff != null && mc.player != null) {
            pendingBuff = buff;
            phase = Phase.CLICK;
            staleContainerId = mc.player.containerMenu.containerId;
        }

        sendRawCommand();
    }

    private static void sendRawCommand() {
        if (mc.player == null) return;
        mc.player.connection.send(new ServerboundChatCommandPacket("potsoff"));
    }

    private static void tick() {
        if (phase == Phase.NONE) return;
        if (mc.player == null || mc.gameMode == null) {
            reset();
            return;
        }
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        switch (phase) {
            case CLICK -> tryClickBuff();
            case VERIFY -> verifyAndReport();
            case CLOSE -> confirmClose();
            default -> {}
        }
    }

    private static void tryClickBuff() {
        if (++waitedTicks > MAX_CLICK_WAIT_TICKS) {
            if (isBuffMenu(mc.screen) && pendingBuff != null) {
                errorMsg("Could not find " + pendingBuff.itemName + " in the menu.");
            }
            reset();
            return;
        }
        if (pendingBuff == null || !isBuffMenu(mc.screen)) return;

        var player = mc.player;
        if (player == null) return;

        AbstractContainerMenu menu = player.containerMenu;
        if (menu.containerId == staleContainerId) return;

        Integer slotIndex = findBuffSlot(menu, pendingBuff);
        if (slotIndex == null) return;

        stateBeforeClick = BuffTracker.readEnabledState(menu.slots.get(slotIndex).getItem());
        clickSlot(slotIndex);
        phase = Phase.VERIFY;
        waitedTicks = 0;
        cooldownTicks = CLICK_COOLDOWN_TICKS;
    }

    private static void verifyAndReport() {
        if (pendingBuff == null || !isBuffMenu(mc.screen)) {
            reset();
            return;
        }

        var player = mc.player;
        if (player == null) {
            reset();
            return;
        }

        String buffName = pendingBuff.itemName;
        AbstractContainerMenu menu = player.containerMenu;
        Integer slotIndex = findBuffSlot(menu, pendingBuff);
        Boolean nowEnabled = slotIndex != null ? BuffTracker.readEnabledState(menu.slots.get(slotIndex).getItem()) : null;

        if (nowEnabled != null && !nowEnabled.equals(stateBeforeClick)) {
            reportToggled(buffName, nowEnabled);
            beginClose();
            return;
        }

        if (++waitedTicks > MAX_VERIFY_WAIT_TICKS) {
            errorMsg("Could not read the new state for " + buffName + ".");
            beginClose();
        }
    }

    private static void beginClose() {
        pendingBuff = null;
        closeContainerNow();
        phase = Phase.CLOSE;
        cooldownTicks = CLOSE_DELAY_TICKS;
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
        waitedTicks = 0;
        cooldownTicks = 0;
        staleContainerId = Integer.MIN_VALUE;
        stateBeforeClick = null;
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