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

        // --- Damage de-buffs ---

        float darkFlamePenalty = StatusEffectCapability.get(target).map(cap -> {
            var df = cap.getEffect(StatusEffectCapability.EffectType.POWER_DOWN);
            return df.isExpired() ? 0f : (float) df.getCount();
        }).orElse(0f);

        float powerDownPenalty = StatusEffectCapability.get(attacker).map(cap -> {
            var pd = cap.getEffect(StatusEffectCapability.EffectType.POWER_DOWN);
            return pd.isExpired() ? 0f : (float) pd.getCount();
        }).orElse(0f);

        int poiseChance = StatusEffectCapability.get(attacker).map(cap -> {
            var pd = cap.getEffect(StatusEffectCapability.EffectType.POISE);
            return pd.isExpired() ? 0 : pd.getPotency();
        }).orElse(0);

        // --- Calculate final damage ---

        float raw = event.getAmount();
        float final_ = DamageCalculator.calculate(raw, attackerRisk, defenderRisk, resistance, isStaggered);

        // ---> Attacker side <---
        Random r = new Random();
        if (r.nextInt(100) < Math.min(5 * poiseChance, 100)) { // Pose bonus
            final_ *= 1.2f;
        }
        final_ += 0 /* Attack Power Up placeholder */ - powerDownPenalty; // Attack Power Up & Attack Power Down

        // ---> Target side <---
        final_ = CUMath.increase(final_, darkFlamePenalty - 0 + 0); // Dark flame - Defense Level Up + Defense Level Down
        final_ = CUMath.increase(final_, 10 * 0); // Fragile
        event.setAmount(final_);

        // --- Fire ON_ATTACK effects on attacker ---

        StatusEffectCapability.ifPresent(attacker, cap -> cap.triggerAll(attacker, CUStatusEffect.TriggerType.ON_ATTACK));

        // --- Fire ON_HIT effects on target ---
        // Note: Poise bonus was already consumed above; the proc chance roll
        // happens inside PoiseEffect.onTrigger which sets the bonus for the
        // *next* call - so the order here is:
        //     1. consume last-frame Poise bonus (above)
        //     2. trigger ON_HIT -> Poise may set bonus for the next hit
        //     3. next hit reads is

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

        // --- Threshold-based stagger check ---

        // hpAfter is approximate here since event.setAmount doesn't deal damage yet;
        // Forge applies it after the event. We use it for the threshold check only.
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
