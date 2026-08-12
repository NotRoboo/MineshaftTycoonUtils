package com.roboo.mineshafttycoonutils.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HudEditorRegistry {

    private static final List<MovableHud> HUDS = new ArrayList<>();
    private static volatile boolean editing = false;

    private HudEditorRegistry() {}

    public static void register(MovableHud hud) {
        HUDS.add(hud);
    }

    public static List<MovableHud> getAll() {
        return Collections.unmodifiableList(HUDS);
    }

    public static boolean isEditing() {
        return editing;
    }

    public static void setEditing(boolean value) {
        editing = value;
    }
}