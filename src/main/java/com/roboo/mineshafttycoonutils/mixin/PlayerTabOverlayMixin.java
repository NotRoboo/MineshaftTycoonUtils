package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.features.tablist.TabListEntryParser;
import com.roboo.mineshafttycoonutils.utils.ComponentTextUtils;
import com.roboo.mineshafttycoonutils.utils.RankTierData;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * TODO verify both @Inject targets against your decompiled PlayerTabOverlay.
 * In recent Mojang-mapped 1.21.x these are commonly named getNameForDisplay(PlayerInfo)
 * and getPlayerInfos() (used by render(...) to build the sorted visible list),
 * but exact names/signatures can shift between versions — right-click the
 * class in your IDE and pick "Show decompiled" to confirm, then adjust the
 * method strings below if needed.
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Unique
    private static int mineshaftUtils$loggedDisplay = 0;
    @Unique
    private static int mineshaftUtils$loggedSort = 0;

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void mineshaftUtils$rewriteDisplayName(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        if (!ConfigManager.config.playerMessages.glyph.tablistGlyphs) return;

        Component original = cir.getReturnValue();
        if (original == null) return;

        String raw = ComponentTextUtils.formattedText(original);

        if (mineshaftUtils$loggedDisplay < 5 && raw.contains("[")) {
            mineshaftUtils$loggedDisplay++;
            System.out.println("[MTU DEBUG][getNameForDisplay] raw=" + raw.replace("\u00A7", "&"));
        }

        TabListEntryParser.Parsed parsed = TabListEntryParser.parse(raw);
        if (parsed == null || parsed.tierTag() == null) return;

        String glyphKey = RankTierData.glyphKeyFor(parsed.tierColor(), parsed.tierTag());
        String glyph = RankTierData.glyphFor(glyphKey);
        if (glyph == null) return;

        String nameColor = parsed.usernameColorCode() != null ? parsed.usernameColorCode() : "§f";
        MutableComponent result = Component.literal("§f" + glyph + " " + nameColor + parsed.username());
        cir.setReturnValue(result);
    }

    @Inject(method = "getPlayerInfos", at = @At("RETURN"), cancellable = true)
    private void mineshaftUtils$forceSort(CallbackInfoReturnable<List<PlayerInfo>> cir) {
        if (!ConfigManager.config.misc.forceTabListSort) return;
        if (cir.getReturnValue() == null) return;

        List<PlayerInfo> sorted = new ArrayList<>(cir.getReturnValue());
        sorted.sort(mineshaftUtils$comparator());
        cir.setReturnValue(sorted);
    }

    @Unique
    private static TabListEntryParser.Parsed mineshaftUtils$parse(PlayerInfo info) {
        Component name = info.getTabListDisplayName() != null
                ? info.getTabListDisplayName()
                : Component.literal(info.getProfile().name());
        String raw = ComponentTextUtils.formattedText(name);

        if (mineshaftUtils$loggedSort < 5 && (raw.contains("[") || info.getTeam() != null)) {
            mineshaftUtils$loggedSort++;
            String teamInfo = info.getTeam() == null ? "no team" :
                    "team=" + info.getTeam().getName()
                    + " prefix=" + ComponentTextUtils.formattedText(info.getTeam().getPlayerPrefix()).replace("\u00A7", "&")
                    + " suffix=" + ComponentTextUtils.formattedText(info.getTeam().getPlayerSuffix()).replace("\u00A7", "&");
            System.out.println("[MTU DEBUG][sort-path] raw=" + raw.replace("\u00A7", "&") + " | " + teamInfo);
        }

        return TabListEntryParser.parse(raw);
    }

    @Unique
    private Comparator<PlayerInfo> mineshaftUtils$comparator() {
        return Comparator
                .comparingInt((PlayerInfo info) -> TabListEntryParser.tierSortIndex(mineshaftUtils$parse(info)))
                .thenComparingInt(info -> TabListEntryParser.hypixelRank(mineshaftUtils$parse(info)).ordinal())
                .thenComparing(info -> info.getProfile().name(), String.CASE_INSENSITIVE_ORDER);
    }
}