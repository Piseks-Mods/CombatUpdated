package org.dpdns.pisekpiskovec.combatupdated.api;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.ISanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.MobSanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;

import java.util.Optional;

public class SanityAPI {

    /**
     * Returns whichever sanity capability the entity has, empty if none,
     */
    public static Optional<ISanityCapability> get(LivingEntity entity) {
        var player = SanityCapability.get(entity);
        if (player.isPresent()) return player.map(c -> (ISanityCapability) c);
        var mob = MobSanityCapability.get(entity);
        if (mob.isPresent()) return mob.map(c -> (ISanityCapability) c);
        return Optional.empty();
    }

    public static boolean hasSanity(LivingEntity entity) {
        return get(entity).isPresent();
    }

    public static int getSanity(LivingEntity entity) {
        return get(entity).map(ISanityCapability::getSanity).orElse(0);
    }

    public static void increase(LivingEntity entity, int amount) {
        get(entity).ifPresent(cap -> {
            cap.increase(amount);
            cap.sync(entity);
        });
    }

    public static void reduce(LivingEntity entity, int amount) {
        get(entity).ifPresent(cap -> {
            cap.reduce(amount);
            cap.sync(entity);
        });
    }

    public static void set(LivingEntity entity, int amount) {
        get(entity).ifPresent(cap -> {
            cap.setSanity(amount);
            cap.sync(entity);
        });
    }
}
