package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

public class TremorEffect extends CUStatusEffect {
    public TremorEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 1));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // Passive decay: just consume 1 count, no other effect
        // BURST trigger is handled by TremorBurstEffect directly, not here
    }

    /**
     * Called by TremorBurstEffect.apply() when a burst is applied to this entity.
     */
    public static void onTremorBurst(LivingEntity entity) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            CUStatusEffect eff = cap.getEffect(StatusEffectCapability.EffectType.TREMOR);
            if (!(eff instanceof TremorEffect t) || t.isExpired()) return;
            onTremorBurst(entity, t.getPotency());
        });
    }

    public static void onTremorBurst(LivingEntity entity, int potency) {
        // Raise stagger threshold by this effect's potency
        StaggerCapability.get(entity).ifPresent(s -> s.addThresholdBonus(potency));
    }
}
