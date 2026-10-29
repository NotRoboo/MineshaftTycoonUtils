package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.chat.ChatCompact;
import com.roboo.mineshafttycoonutils.features.chat.ChatCompactor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatCompactMixin {

    @Shadow @Final private List<GuiMessage> allMessages;

    @Shadow protected abstract void refreshTrimmedMessages();

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true)
    private Component mineshaftUtils$compactDuplicates(Component component) {
        ChatCompact cfg = ConfigManager.config.chat.compact;
        if (!cfg.enabled) return component;

        int before = this.allMessages.size();
        Component result = ChatCompactor.compact(component, this.allMessages, cfg.windowSeconds * 1000L);
        if (this.allMessages.size() != before) {
            this.refreshTrimmedMessages();
        }
        return result;
    }
}