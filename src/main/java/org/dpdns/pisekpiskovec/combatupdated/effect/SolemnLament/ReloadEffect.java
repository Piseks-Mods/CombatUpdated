package org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.MobSanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.damage.TrueDamageSource;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

public class ReloadEffect extends CUStatusEffect {

    public ReloadEffect() {
        super(props().category(Category.NEUTRAL).stackType(StackType.INSTANT).maxPotency(0).maxCount(0).defaults(0, 0));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {

    }

    /**
     * Reload: lose all current Ammo, restore count to capacity (potency).
     * Does nothing if the entity has no Ammo effect active.
     */
    public static void apply(LivingEntity entity) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            CUStatusEffect ammo = cap.getEffect(StatusEffectCapability.EffectType.THE_LIVING_AND_THE_DEPARTED);

            int spConsumed = (30 - ammo.getCount()) / 2;
            if (entity instanceof Player player) {
                SanityCapability.get(entity).ifPresent(spCap -> spCap.reduceAndSync(spConsumed, player));
            } else if (MobSanityCapability.get(entity).isPresent()) {
                MobSanityCapability.get(entity).ifPresent(spCap -> spCap.reduce(spConsumed));
            } else {
                entity.hurt(TrueDamageSource.get(entity), spConsumed);
            }

            int capacity = ammo.isExpired() ? ammo.getDefaultPotency() : ammo.getPotency();
            ammo.apply(capacity, capacity);
        });
    }
}
