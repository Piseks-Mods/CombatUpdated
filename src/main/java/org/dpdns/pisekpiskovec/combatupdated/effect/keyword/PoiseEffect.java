package org.dpdns.pisekpiskovec.combatupdated.effect.keyword;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

public class PoiseEffect extends CUStatusEffect {

    public PoiseEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.POSITIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 1));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // TURN_END: no extra logic, just count -= 1 from trigger()
    }
}
