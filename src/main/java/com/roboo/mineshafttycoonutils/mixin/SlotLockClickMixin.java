package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.features.misc.SlotLocking;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class SlotLockClickMixin {

    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
    private void mineshaftUtils$blockLockedSlots(int containerId, int slotId, int buttonNum, ClickType clickType,
                                                  Player player, CallbackInfo ci) {
        if (SlotLocking.shouldBlock(containerId, slotId, buttonNum, clickType, player)) {
            ci.cancel();
        }
    }
}
