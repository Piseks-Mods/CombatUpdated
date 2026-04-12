package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Set;

public class BurnEffect extends CUStatusEffect {
    public BurnEffect() {
        super(Set.of(TriggerType.TURN_END));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        dealTrueDamage(entity, potency);
    }
}
