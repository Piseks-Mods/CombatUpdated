package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Set;

public class PoiseEffect extends CUStatusEffect {

    public PoiseEffect() {
        super(Set.of(TriggerType.ON_HIT, TriggerType.TURN_END));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        if (type == TriggerType.ON_HIT) {
            // Proc chance: (potency * 5)%
            float procChance = potency * 0.05f;
            if (entity.getRandom().nextFloat() < procChance) {
                // +20 % damage bonus - stored as a flag for DamageCalculator to read this tick
                // Set a short-lived flag on the capability; CombatEventHandler reads and clears it
                StatusEffectCapability.ifPresent(entity, cap -> cap.setPoiseDamageBonus(0.20f));
            }
            // count -= 1 handled by trigger()
        }
        // TURN_END: no extra logic, just count -= 1 from trigger()
    }
}
