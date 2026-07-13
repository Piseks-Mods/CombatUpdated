package org.dpdns.pisekpiskovec.combatupdated.data;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.SinkingDelugeEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.TremorBurstEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.List;
import java.util.Locale;

public class InflictHelper {

    /**
     * Applies a list of inflict/gain entries to a target.
     * Instant-trigger effect (no normal TriggerTypes) bypass the generic
     * capability.apply() path entirely and dispatch to their dedicated
     * static handler instead - they are not persistent status state.
     */
    public static void apply(LivingEntity target, List<InflictEntry> entries, AttackType attackType) {
        if (entries.isEmpty()) return;

        StatusEffectCapability.get(target).ifPresent(cap -> {
            for (InflictEntry entry : entries) {
                CUStatusEffect effect = cap.getEffect(entry.effect());

                if (effect.getStackType() == CUStatusEffect.StackType.INSTANT) {
                    switch (entry.effect()) {
                        case SINKING_DELUGE -> SinkingDelugeEffect.apply(target, attackType);
                        case TREMOR_BURST -> TremorBurstEffect.apply(target);
                        default -> {
                        }
                    }
                } else {
                    String uniqueOf = effect.getUniqueOf();
                    if (!effect.isExpired() && uniqueOf != null) {
                        try {
                            var baseType = StatusEffectCapability.EffectType.valueOf(uniqueOf.toUpperCase(Locale.ROOT));
                            cap.apply(baseType, entry.count(), entry.potency());
                        } catch (IllegalArgumentException ignored) {
                            // Unknown base type
                        }
                    } else {
                        cap.apply(entry.effect(), entry.count(), entry.potency());
                    }
                }
            }
        });
    }
}
