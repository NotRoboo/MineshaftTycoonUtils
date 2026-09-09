package com.roboo.mineshafttycoonutils.features.pve;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.HudZones;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PvETracker {

    private static final long LAST_KILL_WINDOW_MS = 100L;

    private static final Pattern KILL_PATTERN =
            Pattern.compile("(?i)^\\*\\s*\\[system]:\\s*you defeated\\s*⚔(.+?)⚔!$");

    private static final Pattern PET_DOUBLE_KILL_PATTERN =
            Pattern.compile("(?i)^\\*\\s*pets!\\s*you killed this mob twice due to your bone dragon pet!$");

    private static final Map<String, String> CHAT_NAME_TO_DISPLAY = new LinkedHashMap<>();
    static {
        CHAT_NAME_TO_DISPLAY.put("celestial emperor", "Emperor");
        CHAT_NAME_TO_DISPLAY.put("galactic terror", "Terror");
        CHAT_NAME_TO_DISPLAY.put("space pirate", "Pirate");
        CHAT_NAME_TO_DISPLAY.put("stardust wizard", "Wizard");
        CHAT_NAME_TO_DISPLAY.put("corruptonaut", "Corruptonaut");
    }

    private static String lastKilledMob = null;
    private static long lastKillAtMillis = -1;

    private static String lastIntervalMob = null;
    private static long lastKillEventMillis = -1;
    private static double totalIntervalMillis = 0;
    private static long intervalSamples = 0;

    private static boolean paused = true;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());

        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            handleMessage(msg.getString());
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> {
            handleMessage(msg.getString());
            return true;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetKillsPerHour());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetKillsPerHour());
    }

    private static void handleMessage(String msg) {
        if (msg == null) return;
        String stripped = msg.replaceAll("§.", "").trim();
        if (stripped.isEmpty()) return;

        if (PET_DOUBLE_KILL_PATTERN.matcher(stripped).matches()) {
            registerPetProcKill();
            return;
        }

        // TODO: Implement mob drops tracking

        Matcher m = KILL_PATTERN.matcher(stripped);
        if (m.matches()) {
            String display = CHAT_NAME_TO_DISPLAY.get(m.group(1).trim().toLowerCase(Locale.ROOT));
            if (display != null) registerKill(display);
        }
    }

    private static void registerKill(String displayName) {
        addKill(displayName);

        long now = System.currentTimeMillis();

        if (!displayName.equals(lastIntervalMob)) {
            totalIntervalMillis = 0;
            intervalSamples = 0;
            lastKillEventMillis = -1;
            lastIntervalMob = displayName;
        }

        if (lastKillEventMillis > 0) {
            totalIntervalMillis += (now - lastKillEventMillis);
            intervalSamples++;
        }
        lastKillEventMillis = now;

        lastKilledMob = displayName;
        lastKillAtMillis = now;
    }

    private static void registerPetProcKill() {
        if (lastKilledMob == null) return;
        if (System.currentTimeMillis() - lastKillAtMillis > LAST_KILL_WINDOW_MS) return;

        addKill(lastKilledMob);
    }

    private static void addKill(String displayName) {
        ConfigManager.config.pve.kills.merge(displayName, 1, Integer::sum);
        MineshaftTycoonUtils.configManager.saveConfig();
    }

    private static void onTick() {
        Minecraft mc = Minecraft.getInstance();
        paused = mc.player == null || !HudZones.isInPveZone(mc.player.blockPosition());
    }

    public static int getKills(String displayName) {
        Integer count = ConfigManager.config.pve.kills.get(displayName);
        return count != null ? count : 0;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static long getKillsPerHour() {
        if (intervalSamples <= 0) return 0;

        double avgIntervalMillis = totalIntervalMillis / intervalSamples;
        if (avgIntervalMillis <= 0) return 0;

        return Math.round(3600000.0 / avgIntervalMillis);
    }

    public static void resetAll() {
        ConfigManager.config.pve.kills.clear();
        resetKillsPerHour();
        lastKilledMob = null;
        lastKillAtMillis = -1;
        MineshaftTycoonUtils.configManager.saveConfig();
    }

    public static void resetKillsPerHour() {
        paused = true;
        lastIntervalMob = null;
        lastKillEventMillis = -1;
        totalIntervalMillis = 0;
        intervalSamples = 0;
    }

    public static void resetMob(String displayName) {
        ConfigManager.config.pve.kills.remove(displayName);
        if (displayName.equals(lastKilledMob)) {
            lastKilledMob = null;
            lastKillAtMillis = -1;
        }
        MineshaftTycoonUtils.configManager.saveConfig();
    }
}