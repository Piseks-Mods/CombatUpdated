package org.dpdns.pisekpiskovec.combatupdated.data;

import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Describes one status effect application attached to a mob or item.
 *
 * @param effect  which effect to apply
 * @param count   count stacks to add
 * @param potency potency to apply (uses max-of-existing rule in StatusEffectCapability)
 */
public record InflictEntry(StatusEffectCapability.EffectType effect, int count, int potency,
                           @Nullable ConsumeCondition consume, @Nullable ConsumeCondition drain) {
    public InflictEntry(StatusEffectCapability.EffectType effect, int count, int potency) {
        this(effect, count, potency, null, null);
    }

    public static InflictEntry countOnly(StatusEffectCapability.EffectType effect, int count) {
        return new InflictEntry(effect, count, 0);
    }

    /**
     * Raises potency on an EXISTING active effect only.
     */
    public static InflictEntry potencyOnly(StatusEffectCapability.EffectType effect, int potency) {
        return new InflictEntry(effect, 0, potency);
    }
}
