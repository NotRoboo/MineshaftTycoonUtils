package com.roboo.mineshafttycoonutils.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("hoveredSlot")
    Slot mineshaftUtils$getHoveredSlot();

    @Accessor("leftPos")
    int mineshaftUtils$getLeftPos();

    @Accessor("topPos")
    int mineshaftUtils$getTopPos();
}