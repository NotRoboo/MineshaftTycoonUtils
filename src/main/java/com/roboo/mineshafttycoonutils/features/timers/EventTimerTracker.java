package com.roboo.mineshafttycoonutils.features.timers;

public class EventTimerTracker {

    private record Reading(String name, long secondsAtRead, long readAt) {}

    private static Reading reading = null;

    private EventTimerTracker() {}

    static void apply(String name, long seconds) {
        reading = new Reading(name, seconds, System.currentTimeMillis());
    }

    static void clear() {
        reading = null;
    }

    public static String getName() {
        return reading == null ? null : reading.name();
    }

    public static long getSecondsLeft() {
        if (reading == null) return -1;
        long elapsed = (System.currentTimeMillis() - reading.readAt()) / 1000;
        return Math.max(0, reading.secondsAtRead() - elapsed);
    }

    public static void reset() {
        reading = null;
    }
}