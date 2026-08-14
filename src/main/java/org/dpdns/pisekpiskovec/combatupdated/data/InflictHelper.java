package org.dpdns.pisekpiskovec.combatupdated.data;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament.TheLivingAndTheDepartedEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.Thoracalgia.NebulizerAlphaHandler;
import org.dpdns.pisekpiskovec.combatupdated.effect.TremorAmplitudeHelper;
import org.dpdns.pisekpiskovec.combatupdated.effect.TremorType;

import java.util.List;

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
                // --- Check `require` ---
                if (entry.require() != null) {
                    var attackerCap = StatusEffectCapability.get(attackerSide);
                    if (!attackerCap.isPresent()) continue;
                    boolean[] met = {false};
                    attackerCap.ifPresent(cap -> met[0] = entry.require().checkAndConsume(cap));
                    if (!met[0]) continue;
                }

                // --- Check `requireTarget` ---
                if (entry.requireTarget() != null) {
                    var targetCap = StatusEffectCapability.get(targetSide);
                    if (!targetCap.isPresent()) continue;
                    boolean[] met = {false};
                    targetCap.ifPresent(cap -> met[0] = entry.requireTarget().checkAndConsume(cap));
                    if (!met[0]) continue;
                }

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
                    recipientCap.applyInstant(entry.effect(), recipient, attackType);
                } else {
                    TremorType tremorType = TremorType.fromEffectType(entry.effect());
                    StatusEffectCapability.EffectType uniqueOf = effect.getUniqueOf();

                    if (tremorType != null && entry.amplitudeConversion()) {
                        TremorAmplitudeHelper.applyConversion(recipientCap, tremorType);
                    } else if (tremorType != null && entry.amplitudeEntanglement()) {
                        TremorAmplitudeHelper.applyEntanglement(recipientCap, tremorType);
                    } else if (!effect.isExpired() && uniqueOf != null && effect.getStackType() == CUStatusEffect.StackType.LOCKED) {
                        recipientCap.apply(uniqueOf, entry.count(), entry.potency());
                    } else {
                        recipientCap.apply(entry.effect(), entry.count(), entry.potency());
                    }

                    // Nebulizer α tracking: poise count applied to this recipient
                    if (entry.effect() == StatusEffectCapability.EffectType.POISE && entry.count() > 0 && recipient.level() instanceof ServerLevel sl) {
                        long day = sl.getDayTime() / 24000L;
                        NebulizerAlphaHandler.onPoiseCountApplied(attackerSide, recipient, entry.count(), day);
                    }
                }
            }
        });
    }

    /**
     * Unconditionally consumes effects from `entity` up to the amounts listed.
     * Used for the `spends` data pack array - no condition check, no effect applied.
     */
    public static void spend(LivingEntity spender, LivingEntity target, List<EffectCondition> spends) {
        if (spends.isEmpty()) return;
        StatusEffectCapability.get(spender).ifPresent(cap -> {
            for (EffectCondition spend : spends) {
                CUStatusEffect eff = cap.getEffect(spend.effect());

                if (eff instanceof TheLivingAndTheDepartedEffect tltd) {
                    tltd.spend(spend.count(), spender, target);
                } else {
                    spend.forceConsume(cap);
                }
            }
        });
    }
}
