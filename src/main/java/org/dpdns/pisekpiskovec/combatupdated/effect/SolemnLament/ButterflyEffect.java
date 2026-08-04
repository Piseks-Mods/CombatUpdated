package org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.ICUEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

import static org.dpdns.pisekpiskovec.combatupdated.api.SanityAPI.getSanity;
import static org.dpdns.pisekpiskovec.combatupdated.api.SanityAPI.increase;

public class ButterflyEffect extends CUStatusEffect {
    public ButterflyEffect() {
        super(props().triggers(TriggerType.TURN_END, TriggerType.ON_HIT).category(Category.NEGATIVE).stackType(StackType.LOCKED).maxCount(15).maxPotency(15).defaults(0, 0).uniqueOf(StatusEffectCapability.EffectType.SINKING));
    }

    @Override
    public boolean isExpired() {
        return getCount() <= 0 && getPotency() <= 0;
    }

    @Override
    protected void onTrigger(LivingEntity entity, int theLiving, int theDeparted, TriggerType type) {

        if (type == TriggerType.ON_HIT) {
            handleOnHit(entity, theLiving, theDeparted);
        } else {
            handleTurnEnd(entity, theLiving);
        }
    }

    private void handleOnHit(LivingEntity entity, int theLiving, int theDeparted) {
        // Heal attacker's SP: Living / 4; min 1
        StatusEffectCapability.get(entity).map(StatusEffectCapability::getAttackerContext).ifPresent(attacker -> increase(attacker, Math.max(1, theLiving / 4)));

        // If this unit's SP < 0 and Departed > 0: deal Pierce dmg
        if (theDeparted > 0 && getSanity(entity) < 0) {
            int sinkingPotency = getSinkingPotency(entity);

            // floor(SinkingPotency / 5) per Departed, total capped at 30
            float damagePerStack = (float) Math.floor(sinkingPotency / 5.0);
            float total = Math.min(30f, damagePerStack * theDeparted);

            // Non-SP units take half damage
            if (!hasSP(entity)) total /= 2f;

            // Apply Pierce resistance
            total = applyPierceResistance(entity, total);

            if (total > 0f) entity.setHealth(Math.max(0f, entity.getHealth() - total));
        }
    }

    private void handleTurnEnd(LivingEntity entity, int theLiving) {
        // Gain Sinking equal to The Living
        if (theLiving > 0)
            StatusEffectCapability.ifPresent(entity, cap -> cap.apply(StatusEffectCapability.EffectType.SINKING, 0, theLiving));

        apply(theLiving, 0);
    }

    private boolean hasSP(LivingEntity entity) {
        return SanityCapability.get(entity).isPresent() || MobDataManager.get(entity).hasSanity();
    }

    private int getSinkingPotency(LivingEntity entity) {
        return StatusEffectCapability.get(entity).map(cap -> {
            var sinking = cap.getEffect(StatusEffectCapability.EffectType.SINKING);
            return sinking.isExpired() ? 0 : sinking.getPotency();
        }).orElse(0);
    }

    private float applyPierceResistance(LivingEntity entity, float damage) {
        ResistanceType res = entity instanceof ICUEntity adv ? adv.getResistance(AttackType.PIERCE) : MobDataManager.get(entity).getResistance(AttackType.PIERCE);
        return damage * (float) res.getMultiplier();
    }
}
