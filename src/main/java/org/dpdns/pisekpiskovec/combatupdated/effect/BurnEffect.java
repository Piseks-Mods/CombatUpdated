package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;

public class BurnEffect extends CUStatusEffect {
    public BurnEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 1));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        dealTrueDamage(entity, potency);
    }
}
