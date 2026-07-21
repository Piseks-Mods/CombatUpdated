package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.damage.TrueDamageSource;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class CUStatusEffect {
    public enum TriggerType {
        TURN_END, // on new day
        ON_HIT, // entity receives a hit
        ON_ATTACK, // this entity lands a hit
        ON_USE, // entity uses an item (attack swing, place, right-click)
    }

    public enum Category {POSITIVE, NEGATIVE, NEUTRAL}

    public enum StackType {
        STACKABLE, // count and potency accumulate on application
        REPLACEABLE, // on application set count and potency to the applied values
        INSTANT, // fires immediately on application, never stored as state
        LOCKED, // fresh application only; external re-application blocked
    }

    public static final class Properties {
        Set<TriggerType> triggers = Set.of();
        Category category = Category.NEUTRAL;
        StackType stackType = StackType.STACKABLE;
        @Nullable String uniqueOf = null;
        int maxCount = 99;
        int maxPotency = 99;
        int defaultCount = 1;
        int defaultPotency = 1;

        public Properties triggers(TriggerType... t) {
            this.triggers = Set.of(t);
            return this;
        }

        public Properties category(Category c) {
            this.category = c;
            return this;
        }

        public Properties stackType(StackType s) {
            this.stackType = s;
            return this;
        }

        public Properties maxCount(int n) {
            this.maxCount = n;
            return this;
        }

        public Properties maxPotency(int n) {
            this.maxPotency = n;
            return this;
        }

        public Properties defaults(int count, int potency) {
            this.defaultCount = count;
            this.defaultPotency = potency;
            return this;
        }

        public Properties uniqueOf(String basicEffectName) {
            this.uniqueOf = basicEffectName;
            return this;
        }
    }

    public static Properties props() {
        return new Properties();
    }

    private int count = 0;
    private int potency = 0;
    private final Set<TriggerType> triggerTypes;
    private final Category category;
    private final StackType stackType;
    @Nullable String uniqueOf;
    private final int maxCount;
    private final int maxPotency;
    private final int defaultCount;
    private final int defaultPotency;

    protected CUStatusEffect(Properties p) {
        this.triggerTypes = p.triggers;
        this.category = p.category;
        this.stackType = p.stackType;
        this.maxCount = p.maxCount;
        this.maxPotency = p.maxPotency;
        this.defaultCount = p.defaultCount;
        this.defaultPotency = p.defaultPotency;
        this.uniqueOf = p.uniqueOf;
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

    /**
     * Bypass all stacking logic.
     * Use for fresh apply and NBT restore.
     */
    public void apply(int count, int potency) {
        this.count = count;
        this.potency = potency;
    }

    public void addPotency(int addPotency) {
        this.potency += addPotency;
    }

    public void addCount(int addCount) {
        this.count += addCount;
    }

    // --- Helpers ---

    /**
     * Decrements count directly without firing onTrigger.
     *
     * @return true if expired.
     */
    public boolean decrementCount(int subCount) {
        this.count -= subCount;
        return count <= 0;
    }

    /**
     * Deals damage that bypasses armor, resistance, and enchantments.
     */
    protected void dealTrueDamage(LivingEntity entity, float amount) {
        entity.hurt(TrueDamageSource.get(entity), amount);
    }

    public boolean hasTrigger(TriggerType type) {
        return triggerTypes.contains(type);
    }

    public Set<TriggerType> getTriggerTypes() {
        return triggerTypes;
    }

    public Category getCategory() {
        return category;
    }

    public StackType getStackType() {
        return stackType;
    }

    public @Nullable String getUniqueOf() {
        return uniqueOf;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getMaxPotency() {
        return maxPotency;
    }

    public int getDefaultCount() {
        return defaultCount;
    }

    public int getDefaultPotency() {
        return defaultPotency;
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
