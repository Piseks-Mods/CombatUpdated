package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.ICUEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.MobSanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;

public class ButterflyEffect extends CUStatusEffect {
    public ButterflyEffect() {
        super(props().triggers(TriggerType.TURN_END, TriggerType.ON_HIT).category(Category.NEGATIVE).stackType(StackType.LOCKED).maxCount(15).maxPotency(15).defaults(0, 0).uniqueOf("SINKING"));
    }

    @Override
    public boolean isExpired() {
        return getCount() <= 0 && getPotency() <= 0;
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        int theLiving = potency;
        int theDeparted = count;

        if (type == TriggerType.ON_HIT) {
            handleOnHit(entity, theLiving, theDeparted);
        } else {
            handleTurnEnd(entity, theLiving);
        }
    }

    private void handleOnHit(LivingEntity entity, int theLiving, int theDeparted) {
        // Heal attacker's SP: Livint / 4; min 1
        LivingEntity attacker = StatusEffectCapability.get(entity).map(StatusEffectCapability::getAttackerContext).orElse(null);

        if (attacker != null) healSP(attacker, Math.max(1, theLiving / 4));

        // If this unit's SP < 0 and Departed > 0: deal Pierce dmg
        if (theDeparted > 0 && getCurrentSP(entity) < 0) {
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

    private void healSP(LivingEntity entity, int amount) {
        if (entity instanceof Player player) {
            SanityCapability.get(entity).ifPresent(cap -> cap.increaseAndSync(amount, player));
        } else {
            MobSanityCapability.get(entity).ifPresent(cap -> cap.increase(amount));
        }
    }

    private int getCurrentSP(LivingEntity entity) {
        var playerSanity = SanityCapability.get(entity);
        if (playerSanity.isPresent()) {
            return playerSanity.map(SanityCapability::getSanity).orElse(0);
        }
        return MobSanityCapability.get(entity).map(MobSanityCapability::getSanity).orElse(Integer.MAX_VALUE);
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
