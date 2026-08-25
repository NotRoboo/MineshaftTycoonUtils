package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.GlyphCategory;
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

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Unique
    private static List<PlayerInfo> mineshaftUtils$cachedSorted = new ArrayList<>();
    @Unique
    private static long mineshaftUtils$lastSortTime = 0L;
    @Unique
    private static final long mineshaftUtils$SORT_INTERVAL_MS = 500L;

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void mineshaftUtils$rewriteDisplayName(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        if (!ConfigManager.config.glyph.tablistGlyphs.isEnabled()) return;

        TabListEntryParser.Parsed parsed = mineshaftUtils$parse(info);
        if (parsed == null) return;
        if (parsed.rankTag() == null && parsed.tierTag() == null) return;

        MutableComponent result = Component.empty();
        boolean first = true;

        for (GlyphCategory.TabListPart part : ConfigManager.config.glyph.tabListPartOrder) {
            String segment = switch (part) {
                case RANK -> mineshaftUtils$rankSegment(parsed);
                case TIER -> mineshaftUtils$tierSegment(parsed);
                case USERNAME -> mineshaftUtils$nameColor(info, parsed) + parsed.username();
            };

            if (segment == null) continue;
            if (!first) result.append(Component.literal(" "));
            result.append(Component.literal(segment));
            first = false;
        }

        cir.setReturnValue(result);
    }

    @Inject(method = "getPlayerInfos", at = @At("RETURN"), cancellable = true)
    private void mineshaftUtils$forceSort(CallbackInfoReturnable<List<PlayerInfo>> cir) {
        if (!ConfigManager.config.misc.forceTabListSort) return;

        List<PlayerInfo> source = cir.getReturnValue();
        if (source == null) return;

        long now = System.currentTimeMillis();
        if (now - mineshaftUtils$lastSortTime >= mineshaftUtils$SORT_INTERVAL_MS
                || mineshaftUtils$cachedSorted.size() != source.size()) {
            List<PlayerInfo> resorted = new ArrayList<>(source);
            resorted.sort(mineshaftUtils$comparator());
            mineshaftUtils$cachedSorted = resorted;
            mineshaftUtils$lastSortTime = now;
        }

        cir.setReturnValue(mineshaftUtils$cachedSorted);
    }

    @Unique
    private static String mineshaftUtils$rankSegment(TabListEntryParser.Parsed parsed) {
        if (parsed.rankTag() == null) return null;
        String color = parsed.rankColor() != ' ' ? "§" + parsed.rankColor() : "§7";
        return color + "[" + parsed.rankTag() + "]";
    }

    @Unique
    private static String mineshaftUtils$tierSegment(TabListEntryParser.Parsed parsed) {
        if (parsed.tierTag() == null) return null;
        GlyphCategory.GlyphMode mode = ConfigManager.config.glyph.tablistGlyphs;
        String glyphKey = RankTierData.glyphKeyFor(parsed.tierColor(), parsed.tierTag());
        String glyph = RankTierData.glyphFor(glyphKey, mode);
        if (glyph != null) return "§f" + glyph;
        return "§" + parsed.tierColor() + "[" + parsed.tierTag() + "]";
    }

    @Unique
    private static String mineshaftUtils$nameColor(PlayerInfo info, TabListEntryParser.Parsed parsed) {
        if (info.getTeam() != null && info.getTeam().getColor() != null && info.getTeam().getColor().isColor()) {
            return "§" + info.getTeam().getColor().getChar();
        }
        if (parsed.usernameColorCode() != null) return parsed.usernameColorCode();
        return "§7";
    }

    @Unique
    private static String mineshaftUtils$withTeamAffixes(PlayerInfo info, Component name) {
        String raw = ComponentTextUtils.formattedText(name);
        if (info.getTeam() == null) return raw;

        String prefix = ComponentTextUtils.formattedText(info.getTeam().getPlayerPrefix());
        String suffix = ComponentTextUtils.formattedText(info.getTeam().getPlayerSuffix());
        return prefix + raw + suffix;
    }

    @Unique
    private static TabListEntryParser.Parsed mineshaftUtils$parse(PlayerInfo info) {
        Component name = info.getTabListDisplayName() != null
                ? info.getTabListDisplayName()
                : Component.literal(info.getProfile().name());
        String raw = mineshaftUtils$withTeamAffixes(info, name);
        return TabListEntryParser.parse(raw);
    }

    @Unique
    private Comparator<PlayerInfo> mineshaftUtils$comparator() {
        return Comparator
                .comparingInt((PlayerInfo info) -> TabListEntryParser.tierSortIndex(mineshaftUtils$parse(info)))
                .thenComparingInt(info -> TabListEntryParser.resolveHypixelRank(mineshaftUtils$parse(info)).ordinal())
                .thenComparing(info -> info.getProfile().name(), String.CASE_INSENSITIVE_ORDER);
    }
}