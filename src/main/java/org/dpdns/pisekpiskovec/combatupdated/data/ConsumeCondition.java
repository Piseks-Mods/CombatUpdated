package org.dpdns.pisekpiskovec.combatupdated.data;

import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

/**
 * A conditional requirement attached to an InflictEntry.
 * Before the effect is applied, the specified side's effect must have
 * at least the required potency/count - which are then consumed.1
 * If either requirement isn't met, the inflict/gain is skipped entirely.
 */
public record ConsumeCondition(StatusEffectCapability.EffectType effect, int potency, int count) {
    /**
     * Checks if the condition is met on the given entity, and if so, consumes
     * the required potency/count.
     *
     * @return true if met and consumed, false if not met (nothing consumed)
     */
    public boolean checkAndConsume(StatusEffectCapability cap) {
        CUStatusEffect eff = cap.getEffect(effect);

        if (eff.isExpired()) return false;

        // Check both requirements before consuming either
        if (potency > 0 && eff.getPotency() < potency) return false;
        if (count > 0 && eff.getCount() < count) return false;

        // All checks passed - consume
        if (potency > 0) eff.addPotency(-potency);
        if (count > 0) eff.decrementCount(count);

        return true;
    }
}
