package org.dpdns.pisekpiskovec.combatupdated.effect;

import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.jetbrains.annotations.Nullable;

public enum TremorType {
    BASE(StatusEffectCapability.EffectType.TREMOR), EVERLASTING(StatusEffectCapability.EffectType.TREMOR_EVERLASTING), SCORCH(StatusEffectCapability.EffectType.TREMOR_SCORCH);

    public final StatusEffectCapability.EffectType effectType;

    TremorType(StatusEffectCapability.EffectType effectType) {
        this.effectType = effectType;
    }

    @Nullable
    public static TremorType fromEffectType(StatusEffectCapability.EffectType type) {
        for (TremorType t : values()) if (t.effectType == type) return t;
        return null;
    }

    /**
     * Returns the active single TremorType on this cap, or null if none or if
     * Superposition is active instead.
     */
    @Nullable
    public static TremorType getActive(StatusEffectCapability cap) {
        for (TremorType t : values()) {
            if (!cap.getEffect(t.effectType).isExpired()) return t;
        }
        return null;
    }
}
