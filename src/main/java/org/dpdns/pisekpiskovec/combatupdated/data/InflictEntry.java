package org.dpdns.pisekpiskovec.combatupdated.data;

import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

/**
 * Describes one status effect application attached to a mob or item.
 *
 * @param effect  which effect to apply
 * @param count   count stacks to add
 * @param potency potency to apply (uses max-of-existing rule in StatusEffectCapability)
 */
public record InflictEntry(StatusEffectCapability.EffectType effect, int count, int potency) {
    public static InflictEntry countOnly(StatusEffectCapability.EffectType effect, int count) {
        return new InflictEntry(effect, count, 0);
    }

    /**
     * Raises potency on an EXISTING active effect only.
     * Has no effect if the target doesn't already have this effect active.
     */
    public static InflictEntry potencyOnly(StatusEffectCapability.EffectType effect, int potency) {
        return new InflictEntry(effect, 0, potency);
    }
}
