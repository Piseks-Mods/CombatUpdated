package org.dpdns.pisekpiskovec.combatupdated.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.data.InflictHelper;
import org.dpdns.pisekpiskovec.combatupdated.data.ItemDataManager;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicBulletHandler;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicBulletType;

public record AttackContext(RiskLevel attackerRisk, RiskLevel defenderRisk, AttackType attackType,
                            ResistanceType resistance, boolean isStaggered, boolean hasItemEntry,
                            ItemDataManager.ItemData itemData) {
    public static AttackContext resolve(LivingEntity attacker, LivingEntity target) {
        // Item data
        ItemStack held = attacker.getMainHandItem();
        ItemDataManager.ItemData itemData = ItemDataManager.get(held);
        boolean hasItemEntry = !held.isEmpty() && itemData != ItemDataManager.ItemData.DEFAULT;

        // Attacker risk + type
        RiskLevel attackerRisk;
        AttackType attackType;
        if (hasItemEntry) {
            attackerRisk = itemData.riskLevel().max(MobDataManager.get(attacker).riskLevel());
            attackType = itemData.attackType();
        } else if (attacker instanceof ICUEntity adv) {
            attackerRisk = itemData.riskLevel().max(adv.getRiskLevel());
            attackType = itemData.attackType();
        } else {
            MobDataManager.MobData att = MobDataManager.get(attacker);
            attackerRisk = att.riskLevel();
            attackType = att.attackType();
        }

        // Target risk + type + stagger
        RiskLevel defenderRisk;
        ResistanceType defenderType;
        boolean isStaggered;
        if (target instanceof ICUEntity adv) {
            defenderRisk = adv.getRiskLevel();
            defenderType = adv.getResistance(attackType);
            isStaggered = adv.isStaggered();
        } else {
            MobDataManager.MobData tar = MobDataManager.get(target);
            defenderRisk = tar.riskLevel();
            defenderType = tar.getResistance(attackType);
            isStaggered = StaggerCapability.get(target).map(StaggerCapability::isStaggered).orElse(false);
        }

        MagicBulletType bullet = MagicBulletHandler.getActiveBullet(attacker);
        if (bullet == MagicBulletType.SEVEN)
            defenderType = MagicBulletHandler.applyBullet7ResistanceOverride(attacker, target, defenderType);

        return new AttackContext(attackerRisk, defenderRisk, attackType, defenderType, isStaggered, hasItemEntry, itemData);
    }

    public void applyInflictsAndGains(LivingEntity attacker, LivingEntity target, AttackType attackType) {
        if (hasItemEntry) {
            InflictHelper.apply(target, attacker, target, itemData.inflicts(), attackType);
            InflictHelper.apply(attacker, attacker, target, itemData.gains(), attackType);
        }
        InflictHelper.apply(target, attacker, target, MobDataManager.get(attacker).inflicts(), attackType);
        InflictHelper.apply(attacker, attacker, target, MobDataManager.get(attacker).gains(), attackType);
    }
}
