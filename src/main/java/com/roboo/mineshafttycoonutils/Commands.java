package com.roboo.mineshafttycoonutils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.roboo.mineshafttycoonutils.config.ConfigGuiManager;
import com.roboo.mineshafttycoonutils.features.fishing.FishingTracker;
import com.roboo.mineshafttycoonutils.features.profit.OreDropTracker;
import com.roboo.mineshafttycoonutils.features.profit.ProfitTracker;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Commands {

    private static final Minecraft mc = Minecraft.getInstance();

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
        );
    }

    private static void msg(String text) {
        if (mc.player != null)
            mc.player.displayClientMessage(SystemMessages.get(text), false);
    }
}