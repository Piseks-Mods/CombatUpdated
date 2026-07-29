package org.dpdns.pisekpiskovec.combatupdated.data;

import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

/**
 * A conditional requirement attached to an InflictEntry.
 * Before the effect is applied, the specified side's effect must have
 * at least the required potency/count - which are not consumed.
 * If either requirement isn't met, the inflict/gain is skipped entirely.
 */
public record RequireCondition(StatusEffectCapability.EffectType effect, int potency, int count) {
    /**
     * Checks if the condition is met on the given entity.
     *
     * @return true if met, false if not met
     */
    public boolean check(StatusEffectCapability cap) {
        CUStatusEffect eff = cap.getEffect(effect);
        if (eff.isExpired()) return false;
        // Check both requirements
        if (potency > 0 && eff.getPotency() < potency) return false;
        if (count > 0 && eff.getCount() < count) return false;
        return true;
    }
}
