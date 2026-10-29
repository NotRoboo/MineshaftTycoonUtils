package com.roboo.mineshafttycoonutils.features.chat;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.categories.MiscCategory;
import com.roboo.mineshafttycoonutils.mixin.ChatComponentAccessor;
import com.roboo.mineshafttycoonutils.utils.LogTextUtils;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayDeque;
import java.util.List;

public class ChatMessageCopier {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int MAX_REMEMBERED = 300;

    private record Remembered(Component component, String raw) {}

    private static final ArrayDeque<Remembered> remembered = new ArrayDeque<>();

    private ChatMessageCopier() {}

    public static void remember(Component component, String raw) {
        if (component == null || raw == null) return;
        remembered.addFirst(new Remembered(component, raw));
        while (remembered.size() > MAX_REMEMBERED) {
            remembered.removeLast();
        }
    }

    public static void carryOver(Component original, Component merged, int count) {
        for (Remembered entry : remembered) {
            if (entry.component() == original) {
                remember(merged, entry.raw() + " (" + count + ")");
                return;
            }
        }
    }

    public static boolean tryCopy(MouseButtonEvent event) {
        MiscCategory cfg = ConfigManager.config.misc;
        if (!cfg.copyChatMessages || !matches(cfg.copyMethod, event)) return false;

        ChatComponent chat = mc.gui.getChat();
        ChatComponentAccessor accessor = (ChatComponentAccessor) chat;
        List<GuiMessage.Line> lines = accessor.mineshaftUtils$getTrimmedMessages();
        List<GuiMessage> messages = accessor.mineshaftUtils$getAllMessages();

        int lineIndex = lineIndexAt(accessor, event.x(), event.y(), lines.size());
        if (lineIndex < 0) return false;

        int endIndex = lineIndex;
        while (endIndex > 0 && !lines.get(endIndex).endOfEntry()) {
            endIndex--;
        }

        int messageIndex = 0;
        for (int i = 0; i < endIndex; i++) {
            if (lines.get(i).endOfEntry()) messageIndex++;
        }
        if (messageIndex >= messages.size()) return false;

        String text = textFor(messages.get(messageIndex).content());
        if (text.isEmpty()) return false;

        mc.keyboardHandler.setClipboard(text);
        notifyCopied();
        return true;
    }

    private static String textFor(Component content) {
        for (Remembered entry : remembered) {
            if (entry.component() == content) {
                String formatted = PlayerMessageFormatter.copyText(entry.raw());
                return ChatFormatting.stripFormatting(formatted != null ? formatted : entry.raw()).trim();
            }
        }
        return LogTextUtils.restore(ChatFormatting.stripFormatting(content.getString())).trim();
    }

    private static int lineIndexAt(ChatComponentAccessor accessor, double x, double y, int lineCount) {
        double scale = mc.options.chatScale().get();
        if (scale <= 0.0) return -1;

        double chatX = (x - 4.0) / scale;
        double chatY = (mc.getWindow().getGuiScaledHeight() - y - 40.0) / scale;
        double chatWidth = Math.floor(mc.options.chatWidth().get() * (mc.getWindow().getGuiScaledWidth() - 40) + 40);
        if (chatX < 0.0 || chatY < 0.0 || chatX > Math.floor(chatWidth / scale)) return -1;

        double lineHeight = 9.0 * (mc.options.chatLineSpacing().get() + 1.0);
        int index = (int) (chatY / lineHeight) + accessor.mineshaftUtils$getScrollPos();
        return index >= 0 && index < lineCount ? index : -1;
    }

    private static boolean matches(MiscCategory.CopyMethod method, MouseButtonEvent event) {
        if (event.button() != method.button()) return false;
        return method.usesControl()
                ? event.hasControlDown() && !event.hasShiftDown()
                : event.hasShiftDown() && !event.hasControlDown();
    }

    private static void notifyCopied() {
        if (mc.player == null) return;
        mc.player.displayClientMessage(SystemMessages.buildPrefix().append(
                Component.literal(" Copied message!").withStyle(Style.EMPTY.withColor(0x55FF55))), false);
    }
}