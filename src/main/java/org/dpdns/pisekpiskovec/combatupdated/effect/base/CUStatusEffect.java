package org.dpdns.pisekpiskovec.combatupdated.effect.base;

import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public abstract class CUStatusEffect {
    public enum TriggerType {
        TURN_END, // on new day
        ON_HIT, // entity receives a hit
        ON_USE, // entity uses an item (attack swing, place, right-click)
    }

    private int count;
    private int potency;
    private final Set<TriggerType> triggerTypes;

    protected CUStatusEffect(Set<TriggerType> triggerTypes) {
        this.triggerTypes = triggerTypes;
    }

    // --- Core trigger entry point ---

    /**
     * Called by the appropriate event handler when this effect's trigger fires.
     * Runs the effect logic, then decrements count by the given amount.
     *
     * @param countDecrement how many counts to consume (usually 1, Tremor TURN_END uses 3)
     * @return true if the effect has expired (count <= 0)
     */
    public final boolean trigger(LivingEntity entity, TriggerType type, int countDecrement) {
        if (!triggerTypes.contains(type)) return false;
        onTrigger(entity, potency, count, type);
        count -= countDecrement;
        return count <= 0;
    }

    /**
     * Triggers with count decrement of 1.
     */
    public final boolean trigger(LivingEntity entity, TriggerType type) {
        return trigger(entity, type, 1);
    }

    protected abstract void onTrigger(LivingEntity entity, int potency, int count, TriggerType type);

    // --- Stack management ---

    public void apply(int count, int potency) {
        this.count = count;
        this.potency = potency;
    }

    /**
     * Re-applying the same effect: count adds, potency takes the higher value.
     */
    public void stack(int addCount, int newPotency) {
        this.count += addCount;
        this.potency = Math.max(this.potency, newPotency);
    }

    public void addPotency(int addPotency) {
        this.potency += addPotency;
    }

    public void addCount(int addCount) {
        this.count += addCount;
    }

    // --- Helpers ---

    /**
     * Deals damage that bypasses armor, resistance, and enchantments.
     */
    protected void dealTrueDamage(LivingEntity entity, float amount) {
        float newHealth = entity.getHealth() - amount;
        entity.setHealth(Math.max(0f, newHealth));
    }

    public boolean hasTrigger(TriggerType type) {
        return triggerTypes.contains(type);
    }

    public Set<TriggerType> getTriggerTypes() {
        return triggerTypes;
    }

    public int getCount() {
        return count;
    }

    public int getPotency() {
        return potency;
    }

    public boolean isExpired() {
        return count <= 0;
    }
}
