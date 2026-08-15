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

public class Commands {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int DEFAULT_FISHING_HUD_X = 10;
    private static final int DEFAULT_FISHING_HUD_Y = 50;
    private static final int DEFAULT_PROFIT_HUD_X = 10;
    private static final int DEFAULT_PROFIT_HUD_Y = 50;
    private static final int DEFAULT_TIMERS_HUD_X = 70;
    private static final int DEFAULT_TIMERS_HUD_Y = 20;

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
                            msg("Fishing tracker has been reset.");
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("resetprofittracker")
                        .executes(ctx -> {
                            ProfitTracker.clear();
                            OreDropTracker.reset();
                            msg("Profit tracker has been reset.");
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("hudpositionsreset")
                        .executes(ctx -> {
                            ConfigManager.config.fishing.hudX = DEFAULT_FISHING_HUD_X;
                            ConfigManager.config.fishing.hudY = DEFAULT_FISHING_HUD_Y;
                            ConfigManager.config.profit.profitHudX = DEFAULT_PROFIT_HUD_X;
                            ConfigManager.config.profit.profitHudY = DEFAULT_PROFIT_HUD_Y;
                            ConfigManager.config.timers.timersHudX = DEFAULT_TIMERS_HUD_X;
                            ConfigManager.config.timers.timersHudY = DEFAULT_TIMERS_HUD_Y;
                            MineshaftTycoonUtils.configManager.saveConfig();
                            msg("HUD positions have been reset to default.");
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

    private static void msg(String text) {
        if (mc.player != null)
            mc.player.displayClientMessage(SystemMessages.get(text), false);
    }
}