package com.roboo.mineshafttycoonutils.hud;

public final class HudScale {

    public static final float DEFAULT = 1.0f;
    public static final float MIN = 0.1f;
    public static final float MAX = 5.0f;
    public static final float STEP = 0.1f;

    private HudScale() {}

    public static float clamp(float scale) {
        return Math.clamp(scale, MIN, MAX);
    }

    public static float normalize(float scale) {
        return scale <= 0f ? DEFAULT : clamp(scale);
    }
}