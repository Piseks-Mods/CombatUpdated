package org.dpdns.pisekpiskovec.combatupdated.data;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.SinkingDelugeEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.TremorBurstEffect;

import java.util.List;

public class InflictHelper {

    /**
     * Applies a list of inflict/gain entries to a target.
     * Instant-trigger effect (no normal TriggerTypes) bypass the generic
     * capability.apply() path entirely and dispatch to their dedicated
     * static handler instead - they are not persistent status state.
     */
    public static void apply(LivingEntity target, List<InflictEntry> entries, AttackType attackType) {
        if (entries.isEmpty()) return;

        for (InflictEntry entry : entries) {
            switch (entry.effect()) {
                case TREMOR_BURST -> TremorBurstEffect.apply(target);
                case SINKING_DELUGE -> SinkingDelugeEffect.apply(target, attackType);
                default ->
                        StatusEffectCapability.ifPresent(target, cap -> cap.apply(entry.effect(), entry.count(), entry.potency()));
            }
        }
    }
}
