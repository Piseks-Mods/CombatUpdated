package org.dpdns.pisekpiskovec.combatupdated.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

import java.util.EnumSet;
import java.util.Set;

public class TremorSuperpositionEffect extends CUStatusEffect {
    private final EnumSet<TremorType> activeTypes = EnumSet.noneOf(TremorType.class);

    public TremorSuperpositionEffect() {
        super(props().triggers(TriggerType.TURN_END).category(Category.NEGATIVE).stackType(StackType.STACKABLE).maxCount(99).maxPotency(99).defaults(1, 1).managesOwnCount(true));
    }

    @Override
    protected void onTrigger(LivingEntity entity, int potency, int count, TriggerType type) {
        if (type != TriggerType.TURN_END) return;
        // Convert to base Tremor with same P/C, clear self
        int savedCount = getCount();
        int savedPotency = getPotency();
        apply(0, 0);
        activeTypes.clear();
        StatusEffectCapability.ifPresent(entity, cap -> cap.getEffect(StatusEffectCapability.EffectType.TREMOR).apply(savedCount, savedPotency));
    }

    public void addType(TremorType type) {
        activeTypes.add(type);
    }

    public boolean hasType(TremorType type) {
        return activeTypes.contains(type);
    }

    public Set<TremorType> getActiveTypes() {
        return activeTypes.isEmpty() ? EnumSet.noneOf(TremorType.class) : EnumSet.copyOf(activeTypes);
    }

    @Override
    public void serializeExtra(CompoundTag tag) {
        tag.putIntArray("tremor_types", activeTypes.stream().mapToInt(Enum::ordinal).toArray());
    }

    @Override
    public void deserializeExtra(CompoundTag tag) {
        activeTypes.clear();
        TremorType[] vals = TremorType.values();
        for (int id : tag.getIntArray("tremor_types")) {
            if (id >= 0 && id < vals.length) activeTypes.add(vals[id]);
        }
    }

    /**
     * Called by TremorBurstEffect - fires burst reactions for constituent types.
     */
    public static void onTremorBurst(LivingEntity entity) {
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            CUStatusEffect sup = cap.getEffect(StatusEffectCapability.EffectType.TREMOR_SUPERPOSITION);
            if (!(sup instanceof TremorSuperpositionEffect tSup) || tSup.isExpired()) return;

            if (tSup.hasType(TremorType.EVERLASTING)) {
                TremorEverlastingEffect.onTremorBurst(entity, tSup.getPotency(), tSup.getCount());
            }
        });
    }
}
