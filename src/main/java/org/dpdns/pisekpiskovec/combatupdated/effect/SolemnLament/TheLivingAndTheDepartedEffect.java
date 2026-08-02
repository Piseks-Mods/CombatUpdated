package org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.MobSanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.damage.TrueDamageSource;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

import java.util.Random;

public class TheLivingAndTheDepartedEffect extends CUStatusEffect {
    private static final Random RNG = new Random();

    public TheLivingAndTheDepartedEffect() {
        super(props().category(Category.NEUTRAL).stackType(StackType.STACKABLE).maxCount(20).maxPotency(20).defaults(15, 20).uniqueOf(StatusEffectCapability.EffectType.AMMO));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {

    }

    /**
     * Attempts to spent `amount` Ammo from the entity.
     * Inflicting Butterfly on `target` randomly.
     *
     * @return true if spending succeeded (enough ammo), false if insufficient
     */
    public boolean spend(int amount, LivingEntity spender, LivingEntity target) {
        if (getCount() < amount) {
            reload(spender);
            return false;
        }
        decrementCount(amount);

        //Roll once per ammo point
        int living = 0;
        int departed = 0;
        for (int i = 0; i < amount; i++) {
            if (RNG.nextBoolean()) living++;
            else departed++;
        }

        final int finalLiving = living;
        final int finalDeparted = departed;

        StatusEffectCapability.get(target).ifPresent(cap -> {
            CUStatusEffect butterfly = cap.getEffect(StatusEffectCapability.EffectType.BUTTERFLY);
            if (butterfly.isExpired()) {
                butterfly.apply(finalDeparted, finalLiving);
            } else {
                // Manually accumulate
                int newCount = Math.min(butterfly.getCount() + finalDeparted, butterfly.getMaxCount());
                int newPotency = Math.min(butterfly.getPotency() + finalLiving, butterfly.getMaxPotency());
                butterfly.addCount(newCount - butterfly.getCount());
                butterfly.addPotency(newPotency - butterfly.getPotency());
            }
        });

        return true;
    }

    /**
     * @return true if the entity has at least `amount` Ammo.
     */
    public boolean hasAmmo(int amount) {
        return !isExpired() && getCount() >= amount;
    }

    public void reload(LivingEntity entity) {
        int spConsumed = (30 - getCount()) / 2;
        if (entity instanceof Player player) {
            SanityCapability.get(entity).ifPresent(spCap -> spCap.reduceAndSync(spConsumed, player));
        } else if (MobSanityCapability.get(entity).isPresent()) {
            MobSanityCapability.get(entity).ifPresent(spCap -> spCap.reduce(spConsumed));
        } else {
            entity.hurt(TrueDamageSource.get(entity), spConsumed);
        }

        int capacity = isExpired() ? getDefaultPotency() : getPotency();
        apply(capacity, getDefaultPotency());
    }
}
