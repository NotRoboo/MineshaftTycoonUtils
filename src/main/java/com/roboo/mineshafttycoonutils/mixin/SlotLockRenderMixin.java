package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.features.misc.SlotLocking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class SlotLockRenderMixin {

    @Inject(method = "renderDeferredElements", at = @At("HEAD"))
    private void mineshaftUtils$renderLockedSlots(CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            SlotLocking.render(screen, (GuiGraphics) (Object) this);
        }
    }
}