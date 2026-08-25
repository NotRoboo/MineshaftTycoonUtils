package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.GlyphCategory;
import com.roboo.mineshafttycoonutils.utils.ComponentTextUtils;
import com.roboo.mineshafttycoonutils.utils.GlyphTextUtils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class NameTagGlyphMixin<T extends Entity> {

    @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
    private void mineshaftUtils$substituteGlyphs(T entity, CallbackInfoReturnable<Component> cir) {
        GlyphCategory.GlyphMode mode = ConfigManager.config.glyph.nametagGlyphs;
        if (!mode.isEnabled()) return;

        Component original = cir.getReturnValue();
        if (original == null) return;

        String raw = ComponentTextUtils.formattedText(original);
        String replaced = GlyphTextUtils.substituteTierTags(raw, mode);
        if (replaced.equals(raw)) return;

        MutableComponent result = Component.literal(replaced);
        cir.setReturnValue(result);
    }
}