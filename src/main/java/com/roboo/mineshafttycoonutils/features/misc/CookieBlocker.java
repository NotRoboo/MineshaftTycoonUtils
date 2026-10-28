package com.roboo.mineshafttycoonutils.features.misc;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CookieBlocker {

    private static final int REQUIRED_SLOT = 4;
    private static final long MESSAGE_COOLDOWN_MS = 1000L;

    private static long lastMessageAt = 0L;

    private CookieBlocker() {}

    public static void init() {
        UseItemCallback.EVENT.register(CookieBlocker::check);
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> check(player, level, hand));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> check(player, level, hand));
    }

    private static InteractionResult check(Player player, Level level, InteractionHand hand) {
        if (!level.isClientSide() || !ConfigManager.config.misc.cookieSlotLock) return InteractionResult.PASS;
        if (!player.getItemInHand(hand).is(Items.COOKIE)) return InteractionResult.PASS;
        if (hand == InteractionHand.MAIN_HAND && player.getInventory().getSelectedSlot() == REQUIRED_SLOT) {
            return InteractionResult.PASS;
        }

        sendMessage(player);
        return InteractionResult.FAIL;
    }

    private static void sendMessage(Player player) {
        long now = System.currentTimeMillis();
        if (now - lastMessageAt < MESSAGE_COOLDOWN_MS) return;
        lastMessageAt = now;

        player.displayClientMessage(SystemMessages.buildPrefix().append(
                Component.literal(" Cookie use blocked. Move it to hotbar slot 5 to use it.")
                        .withStyle(Style.EMPTY.withColor(0xFF5555))), false);
    }
}