package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.features.lavafishing.PanningHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class TitleMixin {

    @Inject(method = "setTitleText", at = @At("HEAD"), cancellable = true)
    private void mineshaftUtils$onTitleText(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        if (PanningHelper.shouldBlockTitle(packet.text())) {
            ci.cancel();
        }
    }

    @Inject(method = "setSubtitleText", at = @At("HEAD"), cancellable = true)
    private void mineshaftUtils$onSubtitleText(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        Component original = packet.text();
        Component resolved = PanningHelper.resolveSubtitle(original);

        if (resolved == null) {
            ci.cancel();
            return;
        }

        if (resolved != original) {
            ci.cancel();
            Minecraft.getInstance().gui.setSubtitle(resolved);
        }
    }

    @Inject(method = "setTitlesAnimation", at = @At("HEAD"), cancellable = true)
    private void mineshaftUtils$onTitlesAnimation(ClientboundSetTitlesAnimationPacket packet, CallbackInfo ci) {
        if (PanningHelper.shouldBlockTimes()) {
            ci.cancel();
        }
    }
}