package com.roboo.mineshafttycoonutils.features.timers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetAdTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double PETAD_X = 4;
    private static final double PETAD_Y = 93;
    private static final double PETAD_Z = 10;
    private static final double NEARBY_RANGE = 32.0;
    private static final double HOLOGRAM_SEARCH_RADIUS = 5;
    private static final long ANCHOR_THRESHOLD_MS = 1500;
    private static final int SCAN_INTERVAL_TICKS = 10;

    private static final Pattern TIMER_NOW_PATTERN =
            Pattern.compile("(?i)adventure timer?\\s*is now\\s*(?:(\\d+)h\\s*)?(?:(\\d+)m\\s*)?(?:(\\d+)s)?");

    private static final Pattern CHAT_PATTERN =
            Pattern.compile("(?i)adventure time left:\\s*(?:(\\d+)h\\s*)?(?:(\\d+)m\\s*)?(?:(\\d+)s)?");

    private static final Pattern HOLOGRAM_PATTERN =
            Pattern.compile("(?i)adventure time:\\s*(?:(\\d+)h\\s*)?(?:(\\d+)m\\s*)?(?:(\\d+)s)?");

    private static boolean known = false;
    private static long endTime = -1;
    private static int lastSeenSeconds = -1;
    private static int tickCounter = 0;

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            handleMessage(msg.getString());
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> {
            handleMessage(msg.getString());
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    // Check if petad is inactive
    private static void handleMessage(String msg) {
        if (msg == null) return;
        String stripped = msg.replaceAll("§.", "").trim();
        String lower = stripped.toLowerCase(Locale.ROOT);

        if (lower.contains("no pet adventure active")) {
            applyReading(0);
            return;
        }

        if (lower.contains("is back from its adventure") && lower.contains("meet me to pick it up")) {
            applyReading(0);
            return;
        }

        Matcher advTime = CHAT_PATTERN.matcher(stripped);
        if (advTime.find() && lower.contains("adventure time left:")) {
            applyReading(parseSeconds(advTime));
            return;
        }

        if (lower.contains("adventure time") && lower.contains("is now")) {
            Matcher nowMatcher = TIMER_NOW_PATTERN.matcher(stripped);
            if (nowMatcher.find()) {
                applyReading(parseSeconds(nowMatcher));
            }
        }
    }

    // read petad time from hologram
    private static void onTick() {
        if (mc.player == null || mc.level == null) return;

        double distSq = mc.player.position().distanceToSqr(PETAD_X, PETAD_Y, PETAD_Z);
        if (distSq > NEARBY_RANGE * NEARBY_RANGE) {
            tickCounter = 0;
            return;
        }

        if (++tickCounter < SCAN_INTERVAL_TICKS) return;
        tickCounter = 0;

        AABB box = new AABB(
                PETAD_X - HOLOGRAM_SEARCH_RADIUS, PETAD_Y - HOLOGRAM_SEARCH_RADIUS, PETAD_Z - HOLOGRAM_SEARCH_RADIUS,
                PETAD_X + HOLOGRAM_SEARCH_RADIUS, PETAD_Y + HOLOGRAM_SEARCH_RADIUS, PETAD_Z + HOLOGRAM_SEARCH_RADIUS
        );
        for (ArmorStand stand : mc.level.getEntitiesOfClass(ArmorStand.class, box,
                s -> s.isInvisible() && s.hasCustomName())) {
            String name = cleanName(stand);
            String lower = name.toLowerCase(Locale.ROOT);
            if (!lower.contains("adventure time:")) continue;

            Matcher hologramMatcher = HOLOGRAM_PATTERN.matcher(name);
            if (hologramMatcher.find()) {
                applyReading(parseSeconds(hologramMatcher));
                break;
            }
        }
    }

    private static int parseSeconds(Matcher time) {
        int hours = time.group(1) != null ? Integer.parseInt(time.group(1)) : 0;
        int minutes = time.group(2) != null ? Integer.parseInt(time.group(2)) : 0;
        int seconds = time.group(3) != null ? Integer.parseInt(time.group(3)) : 0;
        return hours * 3600 + minutes * 60 + seconds;
    }

    private static void applyReading(int totalSeconds) {
        known = true;
        long projectedEnd = System.currentTimeMillis() + totalSeconds * 1000L;

        if (totalSeconds != lastSeenSeconds || endTime < 0
                || Math.abs(projectedEnd - endTime) > ANCHOR_THRESHOLD_MS) {
            endTime = projectedEnd;
            lastSeenSeconds = totalSeconds;
        }
    }

    private static String cleanName(ArmorStand stand) {
        var custom = stand.getCustomName();
        if (custom == null) return "";
        return custom.getString().replaceAll("§[0-9a-fk-or]", "").trim();
    }

    public static boolean isKnown() {
        return known;
    }

    public static long getSecondsLeft() {
        if (!known || endTime < 0) return -1;
        return Math.max(0, (endTime - System.currentTimeMillis()) / 1000);
    }

    public static void reset() {
        known = false;
        endTime = -1;
        lastSeenSeconds = -1;
        tickCounter = 0;
    }
}