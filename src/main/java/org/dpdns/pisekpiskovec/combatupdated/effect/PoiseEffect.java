package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

public class PoiseEffect extends CUStatusEffect {

    public PoiseEffect() {
        super(props().triggers(TriggerType.ON_ATTACK).triggers(TriggerType.TURN_END).category(Category.POSITIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 1).keywording(Keywordness.KEYWORD));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        if (type == TriggerType.ON_ATTACK) {
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
