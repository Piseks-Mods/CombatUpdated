package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

public class TremorBurstEffect extends CUStatusEffect {
    public TremorBurstEffect() {
        super(props().category(Category.NEGATIVE).stackType(StackType.INSTANT).maxCount(0).maxPotency(0).defaults(0, 0).keywording(Keywordness.REGULAR));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // No trigger type
    }

    public static void apply(LivingEntity entity) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            TremorEffect tremor = (TremorEffect) cap.getEffect(StatusEffectCapability.EffectType.TREMOR);

            if (tremor.isExpired()) return; // No Tremor stack - nothing happens
            tremor.applyBurst(entity);
        });
    }
}
