package com.roboo.mineshafttycoonutils.features.misc;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

public class CommandAliases {

    private static final Minecraft mc = Minecraft.getInstance();

    private CommandAliases() {}

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("lh")
                    .executes(ctx -> {
                        send("lobby housing");
                        return 1;
                    })
            );

            dispatcher.register(ClientCommandManager.literal("vm")
                    .executes(ctx -> {
                        send("visit masterplaysxd");
                        return 1;
                    })
            );

            dispatcher.register(ClientCommandManager.literal("pv")
                    .executes(ctx -> {
                        send("pvs");
                        return 1;
                    })
            );

            dispatcher.register(ClientCommandManager.literal("dur")
                    .executes(ctx -> {
                        send("potdur");
                        return 1;
                    })
            );

            dispatcher.register(ClientCommandManager.literal("po")
                    .executes(ctx -> {
                        send("potsoff");
                        return 1;
                    })
            );
        });
    }

    private static void send(String command) {
        if (mc.player == null) return;
        mc.player.connection.send(new ServerboundChatCommandPacket(command));
    }
}