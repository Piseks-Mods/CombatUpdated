package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;

public class HasteEffect extends CUStatusEffect {

    public HasteEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.POSITIVE).stackType(StackType.STACKABLE).maxPotency(0).maxCount(99).defaults(1, 0));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        decrementCount(count - 1);
    }
}
