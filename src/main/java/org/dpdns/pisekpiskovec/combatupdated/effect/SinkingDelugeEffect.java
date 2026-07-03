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

public class SinkingDelugeEffect extends CUStatusEffect {
    public SinkingDelugeEffect() {
        super(props().category(Category.NEGATIVE).stackType(StackType.INSTANT).maxCount(0).maxPotency(0).defaults(0, 0).keywording(Keywordness.REGULAR));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // No trigger type
    }

    public static void apply(LivingEntity entity, AttackType attackType) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            SinkingEffect sinking = (SinkingEffect) cap.getEffect(StatusEffectCapability.EffectType.SINKING);
            if (sinking.isExpired()) return;

            int totalSPDamage = sinking.getCount() * sinking.getPotency();
            sinking.apply(0, 0);

            boolean isPlayerWithSanity = SanityCapability.get(entity).isPresent();
            boolean isMobWithSanity = !isPlayerWithSanity && MobDataManager.get(entity).hasSanity();

            if (!isPlayerWithSanity && !isMobWithSanity) {
                // No SP pool at all - full amount converts to HP damage
                float hpDamage = applyTypeResistance(entity, totalSPDamage, attackType);
                entity.setHealth(Math.max(0f, entity.getHealth() - hpDamage));
                return;
            }

            int currentSP = isPlayerWithSanity ? SanityCapability.get(entity).map(SanityCapability::getSanity).orElse(0) : MobSanityCapability.get(entity).map(MobSanityCapability::getSanity).orElse(0);
            int spAfter = currentSP - totalSPDamage;

            if (spAfter < SanityCapability.MIN_SANITY) {
                int excess = SanityCapability.MIN_SANITY - spAfter;
                float hpDamage = applyTypeResistance(entity, excess, attackType);

                if (isPlayerWithSanity && entity instanceof Player player) {
                    SanityCapability.get(entity).ifPresent(c -> c.setSanityAndSync(SanityCapability.MIN_SANITY, player));
                } else {
                    MobSanityCapability.get(entity).ifPresent(c -> {
                        c.setSanity(MobSanityCapability.MIN_SANITY);
                        c.triggerPanic(entity);
                    });
                }

                entity.setHealth(Math.max(0f, entity.getHealth() - hpDamage));
            } else {
                if (isPlayerWithSanity && entity instanceof Player player) {
                    SanityCapability.get(entity).ifPresent(c -> c.setSanityAndSync(spAfter, player));
                } else {
                    MobSanityCapability.get(entity).ifPresent(c -> c.setSanity(spAfter));
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
