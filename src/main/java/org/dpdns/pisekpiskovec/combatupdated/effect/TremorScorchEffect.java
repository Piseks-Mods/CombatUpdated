package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.damage.TrueDamageSource;

public class TremorScorchEffect extends CUStatusEffect {
    public TremorScorchEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(0, 0).uniqueOf(StatusEffectCapability.EffectType.TREMOR));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // Passive decay: just consume 1 count, no other effect
        // BURST trigger is handled by TremorBurstEffect directly, not here
    }

    /**
     * Called by TremorBurstEffect.apply() when a burst is applied to this entity.
     * Guarded against recursion - additional burst fired here will NOT re-enter.
     */
    public static void onTremorBurst(LivingEntity entity) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            CUStatusEffect scorch = cap.getEffect(StatusEffectCapability.EffectType.TREMOR_SCORCH);
            CUStatusEffect burn = cap.getEffect(StatusEffectCapability.EffectType.BURN);
            if (!(scorch instanceof TremorScorchEffect ts) || ts.isExpired()) return;
            int damage = (scorch.getPotency() + burn.getPotency()) / 2;
            entity.hurt(TrueDamageSource.get(entity), damage);
            burn.decrementCount(1);
            StaggerCapability.get(entity).ifPresent(s -> s.addThresholdBonus(scorch.getPotency()));
        });
    }
}
