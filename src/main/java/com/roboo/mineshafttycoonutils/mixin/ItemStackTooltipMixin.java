package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.ComponentTextUtils;
import com.roboo.mineshafttycoonutils.utils.LoreNumberUtils;
import com.roboo.mineshafttycoonutils.utils.LoreTimeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackTooltipMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void mineshaftUtils$rewriteLoreNumbers(CallbackInfoReturnable<List<Component>> cir) {
        boolean numbersEnabled = ConfigManager.config.itemLore.shortenLoreNumbers;
        boolean timeEnabled = ConfigManager.config.itemLore.shortenLoreTime;
        if (!numbersEnabled && !timeEnabled) return;

        List<Component> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;

        List<Component> replaced = new ArrayList<>(original.size());
        boolean changed = false;

        for (Component line : original) {
            String raw = ComponentTextUtils.formattedText(line);
            String result = raw;

            if (numbersEnabled) result = LoreNumberUtils.shortenLargeNumbers(result);
            if (timeEnabled) result = LoreTimeUtils.shortenSeconds(result);

            if (result.equals(raw)) {
                replaced.add(line);
            } else {
                replaced.add(mineshaftUtils$legacyToComponent(result));
                changed = true;
            }
        }

        if (changed) cir.setReturnValue(replaced);
    }

    @Unique
    private static Component mineshaftUtils$legacyToComponent(String legacyText) {
        MutableComponent result = Component.empty();
        Style style = Style.EMPTY;

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < legacyText.length(); i++) {
            char c = legacyText.charAt(i);
            if (c == '§' && i + 1 < legacyText.length()) {
                if (!buffer.isEmpty()) {
                    result.append(Component.literal(buffer.toString()).setStyle(style));
                    buffer.setLength(0);
                }
                ChatFormatting formatting = ChatFormatting.getByCode(legacyText.charAt(i + 1));
                if (formatting != null) {
                    style = formatting.isColor() ? Style.EMPTY.withColor(formatting) : style.applyFormat(formatting);
                }
                i++;
            } else {
                buffer.append(c);
            }
        }
        if (!buffer.isEmpty()) {
            result.append(Component.literal(buffer.toString()).setStyle(style));
        }

        return result;
    }
}