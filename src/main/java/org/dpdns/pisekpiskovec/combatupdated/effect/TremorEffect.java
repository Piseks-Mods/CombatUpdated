package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Set;

public class TremorEffect extends CUStatusEffect {
    public TremorEffect() {
        super(Set.of(TriggerType.TURN_END));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        if (type == TriggerType.TURN_END) {
            // Passive decay: just consume 3 count, no other effect
        }

        // BURST trigger is handled by TremorBurstEffect directly, not here
    }

    /**
     * Called by TremorBurstEffect - not a normal trigger flow.
     * Depletes hunger by potency, staggers if hunger zeroed, decrements count by 1.
     *
     * @return true if expired
     */
    public boolean applyBurst(Player player) {
        // 1. Deplete hunger by potency
        int newHunger = Math.max(0, player.getFoodData().getFoodLevel() - getPotency());
        player.getFoodData().setFoodLevel(newHunger);

        // 2. Stagger if hunger zeroed
        if (newHunger == 0) {
            int staggerTicks = (int) Math.ceil(getCount() * 1.5f);
            StaggerCapability.get(player).ifpresent(cap -> cap.applyStagger(staggerTicks));
        }

        // 3. Decrement count by 1
        return decrementCount(1);
    }
}
