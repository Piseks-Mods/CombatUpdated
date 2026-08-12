package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

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
    @Override
    public void fireInstant(LivingEntity entity, AttackType attackType) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            CUStatusEffect ammo = cap.getEffect(StatusEffectCapability.EffectType.AMMO);
            if (ammo instanceof AmmoEffect ammoEffect) {
                ammoEffect.reload();
            }
        });
    }
}
