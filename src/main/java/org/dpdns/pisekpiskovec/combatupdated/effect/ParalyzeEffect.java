package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;

public class ParalyzeEffect extends CUStatusEffect {
    public ParalyzeEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(1).defaults(1, 0));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // Silently Pass On on Turn End.
    }
}
