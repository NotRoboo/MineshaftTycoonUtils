package com.roboo.mineshafttycoonutils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.roboo.mineshafttycoonutils.config.ConfigGuiManager;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.fishing.FishingTracker;
import com.roboo.mineshafttycoonutils.features.profit.OreDropTracker;
import com.roboo.mineshafttycoonutils.features.profit.ProfitTracker;
import com.roboo.mineshafttycoonutils.hud.HudEditScreen;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class Commands {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int DEFAULT_FISHING_HUD_X = 10;
    private static final int DEFAULT_FISHING_HUD_Y = 80;
    private static final int DEFAULT_PROFIT_HUD_X = 10;
    private static final int DEFAULT_PROFIT_HUD_Y = 80;
    private static final int DEFAULT_BAG_VALUE_HUD_X = 200;
    private static final int DEFAULT_BAG_VALUE_HUD_Y = 85;
    private static final int DEFAULT_TIMERS_HUD_X = 180;
    private static final int DEFAULT_TIMERS_HUD_Y = 20;
    private static final int DEFAULT_INFORMATION_HUD_X = 10;
    private static final int DEFAULT_INFORMATION_HUD_Y = 10;

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            register(dispatcher, "mineshafttycoonutils");
            register(dispatcher, "mstutils");
            register(dispatcher, "mstu");
        });
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, String name) {
        dispatcher.register(ClientCommandManager.literal(name)
                .executes(ctx -> {
                    mc.execute(() -> ConfigGuiManager.openConfigGui(null));
                    return 1;
                })
                .then(ClientCommandManager.argument("search", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String search = StringArgumentType.getString(ctx, "search");
                            mc.execute(() -> ConfigGuiManager.openConfigGui(search));
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("resetfishinghud")
                        .executes(ctx -> {
                            FishingTracker.reset();
                            resetMsg("Fishing Tracker");
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("resetprofittracker")
                        .executes(ctx -> {
                            ProfitTracker.resetProfit();
                            OreDropTracker.reset();
                            resetMsg("Profit Tracker");
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("hudpositionsreset")
                        .executes(ctx -> {
                            ConfigManager.config.fishing.hudX = DEFAULT_FISHING_HUD_X;
                            ConfigManager.config.fishing.hudY = DEFAULT_FISHING_HUD_Y;
                            ConfigManager.config.profit.tracker.profitHudX = DEFAULT_PROFIT_HUD_X;
                            ConfigManager.config.profit.tracker.profitHudY = DEFAULT_PROFIT_HUD_Y;
                            ConfigManager.config.profit.bagValue.bagValueHudX = DEFAULT_BAG_VALUE_HUD_X;
                            ConfigManager.config.profit.bagValue.bagValueHudY = DEFAULT_BAG_VALUE_HUD_Y;
                            ConfigManager.config.timers.timersHudX = DEFAULT_TIMERS_HUD_X;
                            ConfigManager.config.timers.timersHudY = DEFAULT_TIMERS_HUD_Y;
                            ConfigManager.config.information.informationHudX = DEFAULT_INFORMATION_HUD_X;
                            ConfigManager.config.information.informationHudY = DEFAULT_INFORMATION_HUD_Y;
                            MineshaftTycoonUtils.configManager.saveConfig();
                            resetMsg("HUD positions");
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("edithud")
                        .executes(ctx -> {
                            mc.execute(HudEditScreen::open);
                            return 1;
                        })
                )
        );
    }

    private static void resetMsg(String label) {
        if (mc.player == null) return;

        MutableComponent message = Component.literal(" " + label + " has been ")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA));

        message.append(Component.literal("Reset!")
                .withStyle(Style.EMPTY.withColor(0xFF5555)));

        mc.player.displayClientMessage(SystemMessages.get().append(message), false);
    }
}