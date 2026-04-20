package org.dpdns.pisekpiskovec.combatupdated.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import org.dpdns.pisekpiskovec.combatupdated.data.ItemDataManager;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class CombatEventHandler {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        Entity rawAttacker = source.getEntity();

        if (!(rawAttacker instanceof LivingEntity attacker)) return;

        // --- Resolve attacker risk + attack type ---

        RiskLevel attackerRisk;
        AttackType attackType;

        if (attacker instanceof Player player) {
            ItemStack held = player.getMainHandItem();
            ItemDataManager.ItemData itemData = ItemDataManager.get(held);
            attackerRisk = itemData.riskLevel();
            attackType = itemData.attackType();
        } else if (attacker instanceof ICUEntity adv) {
            attackerRisk = adv.getRiskLevel();
            attackType = AttackType.BLUNT;
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

        // --- Poise bonus (set by PoiseEffect.onTrigger this same hit) ---

        float poiseBonus = StatusEffectCapability.get(attacker).map(StatusEffectCapability::consumePoiseDamageBonus).orElse(0f);

        // --- Calculate final damage ---

        float raw = event.getAmount();
        float final_ = DamageCalculator.calculate(raw, attackerRisk, defenderRisk, resistance, isStaggered, poiseBonus);
        event.setAmount(final_);

        // --- Fire ON_HIT effects on target ---
        // Note: Poise bonus was already consumed above; the proc chance roll
        // happens inside PoiseEffect.onTrigger which sets the bonus for the
        // *next* call - so the order here is:
        //     1. consume last-frame Poise bonus (above)
        //     2. trigger ON_HIT -> Poise may set bonus for the next hit
        //     3. next hit reads is

        StatusEffectCapability.ifPresent(target, cap -> cap.triggerAll(target, CUStatusEffect.TriggerType.ON_HIT));

        // --- Low-HP stagger check ---

        float hpAfter = target.getHealth() - final_;
        StaggerCapability.get(target).ifPresent(stagger -> {
            if (hpAfter <= 6.0f && !stagger.isOnCooldown()) {
                stagger.applyStagger(40); // 2 seconds
                stagger.setCooldown(200); // 10 seconds immunity
            }
        });
    }
}
