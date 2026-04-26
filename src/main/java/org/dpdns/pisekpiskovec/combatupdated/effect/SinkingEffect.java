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
        // SanityCapability only exist on players - ifPresent is a no-op for mobs,
        // which is intentional: mobs have not sanity to drain.
        // TODO: Add sanity to drain to panic-capable mobs
        SanityCapability.get(entity).ifPresent(cap -> cap.reduce(potency));
    }
}
