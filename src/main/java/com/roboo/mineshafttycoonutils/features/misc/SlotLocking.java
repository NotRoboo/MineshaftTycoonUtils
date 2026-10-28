package com.roboo.mineshafttycoonutils.features.misc;

import com.roboo.mineshafttycoonutils.MineshaftTycoonUtils;
import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.config.misc.SlotLockingConfig;
import com.roboo.mineshafttycoonutils.mixin.AbstractContainerScreenAccessor;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class SlotLocking {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final long MESSAGE_COOLDOWN_MS = 1000L;
    private static final int SLOT_SIZE = 16;
    private static final int ICON_TINT = 0xB3FFFFFF;

    private static final Identifier SLOT_TEXTURE =
            Identifier.fromNamespaceAndPath("mineshafttycoonutils", "textures/gui/locked_slot.png");
    private static final Identifier ICON_TEXTURE =
            Identifier.fromNamespaceAndPath("mineshafttycoonutils", "textures/gui/locked_icon.png");

    private static boolean lastLockKeyDown = false;
    private static long lastMessageAt = 0L;

    private SlotLocking() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
                    render(containerScreen, graphics));
        });
    }

    private static void onTick() {
        SlotLockingConfig cfg = ConfigManager.config.misc.slotLocking;
        boolean keyDown = isKeyDown(cfg.lockKeybind);

        if (!cfg.enabled || mc.player == null || !(mc.screen instanceof AbstractContainerScreen<?> screen)) {
            lastLockKeyDown = keyDown;
            return;
        }

        boolean pressed = keyDown && !lastLockKeyDown;
        lastLockKeyDown = keyDown;
        if (!pressed) return;

        if (screen.getFocused() instanceof EditBox) return;

        Slot hovered = ((AbstractContainerScreenAccessor) screen).mineshaftUtils$getHoveredSlot();
        if (hovered == null || hovered.container != mc.player.getInventory()) return;

        int index = hovered.getContainerSlot();
        if (!cfg.lockedSlots.remove(index)) {
            cfg.lockedSlots.add(index);
        }
        MineshaftTycoonUtils.configManager.saveConfig();
    }

    private static void render(AbstractContainerScreen<?> screen, GuiGraphics graphics) {
        SlotLockingConfig cfg = ConfigManager.config.misc.slotLocking;
        if (!cfg.enabled || cfg.iconType == SlotLockingConfig.IconType.NONE) return;
        if (cfg.lockedSlots.isEmpty() || mc.player == null) return;

        Inventory inventory = mc.player.getInventory();

        for (Slot slot : screen.getMenu().slots) {
            if (slot.container != inventory || !cfg.lockedSlots.contains(slot.getContainerSlot())) continue;

            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
            int x = accessor.mineshaftUtils$getLeftPos() + slot.x;
            int y = accessor.mineshaftUtils$getTopPos() + slot.y;

            if (cfg.iconType == SlotLockingConfig.IconType.SLOT) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, x, y, 0f, 0f,
                        SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, x, y, 0f, 0f,
                        SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, ICON_TINT);
            }
        }
    }

    public static boolean shouldBlock(int containerId, int slotId, int button, ClickType clickType, Player player) {
        SlotLockingConfig cfg = ConfigManager.config.misc.slotLocking;
        if (!cfg.enabled || cfg.lockedSlots.isEmpty()) return false;

        AbstractContainerMenu menu = player.containerMenu;
        if (menu.containerId != containerId) return false;

        boolean blocked = switch (clickType) {
            case PICKUP, QUICK_MOVE, THROW, QUICK_CRAFT, CLONE -> isLockedSlot(menu, slotId, player);
            case SWAP -> isLockedSlot(menu, slotId, player) || cfg.lockedSlots.contains(button);
            case PICKUP_ALL -> pickupAllTouchesLockedSlot(menu, player);
        };

        if (blocked) notifyBlocked();
        return blocked;
    }

    private static boolean isLockedSlot(AbstractContainerMenu menu, int slotId, Player player) {
        if (slotId < 0 || slotId >= menu.slots.size()) return false;

        Slot slot = menu.slots.get(slotId);
        if (slot.container != player.getInventory()) return false;

        return ConfigManager.config.misc.slotLocking.lockedSlots.contains(slot.getContainerSlot());
    }

    private static boolean pickupAllTouchesLockedSlot(AbstractContainerMenu menu, Player player) {
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) return false;

        for (int i = 0; i < menu.slots.size(); i++) {
            if (!isLockedSlot(menu, i, player)) continue;

            ItemStack stack = menu.slots.get(i).getItem();
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, carried)) return true;
        }

        return false;
    }

    private static void notifyBlocked() {
        long now = System.currentTimeMillis();
        if (now - lastMessageAt < MESSAGE_COOLDOWN_MS) return;
        lastMessageAt = now;

        if (mc.player == null) return;

        mc.player.displayClientMessage(SystemMessages.buildPrefix().append(
                Component.literal(" That slot is locked!").withStyle(Style.EMPTY.withColor(0xFF5555))), false);
    }

    private static boolean isKeyDown(int glfwKey) {
        if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) return false;
        long handle = GLFW.glfwGetCurrentContext();
        if (handle == 0L) return false;
        return GLFW.glfwGetKey(handle, glfwKey) == GLFW.GLFW_PRESS;
    }
}
