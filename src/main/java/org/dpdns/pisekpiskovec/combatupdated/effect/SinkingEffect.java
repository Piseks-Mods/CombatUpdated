package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Set;

public class SinkingEffect extends CUStatusEffect {
    public SinkingEffect() {
        super(Set.of(TriggerType.ON_HIT));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        SanityCapability.get(entity).ifPresent(cap -> cap.reduce(potency));
    }
}
