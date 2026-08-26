package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.util.SpeedCalculator;

public class LassoEffect extends CUStatusEffect {
    public LassoEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(3).maxPotency(0).defaults(1, 0));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        int speed = Math.min(SpeedCalculator.getEffectiveSpeed(entity), 5);
        StatusEffectCapability.ifPresent(entity, cap -> {
            cap.apply(StatusEffectCapability.EffectType.RUPTURE, 0, speed);
            cap.apply(StatusEffectCapability.EffectType.BIND, 1, 0);
        });
    }
}
