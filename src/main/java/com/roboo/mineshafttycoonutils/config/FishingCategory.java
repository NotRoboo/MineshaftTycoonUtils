package com.roboo.mineshafttycoonutils.config;

import com.google.gson.annotations.Expose;
import com.roboo.mineshafttycoonutils.config.fishing.CrateOrderConfig;
import com.roboo.mineshafttycoonutils.config.fishing.SeaCreatureOrderConfig;
import com.roboo.mineshafttycoonutils.config.fishing.TreasureOrderConfig;
import com.roboo.mineshafttycoonutils.config.fishing.TrophyOrderConfig;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FishingCategory {

    @Expose
    @ConfigOption(name = "Show Fishing HUD", desc = "Show the fishing tracker HUD overlay")
    @ConfigEditorBoolean
    public boolean hudEnabled = true;

    @Expose
    @ConfigOption(name = "Only Show In Ocean", desc = "Only show the fishing HUD while inside a designated fishing area")
    @ConfigEditorBoolean
    public boolean onlyShowWhenFishing = true;

    @Expose
    @ConfigOption(
            name = "Use Colored Names",
            desc = "Color sea creatures, crates, plates, and trophy fish based off their rarity in the HUD")
    @ConfigEditorBoolean
    public boolean coloredNamesEnabled = true;

    @Expose
    @ConfigOption(
            name = "Show Double Hook Count",
            desc = "Show the double hook count next to Total on the HUD. " +
                    "Note: The Total line always subtracts double hooks either way.")
    @ConfigEditorBoolean
    public boolean showDoubleHookCount = true;

    @Expose
    @ConfigOption(
            name = "Disable Right-Align Flip",
            desc = "Keep this HUD's text left-aligned even when positioned past the middle of the screen, " +
                    "instead of automatically flipping to right-aligned")
    @ConfigEditorBoolean
    public boolean disableRightAlignFlip = false;

    @Expose
    @ConfigOption(
            name = "HUD Line Order",
            desc = "Drag to reorder the sections shown on the fishing HUD. " +
                    "Remove an entry to hide that section entirely; it will still be tracked.")
    @ConfigEditorDraggableList
    public List<LineEntry> hudLineOrder = new ArrayList<>(Arrays.asList(LineEntry.values()));

    @Expose
    public int hudX = 10;

    @Expose
    public int hudY = 70;

    @Expose
    @Category(name = "Treasure", desc = "Treasure and plate drop tracking settings")
    public TreasureOrderConfig treasure = new TreasureOrderConfig();

    @Expose
    @Category(name = "Trophy Fish", desc = "Trophy fish tracking settings")
    public TrophyOrderConfig trophyFish = new TrophyOrderConfig();

    @Expose
    @Category(name = "Sea Creatures", desc = "Sea creature tracking settings")
    public SeaCreatureOrderConfig seaCreatures = new SeaCreatureOrderConfig();

    @Expose
    @Category(name = "Crates", desc = "Crate tracking settings")
    public CrateOrderConfig crates = new CrateOrderConfig();

    public enum LineEntry {
        TOTAL("Total"),
        TREASURE_DROPS("Treasure Drops"),
        TROPHY_FISH("Trophy Fish"),
        SEA_CREATURES("Sea Creatures"),
        CRATES("Crates");

        private final String displayName;

        LineEntry(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}