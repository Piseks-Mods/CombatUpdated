package org.dpdns.pisekpiskovec.combatupdated.data;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.ReloadEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.SinkingDelugeEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.TremorBurstEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

import java.util.List;
import java.util.Locale;

public class InflictHelper {

    /**
     * Applies a list of inflict/gain entries to a target.
     * Instant-trigger effect (no normal TriggerTypes) bypass the generic
     * capability.apply() path entirely and dispatch to their dedicated
     * static handler instead - they are not persistent status state.
     *
     * @param recipient    entity receiving the effect (target for inflicts, attacker for gains)
     * @param attackerSide entity checked/consumed by `consume` conditions
     * @param targetSide   entity checked/consumed by `drain` conditions
     */
    public static void apply(LivingEntity recipient, LivingEntity attackerSide, LivingEntity targetSide, List<InflictEntry> entries, AttackType attackType) {
        if (entries.isEmpty()) return;

        StatusEffectCapability.get(recipient).ifPresent(recipientCap -> {
            for (InflictEntry entry : entries) {
                // --- Check `consume` (attacker-side) ---
                if (entry.consume() != null) {
                    var attackerCap = StatusEffectCapability.get(attackerSide);
                    if (!attackerCap.isPresent()) continue;
                    boolean[] met = {false};
                    attackerCap.ifPresent(cap -> met[0] = entry.consume().checkAndConsume(cap));
                    if (!met[0]) continue;
                }

                // --- Check `drain` (target-side) ---
                if (entry.drain() != null) {
                    var targetCap = StatusEffectCapability.get(targetSide);
                    if (!targetCap.isPresent()) continue;
                    boolean[] met = {false};
                    targetCap.ifPresent(cap -> met[0] = entry.drain().checkAndConsume(cap));
                    if (!met[0]) continue;
                }

                CUStatusEffect effect = recipientCap.getEffect(entry.effect());

                if (effect.getStackType() == CUStatusEffect.StackType.INSTANT) {
                    switch (entry.effect()) {
                        case SINKING_DELUGE -> SinkingDelugeEffect.apply(recipient, attackType);
                        case TREMOR_BURST -> TremorBurstEffect.apply(recipient);
                        case RELOAD -> ReloadEffect.apply(recipient);
                        default -> {
                        }
                    }
                } else {
                    String uniqueOf = effect.getUniqueOf();
                    if (!effect.isExpired() && uniqueOf != null) {
                        try {
                            var baseType = StatusEffectCapability.EffectType.valueOf(uniqueOf.toUpperCase(Locale.ROOT));
                            recipientCap.apply(baseType, entry.count(), entry.potency());
                        } catch (IllegalArgumentException ignored) {
                            // Unknown base type
                        }
                    } else {
                        recipientCap.apply(entry.effect(), entry.count(), entry.potency());
                    }
                }
            }
        });
    }
}
