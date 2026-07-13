package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

public class AmmoEffect extends CUStatusEffect {
    public AmmoEffect() {
        super(props().category(Category.NEUTRAL).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 1));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {

    }

    /**
     * Attempts to spent `amount` Ammo from the entity.
     *
     * @return true if spending succeeded (enough ammo), false if insufficient
     */
    public boolean spend(int amount) {
        if (getCount() < amount) return false;
        decrementCount(amount);
        return true;
    }

    /**
     * @return true if the entity has at least `amount` Ammo.
     */
    public boolean hasAmmo(int amount) {
        return !isExpired() && getCount() >= amount;
    }
}
