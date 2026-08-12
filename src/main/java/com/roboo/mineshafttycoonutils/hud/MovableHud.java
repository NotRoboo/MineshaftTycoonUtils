package com.roboo.mineshafttycoonutils.hud;

import net.minecraft.client.gui.GuiGraphics;

public interface MovableHud {

    String getDisplayName();

    boolean isMasterEnabled();

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    void setPosition(int x, int y);

    void render(GuiGraphics graphics);
}