package org.dpdns.pisekpiskovec.combatupdated.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackContext;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.damage.DamageCalculator;
import org.dpdns.pisekpiskovec.combatupdated.damage.TrueDamageSource;
import org.dpdns.pisekpiskovec.combatupdated.data.InflictHelper;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicBulletHandler;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicBulletType;
import org.dpdns.pisekpiskovec.combatupdated.effect.Thoracalgia.NebulizerAlphaHandler;
import org.dpdns.pisekpiskovec.combatupdated.util.DamageModifiers;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class CombatEventHandler {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {

        if (event.getSource().is(TrueDamageSource.TRUE_DAMAGE)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity target = event.getEntity();

        AttackContext ctx = AttackContext.resolve(attacker, target);
        DamageModifiers mods = DamageModifiers.resolve(attacker, target);

        float damage = DamageCalculator.calculate(event.getAmount(), ctx.attackerRisk(), ctx.defenderRisk(), ctx.resistance(), ctx.isStaggered());
        damage = mods.apply(damage, attacker, target);
        event.setAmount(damage);

        NebulizerAlphaHandler.tryFireCombatStart(attacker);
        StatusEffectCapability.ifPresent(attacker, cap -> cap.triggerAll(attacker, CUStatusEffect.TriggerType.ON_ATTACK));
        StatusEffectCapability.ifPresent(target, cap -> cap.setAttackerContext(attacker));
        StatusEffectCapability.ifPresent(target, cap -> cap.triggerAll(target, CUStatusEffect.TriggerType.ON_HIT));
        StatusEffectCapability.ifPresent(target, cap -> cap.setAttackerContext(null));

        ctx.applyInflictsAndGains(attacker, target, ctx.attackType());
        InflictHelper.spend(attacker, target, MobDataManager.get(attacker).spends());
        if (ctx.hasItemEntry()) InflictHelper.spend(attacker, target, ctx.itemData().spends());

        MagicBulletType bullet = MagicBulletHandler.getActiveBullet(attacker);
        if (bullet != null) {
            MagicBulletHandler.onHit(bullet, attacker, target, damage);
            if (bullet == MagicBulletType.SEVEN) attacker.hurt(TrueDamageSource.get(attacker), Float.MAX_VALUE);
        }

        float hpAfter = target.getHealth() - damage;
        StaggerCapability.get(target).ifPresent(stagger -> {
            if (stagger.isStaggered() || stagger.isOnCooldown()) return;
            if (hpAfter <= stagger.getEffectiveThreshold(target)) {
                stagger.applyStagger(40);
                stagger.setCooldown(100);
            }
        });
    }
}
