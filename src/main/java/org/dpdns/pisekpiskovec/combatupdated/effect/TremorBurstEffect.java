package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;

public class TremorBurstEffect extends CUStatusEffect {
    public TremorBurstEffect() {
        super(props().category(Category.NEGATIVE).stackType(StackType.INSTANT).maxCount(0).maxPotency(0).defaults(0, 0));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // No trigger type
    }

    public static void apply(LivingEntity entity) {
        TremorEffect.onTremorBurst(entity);
        TremorEverlastingEffect.onTremorBurst(entity);
        TremorSuperpositionEffect.onTremorBurst(entity);
    }

    @Override
    public void fireInstant(LivingEntity entity, AttackType attackType) {
        apply(entity);
    }
}
