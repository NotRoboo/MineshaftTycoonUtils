package com.roboo.mineshafttycoonutils.mixin;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.GlyphCategory;
import com.roboo.mineshafttycoonutils.features.tablist.TabListEntryParser;
import com.roboo.mineshafttycoonutils.utils.OnlinePlayers;
import com.roboo.mineshafttycoonutils.utils.RankTierData;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Unique
    private static List<PlayerInfo> mineshaftUtils$cachedSorted = new ArrayList<>();
    @Unique
    private static long mineshaftUtils$lastSortTime = 0L;
    @Unique
    private static final long mineshaftUtils$SORT_INTERVAL_MS = 500L;
    @Unique
    private static final int mineshaftUtils$MAX_ENTRIES = 80;
    @Unique
    private static int mineshaftUtils$cachedSourceSize = -1;

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void mineshaftUtils$rewriteDisplayName(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        if (!ConfigManager.config.glyph.tablistGlyphs.isEnabled()) return;

        TabListEntryParser.Parsed parsed = OnlinePlayers.parse(info);
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

        List<PlayerInfo> source = OnlinePlayers.all();
        if (source.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (now - mineshaftUtils$lastSortTime >= mineshaftUtils$SORT_INTERVAL_MS
                || mineshaftUtils$cachedSourceSize != source.size()) {
            mineshaftUtils$cachedSorted = OnlinePlayers.sorted().stream()
                    .limit(mineshaftUtils$MAX_ENTRIES)
                    .toList();
            mineshaftUtils$cachedSourceSize = source.size();
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
        PlayerTeam team = info.getTeam();
        if (team != null && team.getColor().isColor()) {
            return "§" + team.getColor().getChar();
        }
        if (parsed.usernameColorCode() != null) return parsed.usernameColorCode();
        return "§7";
    }
}