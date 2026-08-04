package org.dpdns.pisekpiskovec.combatupdated.capability.sanity;

import net.minecraft.world.entity.LivingEntity;

public interface ISanityCapability {
    int getSanity();

    void setSanity(int value);

    void increase(int amount);

    void reduce(int amount);

    int getMinSanity();

    int getMaxSanity();

    /**
     * Syncs side-effects of sanity change (Luck attribute for players, no-op for mobs).
     * Always call after modifying sanity if entity context is available.
     */
    void sync(LivingEntity entity);
}
