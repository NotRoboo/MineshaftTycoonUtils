package com.roboo.mineshafttycoonutils.features.eggvalue;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.misc.EggValueConfig;
import com.roboo.mineshafttycoonutils.features.misc.HousingVerifier;
import com.roboo.mineshafttycoonutils.hud.ContainerHudDragHandler;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.HudScale;
import com.roboo.mineshafttycoonutils.hud.MovableHud;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class EggValueHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;
    private static final String TITLE_TEXT = "§lEgg Value";

    private static final ContainerHudDragHandler dragHandler = new ContainerHudDragHandler();

    private static final MovableHud EGG_BOX = new MovableHud() {
        @Override
        public String getDisplayName() {
            return "Egg Value";
        }

        @Override
        public boolean isMasterEnabled() {
            return cfg().hudEnabled && HousingVerifier.isVerified();
        }

        @Override
        public int getX() {
            return cfg().eggHudX;
        }

        @Override
        public int getY() {
            return cfg().eggHudY;
        }

        @Override
        public int getWidth() {
            return calcWidth();
        }

        @Override
        public int getHeight() {
            return calcHeight();
        }

        @Override
        public float getScale() {
            return cfg().eggHudScale;
        }

        @Override
        public void setScale(float scale) {
            cfg().eggHudScale = HudScale.clamp(scale);
        }

        @Override
        public void setPosition(int x, int y) {
            cfg().eggHudX = x;
            cfg().eggHudY = y;
        }

        @Override
        public void render(GuiGraphics graphics) {
            drawContent(graphics, cfg().eggHudX, cfg().eggHudY);
        }

        @Override
        public boolean usesFlip() {
            return false;
        }
    };

    public static void init() {
        HudEditorRegistry.register(EGG_BOX);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "egg_value_hud"),
                (graphics, tickCounter) -> {
                    if (mc.screen instanceof AbstractContainerScreen<?>) return;
                    if (!shouldShow()) return;

                    EggValueConfig cfg = cfg();
                    int x = HudTextUtils.clampX(cfg.eggHudX);
                    int y = HudTextUtils.clampY(cfg.eggHudY, calcHeight());
                    drawContent(graphics, x, y);
                }
        );

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?>)) return;

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> renderOnScreen(graphics));
            ScreenEvents.remove(screen).register(s -> dragHandler.reset());

            dragHandler.registerScroll(
                    screen,
                    () -> cfg().eggHudX,
                    () -> cfg().eggHudY,
                    EggValueHud::calcWidth,
                    EggValueHud::calcHeight,
                    () -> cfg().eggHudScale,
                    scale -> cfg().eggHudScale = scale
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static EggValueConfig cfg() {
        return ConfigManager.config.misc.eggValue;
    }

    private static boolean shouldShow() {
        return mc.player != null
                && cfg().hudEnabled
                && HousingVerifier.isVerified()
                && EggValueTracker.isActive();
    }

    private static void onTick() {
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) {
            dragHandler.reset();
            return;
        }
        if (!shouldShow()) return;

        EggValueConfig cfg = cfg();
        dragHandler.tick(
                ConfigManager.config.general.editBagHudKeybind,
                cfg.eggHudX,
                cfg.eggHudY,
                calcWidth(),
                calcHeight(),
                (x, y) -> {
                    cfg.eggHudX = x;
                    cfg.eggHudY = y;
                },
                () -> MineshaftTycoonUtils.configManager.saveConfig()
        );
    }

    private static void renderOnScreen(GuiGraphics graphics) {
        if (!shouldShow()) return;

        EggValueConfig cfg = cfg();
        dragHandler.drawBoxIfEditing(graphics, cfg.eggHudX, cfg.eggHudY, calcWidth(), calcHeight());
        drawContent(graphics, cfg.eggHudX, cfg.eggHudY);
    }

    private static void drawContent(GuiGraphics graphics, int x, int y) {
        EggValueConfig cfg = cfg();
        List<String> lines = calcLines();
        int titleColor = HudTextUtils.chromaToArgb(cfg.titleColor);
        float scale = HudScale.normalize(cfg.eggHudScale);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);

        HudTextUtils.drawLine(graphics, TITLE_TEXT, 0, 0, false, titleColor);
        int line = 1;
        for (String l : lines) {
            if (!l.isEmpty()) {
                HudTextUtils.drawLine(graphics, l, 0, LINE_HEIGHT * line, false);
            }
            line++;
        }

        graphics.pose().popMatrix();
    }

    private static String entryLine(EggValueTracker.Entry entry) {
        return "§7" + entry.count() + "x " + entry.displayName() + ": §e" + String.format("%,d", entry.total());
    }

    private static List<String> calcLines() {
        List<String> lines = new ArrayList<>();
        if (!EggValueTracker.isActive()) return lines;

        if (EggValueTracker.isInChest()) {
            for (EggValueTracker.Entry entry : EggValueTracker.getChestEntries()) {
                lines.add(entryLine(entry));
            }
            lines.add("§lChest Total: §e" + String.format("%,d", EggValueTracker.getChestTotal()));
            lines.add("");

            for (EggValueTracker.Entry entry : EggValueTracker.getInvEntries()) {
                lines.add(entryLine(entry));
            }
            lines.add("§lInv Total: §e" + String.format("%,d", EggValueTracker.getInvTotal()));
        } else {
            for (EggValueTracker.Entry entry : EggValueTracker.getInvEntries()) {
                lines.add(entryLine(entry));
            }
            lines.add("§lTotal: §e" + String.format("%,d", EggValueTracker.getInvTotal()));
        }

        return lines;
    }

    private static int calcUnscaledWidth(List<String> lines) {
        int width = mc.font.width(TITLE_TEXT);
        for (String line : lines) {
            width = Math.max(width, mc.font.width(line));
        }
        return width;
    }

    private static int calcWidth() {
        return Math.round(calcUnscaledWidth(calcLines()) * HudScale.normalize(cfg().eggHudScale));
    }

    private static int calcHeight() {
        return Math.round((calcLines().size() + 1) * LINE_HEIGHT * HudScale.normalize(cfg().eggHudScale));
    }
}