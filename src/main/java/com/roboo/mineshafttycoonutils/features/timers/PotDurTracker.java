package com.roboo.mineshafttycoonutils.features.timers;

import com.roboo.mineshafttycoonutils.features.misc.HousingVerifier;
import com.roboo.mineshafttycoonutils.utils.TimeParseUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PotDurTracker {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int JOIN_DELAY_TICKS = 40;
    private static final int BUFF_DELAY_TICKS = 50;
    private static final int IDLE_TICKS = 20;
    private static final int TIMEOUT_TICKS = 100;
    private static final long MIN_INTERVAL_MS = 10_000L;

    private static final Set<BuffTracker.Buff> seenBuffs = EnumSet.noneOf(BuffTracker.Buff.class);
    private static final Set<CompactorTracker.Compactor> seenCompactors = EnumSet.noneOf(CompactorTracker.Compactor.class);

    private static long lastSentAt = 0L;
    private static int queuedTicks = -1;
    private static int expectTicks = 0;
    private static int idleTicks = 0;
    private static boolean inResponse = false;
    private static boolean hiding = false;
    private static boolean sawPaused = false;

    private PotDurTracker() {}

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> overlay || handle(msg.getString()));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> handle(msg.getString()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    }

    public static void onVerified() {
        schedule(JOIN_DELAY_TICKS);
    }

    private static void schedule(int delayTicks) {
        if (queuedTicks > 0) return;

        long cooldownMs = lastSentAt + MIN_INTERVAL_MS - System.currentTimeMillis();
        int cooldownTicks = cooldownMs > 0 ? (int) (cooldownMs / 50L) + 1 : 0;
        queuedTicks = Math.max(delayTicks, cooldownTicks);
    }

    private static void onTick() {
        if (queuedTicks > 0 && --queuedTicks == 0) {
            queuedTicks = -1;
            send();
        }
        if (expectTicks > 0) expectTicks--;
        if (inResponse && --idleTicks <= 0) finish();
    }

    private static void send() {
        if (mc.player == null || !HousingVerifier.isVerified()) return;
        expectTicks = TIMEOUT_TICKS;
        lastSentAt = System.currentTimeMillis();
        mc.player.connection.send(new ServerboundChatCommandPacket("potdur"));
    }

    private static boolean handle(String msg) {
        if (msg == null) return true;

        String s = ChatFormatting.stripFormatting(msg).trim();
        if (s.isEmpty() || s.charAt(0) != '*') {
            finish();
            return true;
        }

        String body = s.substring(1).trim();

        if (body.equalsIgnoreCase("active buffs:")) {
            finish();
            inResponse = true;
            hiding = expectTicks > 0;
            expectTicks = 0;
            idleTicks = IDLE_TICKS;
            return !hiding;
        }

        if (!inResponse) {
            checkTriggers(s);
            return true;
        }

        boolean hide = hiding;
        parseLine(body);
        idleTicks = IDLE_TICKS;

        return !hide;
    }

    private static String valueOf(String body, String key) {
        if (!body.regionMatches(true, 0, key, 0, key.length())) return null;
        return body.substring(key.length()).trim();
    }

    private static String[] units(String text) {
        return text.replace('|', '\n').split("\n");
    }

    private static void parseLine(String body) {
        String potions = valueOf(body, "potions:");
        if (potions != null) {
            parsePotions(potions);
            return;
        }

        String plots = valueOf(body, "greenhouse:");
        if (plots != null) {
            for (String unit : units(plots)) {
                String[] words = unit.trim().split(" ");
                if (words.length < 2 || words[0].length() != 2 || words[0].charAt(0) != 'P' || !words[1].endsWith("%")) continue;
                try {
                    double percent = Double.parseDouble(words[1].replace("%", "").replace(",", ""));
                    GreenhouseTracker.applyPercent(Character.digit(words[0].charAt(1), 10), percent);
                } catch (NumberFormatException ignored) {}
            }
            return;
        }

        String compactors = valueOf(body, "compactor:");
        if (compactors != null) {
            for (String unit : units(compactors)) {
                String[] words = unit.trim().split(" ");
                if (words.length < 2 || words[0].length() != 2 || words[0].charAt(0) != 'C') continue;
                CompactorTracker.Compactor compactor = CompactorTracker.Compactor.fromNumber(Character.digit(words[0].charAt(1), 10));
                long seconds = TimeParseUtils.parseSeconds(words[1]);
                if (compactor == null || seconds < 0) continue;
                CompactorTracker.applyPotdurReading(compactor, seconds);
                seenCompactors.add(compactor);
            }
            return;
        }

        String pet = valueOf(body, "pet adventure:");
        if (pet != null) {
            long seconds = TimeParseUtils.parseSeconds(pet);
            String t = pet.toLowerCase(Locale.ROOT);
            if (seconds >= 0) PetAdTracker.applyReading(seconds);
            else if (t.contains("none") || t.contains("ready") || t.contains("no active")) PetAdTracker.applyReading(0);
            return;
        }

        if (body.toLowerCase(Locale.ROOT).contains("paused buffs")) {
            sawPaused = true;
            return;
        }

        int colon = body.indexOf(':');
        if (colon < 0) return;

        BuffTracker.Buff buff = BuffTracker.fromPotdurName(body.substring(0, colon));
        if (buff == null) return;

        String rest = body.substring(colon + 1).trim();
        int space = rest.indexOf(' ');
        if (space < 0) return;

        long seconds = TimeParseUtils.parseSeconds(rest.substring(space + 1));
        if (seconds >= 0) {
            BuffTracker.applyPotdurBuff(buff, rest.substring(0, space).contains("▶"), seconds);
            seenBuffs.add(buff);
        }
    }

    private static void parsePotions(String rest) {
        Map<BuffTracker.Buff, BuffTracker.PotdurPotion> potions = new EnumMap<>(BuffTracker.Buff.class);
        for (String unit : units(rest)) {
            String[] words = unit.trim().split(" ");
            if (words.length < 3 || words[0].length() != 2 || words[0].charAt(0) != 'T') continue;
            int tier = Character.digit(words[0].charAt(1), 10);
            long seconds = TimeParseUtils.parseSeconds(words[2]);
            if (tier < 1 || tier > 4 || seconds < 0) continue;
            potions.put(BuffTracker.Buff.valueOf("T" + tier + "_POTION"),
                    new BuffTracker.PotdurPotion(seconds, words[1].contains("▶")));
        }
        if (!potions.isEmpty()) BuffTracker.applyPotdurPotions(potions);
    }

    private static void finish() {
        if (!inResponse) return;
        BuffTracker.finishPotdur(seenBuffs, sawPaused);
        CompactorTracker.finishPotdur(seenCompactors);
        inResponse = false;
        hiding = false;
        sawPaused = false;
        seenBuffs.clear();
        seenCompactors.clear();
    }

    private static void checkTriggers(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.contains("your parrot pet's potion echo triggered")
                || lower.contains("* [npc] martian: 1 hour of")
                || lower.contains("* you ate")
                || lower.contains("[compactor]: you began compacting")) {
            schedule(BUFF_DELAY_TICKS);
        }
    }

    private static void reset() {
        queuedTicks = -1;
        expectTicks = 0;
        lastSentAt = 0L;
        inResponse = false;
        hiding = false;
        sawPaused = false;
        seenBuffs.clear();
        seenCompactors.clear();
    }
}