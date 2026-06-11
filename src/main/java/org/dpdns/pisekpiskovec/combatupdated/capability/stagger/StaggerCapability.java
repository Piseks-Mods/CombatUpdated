package org.dpdns.pisekpiskovec.combatupdated.capability.stagger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

public class StaggerCapability implements INBTSerializable<CompoundTag> {
    public static final float DEFAULT_THRESHOLD_FRACTION = 0.18f;

    private int staggerTickRemaining = 0;
    private int cooldownTickRemaining = 0; // low-HP stagger cooldown only
    private float baseThresholdFraction = DEFAULT_THRESHOLD_FRACTION;
    private float thresholdBonus = 0f;

    // --- Static accessor ---

    public static LazyOptional<StaggerCapability> get(LivingEntity entity) {
        return entity.getCapability(StaggerCapabilityProvider.CAPABILITY);
    }

    /**
     * Runs the action only if the capability is present.
     */
    public static void ifPresent(LivingEntity entity, java.util.function.Consumer<StaggerCapability> action) {
        get(entity).ifPresent(action::accept);
    }

    // --- Threshold ---

    /**
     * The HP value at or below which this entity staggers.
     * = maxHP * baseThresholdFraction + thresholdBonus
     */
    public float getEffectiveThreshold(LivingEntity entity) {
        return (entity.getMaxHealth() * baseThresholdFraction) + thresholdBonus;
    }

    /**
     * Adds a flat HP bonus to the stagger threshold.
     * Called by Tremor Burst with the Tremor potency value.
     */
    public void addThresholdBonus(float bonus) {
        this.thresholdBonus += bonus;
    }

    public void setBaseThresholdFraction(float fraction) {
        this.baseThresholdFraction = fraction;
    }

    public float getBaseThresholdFraction() {
        return baseThresholdFraction;
    }

    public float getThresholdBonus() {
        return thresholdBonus;
    }

    // --- Stagger control ---

    /**
     * Applies stagger for the given duration.
     * Takes the max so a new trigger never cuts short an exising one.
     */
    public void applyStagger(int ticks) {
        this.staggerTickRemaining = Math.max(this.staggerTickRemaining, ticks);
        this.thresholdBonus = 0f; // Reset threshold bonus when stagger fires
    }

    public boolean isStaggered() {
        return staggerTickRemaining > 0;
    }

    public void clearStagger() {
        this.staggerTickRemaining = 0;
    }

    // --- Low-HP stagger cooldown (prevent stagger re-triggering every tick at low HP) ---

    public void setCooldown(int ticks) {
        this.cooldownTickRemaining = ticks;
    }

    public boolean isOnCooldown() {
        return cooldownTickRemaining > 0;
    }

    // -- Tick (call every server tick from TickEvent handler) ---

    public void tick() {
        if (staggerTickRemaining > 0) staggerTickRemaining--;
        if (cooldownTickRemaining > 0) cooldownTickRemaining--;
    }

    // --- NBT ---

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("stagger_ticks", staggerTickRemaining);
        tag.putInt("cooldown_ticks", cooldownTickRemaining);
        tag.putFloat("base_threshold", baseThresholdFraction);
        tag.putFloat("threshold_bonus", thresholdBonus);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        staggerTickRemaining = nbt.getInt("stagger_ticks");
        cooldownTickRemaining = nbt.getInt("cooldown_ticks");
        baseThresholdFraction = nbt.contains("base_threshold") ? nbt.getFloat("base_threshold") : DEFAULT_THRESHOLD_FRACTION;
        thresholdBonus = nbt.getFloat("threshold_bonus");
    }
}
