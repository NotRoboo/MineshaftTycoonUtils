package com.roboo.mineshafttycoonutils.features.wiki;

import com.roboo.mineshafttycoonutils.config.ConfigManager;
import com.roboo.mineshafttycoonutils.utils.ComponentTextUtils;
import com.roboo.mineshafttycoonutils.utils.SystemMessages;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.component.ItemLore;
import org.apache.commons.io.FileUtils;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WikiExporter {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final File WIKI_DIR = new File("config/mineshafttycoonutils/wiki");
    private static final File ITEM_DIR = new File(WIKI_DIR, "items");
    private static final int GRID_COLUMNS = 9;

    private static final Map<String, String> ID_FILE_OVERRIDES = Map.ofEntries(
            Map.entry("minecraft:diamond_block", "Block Of Diamond"),
            Map.entry("minecraft:netherite_block", "Block Of Netherite"),
            Map.entry("minecraft:redstone_block", "Block Of Redstone"),
            Map.entry("minecraft:emerald_block", "Block Of Emerald"),
            Map.entry("minecraft:gold_block", "Block Of Gold"),
            Map.entry("minecraft:iron_block", "Block Of Iron"),
            Map.entry("minecraft:coal_block", "Block Of Coal"),
            Map.entry("minecraft:lapis_block", "Block Of Lapis Lazuli"),
            Map.entry("minecraft:copper_block", "Block Of Copper"),
            Map.entry("minecraft:raw_iron_block", "Block Of Raw Iron"),
            Map.entry("minecraft:raw_gold_block", "Block Of Raw Gold"),
            Map.entry("minecraft:raw_copper_block", "Block Of Raw Copper"),
            Map.entry("minecraft:quartz_block", "Block Of Quartz"),
            Map.entry("minecraft:amethyst_block", "Block Of Amethyst")
    );

    private static final Map<String, String> DYEABLE_ARMOR_PIECE = Map.of(
            "minecraft:leather_helmet", "helmet",
            "minecraft:leather_chestplate", "chestplate",
            "minecraft:leather_leggings", "leggings",
            "minecraft:leather_boots", "boots"
    );

    private static boolean lastContainerKeyDown = false;
    private static boolean lastItemKeyDown = false;

    private static final Field HOVERED_SLOT_FIELD = resolveHoveredSlotField();

    private record SlotIcon(String icon, String piece, String dye) {
        static SlotIcon plain(String icon) {
            return new SlotIcon(icon, null, null);
        }

        static SlotIcon dyedArmor(String piece, String dye) {
            return new SlotIcon(null, piece, dye);
        }
    }

    private record SlotEntry(String name, String count, List<String> lore, SlotIcon icon) {
        String contentKey() {
            return name + "\u0000" + count + "\u0000" + String.join("\u0001", lore)
                    + "\u0000" + (icon != null ? icon.icon() : "")
                    + "\u0000" + (icon != null ? icon.piece() : "")
                    + "\u0000" + (icon != null ? icon.dye() : "");
        }
    }

    private WikiExporter() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private static void onTick() {
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            lastContainerKeyDown = false;
            lastItemKeyDown = false;
            return;
        }

        boolean containerKeyDown = isKeyDown(ConfigManager.config.wiki.exportKeybind);
        boolean itemKeyDown = isKeyDown(ConfigManager.config.wiki.exportItemKeybind);

        if (containerKeyDown && !lastContainerKeyDown) {
            exportContainer(containerScreen);
        }
        if (itemKeyDown && !lastItemKeyDown) {
            exportHoveredItem(containerScreen);
        }

        lastContainerKeyDown = containerKeyDown;
        lastItemKeyDown = itemKeyDown;
    }

    private static void exportContainer(AbstractContainerScreen<?> screen) {
        if (mc.player == null) return;

        AbstractContainerMenu menu = screen.getMenu();
        var playerInventory = mc.player.getInventory();

        List<Slot> containerSlots = new ArrayList<>();
        List<SlotEntry> entries = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container == playerInventory) continue;
            containerSlots.add(slot);
            entries.add(readSlot(slot.getItem()));
        }

        if (entries.isEmpty()) {
            errorMsg("No container slots found to export.");
            return;
        }

        String plainTitle = screen.getTitle().getString().trim();
        if (plainTitle.isEmpty()) plainTitle = "Unknown Container";

        String formattedTitle = legacyToAmpersand(ComponentTextUtils.formattedText(screen.getTitle()));

        String content = buildTemplate(formattedTitle, entries, containerSlots);
        String fileName = uniqueFileName(WIKI_DIR, sanitizeFileName(plainTitle));

        if (writeFile(WIKI_DIR, fileName, content)) {
            successMsg(plainTitle, "wiki/" + fileName);
        } else {
            errorMsg("Failed to save wiki export for " + plainTitle);
        }
    }

    private static String buildTemplate(String formattedTitle, List<SlotEntry> entries, List<Slot> slots) {
        int[] dims = computeGridDimensions(slots, entries.size());
        int cols = dims[0];
        int rows = dims[1];

        StringBuilder sb = new StringBuilder();
        sb.append("{{UI\n");
        sb.append("|title = ").append(formattedTitle).append("\n");
        sb.append("|cols = ").append(cols).append(" |rows = ").append(rows).append("\n");

        Map<String, Integer> firstSeenIndex = new LinkedHashMap<>();

        for (int i = 0; i < entries.size(); i++) {
            SlotEntry entry = entries.get(i);
            if (entry == null) continue;

            int slotNumber = i + 1;
            String key = entry.contentKey();
            Integer duplicateOf = firstSeenIndex.get(key);

            if (duplicateOf != null) {
                sb.append("|slot").append(slotNumber).append(" = slot").append(duplicateOf).append("\n");
            } else {
                firstSeenIndex.put(key, slotNumber);
                sb.append("|slot").append(slotNumber).append(" = ").append(formatSlotValue(entry)).append("\n");
                appendIconParams(sb, slotNumber, entry.icon());
            }
        }

        sb.append("}}\n");
        return sb.toString();
    }

    private static int[] computeGridDimensions(List<Slot> slots, int totalSlots) {
        if (slots.isEmpty()) return new int[]{GRID_COLUMNS, 1};

        Map<Integer, Integer> rowCounts = new LinkedHashMap<>();
        for (Slot slot : slots) {
            rowCounts.merge(slot.y, 1, Integer::sum);
        }

        int rows = rowCounts.size();
        int cols = rowCounts.values().stream().mapToInt(Integer::intValue).max().orElse(GRID_COLUMNS);

        if (cols <= 0) cols = GRID_COLUMNS;
        if (rows <= 0) rows = 1;
        if ((long) cols * rows < totalSlots) {
            rows = (int) Math.ceil(totalSlots / (double) cols);
        }

        return new int[]{cols, rows};
    }

    private static void exportHoveredItem(AbstractContainerScreen<?> screen) {
        if (mc.player == null) return;

        if (HOVERED_SLOT_FIELD == null) {
            errorMsg("Could not detect hovered slot on this screen (reflection failed).");
            return;
        }

        Slot hovered = getHoveredSlot(screen);
        if (hovered == null || !hovered.hasItem()) {
            errorMsg("No item is being hovered.");
            return;
        }

        SlotEntry entry = readSlot(hovered.getItem());
        if (entry == null) return;

        String plainName = stripFormatting(entry.name()).trim();
        if (plainName.isEmpty()) plainName = "Unknown Item";

        String content = buildStandaloneTemplate(entry);
        String fileName = uniqueFileName(ITEM_DIR, sanitizeFileName(plainName));

        if (writeFile(ITEM_DIR, fileName, content)) {
            successMsg(plainName, "wiki/items/" + fileName);
        } else {
            errorMsg("Failed to save wiki export for " + plainName);
        }
    }

    private static String buildStandaloneTemplate(SlotEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("{{UI\n");
        sb.append("|cols = 1 |rows = 1\n");
        sb.append("|slot1 = ").append(formatSlotValue(entry)).append("\n");
        appendIconParams(sb, 1, entry.icon());
        sb.append("}}\n");
        return sb.toString();
    }

    private static Field resolveHoveredSlotField() {
        Class<?> clazz = AbstractContainerScreen.class;
        while (clazz != null && clazz != Object.class) {
            Field candidate = null;
            for (Field f : clazz.getDeclaredFields()) {
                if (!Slot.class.isAssignableFrom(f.getType())) continue;
                if (f.getName().toLowerCase().contains("hover")) {
                    candidate = f;
                    break;
                }
                if (candidate == null) candidate = f;
            }
            if (candidate != null) {
                candidate.setAccessible(true);
                return candidate;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private static Slot getHoveredSlot(AbstractContainerScreen<?> screen) {
        if (HOVERED_SLOT_FIELD == null) return null;
        try {
            return (Slot) HOVERED_SLOT_FIELD.get(screen);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static SlotIcon resolveIcon(ItemStack stack) {
        String id = itemId(stack);

        String piece = DYEABLE_ARMOR_PIECE.get(id);
        if (piece != null) {
            DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);
            String dye = dyedColor != null
                    ? String.format("#%06X", dyedColor.rgb() & 0xFFFFFF)
                    : null;
            return SlotIcon.dyedArmor(piece, dye);
        }

        if (id.equals("minecraft:player_head")) {
            String plainName = stack.getHoverName().getString().trim();
            if (plainName.isEmpty()) plainName = "Unknown Skull";
            return SlotIcon.plain(plainName + ".png");
        }

        String fileName = ID_FILE_OVERRIDES.getOrDefault(id, prettifyId(id));
        if (fileName == null || fileName.isEmpty()) return null;

        if (isEnchanted(stack)) {
            fileName = "Enchanted " + fileName;
        }

        return SlotIcon.plain(fileName + ".png");
    }

    private static boolean isEnchanted(ItemStack stack) {
        Boolean glintOverride = stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (Boolean.TRUE.equals(glintOverride)) return true;

        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments != null && !enchantments.isEmpty()) return true;

        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        return stored != null && !stored.isEmpty();
    }

    private static void appendIconParams(StringBuilder sb, int slotNumber, SlotIcon icon) {
        if (icon == null) return;

        if (icon.icon() != null) {
            sb.append("|icon").append(slotNumber).append(" = ").append(icon.icon()).append("\n");
        }
        if (icon.piece() != null) {
            sb.append("|piece").append(slotNumber).append(" = ").append(icon.piece()).append("\n");
            if (icon.dye() != null) {
                sb.append("|dye").append(slotNumber).append(" = ").append(icon.dye()).append("\n");
            }
        }
    }

    private static String itemId(ItemStack stack) {
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.toString();
    }

    private static String prettifyId(String id) {
        if (id == null) return "";
        String shortId = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        String[] parts = shortId.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    private static SlotEntry readSlot(ItemStack stack) {
        if (stack.isEmpty()) return null;

        String name = legacyToAmpersand(ComponentTextUtils.formattedText(stack.getHoverName()));

        List<String> lore = new ArrayList<>();
        ItemLore itemLore = stack.get(DataComponents.LORE);
        if (itemLore != null) {
            for (Component line : itemLore.lines()) {
                lore.add(legacyToAmpersand(ComponentTextUtils.formattedText(line)));
            }
        }

        SlotIcon icon = resolveIcon(stack);

        return new SlotEntry(name, String.valueOf(stack.getCount()), lore, icon);
    }

    private static String formatSlotValue(SlotEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append(entry.name()).append(";").append(entry.count());
        for (String line : entry.lore()) {
            sb.append(";").append(line);
        }
        return sb.toString();
    }

    private static String legacyToAmpersand(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (code == 'k') {
                    i++;
                    continue;
                }
                if ("0123456789abcdeflmnor".indexOf(code) >= 0) {
                    sb.append('&').append(code);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)&[0-9a-fklmnor]", "");
    }

    private static String sanitizeFileName(String rawTitle) {
        String stripped = rawTitle.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        return stripped.isEmpty() ? "Unknown" : stripped;
    }

    private static String uniqueFileName(File dir, String baseName) {
        String fileName = baseName + ".txt";
        if (!new File(dir, fileName).exists()) return fileName;

        int counter = 2;
        while (new File(dir, baseName + " (" + counter + ").txt").exists()) {
            counter++;
        }
        return baseName + " (" + counter + ").txt";
    }

    private static boolean writeFile(File dir, String fileName, String content) {
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                return false;
            }
            FileUtils.writeStringToFile(new File(dir, fileName), content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isKeyDown(int glfwKey) {
        if (glfwKey == GLFW.GLFW_KEY_UNKNOWN) return false;
        long handle = GLFW.glfwGetCurrentContext();
        if (handle == 0L) return false;
        return GLFW.glfwGetKey(handle, glfwKey) == GLFW.GLFW_PRESS;
    }

    private static void successMsg(String label, String relativePath) {
        if (mc.player == null) return;

        MutableComponent message = Component.literal(" Exported ")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA));

        message.append(Component.literal(label)
                .withStyle(Style.EMPTY.withColor(0xFFFF55)));

        message.append(Component.literal(" to ")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA)));

        message.append(Component.literal(relativePath)
                .withStyle(Style.EMPTY.withColor(0x55FF55)));

        mc.player.displayClientMessage(SystemMessages.buildPrefix().append(message), false);
    }

    private static void errorMsg(String text) {
        if (mc.player == null) return;

        MutableComponent message = Component.literal(" " + text)
                .withStyle(Style.EMPTY.withColor(0xFF5555));

        mc.player.displayClientMessage(SystemMessages.buildPrefix().append(message), false);
    }
}