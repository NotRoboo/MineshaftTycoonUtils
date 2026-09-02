package com.roboo.mineshafttycoonutils.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ScoreboardCategory {

    public static final int AUTO_POSITION = Integer.MIN_VALUE;

    @Expose
    @ConfigOption(
            name = "Custom Scoreboard",
            desc = "Reorder/hide lines on the scoreboard")
    @ConfigEditorBoolean
    public boolean enabled = false;

    @Expose
    @ConfigOption(
            name = "Line Order",
            desc = "Drag to reorder scoreboard lines. Remove a line to hide it, drag it back in from the pool " +
                    "to show it again - it'll keep tracking in the background either way. " +
                    "The title is always shown first and isn't included here.")
    @ConfigEditorDraggableList
    public List<Line> lineOrder = new ArrayList<>(List.of(
            Line.BY_ITV,
            Line.BLANK_1,
            Line.LEVEL,
            Line.COINS,
            Line.ASH,
            Line.MAGMA,
            Line.ICE,
            Line.ASC,
            Line.SHARDS,
            Line.FISHING_TIME,
            Line.EVENT,
            Line.BLANK_2,
            Line.TIME
    ));

    @Expose
    public int hudX = AUTO_POSITION;

    @Expose
    public int hudY = AUTO_POSITION;

    @Expose
    public Map<Line, String> lastKnownLines = new EnumMap<>(Line.class);
    public enum Line {
        BY_ITV("By [ITV]", "By [ITV]", false),
        DATE("Date", "Date", false),
        LEVEL("Level", "§b◆§f Level", false),
        COINS("Coins", "§6◆§f Coins", false),
        ASH("Ash", "§7◆§f Ash", false),
        MAGMA("Magma", "§c◆§f Magma", false),
        ICE("Ice", "§3◆§f Ice", false),
        ASC("Asc", "§d◆§f Asc", false),
        SHARDS("Shards", "§5◆§f Shards", false),
        EVENT("Event (Candy/Skin)", "", true),
        TIME("Time", "Time", false),
        FISHING_TIME("Fishing Time", "", true),
        HYPIXEL_NET("www.hypixel.net", "www.hypixel.net", false),
        BLANK_1("Blank Line", "", false),
        BLANK_2("Blank Line", "", false),
        BLANK_3("Blank Line", "", false),
        BLANK_4("Blank Line", "", false),
        BLANK_5("Blank Line", "", false);

        private final String displayName;
        private final String placeholderLabel;
        private final boolean onlyShowWhenKnown;

        Line(String displayName, String placeholderLabel, boolean onlyShowWhenKnown) {
            this.displayName = displayName;
            this.placeholderLabel = placeholderLabel;
            this.onlyShowWhenKnown = onlyShowWhenKnown;
        }

        public String placeholderLabel() {
            return placeholderLabel;
        }

        public boolean onlyShowWhenKnown() {
            return onlyShowWhenKnown;
        }

        public boolean isBlank() {
            return name().startsWith("BLANK");
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}