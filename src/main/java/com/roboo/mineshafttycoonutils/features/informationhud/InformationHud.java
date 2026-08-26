package com.roboo.mineshafttycoonutils.features.informationhud;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.InformationCategory;
import com.roboo.mineshafttycoonutils.hud.HudEditorRegistry;
import com.roboo.mineshafttycoonutils.hud.MovableHud;
import com.roboo.mineshafttycoonutils.utils.HudTextUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InformationHud {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int LINE_HEIGHT = 10;
    private static final int HOTBAR_PET_SLOT = 8;

    private static final MovableHud INFO_BOX = new MovableHud() {
        @Override
        public String getDisplayName() {
            return "Information";
        }

        @Override
        public boolean isMasterEnabled() {
            return ConfigManager.config.information.hudEnabled;
        }

        @Override
        public int getX() {
            InformationCategory cfg = ConfigManager.config.information;
            int anchorX = cfg.informationHudX;
            return HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip) ? anchorX - getWidth() : anchorX;
        }

        @Override
        public int getY() {
            return ConfigManager.config.information.informationHudY;
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
        public void setPosition(int x, int y) {
            ConfigManager.config.information.informationHudX = x;
            ConfigManager.config.information.informationHudY = y;
        }

        @Override
        public void render(GuiGraphics graphics) {
            InformationCategory cfg = ConfigManager.config.information;
            drawContent(graphics, cfg.informationHudX, cfg.informationHudY);
        }
    };

    public static void init() {
        HudEditorRegistry.register(INFO_BOX);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mineshafttycoonutils", "information_hud"),
                (graphics, tickCounter) -> {
                    InformationCategory cfg = ConfigManager.config.information;
                    if (mc.player == null || !cfg.hudEnabled) return;

                    List<String> lines = calcLines(cfg);

                    int totalHeight = (lines.size() + 1) * LINE_HEIGHT;

                    int x = HudTextUtils.clampX(cfg.informationHudX);
                    int y = HudTextUtils.clampY(cfg.informationHudY, totalHeight);

                    drawContent(graphics, x, y);
                }
        );
    }

    private static final String TITLE_TEXT = "§lInformation Hud";

    private static void drawContent(GuiGraphics graphics, int anchorX, int y) {
        InformationCategory cfg = ConfigManager.config.information;
        List<String> lines = calcLines(cfg);
        boolean rightAligned = HudTextUtils.isRightAligned(anchorX, cfg.disableRightAlignFlip);

        HudTextUtils.drawLine(graphics, TITLE_TEXT, anchorX, y, rightAligned, HudTextUtils.chromaToArgb(cfg.titleColor));
        int line = 1;
        for (String l : lines) {
            HudTextUtils.drawLine(graphics, l, anchorX, y + (LINE_HEIGHT * line++), rightAligned);
        }
    }

    private static List<String> calcLines(InformationCategory cfg) {
        List<String> lines = new ArrayList<>();
        for (InformationCategory.Entry entry : cfg.order) {
            lines.add(renderEntry(entry));
        }
        return lines;
    }

    private static int calcWidth() {
        int width = mc.font.width(TITLE_TEXT);
        for (String line : calcLines(ConfigManager.config.information)) {
            width = Math.max(width, mc.font.width(line));
        }
        return width;
    }

    private static int calcHeight() {
        List<String> lines = calcLines(ConfigManager.config.information);
        return (lines.size() + 1) * LINE_HEIGHT;
    }

    private static String renderEntry(InformationCategory.Entry entry) {
        return switch (entry) {
            case SPRINT -> renderSprint();
            case LEFT_CLICK -> renderLeftClick();
            case RIGHT_CLICK -> renderRightClick();
            case PET -> renderPet();
        };
    }

    private static String renderSprint() {
        assert mc.player != null;
        return renderToggleLine("Sprint",
                mc.options.toggleSprint().get(),
                mc.options.keySprint.isDown(),
                mc.player.isSprinting());
    }

    private static String renderRightClick() {
        assert mc.player != null;
        return renderToggleLine("Right Click",
                mc.options.toggleUse().get(),
                mc.options.keyUse.isDown(),
                mc.player.isUsingItem());
    }

    private static String renderLeftClick() {
        return renderToggleLine("Left Click",
                mc.options.toggleAttack().get(),
                mc.options.keyAttack.isDown(),
                false);
    }

    private static String renderToggleLine(String label, boolean toggled, boolean keyDown, boolean stateActive) {
        String mode = null;
        boolean active = false;

        if (toggled && (keyDown || stateActive)) {
            active = true;
            mode = "Toggled";
        } else if (keyDown || stateActive) {
            active = true;
            mode = "Holding";
        }

        String status = (active ? "§aON" : "§cOFF") + (mode != null ? " §a(" + mode + ")" : "");
        return "§7" + label + ": " + status;
    }

    private static final java.util.Set<String> NON_PET_SUFFIXED_NAMES = java.util.Set.of(
            "Caspian Tiger"
    );

    private static String renderPet() {
        assert mc.player != null;
        ItemStack stack = mc.player.getInventory().getItem(HOTBAR_PET_SLOT);
        if (stack.isEmpty()) return "§7Pet: §c(None)";
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) return "§7Pet: §c(None)";
        String firstLine = ChatFormatting.stripFormatting(lore.lines().getFirst().getString()).trim();
        if (firstLine.isEmpty()) return "§7Pet: §c(None)";

        boolean isKnownPet = firstLine.toLowerCase(Locale.ROOT).contains("pet")
                || NON_PET_SUFFIXED_NAMES.contains(firstLine);
        if (!isKnownPet) return "§7Pet: §c(None)";

        if (firstLine.endsWith(" Pet")) {
            firstLine = firstLine.substring(0, firstLine.length() - 4).trim();
        }

        return "§7Pet: §e" + firstLine;
    }
}