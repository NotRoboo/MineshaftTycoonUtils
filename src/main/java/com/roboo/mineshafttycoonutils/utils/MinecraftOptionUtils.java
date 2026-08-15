package com.roboo.mineshafttycoonutils.utils;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class MinecraftOptionUtils {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final double PIXEL_ART_LINE_SPACING = 0.15;

    private static boolean lastPixelArtTags = false;
    private static boolean lastToggleAttackKeyDown = false;
    private static boolean lastToggleUseKeyDown = false;

    public static void init() {
        lastPixelArtTags = ConfigManager.config.playerMessages.glyph.playerMessageGlyphs;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            handleChatLineSpacing();
            handleKeybinds();
        });
    }

    private static void handleChatLineSpacing() {
        var cfg = ConfigManager.config.playerMessages;
        boolean enabled = cfg.glyph.playerMessageGlyphs;
        if (enabled == lastPixelArtTags) return;
        lastPixelArtTags = enabled;

        if (enabled) {
            cfg.savedChatLineSpacing = mc.options.chatLineSpacing().get();
            mc.options.chatLineSpacing().set(PIXEL_ART_LINE_SPACING);
        } else if (cfg.savedChatLineSpacing >= 0.0) {
            mc.options.chatLineSpacing().set(cfg.savedChatLineSpacing);
            cfg.savedChatLineSpacing = -1.0;
        }

        mc.options.save();
        MineshaftTycoonUtils.configManager.saveConfig();
    }

    private static void handleKeybinds() {
        var misc = ConfigManager.config.misc;

        boolean attackDown = isKeyDown(misc.toggleAttackKeybind);
        if (attackDown && !lastToggleAttackKeyDown) {
            boolean newState = !mc.options.toggleAttack().get();
            mc.options.toggleAttack().set(newState);
            mc.options.save();
            msg("Toggle Left Click: " + (newState ? "Enabled" : "Disabled"));
        }
        lastToggleAttackKeyDown = attackDown;

        boolean useDown = isKeyDown(misc.toggleUseKeybind);
        if (useDown && !lastToggleUseKeyDown) {
            boolean newState = !mc.options.toggleUse().get();
            mc.options.toggleUse().set(newState);
            mc.options.save();
            msg("Toggle Right Click: " + (newState ? "Enabled" : "Disabled"));
        }
        lastToggleUseKeyDown = useDown;
    }

    private static void msg(String text) {
        if (mc.player != null) {
            mc.player.displayClientMessage(SystemMessages.get(text), false);
        }
    }

    private static boolean isKeyDown(int glfwKey) {
        if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) return false;
        long handle = GLFW.glfwGetCurrentContext();
        if (handle == 0L) return false;
        return GLFW.glfwGetKey(handle, glfwKey) == GLFW.GLFW_PRESS;
    }
}