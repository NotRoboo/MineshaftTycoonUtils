package com.roboo.mineshafttycoonutils.utils;

import com.roboo.mineshafttycoonutils.features.tablist.TabListEntryParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OnlinePlayers {

    private static final Comparator<PlayerInfo> TAB_ORDER = Comparator
            .comparingInt((PlayerInfo info) -> TabListEntryParser.tierSortIndex(parse(info)))
            .thenComparingInt(info -> TabListEntryParser.resolveHypixelRank(parse(info)).ordinal())
            .thenComparing(info -> info.getProfile().name(), String.CASE_INSENSITIVE_ORDER);

    private OnlinePlayers() {}

    public static List<PlayerInfo> all() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return List.of();
        return new ArrayList<>(connection.getListedOnlinePlayers());
    }

    public static List<PlayerInfo> sorted() {
        List<PlayerInfo> players = all();
        players.sort(TAB_ORDER);
        return players;
    }

    public static TabListEntryParser.Parsed parse(PlayerInfo info) {
        Component name = info.getTabListDisplayName() != null
                ? info.getTabListDisplayName()
                : Component.literal(info.getProfile().name());
        return TabListEntryParser.parse(withTeamAffixes(info, name));
    }

    private static String withTeamAffixes(PlayerInfo info, Component name) {
        String raw = ComponentTextUtils.formattedText(name);
        if (info.getTeam() == null) return raw;

        String prefix = ComponentTextUtils.formattedText(info.getTeam().getPlayerPrefix());
        String suffix = ComponentTextUtils.formattedText(info.getTeam().getPlayerSuffix());
        return prefix + raw + suffix;
    }
}