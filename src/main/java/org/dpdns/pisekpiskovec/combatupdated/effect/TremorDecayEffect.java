package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

import java.util.Random;

public class TremorDecayEffect extends CUStatusEffect {
    private static final Random RNG = new Random();

    private static final ThreadLocal<Boolean> IN_EVERLASTING_BURST = ThreadLocal.withInitial(() -> false);

    public TremorDecayEffect() {
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
            CUStatusEffect eff = cap.getEffect(StatusEffectCapability.EffectType.TREMOR_DECAY);
            if (!(eff instanceof TremorDecayEffect td) || td.isExpired()) return;
            StatusEffectCapability.get(entity).ifPresent(cap2 -> cap.apply(StatusEffectCapability.EffectType.DEFENSE_LEVEL_DOWN, eff.getPotency() / 4, 0));
            StaggerCapability.get(entity).ifPresent(s -> s.addThresholdBonus(eff.getPotency()));
        });
    }
}
