package com.roboo.mineshafttycoonutils.utils;

import com.roboo.mineshafttycoonutils.features.tablist.TabListEntryParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TabListDebugUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("MineshaftTycoonUtils");
    private static final Minecraft mc = Minecraft.getInstance();

    private record Sample(int sortIndex, String label, String username, String raw) {}

    private TabListDebugUtils() {}

    public static void printTabList() {
        if (mc.player == null || mc.getConnection() == null) {
            send("No active connection to read a tab list from.", 0xFF5555, false);
            return;
        }

        Map<String, Sample> groups = new LinkedHashMap<>();

        for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
            String raw = rawEntry(info);
            TabListEntryParser.Parsed parsed = TabListEntryParser.parse(raw);

            String key;
            int sortIndex;
            if (parsed == null) {
                key = "Unparsed";
                sortIndex = Integer.MAX_VALUE;
            } else if (parsed.tierTag() == null) {
                key = "No Tier";
                sortIndex = Integer.MAX_VALUE - 1;
            } else {
                key = RankTierData.glyphKeyFor(parsed.tierColor(), parsed.tierTag());
                sortIndex = TabListEntryParser.tierSortIndex(parsed);
            }

            String username = parsed != null ? parsed.username() : info.getProfile().name();
            groups.putIfAbsent(key, new Sample(sortIndex, key, username, raw));
        }

        List<Sample> samples = new ArrayList<>(groups.values());
        samples.sort(Comparator.comparingInt(Sample::sortIndex));

        send("Tab List Dump (" + samples.size() + " groups)", 0xFFFF55, true);
        LOGGER.info("Tab List Dump ({} groups)", samples.size());

        if (samples.isEmpty()) {
            send("(no entries)", 0xAAAAAA, false);
            return;
        }

        for (Sample sample : samples) {
            String display = describe(sample.raw());
            send("- " + sample.label() + " | " + sample.username() + " | " + display, 0xAAAAAA, false);
            LOGGER.info("  [{}] {} \"{}\"", sample.label(), sample.username(), display);
        }
    }

    private static String rawEntry(PlayerInfo info) {
        Component name = info.getTabListDisplayName() != null
                ? info.getTabListDisplayName()
                : Component.literal(info.getProfile().name());
        String raw = ComponentTextUtils.formattedText(name);
        if (info.getTeam() == null) return raw;

        String prefix = ComponentTextUtils.formattedText(info.getTeam().getPlayerPrefix());
        String suffix = ComponentTextUtils.formattedText(info.getTeam().getPlayerSuffix());
        return prefix + raw + suffix;
    }

    private static String describe(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '§') {
                sb.append('&');
            } else if (c > 127) {
                sb.append(String.format("\\u%04X", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void send(String text, int color, boolean withPrefix) {
        if (mc.player == null) return;
        Component body = Component.literal(withPrefix ? " " + text : text)
                .withStyle(Style.EMPTY.withColor(color));
        Component message = withPrefix ? SystemMessages.buildPrefix().append(body) : body;
        mc.player.displayClientMessage(message, false);
    }
}