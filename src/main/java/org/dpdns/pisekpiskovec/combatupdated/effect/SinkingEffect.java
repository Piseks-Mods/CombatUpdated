package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.SanityAPI;

public class SinkingEffect extends CUStatusEffect {
    public SinkingEffect() {
        super(props().triggers(TriggerType.ON_HIT).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 1));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        if (SanityAPI.hasSanity(entity)) {
            SanityAPI.reduce(entity, potency);
        } else {
            dealTrueDamage(entity, potency);
        }
    }
}
