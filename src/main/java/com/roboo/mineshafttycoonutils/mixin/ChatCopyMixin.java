package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.features.chat.ChatMessageCopier;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatCopyMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mineshaftUtils$copyMessage(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (ChatMessageCopier.tryCopy(event)) {
            cir.setReturnValue(true);
        }
    }
}