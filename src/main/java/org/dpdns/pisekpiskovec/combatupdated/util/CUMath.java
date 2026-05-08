package org.dpdns.pisekpiskovec.combatupdated.util;

public final class CUMath {
    public static int clamp(int min, int ctrl, int max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        return Math.max(min, Math.min(ctrl, max));
    }

    public static float clamp(float min, float ctrl, float max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        return Math.max(min, Math.min(ctrl, max));
    }

    public static double clamp(double min, double ctrl, double max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        return Math.max(min, Math.min(ctrl, max));
    }
}
