package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

import java.util.Random;

public class TremorEverlastingEffect extends CUStatusEffect {
    private static final Random RNG = new Random();

    private static final ThreadLocal<Boolean> IN_EVERLASTING_BURST = ThreadLocal.withInitial(() -> false);

    public TremorEverlastingEffect() {
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
        if (IN_EVERLASTING_BURST.get()) return;

        StatusEffectCapability.get(entity).ifPresent(cap -> {
            CUStatusEffect eff = cap.getEffect(StatusEffectCapability.EffectType.TREMOR_EVERLASTING);
            if (!(eff instanceof TremorEverlastingEffect te) || te.isExpired()) return;
            onTremorBurst(entity, te.getPotency(), te.getCount());
        });
    }

    public static void onTremorBurst(LivingEntity entity, int potency, int count) {
        if (IN_EVERLASTING_BURST.get()) return;

        // Raise stagger threshold by this effect's potency
        StaggerCapability.get(entity).ifPresent(s -> s.addThresholdBonus(potency));

        IN_EVERLASTING_BURST.set(true);
        try {
            // (Tremor Potency) % chance; max 50 %
            if (RNG.nextInt(100) < Math.min(potency, 50)) TremorBurstEffect.apply(entity);
            // (Tremor Count) % chance; max 50 %
            if (RNG.nextInt(100) < Math.min(count, 50)) TremorBurstEffect.apply(entity);
        } finally {
            IN_EVERLASTING_BURST.set(false);
        }
    }
}
