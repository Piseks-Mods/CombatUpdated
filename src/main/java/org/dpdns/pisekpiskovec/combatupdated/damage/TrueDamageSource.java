package org.dpdns.pisekpiskovec.combatupdated.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;

public class TrueDamageSource {
    public static final ResourceKey<DamageType> TRUE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(CombatUpdated.MODID, "true_damage"));

    public static DamageSource get(LivingEntity entity) {
        return entity.level().damageSources().generic();
    }
}
