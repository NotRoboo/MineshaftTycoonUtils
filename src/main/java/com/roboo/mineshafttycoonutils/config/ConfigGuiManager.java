package com.roboo.mineshafttycoonutils.config;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigGuiManager {

    private static final long SEARCH_TIMEOUT_MS = 5_000L;
    private static final long AUTOSAVE_INTERVAL_MS = 5 * 60 * 1000L;

    public static MoulConfigEditor<MSTUConfig> editor = null;
    private static long lastCloseTime = 0L;
    private static long lastSaveTime = 0L;
    private static Screen activeScreen = null;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (activeScreen != null && client.screen != activeScreen) {
                activeScreen = null;
                lastCloseTime = System.currentTimeMillis();
                save();
            }

            long now = System.currentTimeMillis();
            if (now - lastSaveTime >= AUTOSAVE_INTERVAL_MS) {
                save();
            }
        });
    }

    private static void save() {
        MineshaftTycoonUtils.configManager.saveConfig();
        lastSaveTime = System.currentTimeMillis();
    }

    public static void openConfigGui(String search) {
        boolean freshEditor = editor == null;
        if (freshEditor) editor = new MoulConfigEditor<>(MineshaftTycoonUtils.configManager.processor);

        if (search != null) {
            editor.search(search);
        } else if (!freshEditor && System.currentTimeMillis() - lastCloseTime > SEARCH_TIMEOUT_MS) {
            editor.search("");
        }

        MoulConfigScreenComponent screen = new MoulConfigScreenComponent(Component.empty(), new GuiContext(new GuiElementComponent(editor)), null) {
            @Override
            public void onClose() {
                super.onClose();
                lastCloseTime = System.currentTimeMillis();
                save();
                activeScreen = null;
            }
        };

        activeScreen = screen;
        Minecraft.getInstance().setScreen(screen);
    }
}