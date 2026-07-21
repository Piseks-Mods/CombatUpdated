package org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

public class DarkFlameEffect extends CUStatusEffect {
    public DarkFlameEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(7).maxPotency(1).defaults(1, 1).uniqueOf(StatusEffectCapability.EffectType.BURN));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        dealTrueDamage(entity, count * getBurnPotency(entity));
        StatusEffectCapability.ifPresent(entity, statusEffectCapability -> statusEffectCapability.getEffect(StatusEffectCapability.EffectType.DARK_FLAME).apply(0, 0));
    }

    private int getBurnPotency(LivingEntity entity) {
        return StatusEffectCapability.get(entity).map(cap -> {
            var burn = cap.getEffect(StatusEffectCapability.EffectType.BURN);
            return burn.isExpired() ? 0 : burn.getPotency();
        }).orElse(0);
    }
}
