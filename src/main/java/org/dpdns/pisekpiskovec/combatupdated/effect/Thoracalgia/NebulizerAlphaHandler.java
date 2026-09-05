package org.dpdns.pisekpiskovec.combatupdated.effect.Thoracalgia;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

public class NebulizerAlphaHandler {
    /**
     * Called from CombatEventHandler on every attack.
     * Fires COMBAT_START trigger on the attacker at most once per day.
     */
    public static void tryFireCombatStart(LivingEntity attacker) {
        if (!(attacker.level() instanceof ServerLevel sl)) return;
        long currentDay = sl.getDayTime() / 24000L;

        StatusEffectCapability.get(attacker).ifPresent(cap -> {
            CUStatusEffect nebA = cap.getEffect(StatusEffectCapability.EffectType.NEBULIZER_ALPHA);
            if (nebA instanceof NebulizerAlphaEffect nae && nae.tryCombatStart(currentDay)) {
                cap.triggerAll(attacker, CUStatusEffect.TriggerType.COMBAT_START);
            }
        });
    }

    /**
     * Called from InflictHelper after applying Poise count to a recipient.
     * If the recipient has Nebulizer α, notifies the attacker's Nebulizer α tracker.
     */
    public static void onPoiseCountApplied(LivingEntity attacker, LivingEntity recipient, int count, long currentDay) {
        boolean[] recipientHasNeb = {false};
        StatusEffectCapability.get(recipient).ifPresent(cap -> {
            CUStatusEffect neb = cap.getEffect(StatusEffectCapability.EffectType.NEBULIZER_ALPHA);
            recipientHasNeb[0] = !neb.isExpired();
        });

        if (!recipientHasNeb[0]) return;

        StatusEffectCapability.get(attacker).ifPresent(cap -> {
            CUStatusEffect neb = cap.getEffect(StatusEffectCapability.EffectType.NEBULIZER_ALPHA);
            if (neb instanceof NebulizerAlphaEffect nae) {
                nae.trackPoiseCountApplied(count, currentDay);
            }
        });
    }
}
