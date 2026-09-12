package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.features.lavafishing.PanningHelper;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class SoundPlayMixin {

    @Inject(method = "play", at = @At("HEAD"))
    private void mineshaftUtils$onPlay(SoundInstance soundInstance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        PanningHelper.onSoundPlayed(soundInstance.getIdentifier().getPath());
    }
}