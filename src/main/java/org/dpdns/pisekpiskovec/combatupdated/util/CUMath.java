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

    public static int discount(int base, int discount) {
        return base - ((base * discount) / 100);
    }

    public static float discount(float base, float discount) {
        return base - ((base * discount) / 100);
    }

    public static double discount(double base, double discount) {
        return base - ((base * discount) / 100);
    }

    public static int increase(int base, int increase) {
        return base + ((base * increase) / 100);
    }

    public static float increase(float base, float increase) {
        return base + ((base * increase) / 100);
    }

    public static double increase(double base, double increase) {
        return base + ((base * increase) / 100);
    }
}
