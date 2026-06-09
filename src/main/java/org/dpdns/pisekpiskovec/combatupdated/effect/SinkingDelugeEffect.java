package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.ICUEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.MobSanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Set;

public class SinkingDelugeEffect extends CUStatusEffect {
    public SinkingDelugeEffect() {
        super(Set.of()); // No trigger type
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // No trigger type
    }

    public static void apply(LivingEntity entity, AttackType attackType) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            SinkingEffect sinking = (SinkingEffect) cap.getEffect(StatusEffectCapability.EffectType.SINKING);
            if (sinking.isExpired()) return;

            int deluge_effect = sinking.getCount() * sinking.getPotency();
            sinking.apply(0, 0);

            var playerSanityCap = SanityCapability.get(entity);
            var mobSanityCap = MobSanityCapability.get(entity);

            boolean hasSanity = playerSanityCap.isPresent() || mobSanityCap.isPresent();

            if (!hasSanity) {
                float hpDamage = applyTypeResistance(entity, deluge_effect, attackType);
                entity.setHealth(Math.max(0f, entity.getHealth() - hpDamage));
                return;
            }

            int currentSP = playerSanityCap.isPresent() ? playerSanityCap.map(SanityCapability::getSanity).orElse(0) : mobSanityCap.map(MobSanityCapability::getSanity).orElse(0);
            int spAfter = currentSP - deluge_effect;

            if (spAfter < SanityCapability.MIN_SANITY) {
                int excess = SanityCapability.MIN_SANITY - spAfter;
                float hpDamage = applyTypeResistance(entity, excess, attackType);

                if (playerSanityCap.isPresent() && entity instanceof Player player) {
                    playerSanityCap.ifPresent(c -> c.setSanityAndSync(SanityCapability.MIN_SANITY, player));
                } else {
                    mobSanityCap.ifPresent(c -> {
                        c.setSanity(MobSanityCapability.MIN_SANITY);
                        c.triggerPanic(entity);
                    });
                }

                entity.setHealth(Math.max(0f, entity.getHealth() - hpDamage));
            } else {
                if (playerSanityCap.isPresent() && entity instanceof Player player) {
                    playerSanityCap.ifPresent(c -> c.setSanityAndSync(spAfter, player));
                } else {
                    mobSanityCap.ifPresent(c -> c.setSanity(spAfter));
                }
            }
        });
    }

    private static float applyTypeResistance(LivingEntity entity, int rawDamage, AttackType attackType) {
        ResistanceType resistanceType;

        if (entity instanceof ICUEntity adv) {
            resistanceType = adv.getResistance(attackType);
        } else {
            resistanceType = MobDataManager.get(entity).getResistance(attackType);
        }

        return rawDamage * (float) resistanceType.getMultiplier();
    }
}
