package com.roboo.mineshafttycoonutils.features.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ChatCompactor {

    private static final int SCAN_DEPTH = 20;
    private static final int MAX_TRACKED = 100;

    private record Tracked(Component displayed, String key, Component original, int count, long time) {}

    private static final List<Tracked> tracked = new ArrayList<>();

    private ChatCompactor() {}

    public static Component compact(Component incoming, List<GuiMessage> messages, long windowMs) {
        String key = keyOf(incoming);
        if (key == null) return incoming;

        long now = System.currentTimeMillis();
        int limit = Math.min(SCAN_DEPTH, messages.size());

        for (int i = 0; i < limit; i++) {
            Tracked match = findTracked(messages.get(i).content());
            if (match == null || !match.key().equals(key) || now - match.time() > windowMs) continue;

            messages.remove(i);
            tracked.removeIf(entry -> entry == match);

            int count = match.count() + 1;
            Component merged = withCount(match.original(), count);
            ChatMessageCopier.carryOver(match.original(), merged, count);
            remember(new Tracked(merged, key, match.original(), count, now));
            return merged;
        }

        remember(new Tracked(incoming, key, incoming, 1, now));
        return incoming;
    }

    private static String keyOf(Component component) {
        String text = ChatFormatting.stripFormatting(component.getString()).trim();
        if (text.chars().noneMatch(Character::isLetterOrDigit)) return null;
        return text;
    }

    private static Tracked findTracked(Component displayed) {
        for (Tracked entry : tracked) {
            if (entry.displayed() == displayed) return entry;
        }
        return null;
    }

    private static Component withCount(Component original, int count) {
        return Component.empty()
                .append(original)
                .append(Component.literal(" (" + count + ")").withStyle(ChatFormatting.GRAY));
    }

    private static void remember(Tracked entry) {
        tracked.addFirst(entry);
        if (tracked.size() > MAX_TRACKED) {
            tracked.subList(MAX_TRACKED, tracked.size()).clear();
        }
    }
}