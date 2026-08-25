package com.roboo.mineshafttycoonutils.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.pets.PetsHelper;
import com.roboo.mineshafttycoonutils.features.warp.WarpHelper;
import com.roboo.mineshafttycoonutils.utils.EmojiData;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Unique
    private static final String WARP_PREFIX = "/warp ";
    @Unique
    private static final String PETS_PREFIX = "/pets ";
    @Unique
    private static final String PET_PREFIX = "/pet ";
    @Unique
    private static final char EMOJI_TRIGGER = ':';

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
        } else if (ConfigManager.config.general.petsHelperEnabled
                && (lower.startsWith(PETS_PREFIX) || lower.startsWith(PET_PREFIX))) {
            prefix = lower.startsWith(PETS_PREFIX) ? PETS_PREFIX : PET_PREFIX;
            if (cursor < prefix.length()) return;
            matches = PetsHelper.matchingArgs(text.substring(prefix.length(), cursor).toLowerCase(Locale.ROOT));
        } else if (ConfigManager.config.emoji.suggestionsEnabled) {
            int colonIndex = text.lastIndexOf(EMOJI_TRIGGER, cursor - 1);
            if (colonIndex < 0 || colonIndex >= cursor) return;

            String partial = text.substring(colonIndex + 1, cursor);
            if (partial.isEmpty() || !mstu$isShortcodeChars(partial)) return;

            prefix = text.substring(0, colonIndex + 1);
            String lowerPartial = partial.toLowerCase(Locale.ROOT);
            matches = new ArrayList<>();
            for (String shortcode : EmojiData.allShortcodes()) {
                if (shortcode.startsWith(lowerPartial)) matches.add(shortcode + ":");
            }
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

    @Unique
    private static boolean mstu$isShortcodeChars(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') return false;
        }
        return true;
    }
}