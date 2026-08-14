package org.dpdns.pisekpiskovec.combatupdated.data;

import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

public record EffectCondition(StatusEffectCapability.EffectType effect, int potency, int count, boolean consume) {
    /**
     * Checks if the condition is met on the given entity.
     *
     * @return true if met, false if not met
     */
    private boolean check(StatusEffectCapability cap) {
        CUStatusEffect eff = cap.getEffect(effect);
        if (eff.isExpired()) return false;
        // Check both requirements
        if (potency > 0 && eff.getPotency() < potency) return false;
        return count <= 0 || eff.getCount() >= count;
    }

    /**
     * Checks if the condition is met on the given entity, and if so, consumes
     * the required potency/count.
     *
     * @return true if met and consumed, false if not met (nothing consumed)
     */
    public boolean checkAndConsume(StatusEffectCapability cap) {
        if (!check(cap)) return false;
        if (consume) {
            CUStatusEffect eff = cap.getEffect(effect);
            if (potency > 0) eff.addPotency(-potency);
            if (count > 0) eff.decrementCount(count);
        }
        return true;
    }

    /**
     * Forcefully consume the required potency/count
     */
    public void forceConsume(StatusEffectCapability cap) {
        CUStatusEffect eff = cap.getEffect(effect);
        if (eff.isExpired()) return;
        if (count > 0) eff.decrementCount(Math.min(eff.getCount(), count));
        if (potency > 0) eff.addPotency(-Math.min(eff.getPotency(), potency));
    }
}
