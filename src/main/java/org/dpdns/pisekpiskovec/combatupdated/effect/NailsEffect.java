package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

public class NailsEffect extends CUStatusEffect {
    public NailsEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(1).defaults(1, 1).uniqueOf(StatusEffectCapability.EffectType.BLEED).managesOwnCount(true));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            cap.apply(StatusEffectCapability.EffectType.BLEED, count, 1);
            apply(getCount() / 2, 1);
        });
    }
}
