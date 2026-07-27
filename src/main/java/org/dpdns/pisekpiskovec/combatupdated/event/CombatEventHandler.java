package org.dpdns.pisekpiskovec.combatupdated.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.ICUEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.api.RiskLevel;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.damage.DamageCalculator;
import org.dpdns.pisekpiskovec.combatupdated.damage.TrueDamageSource;
import org.dpdns.pisekpiskovec.combatupdated.data.InflictHelper;
import org.dpdns.pisekpiskovec.combatupdated.data.ItemDataManager;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicBulletHandler;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicBulletType;
import org.dpdns.pisekpiskovec.combatupdated.util.CUMath;

import java.util.Random;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class CombatEventHandler {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().is(TrueDamageSource.TRUE_DAMAGE)) return;

        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        Entity rawAttacker = source.getEntity();
        if (!(rawAttacker instanceof LivingEntity attacker)) return;

        // --- Resolve attacker risk + attack type ---
        RiskLevel attackerRisk;
        AttackType attackType;

        ItemStack heldItem = attacker.getMainHandItem();
        ItemDataManager.ItemData itemData = ItemDataManager.get(heldItem);
        boolean hasItemEntry = !heldItem.isEmpty() && itemData != ItemDataManager.ItemData.DEFAULT;

        if (hasItemEntry) {
            MobDataManager.MobData mobData = MobDataManager.get(attacker);
            attackerRisk = itemData.riskLevel().max(mobData.riskLevel());
            attackType = itemData.attackType();
        } else if (attacker instanceof ICUEntity adv) {
            attackerRisk = adv.getRiskLevel();
            attackType = adv.getAttackType();
        } else {
            MobDataManager.MobData mobData = MobDataManager.get(attacker);
            attackerRisk = mobData.riskLevel();
            attackType = mobData.attackType();
        }

        // --- Resolve defender risk + attack type + stagger ---
        RiskLevel defenderRisk;
        ResistanceType resistance;
        boolean isStaggered;

        if (target instanceof ICUEntity adv) {
            defenderRisk = adv.getRiskLevel();
            resistance = adv.getResistance(attackType);
            isStaggered = adv.isStaggered();
        } else {
            MobDataManager.MobData defData = MobDataManager.get(target);
            defenderRisk = defData.riskLevel();
            resistance = defData.getResistance(attackType);
            isStaggered = StaggerCapability.get(target).map(StaggerCapability::isStaggered).orElse(false);
        }

        // --- Detect Magic Bullet ---
        MagicBulletType activeBullet = MagicBulletHandler.getActiveBullet(attacker);
        if (activeBullet == MagicBulletType.SEVEN)
            resistance = MagicBulletHandler.applyBullet7ResistanceOverride(attacker, target, resistance);

        // --- Damage de-buffs ---
        float darkFlamePenalty = StatusEffectCapability.get(target).map(cap -> {
            var df = cap.getEffect(StatusEffectCapability.EffectType.DARK_FLAME);
            return df.isExpired() ? 0f : (float) df.getCount();
        }).orElse(0f);

        float defenseLevelPenalty = StatusEffectCapability.get(target).map(cap -> {
            var dl = cap.getEffect(StatusEffectCapability.EffectType.DEFENSE_LEVEL_DOWN);
            return dl.isExpired() ? 0f : (float) dl.getCount();
        }).orElse(0f);

        float defenseLevelBonus = StatusEffectCapability.get(target).map(cap -> {
            var dl = cap.getEffect(StatusEffectCapability.EffectType.DEFENSE_LEVEL_UP);
            return dl.isExpired() ? 0f : (float) dl.getCount();
        }).orElse(0f);

        float fragilityPenalty = StatusEffectCapability.get(target).map(cap -> {
            var fr = cap.getEffect(StatusEffectCapability.EffectType.FRAGILE);
            return fr.isExpired() ? 0f : (float) fr.getCount();
        }).orElse(0f);

        int poiseChance = StatusEffectCapability.get(attacker).map(cap -> {
            var pc = cap.getEffect(StatusEffectCapability.EffectType.POISE);
            return pc.isExpired() ? 0 : pc.getPotency();
        }).orElse(0);

        float attackPowerPenalty = StatusEffectCapability.get(attacker).map(cap -> {
            var pd = cap.getEffect(StatusEffectCapability.EffectType.ATTACK_POWER_DOWN);
            return pd.isExpired() ? 0f : (float) pd.getCount();
        }).orElse(0f);

        float attackPowerBonus = StatusEffectCapability.get(attacker).map(cap -> {
            var pu = cap.getEffect(StatusEffectCapability.EffectType.ATTACK_POWER_UP);
            return pu.isExpired() ? 0f : (float) pu.getCount();
        }).orElse(0f);

        float paralyzePenalty = StatusEffectCapability.get(attacker).map(cap -> {
            var pr = cap.getEffect(StatusEffectCapability.EffectType.PARALYZE);
            return pr.isExpired() ? 0f : (float) pr.getCount();
        }).orElse(0f);

        // --- Calculate final damage ---
        float raw = event.getAmount();
        float final_ = DamageCalculator.calculate(raw, attackerRisk, defenderRisk, resistance, isStaggered);

        // ---> Attacker side <---
        Random r = new Random();
        if (r.nextInt(100) < Math.min(5 * poiseChance, 100)) { // Pose bonus
            final_ *= 1.2f;
            StatusEffectCapability.get(attacker).map(cap -> cap.getEffect(StatusEffectCapability.EffectType.POISE).decrementCount(1)); // Consume 1 Count if Critical Hit
        }
        final_ += attackPowerBonus - attackPowerPenalty; // Attack Power Up & Attack Power Down

        // ---> Target side <---
        final_ = CUMath.increase(final_, darkFlamePenalty - defenseLevelBonus + defenseLevelPenalty + Math.min(10f * fragilityPenalty, 100f)); // Dark flame - Defense Level Up + Defense Level Down + Fragile
        if (paralyzePenalty > 0) {
            final_ = 0;
            StatusEffectCapability.get(attacker).map(cap -> cap.getEffect(StatusEffectCapability.EffectType.PARALYZE).decrementCount(1)); // Consume 1 Count On Hit
        }
        if (activeBullet != null) {
            final_ = MagicBulletHandler.modifyDamage(activeBullet, final_, target);
        }
        event.setAmount(final_);

        // --- Fire ON_ATTACK effects on attacker ---
        StatusEffectCapability.ifPresent(attacker, cap -> cap.triggerAll(attacker, CUStatusEffect.TriggerType.ON_ATTACK));

        // --- Fire ON_HIT effects on target ---
        StatusEffectCapability.ifPresent(target, cap -> cap.setAttackerContext(attacker));
        StatusEffectCapability.ifPresent(target, cap -> cap.triggerAll(target, CUStatusEffect.TriggerType.ON_HIT));
        StatusEffectCapability.ifPresent(target, cap -> cap.setAttackerContext(null));

        // --- Apply attacker's inflicts to target ---
        if (hasItemEntry) {
            InflictHelper.apply(target, attacker, target, itemData.inflicts(), attackType);
        }
        InflictHelper.apply(target, attacker, target, MobDataManager.get(attacker).inflicts(), attackType);

        // --- Apply attacker's gains to attacker ---
        if (hasItemEntry) {
            InflictHelper.apply(attacker, attacker, target, itemData.gains(), attackType);
        }
        InflictHelper.apply(attacker, attacker, target, MobDataManager.get(attacker).gains(), attackType);

        // --- Magic Bullet on-hit effects ---
        if (activeBullet != null) {
            MagicBulletHandler.onHit(activeBullet, attacker, target, final_);
            MagicBulletHandler.spendBullet(attacker);
            if (activeBullet == MagicBulletType.SEVEN) {
                attacker.hurt(TrueDamageSource.get(attacker), Float.MAX_VALUE);
            }
        }

        // --- Threshold-based stagger check ---
        float hpAfter = target.getHealth() - final_;
        StaggerCapability.get(target).ifPresent(stagger -> {
            if (stagger.isStaggered() || stagger.isOnCooldown()) return;
            float threshold = stagger.getEffectiveThreshold(target);
            if (hpAfter <= threshold) {
                stagger.applyStagger(40);
                stagger.setCooldown(100);
            }
        });
    }
}
