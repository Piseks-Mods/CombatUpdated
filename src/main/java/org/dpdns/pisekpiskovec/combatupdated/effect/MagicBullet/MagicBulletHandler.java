package org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.ICUEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;
import org.jetbrains.annotations.Nullable;

public class MagicBulletHandler {
    // --- Detection ---

    @Nullable
    public static MagicBulletType getActiveBullet(LivingEntity attacker) {
        return StatusEffectCapability.get(attacker).map(cap -> {
            CUStatusEffect eff = cap.getEffect(StatusEffectCapability.EffectType.MAGIC_AMMO);
            if (eff.isExpired()) return null;
            return MagicBulletType.fromCount(eff.getCount());
        }).orElse(null);
    }

    // --- 7th Magic Bullet ---
    // Resistance override (call after resistance is resolved)

    /**
     * If either entity's Pierce resistance is below FATAL, force FATAL for this hit.
     */
    public static ResistanceType applyBullet7ResistanceOverride(LivingEntity attacker, LivingEntity target, ResistanceType current) {
        if (getPierceResistance(target).getMultiplier() < ResistanceType.FATAL.getMultiplier() || getPierceResistance(attacker).getMultiplier() < ResistanceType.FATAL.getMultiplier()) {
            return ResistanceType.FATAL;
        }
        return current;
    }

    private static ResistanceType getPierceResistance(LivingEntity entity) {
        if (entity instanceof ICUEntity adv) return adv.getResistance(AttackType.PIERCE);
        return MobDataManager.get(entity).getResistance(AttackType.PIERCE);
    }

    // --- Damage modifier ---

    public static float modifyDamage(MagicBulletType bullet, float damage, LivingEntity target) {
        return switch (bullet) {
            case ONE, THREE -> damage * 1.2f;
            case TWO -> damage * 1.1f;
            case FOUR, FIVE -> damage * 1.3f;
            case SIX -> damage;
            case SEVEN -> {
                float base = damage * 3.0f;
                float maxHP = target.getMaxHealth();
                float currentHP = target.getHealth();
                float missingPct = Math.max(0f, (maxHP - currentHP) / maxHP * 100f);
                float missingBonus = Math.min(2.5f * missingPct, 200f) / 100f;
                yield base + damage * missingBonus;
            }
        };
    }

    // --- On Hit effects ---

    /**
     * @param finalDamage pre-application damage value from CombatEventHandler, used for stagger threshold raises on bullets 1, 4, 6
     */
    public static void onHit(MagicBulletType bullet, LivingEntity attacker, LivingEntity target, float finalDamage) {
        switch (bullet) {
            case ONE -> addStaggerThreshold(target, Math.min(finalDamage, 30f));

            case TWO -> apply(target, StatusEffectCapability.EffectType.PARALYZE, 3, 0);

            case THREE -> {
                apply(target, StatusEffectCapability.EffectType.ATTACK_POWER_DOWN, 2, 0);
                apply(target, StatusEffectCapability.EffectType.BURN, 0, 10);
            }

            case FOUR -> {
                addStaggerThreshold(target, Math.min(finalDamage, 20f));
                apply(target, StatusEffectCapability.EffectType.FRAGILE, 3, 0);
            }

            case FIVE -> {
                apply(target, StatusEffectCapability.EffectType.BURN, 0, 10);
                apply(target, StatusEffectCapability.EffectType.DEFENSE_LEVEL_DOWN, 6, 0);
            }

            case SIX -> {
                addStaggerThreshold(target, Math.min(finalDamage, 30f));
                apply(attacker, StatusEffectCapability.EffectType.ATTACK_POWER_DOWN, 8, 0);
            }

            case SEVEN -> {
                // Self-kill is handled by CombatEventHandler
            }
        }
    }

    // --- Helpers ---
    private static void apply(LivingEntity entity, StatusEffectCapability.EffectType type, int count, int potency) {
        StatusEffectCapability.get(entity).ifPresent(cap -> cap.apply(type, count, potency));
    }

    private static void addStaggerThreshold(LivingEntity entity, float amount) {
        StaggerCapability.get(entity).ifPresent(s -> s.addThresholdBonus(Math.round(amount)));
    }
}
