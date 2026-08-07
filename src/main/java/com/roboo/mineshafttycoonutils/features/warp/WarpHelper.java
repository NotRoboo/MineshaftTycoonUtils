package com.roboo.mineshafttycoonutils.features.warp;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WarpHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    public enum WarpMenu {
        WARP("Warp"),
        SPACE_WARP("Space Warp");

        public final String title;

        WarpMenu(String title) {
            this.title = title;
        }

        public static WarpMenu fromScreen(net.minecraft.client.gui.screens.Screen screen) {
            if (!(screen instanceof AbstractContainerScreen<?>)) return null;
            String normalized = screen.getTitle().getString().trim().toLowerCase(Locale.ROOT);
            for (WarpMenu menu : values()) {
                if (menu.title.toLowerCase(Locale.ROOT).equals(normalized)) {
                    return menu;
                }
            }
            return null;
        }
    }

    public record WarpEntry(String displayName, WarpMenu menu) {}

    private static final Map<String, WarpEntry> WARP_ITEMS = new LinkedHashMap<>();
    static {
        // Warp menu
        WARP_ITEMS.put("gravel", new WarpEntry("Gravel Mine", WarpMenu.WARP));
        WARP_ITEMS.put("coal", new WarpEntry("Coal Mine", WarpMenu.WARP));
        WARP_ITEMS.put("iron", new WarpEntry("Iron Mine", WarpMenu.WARP));
        WARP_ITEMS.put("gold", new WarpEntry("Gold Mine", WarpMenu.WARP));
        WARP_ITEMS.put("lapis", new WarpEntry("Lapis Mine", WarpMenu.WARP));
        WARP_ITEMS.put("emerald", new WarpEntry("Emerald Mine", WarpMenu.WARP));
        WARP_ITEMS.put("diamond", new WarpEntry("Diamond Mine", WarpMenu.WARP));
        WARP_ITEMS.put("sand", new WarpEntry("Sand Mine", WarpMenu.WARP));
        WARP_ITEMS.put("granite", new WarpEntry("Granite Mine", WarpMenu.WARP));
        WARP_ITEMS.put("compactgranite", new WarpEntry("Compact Granite Mine", WarpMenu.WARP));
        WARP_ITEMS.put("terracotta", new WarpEntry("Terracotta Mine", WarpMenu.WARP));
        WARP_ITEMS.put("soulsand", new WarpEntry("Soul Sand Mine", WarpMenu.WARP));
        WARP_ITEMS.put("glowstone", new WarpEntry("Glow Stone Mine", WarpMenu.WARP));
        WARP_ITEMS.put("quartz", new WarpEntry("Quartz Ore Mine", WarpMenu.WARP));
        WARP_ITEMS.put("quartzblock", new WarpEntry("Quartz Block Mine", WarpMenu.WARP));
        WARP_ITEMS.put("erythrite", new WarpEntry("Erythrite Mine", WarpMenu.WARP));
        WARP_ITEMS.put("pve", new WarpEntry("PvE Area", WarpMenu.WARP));
        WARP_ITEMS.put("ice", new WarpEntry("Ice Mine", WarpMenu.WARP));
        WARP_ITEMS.put("coalblock", new WarpEntry("Coal Block Mine", WarpMenu.WARP));
        WARP_ITEMS.put("ironblock", new WarpEntry("Iron Block Mine", WarpMenu.WARP));
        WARP_ITEMS.put("goldblock", new WarpEntry("Gold Block Mine", WarpMenu.WARP));
        WARP_ITEMS.put("castle", new WarpEntry("The Castle", WarpMenu.WARP));
        WARP_ITEMS.put("armorshop", new WarpEntry("Armor Shop", WarpMenu.WARP));
        WarpEntry t4pve = new WarpEntry("T4 PvE Area", WarpMenu.WARP);
        WARP_ITEMS.put("t4pve", t4pve);
        WARP_ITEMS.put("pvet4", t4pve);
        WARP_ITEMS.put("spawn", new WarpEntry("Spawn", WarpMenu.WARP));
        WARP_ITEMS.put("seasons", new WarpEntry("Seasons", WarpMenu.WARP));

        // Space warp menu
        WARP_ITEMS.put("space", new WarpEntry("Space Station", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("moon", new WarpEntry("The Moon", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("basalt", new WarpEntry("Basalt Mine", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("brecca", new WarpEntry("Brecca Crystal Mine", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("refinery", new WarpEntry("Refinery", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("hasty", new WarpEntry("Hasty", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("mars", new WarpEntry("Mars", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("regolith", new WarpEntry("Regolith Mine", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("amberrock", new WarpEntry("Amber Rock Mine", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("greenhouse", new WarpEntry("Greenhouse", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("ambercrystal", new WarpEntry("Amber Crystal Mine", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("pvp", new WarpEntry("PvP Arena", WarpMenu.SPACE_WARP));
        WARP_ITEMS.put("sun", new WarpEntry("The Sun", WarpMenu.SPACE_WARP));
        WarpEntry blacksmith = new WarpEntry("Plasma Blacksmith", WarpMenu.SPACE_WARP);
        WARP_ITEMS.put("blacksmith", blacksmith);
        WARP_ITEMS.put("plasma", blacksmith);
        WARP_ITEMS.put("sunkeeper", new WarpEntry("Sunkeeper", WarpMenu.SPACE_WARP));
        WarpEntry solarFlare = new WarpEntry("Solar Flare Radar", WarpMenu.SPACE_WARP);
        WARP_ITEMS.put("solarflare", solarFlare);
        WARP_ITEMS.put("radar", solarFlare);
        WarpEntry compactor = new WarpEntry("Sun Compactor", WarpMenu.SPACE_WARP);
        WARP_ITEMS.put("compactor", compactor);
        WARP_ITEMS.put("suncompactor", compactor);
        WarpEntry t5pve = new WarpEntry("T5 PvE", WarpMenu.SPACE_WARP);
        WARP_ITEMS.put("t5pve", t5pve);
        WARP_ITEMS.put("pvet5", t5pve);
    }

    // cancel /warp entirely and run a different command.
    private static final Map<String, String> COMMAND_ALIASES = new LinkedHashMap<>();
    static {
        COMMAND_ALIASES.put("forge", "forge");
    }

    private static final String PREVIOUS_PAGE_NAME = "Previous Page";
    private static final int RETRY_DELAY_TICKS = 5;

    private static WarpEntry pendingEntry = null;
    private static int retryTicks = -1;
    private static int retriesLeft = 0;

    private static final SuggestionProvider<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> SUGGESTIONS =
            (ctx, builder) -> {
                if (ConfigManager.config.general.warpHelperEnabled) {
                    WARP_ITEMS.forEach((shortArg, entry) ->
                            builder.suggest(shortArg, new LiteralMessage(entry.displayName())));
                    COMMAND_ALIASES.forEach((shortArg, command) ->
                            builder.suggest(shortArg, new LiteralMessage("/" + command)));
                }
                return builder.buildFuture();
            };

    public static List<String> matchingArgs(String startsWith) {
        List<String> result = new java.util.ArrayList<>();
        WARP_ITEMS.keySet().forEach(key -> {
            if (key.startsWith(startsWith)) result.add(key);
        });
        COMMAND_ALIASES.keySet().forEach(key -> {
            if (key.startsWith(startsWith)) result.add(key);
        });
        return result;
    }

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("warp")
                        .executes(ctx -> {
                            runWarp(null);
                            return 1;
                        })
                        .then(ClientCommandManager.argument("target", StringArgumentType.greedyString())
                                .suggests(SUGGESTIONS)
                                .executes(ctx -> {
                                    runWarp(StringArgumentType.getString(ctx, "target"));
                                    return 1;
                                })
                        )
                )
        );

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (pendingEntry != null && WarpMenu.fromScreen(screen) != null) {
                retryTicks = -1;
                attemptClick();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (retryTicks > 0 && --retryTicks == 0) {
                attemptClick();
            }
        });
    }

    private static void runWarp(String target) {
        boolean enabled = ConfigManager.config.general.warpHelperEnabled;
        String key = target == null ? null : target.toLowerCase(Locale.ROOT).trim();

        if (enabled && key != null && COMMAND_ALIASES.containsKey(key)) {
            pendingEntry = null;
            retryTicks = -1;
            sendRawCommand(COMMAND_ALIASES.get(key));
            return;
        }

        pendingEntry = (enabled && key != null) ? WARP_ITEMS.get(key) : null;
        retriesLeft = 1;
        retryTicks = -1;

        sendRawCommand("warp");
    }

    private static void sendRawCommand(String command) {
        if (mc.player == null) return;
        mc.player.connection.send(new ServerboundChatCommandPacket(command));
    }

    private static void attemptClick() {
        WarpMenu currentMenu = pendingEntry == null ? null : WarpMenu.fromScreen(mc.screen);
        if (currentMenu == null || mc.player == null) {
            pendingEntry = null;
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;

        if (currentMenu == pendingEntry.menu()) {
            Integer targetSlot = findSlot(menu, pendingEntry.displayName());
            if (targetSlot != null) {
                clickSlot(targetSlot, ClickType.PICKUP, 0);
                pendingEntry = null;
                return;
            }
        }

        if (retriesLeft-- > 0) {
            Integer prevPageSlot = findSlot(menu, PREVIOUS_PAGE_NAME);
            if (prevPageSlot != null) clickSlot(prevPageSlot, ClickType.PICKUP, 0);
            retryTicks = RETRY_DELAY_TICKS;
        } else {
            pendingEntry = null;
        }
    }

    private static Integer findSlot(AbstractContainerMenu menu, String displayName) {
        if (menu == null) return null;
        for (var slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).trim();
            if (name.equalsIgnoreCase(displayName)) {
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