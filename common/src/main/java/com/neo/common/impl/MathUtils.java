package com.neo.common.impl;

/**
 * This class is a provides basic math utilities
 *
 */
public class MathUtils {

    private MathUtils() {}

    public static boolean isInBounds(Integer val, int min, int max) {
        if (val == null) {
            return false;
        }
        return isInBounds(val.intValue(), min, max);
    }

    public static boolean isInBounds(int val, int min, int max) {
        return val >= min && val <= max;
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
