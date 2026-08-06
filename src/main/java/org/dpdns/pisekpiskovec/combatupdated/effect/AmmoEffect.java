package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;

public class AmmoEffect extends CUStatusEffect {
    public AmmoEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEUTRAL).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 10).managesOwnCount(true));
    }

    @Override
    public boolean isExpired() {
        return getCount() <= 0 && getPotency() <= 0;
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        if (count == 0 && potency > 0) {
            ReloadEffect.apply(entity);
        }
    }

    /**
     * Attempts to spent `amount` Ammo from the entity.
     *
     * @return true if spending succeeded (enough ammo), false if insufficient
     */
    public boolean spend(int amount) {
        if (getCount() < amount) return false;
        apply(getCount() - amount, getPotency());
        return true;
    }

    /**
     * @return true if the entity has at least `amount` Ammo.
     */
    public boolean hasAmmo(int amount) {
        return !isExpired() && getCount() >= amount;
    }

    /**
     * Reload: lose all current Ammo, restore count to capacity (potency).
     * Does nothing if the entity has no Ammo effect active.
     */
    public void reload() {
        int capacity = isExpired() ? getDefaultCount() : getPotency();
        apply(capacity, capacity);
    }
}
