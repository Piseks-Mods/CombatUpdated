package org.dpdns.pisekpiskovec.combatupdated.capability.stagger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

public class StaggerCapability implements INBTSerializable<CompoundTag> {
    private int staggerTickRemaining = 0;
    private int cooldownTickRemaining = 0; // low-HP stagger cooldown only

    // --- Static accessor ---

    public static LazyOptional<StaggerCapability> get(LivingEntity entity) {
        return entity.getCapability(StaggerCapabilityProvider.CAPABILITY);
    }

    /**
     * Runs the action only if the capability is present.
     */
    public static void ifPresent(LivingEntity entity, java.util.function.Consumer<StaggerCapability> action) {
        get(entity).ifPresent((NonNullConsumer<? super StaggerCapability>) action);
    }

    // --- Stagger control ---

    /**
     * Applies stagger for the given duration.
     * Takes the max so a new trigger never cuts short an exising one.
     */
    public void applyStagger(int ticks) {
        this.staggerTickRemaining = Math.max(this.staggerTickRemaining, ticks);
    }

    public boolean isStaggered() {
        return staggerTickRemaining > 0;
    }

    public void clearStagger() {
        this.staggerTickRemaining = 0;
    }

    // --- Low-HP stagger cooldown ---

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
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        staggerTickRemaining = nbt.getInt("stagger_ticks");
        cooldownTickRemaining = nbt.getInt("cooldown_ticks");
    }
}
