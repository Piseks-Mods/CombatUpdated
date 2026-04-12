package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Set;

public class BleedEffect extends CUStatusEffect {
    public BleedEffect() {
        super(Set.of(TriggerType.ON_USE));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        dealTrueDamage(entity, potency);
    }
}
