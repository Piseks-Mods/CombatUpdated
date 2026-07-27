package org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet;

import org.jetbrains.annotations.Nullable;

public enum MagicBulletType {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7);

    public final int id;

    MagicBulletType(int id) { this.id = id; }

    @Nullable
    public static MagicBulletType fromCount(int count) {
        for (MagicBulletType t : values()) if (t.id == count) return t;
        return null;
    }
}
