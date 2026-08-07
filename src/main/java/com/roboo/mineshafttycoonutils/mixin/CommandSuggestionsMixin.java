package com.roboo.mineshafttycoonutils.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.pets.PetsHelper;
import com.roboo.mineshafttycoonutils.features.warp.WarpHelper;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    private static final String WARP_PREFIX = "/warp ";
    private static final String PETS_PREFIX = "/pets ";

    @Shadow @Final EditBox input;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow public abstract void showSuggestions(boolean narrateFirst);

    @Inject(method = "updateCommandInfo", at = @At("TAIL"))
    private void mstu$overrideSuggestions(CallbackInfo ci) {
        String text = this.input.getValue();
        String lower = text.toLowerCase(Locale.ROOT);
        int cursor = this.input.getCursorPosition();

        String prefix;
        List<String> matches;

        if (ConfigManager.config.general.warpHelperEnabled && lower.startsWith(WARP_PREFIX)) {
            prefix = WARP_PREFIX;
            if (cursor < prefix.length()) return;
            matches = WarpHelper.matchingArgs(text.substring(prefix.length(), cursor).toLowerCase(Locale.ROOT));
        } else if (ConfigManager.config.general.petsHelperEnabled && lower.startsWith(PETS_PREFIX)) {
            prefix = PETS_PREFIX;
            if (cursor < prefix.length()) return;
            matches = PetsHelper.matchingArgs(text.substring(prefix.length(), cursor).toLowerCase(Locale.ROOT));
        } else {
            return;
        }

        if (matches.isEmpty()) return;

        SuggestionsBuilder builder = new SuggestionsBuilder(text, prefix.length());
        for (String match : matches) {
            builder.suggest(match);
        }

        this.pendingSuggestions = builder.buildFuture();
        this.showSuggestions(false);
    }
}