package com.roboo.mineshafttycoonutils.features.lavafishing;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.HudZones;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.Locale;

public class PanningHelper {

    private static final String LEVEL_UP_MAIN = "LEVEL UP";
    private static final String LEVEL_UP_SUB = "[##]";
    private static final String PANNING_MAIN = "PANNING";
    private static final String FAILED_CLICK_SUB = "You Failed To Click Properly!";
    private static final String PANNING_FAILED_MAIN = "PANNING FAILED";
    private static final String PANNING_FINISHED_MAIN = "FINISHED PANNING";
    private static final String CANNOT_PAN_HERE = "You cannot pan here!";

    private static final String SHAKING_TEXT = "§8>§7>§8> §7Shaking... §8<§7<§8<";
    private static final String RIGHT_CLICK_TEXT = "§6>§e>§6> §eRight Click! §6<§e<§6<";

    private enum Phase { SHAKING, RIGHT_CLICK }

    private static String lastMain = "";
    private static Phase phase = Phase.SHAKING;

    private PanningHelper() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isInactive()) phase = Phase.SHAKING;
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            handleChatMessage(msg.getString());
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, timestamp) -> {
            handleChatMessage(msg.getString());
            return true;
        });
    }

    private static void handleChatMessage(String msg) {
        if (msg == null) return;
        String stripped = ChatFormatting.stripFormatting(msg).trim();
        if (!stripped.contains(CANNOT_PAN_HERE)) return;

        lastMain = "";
        phase = Phase.SHAKING;
        Minecraft.getInstance().gui.clearTitles();
    }

    public static boolean shouldBlockTitle(Component title) {
        String stripped = strip(title);

        if (isInactive()) {
            lastMain = stripped;
            return false;
        }

        boolean wasActive = isActivePanning(lastMain);
        lastMain = stripped;
        boolean isPanningTitle = lastMain.contains(PANNING_MAIN);
        boolean isActive = isActivePanning(lastMain);

        if (!isPanningTitle) {
            phase = Phase.SHAKING;
        } else if (isActive && !wasActive) {
            phase = Phase.SHAKING;
            Minecraft.getInstance().gui.setSubtitle(Component.literal(SHAKING_TEXT));
        }

        return !isPanningTitle;
    }

    public static Component resolveSubtitle(Component subtitle) {
        String stripped = strip(subtitle);

        if (isInactive()) return subtitle;

        boolean isLevelUp = lastMain.contains(LEVEL_UP_MAIN) && stripped.contains(LEVEL_UP_SUB);
        boolean isPanning = lastMain.contains(PANNING_MAIN);

        if (isLevelUp || !isPanning) {
            return null;
        }

        if (lastMain.contains(PANNING_FAILED_MAIN) || lastMain.contains(PANNING_FINISHED_MAIN)) {
            return subtitle;
        }

        if (stripped.contains(FAILED_CLICK_SUB)) {
            return subtitle;
        }

        return null;
    }

    public static boolean shouldBlockTimes() {
        return !isInactive() && !lastMain.contains(PANNING_MAIN);
    }

    public static void onSoundPlayed(String path) {
        if (isInactive()) return;
        if (!isActivePanning(lastMain)) return;

        String lower = path.toLowerCase(Locale.ROOT);
        Phase newPhase = null;

        if (lower.contains("bucket.empty")) {
            newPhase = Phase.RIGHT_CLICK;
        } else if (lower.contains("brush.brushing.sand")) {
            newPhase = Phase.SHAKING;
        }

        if (newPhase == null || newPhase == phase) return;
        phase = newPhase;

        Minecraft.getInstance().gui.setSubtitle(Component.literal(phase == Phase.SHAKING ? SHAKING_TEXT : RIGHT_CLICK_TEXT));
    }

    private static boolean isActivePanning(String main) {
        return main.contains(PANNING_MAIN)
                && !main.contains(PANNING_FINISHED_MAIN)
                && !main.contains(PANNING_FAILED_MAIN);
    }

    private static boolean isInactive() {
        if (!ConfigManager.config.lavaFishing.panning.panningHelperEnabled) return true;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return true;
        if (!HudZones.isInLavaFishingZone(mc.player.blockPosition())) return true;

        return !(mc.player.getMainHandItem().is(Items.BOWL));
    }

    private static String strip(Component component) {
        return ChatFormatting.stripFormatting(component.getString()).trim();
    }
}