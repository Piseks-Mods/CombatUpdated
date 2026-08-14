package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

public class PhotoelectricityEffect extends CUStatusEffect {
    public PhotoelectricityEffect() {
        super(props().triggers(TriggerType.ON_HIT, TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(3).maxPotency(1).defaults(1, 0).managesOwnCount(true));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        switch (type) {
            case ON_HIT ->
                    StatusEffectCapability.get(entity).map(StatusEffectCapability::getAttackerContext).ifPresent(attacker -> {
                        StatusEffectCapability.ifPresent(attacker, cap -> {
                            int chargeToApply = count;
                            if (cap.getEffect(StatusEffectCapability.EffectType.CHARGE).getCount() <= 5)
                                chargeToApply += 3;
                            cap.apply(StatusEffectCapability.EffectType.CHARGE, chargeToApply, 0);
                        });
                    });
            case TURN_END -> decrementCount(count);
        }
    }
}
