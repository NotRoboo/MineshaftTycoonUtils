package com.roboo.mineshafttycoonutils.mixin;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {

    @Accessor("allMessages")
    List<GuiMessage> mineshaftUtils$getAllMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> mineshaftUtils$getTrimmedMessages();

    @Accessor("chatScrollbarPos")
    int mineshaftUtils$getScrollPos();
}