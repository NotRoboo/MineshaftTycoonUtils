package com.roboo.mineshafttycoonutils.features.timers;

public class PetAdTracker {

    public enum State { UNKNOWN, INACTIVE, TIMING, READY }

    private static final long ANCHOR_THRESHOLD_MS = 1500;

    private static State state = State.UNKNOWN;
    private static long endTime = -1;
    private static long lastSeenSeconds = -1;

    private PetAdTracker() {}

    public static void init() {}

    static void applyReading(long totalSeconds) {
        if (totalSeconds <= 0) {
            state = State.READY;
            endTime = -1;
            lastSeenSeconds = -1;
            return;
        }

        state = State.TIMING;
        long projectedEnd = System.currentTimeMillis() + totalSeconds * 1000L;

        if (totalSeconds != lastSeenSeconds || endTime < 0
                || Math.abs(projectedEnd - endTime) > ANCHOR_THRESHOLD_MS) {
            endTime = projectedEnd;
            lastSeenSeconds = totalSeconds;
        }
    }

    static void markInactive() {
        state = State.INACTIVE;
        endTime = -1;
        lastSeenSeconds = -1;
    }

    public static State getState() {
        if (state == State.TIMING && System.currentTimeMillis() >= endTime) return State.READY;
        return state;
    }

    public static long getSecondsLeft() {
        if (state != State.TIMING || endTime < 0) return -1;
        return Math.max(0, (endTime - System.currentTimeMillis()) / 1000);
    }

    public static void reset() {
        state = State.UNKNOWN;
        endTime = -1;
        lastSeenSeconds = -1;
    }
}