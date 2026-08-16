package org.dpdns.pisekpiskovec.combatupdated.util;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicBulletHandler;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicBulletType;

import java.util.Random;

public record DamageModifiers(float darkFlame, float defenseLevelDown, float defenseLevelUp, float fragility,
                              int poiseChance, float attackPowerDown, float attackPowerUp, float paralyze) {
    public static DamageModifiers resolve(LivingEntity attacker, LivingEntity target) {
        return new DamageModifiers(readCount(target, StatusEffectCapability.EffectType.DARK_FLAME), readCount(target, StatusEffectCapability.EffectType.DEFENSE_LEVEL_DOWN), readCount(target, StatusEffectCapability.EffectType.DEFENSE_LEVEL_UP), readCount(target, StatusEffectCapability.EffectType.FRAGILE), (int) readPotency(attacker, StatusEffectCapability.EffectType.POISE), readCount(attacker, StatusEffectCapability.EffectType.ATTACK_POWER_DOWN), readCount(attacker, StatusEffectCapability.EffectType.ATTACK_POWER_UP), readCount(attacker, StatusEffectCapability.EffectType.PARALYZE));
    }

    public float apply(float damage, LivingEntity attacker, LivingEntity target) {
        // Poise
        if (poiseChance > 0 && new Random().nextInt() < Math.min(5 * poiseChance, 100)) {
            damage *= 1.2f;
            StatusEffectCapability.get(attacker).ifPresent(cap -> cap.getEffect(StatusEffectCapability.EffectType.POISE).decrementCount(1));
        }

        damage += attackPowerUp - attackPowerDown;
        damage = CUMath.increase(damage, damage - defenseLevelUp + defenseLevelDown + Math.min(10f * fragility, 100f));

        // Magic Bullet
        MagicBulletType bullet = MagicBulletHandler.getActiveBullet(attacker);
        if (bullet != null) damage = MagicBulletHandler.modifyDamage(bullet, damage, target);

        // Paralyze
        if (paralyze > 0) {
            damage = 0;
            StatusEffectCapability.get(attacker).ifPresent(cap -> cap.getEffect(StatusEffectCapability.EffectType.PARALYZE).decrementCount(1));
        }

        return damage;
    }

    private static float readCount(LivingEntity entity, StatusEffectCapability.EffectType type) {
        return StatusEffectCapability.get(entity).map(cap -> {
            var eff = cap.getEffect(type);
            return eff.isExpired() ? 0f : (float) eff.getCount();
        }).orElse(0f);
    }

    private static float readPotency(LivingEntity entity, StatusEffectCapability.EffectType type) {
        return StatusEffectCapability.get(entity).map(cap -> {
            var eff = cap.getEffect(type);
            return eff.isExpired() ? 0f : (float) eff.getPotency();
        }).orElse(0f);
    }
}
