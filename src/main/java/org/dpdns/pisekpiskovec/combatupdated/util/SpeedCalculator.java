package org.dpdns.pisekpiskovec.combatupdated.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

public class SpeedCalculator {
    public static int getBaseSpeed(LivingEntity entity) {
        return (int) Math.floor(entity.getAttributeValue(Attributes.MOVEMENT_SPEED) / 0.2);
    }

    /**
     * Effective speed accounts for Bind's potency reduction
     * Clamped to 0 - speed cannot go negative
     */
    public static int getEffectiveSpeed(LivingEntity entity) {
        int base = getBaseSpeed(entity);
        int[] bindReduction = {0};
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            CUStatusEffect bind = cap.getEffect(StatusEffectCapability.EffectType.BIND);
            if (!bind.isExpired()) bindReduction[0] = bind.getPotency();
        });
        return Math.max(0, base - bindReduction[0]);
    }
}
