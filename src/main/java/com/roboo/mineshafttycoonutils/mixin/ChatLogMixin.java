package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.utils.LogTextUtils;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatLogMixin {

    @ModifyVariable(method = "logChatMessage", at = @At("STORE"), ordinal = 0, require = 0)
    private static String mstu$restoreLoggedText(String original) {
        return LogTextUtils.restore(original);
    }
}