package org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

public class MagicAmmoEffect extends CUStatusEffect {
    public MagicAmmoEffect() {
        super(props().category(Category.NEUTRAL).stackType(StackType.STACKABLE).maxCount(7).maxPotency(1).defaults(1, 0).uniqueOf(StatusEffectCapability.EffectType.AMMO));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {

    }

    /**
     * @return true if the entity has at least `amount` Ammo.
     */
    public boolean hasAmmo(int amount) {
        return !isExpired() && getCount() >= amount;
    }
}
