package com.roboo.mineshafttycoonutils.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public class ContainerHudDragHandler {

    private static final int BOX_PADDING = 3;
    private static final int BORDER_COLOR_HOVERED = 0xFF55FF55;

    private final Minecraft mc = Minecraft.getInstance();

    private boolean editing = false;
    private boolean dragging = false;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean lastEditKeyDown = false;

    public boolean isEditing() {
        return editing;
    }

    public void reset() {
        editing = false;
        dragging = false;
        lastEditKeyDown = false;
    }

    public void tick(int editKeybind, int x, int y, int width, int height, PositionSetter setter, Runnable onDragEnd) {
        boolean keyDown = isKeyDown(editKeybind);
        if (keyDown && !lastEditKeyDown) {
            editing = !editing;
            dragging = false;
        }
        lastEditKeyDown = keyDown;

        if (editing) handleDrag(x, y, width, height, setter, onDragEnd);
    }

    private void handleDrag(int x, int y, int width, int height, PositionSetter setter, Runnable onDragEnd) {
        long handle = GLFW.glfwGetCurrentContext();
        if (handle == 0L) return;

        boolean mouseDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        int mouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int mouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

        int left = x - BOX_PADDING;
        int top = y - BOX_PADDING;
        int right = x + width + BOX_PADDING;
        int bottom = y + height + BOX_PADDING;

        if (mouseDown && !dragging) {
            if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
        } else if (!mouseDown && dragging) {
            dragging = false;
            if (onDragEnd != null) onDragEnd.run();
        }

        if (dragging) {
            setter.set((int) Math.round(mouseX - dragOffsetX), (int) Math.round(mouseY - dragOffsetY));
        }
    }

    public void drawBoxIfEditing(GuiGraphics graphics, int x, int y, int width, int height) {
        if (!editing) return;

        int left = x - BOX_PADDING;
        int top = y - BOX_PADDING;
        int right = x + width + BOX_PADDING;
        int bottom = y + height + BOX_PADDING;

        graphics.fill(left, top, right, top + 1, BORDER_COLOR_HOVERED);
        graphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR_HOVERED);
        graphics.fill(left, top, left + 1, bottom, BORDER_COLOR_HOVERED);
        graphics.fill(right - 1, top, right, bottom, BORDER_COLOR_HOVERED);
    }

    private boolean isKeyDown(int glfwKey) {
        if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) return false;
        long handle = GLFW.glfwGetCurrentContext();
        if (handle == 0L) return false;
        return GLFW.glfwGetKey(handle, glfwKey) == GLFW.GLFW_PRESS;
    }

    @FunctionalInterface
    public interface PositionSetter {
        void set(int x, int y);
    }
}