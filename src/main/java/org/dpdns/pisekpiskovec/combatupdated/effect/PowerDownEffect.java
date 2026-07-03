package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

public class PowerDownEffect extends CUStatusEffect {
    public PowerDownEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.REPLACEABLE).maxCount(99).maxPotency(1).defaults(1, 0).keywording(Keywordness.REGULAR));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // Silently Pass On on Turn End.
    }
}
