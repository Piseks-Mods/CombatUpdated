package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.MobSanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Set;

public class SinkingEffect extends CUStatusEffect {
    public SinkingEffect() {
        super(Set.of(TriggerType.ON_HIT));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // Players and mobs with sanity: always drain sanity
        var sanityCap = SanityCapability.get(entity);
        if (sanityCap.isPresent()) {
            sanityCap.ifPresent(cap -> cap.reduce(potency));
            return;
        }

        // Mobs without sanity: deal true HP damage (like Rupture)
        MobDataManager.MobData mobData = MobDataManager.get(entity);
        if (mobData.hasSanity()) {
            MobSanityCapability.get(entity).ifPresent(cap -> {
                cap.reduce(potency);
                if (cap.getSanity() <= MobSanityCapability.MIN_SANITY) {
                    cap.triggerPanic(entity);
                }
            });
        } else {
            dealTrueDamage(entity, potency);
        }
    }
}
