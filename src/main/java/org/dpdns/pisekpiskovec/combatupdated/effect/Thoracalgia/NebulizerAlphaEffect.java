package org.dpdns.pisekpiskovec.combatupdated.effect.Thoracalgia;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

import java.util.ArrayList;
import java.util.List;

public class NebulizerAlphaEffect extends CUStatusEffect {
    private long lastCombatStartDay = -1;
    private long lastGainedBy = -1;

    public NebulizerAlphaEffect() {
        super(props().triggers(TriggerType.COMBAT_START).category(Category.POSITIVE).stackType(StackType.STACKABLE).maxCount(5).maxPotency(9).defaults(1, 0).managesOwnCount(true));
    }

    @Override
    public boolean isExpired() {
        return getCount() <= 0;
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        if (type == TriggerType.COMBAT_START) {
            getAllies(entity).forEach(ally -> StatusEffectCapability.ifPresent(ally, statusEffectCapability -> {
                statusEffectCapability.apply(StatusEffectCapability.EffectType.POISE, 1, 1);
            }));
        }
    }

    /**
     * Called by NebulizerAlphaHandler when this entity applies Poise count
     * to an ally that also has Nebulizer α. Gains 1 stack per turn on reaching 10.
     */
    public void trackPoiseCountApplied(int count, long currentDay) {
        if (isExpired()) return;

        int tracker = getPotency() + count;
        if (tracker >= 10) {
            if (lastGainedBy != currentDay && getCount() < getMaxCount()) {
                addCount(1);
                lastGainedBy = currentDay;
            }
            tracker -= 10;
        }

        int targetPotency = Math.min(tracker, getMaxPotency());
        addPotency(targetPotency - getPotency());
    }

    /**
     * Guards the COMBAT_START trigger to fire at most once per day.
     *
     * @return true if combat start should be triggered mow
     */
    public boolean tryCombatStart(long currentDay) {
        if (isExpired() || lastCombatStartDay == currentDay) return false;
        lastCombatStartDay = currentDay;
        return true;
    }

    private List<LivingEntity> getAllies(LivingEntity entity) {
        List<LivingEntity> allies = new ArrayList<>();
        allies.add(entity); // self first
        if (entity.level() instanceof ServerLevel) {
            AABB range = entity.getBoundingBox().inflate(16);
            entity.level().getEntitiesOfClass(LivingEntity.class, range, e -> e != entity && entity.isAlliedTo(e) && StatusEffectCapability.get(e).isPresent()).forEach(allies::add);
        }
        return allies;
    }
}
