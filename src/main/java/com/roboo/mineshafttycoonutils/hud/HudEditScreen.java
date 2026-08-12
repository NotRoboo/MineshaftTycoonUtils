package com.roboo.mineshafttycoonutils.hud;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class HudEditScreen extends Screen {

    private static final int BOX_PADDING = 3;
    private static final int BORDER_COLOR_HOVERED = 0xFF55FF55;

    private MovableHud dragging = null;
    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditScreen() {
        super(Component.literal("Edit HUD Positions"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new HudEditScreen());
    }

    @Override
    protected void init() {
        HudEditorRegistry.setEditing(true);
    }

    @Override
    public void removed() {
        HudEditorRegistry.setEditing(false);
        MineshaftTycoonUtils.configManager.saveConfig();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (MovableHud hud : HudEditorRegistry.getAll()) {
            if (!hud.isMasterEnabled()) continue;

            int left = hud.getX() - BOX_PADDING;
            int top = hud.getY() - BOX_PADDING;
            int right = hud.getX() + hud.getWidth() + BOX_PADDING;
            int bottom = hud.getY() + hud.getHeight() + BOX_PADDING;

            boolean hovered = hud == dragging || isWithin(mouseX, mouseY, left, top, right, bottom);
            if (hovered) {
                drawBox(graphics, left, top, right, bottom);
            }

            hud.render(graphics);
        }

        String hint = "Drag a HUD to move it - press ESC to save and exit";
        float hintScale = 0.75f;
        int hintColor = 0xFFAAAAAA;

        graphics.pose().pushMatrix();
        graphics.pose().translate(this.width / 2f, this.height / 2f);
        graphics.pose().scale(hintScale, hintScale);
        int hintWidth = this.font.width(hint);
        graphics.drawString(this.font, hint, -hintWidth / 2, -this.font.lineHeight / 2, hintColor, true);
        graphics.pose().popMatrix();
    }

    private void drawBox(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, top + 1, BORDER_COLOR_HOVERED);
        graphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR_HOVERED);
        graphics.fill(left, top, left + 1, bottom, BORDER_COLOR_HOVERED);
        graphics.fill(right - 1, top, right, bottom, BORDER_COLOR_HOVERED);
    }

    private boolean isWithin(int mouseX, int mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0) {
            double mouseX = click.x();
            double mouseY = click.y();

            for (MovableHud hud : HudEditorRegistry.getAll()) {
                if (!hud.isMasterEnabled()) continue;

                int left = hud.getX() - BOX_PADDING;
                int top = hud.getY() - BOX_PADDING;
                int right = hud.getX() + hud.getWidth() + BOX_PADDING;
                int bottom = hud.getY() + hud.getHeight() + BOX_PADDING;

                if (isWithin((int) mouseX, (int) mouseY, left, top, right, bottom)) {
                    dragging = hud;
                    dragOffsetX = mouseX - hud.getX();
                    dragOffsetY = mouseY - hud.getY();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent click, double dragX, double dragY) {
        if (dragging == null) return super.mouseDragged(click, dragX, dragY);

        int newLeft = (int) Math.round(click.x() - dragOffsetX);
        int newTop = (int) Math.round(click.y() - dragOffsetY);

        int anchorX = toAnchorX(newLeft, dragging.getWidth(), this.width);
        dragging.setPosition(anchorX, newTop);
        return true;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent click) {
        if (dragging != null) {
            dragging = null;
            return true;
        }
        return super.mouseReleased(click);
    }

    private static int toAnchorX(int boxLeft, int width, int screenWidth) {
        int center = boxLeft + width / 2;
        return center > screenWidth / 2 ? boxLeft + width : boxLeft;
    }
}