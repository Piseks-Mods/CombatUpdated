package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Set;

public class TremorBurstEffect extends CUStatusEffect {
    public TremorBurstEffect() {
        super(Set.of()); // No trigger type
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // No trigger type
    }

    public static void apply(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;

        StatusEffectCapability.get(player).ifPresent(cap -> {
            TremorEffect tremor = (TremorEffect) cap.getEffect(StatusEffectCapability.EffectType.TREMOR);

            if (tremor.isExpired()) return; // No Tremor stack - nothing happens
            tremor.applyBurst(player);
        });
    }
}
