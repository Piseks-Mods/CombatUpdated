package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

public class TremorEffect extends CUStatusEffect {
    public TremorEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 1).keywording(Keywordness.KEYWORD));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        // Passive decay: just consume 1 count, no other effect
        // BURST trigger is handled by TremorBurstEffect directly, not here
    }

    /**
     * Called by TremorBurstEffect - not a normal trigger flow.
     * <p>
     * Raises the target's stagger threshold by Tremor Potency (flat HP).
     * Then check if current HP is at or below the new effective threshold -
     * if so, applies stagger for (count * 1.5) ticks rounded up.
     * Tremor count decrements by 1. Works identically for players and mobs.
     *
     * @return true if expired
     */
    public boolean applyBurst(LivingEntity entity) {
        StaggerCapability.get(entity).ifPresent(stagger -> {
            // 1. Raise threshold by potency
            stagger.addThresholdBonus(getPotency());

            // 2. Check if current HP is now at or below the effective threshold
            float effectiveThreshold = stagger.getEffectiveThreshold(entity);
            if (entity.getHealth() <= effectiveThreshold && !stagger.isOnCooldown()) {
                int staggerTicks = (int) Math.ceil(getCount() * 1.5f);
                stagger.applyStagger(staggerTicks);
                stagger.setCooldown(100);
            }
        });

        // 3. Decrement count by 1
        return decrementCount(1);
    }
}
