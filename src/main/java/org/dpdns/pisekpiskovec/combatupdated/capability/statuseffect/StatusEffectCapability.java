package org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.dpdns.pisekpiskovec.combatupdated.effect.*;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;
import org.dpdns.pisekpiskovec.combatupdated.util.CUMath;

public class StatusEffectCapability implements INBTSerializable<CompoundTag> {

    // --- Effects ---
    /// --- Keyword ---
    private final BleedEffect bleed = new BleedEffect();
    private final BurnEffect burn = new BurnEffect();
    private final ChargeEffect charge = new ChargeEffect();
    private final PoiseEffect poise = new PoiseEffect();
    private final RuptureEffect rupture = new RuptureEffect();
    private final SinkingEffect sinking = new SinkingEffect();
    private final TremorEffect tremor = new TremorEffect();

    /// --- Debuffs ---
    private final PowerDownEffect power_down = new PowerDownEffect();
    private final SinkingDelugeEffect sinking_deluge = new SinkingDelugeEffect();
    private final TremorBurstEffect tremor_burst = new TremorBurstEffect();

    // Transient - set by Poise proc, read and cleared by CombatEventHandler in the same hit
    private float poiseDamageBonus = 0f;

    // --- Static accessor ---
    public static LazyOptional<StatusEffectCapability> get(LivingEntity entity) {
        return entity.getCapability(StatusEffectCapabilityProvider.CAPABILITY);
    }

    public static void ifPresent(LivingEntity entity, java.util.function.Consumer<StatusEffectCapability> action) {
        get(entity).ifPresent(action::accept);
    }

    // --- Apply / Stack ---

    /**
     * Applies or stacks a status effect.
     * Count and potency are clamped to their respective maximums.
     */
    public void apply(EffectType type, int count, int potency) {
        CUStatusEffect effect = getEffect(type);

        if (effect.getStackType() == CUStatusEffect.StackType.INSTANT) return; // Dispatched by InflictHelper directly

        int maxCount = effect.getMaxCount();
        int maxPotency = effect.getMaxPotency();

        int resolvedCount = count > 0 ? count : effect.getDefaultCount();
        int resolvedPotency = potency > 0 ? potency : effect.getDefaultPotency();

        int clampedCount = CUMath.clamp(0, resolvedCount, maxCount);
        int clampedPotency = CUMath.clamp(0, resolvedPotency, maxPotency);

        if (clampedCount == 0 && clampedPotency == 0) return;

        if (effect.isExpired()) {
            // Fresh application
            // If potency=0 -> default to 1 potency
            // If count=0 -> default to 1 count
            int freshCount = clampedCount > 0 ? clampedCount : 1;
            int freshPotency = clampedPotency > 0 ? clampedPotency : 1;
            effect.apply(freshCount, freshPotency);
        } else {
            switch (effect.getStackType()) {
                case STACKABLE -> {
                    if (clampedCount > 0) {
                        // Add count, raise potency if higher
                        int newCount = Math.min(effect.getCount() + clampedCount, maxCount);
                        effect.addCount(newCount - effect.getCount());
                    }
                    if (clampedPotency > 0) {
                        // Add count, leave potency untouched
                        int newPotency = Math.min(effect.getPotency() + clampedPotency, maxPotency);
                        effect.addPotency(newPotency - effect.getPotency());
                    }
                }
                case REPLACEABLE -> {
                    int newCount = Math.max(effect.getCount(), clampedCount);
                    int newPotency = Math.max(effect.getPotency(), clampedPotency);
                    effect.apply(newCount, newPotency);
                }
            }

        }
    }

    // --- Trigger dispatch ---

    /**
     * Fire all effects that respond to the given trigger. Removes expired effects.
     */
    public void triggerAll(net.minecraft.world.entity.LivingEntity entity, CUStatusEffect.TriggerType type) {
        for (EffectType et : EffectType.values()) {
            CUStatusEffect effect = getEffect(et);
            if (effect.isExpired()) continue;
            if (!effect.hasTrigger(type)) continue;

            int decrement = (et == EffectType.TREMOR && type == CUStatusEffect.TriggerType.TURN_END) ? 3 : 1;
            effect.trigger(entity, type, decrement);
        }
    }

    // --- Poise bonus (transient, cleared after each hit) ---

    public void setPoiseDamageBonus(float bonus) {
        this.poiseDamageBonus = bonus;
    }

    /**
     * Read by CombatEventHandler - clears itself after being read.
     */
    public float consumePoiseDamageBonus() {
        float val = this.poiseDamageBonus;
        this.poiseDamageBonus = 0f;
        return val;
    }

    // --- Direct accessors ---

    public CUStatusEffect getEffect(EffectType type) {
        return switch (type) {
            case BLEED -> bleed;
            case BURN -> burn;
            case CHARGE -> charge;
            case POISE -> poise;
            case POWER_DOWN -> power_down;
            case RUPTURE -> rupture;
            case SINKING_DELUGE -> sinking_deluge;
            case SINKING -> sinking;
            case TREMOR_BURST -> tremor_burst;
            case TREMOR -> tremor;
        };
    }

    // --- NBT serialization ---

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (EffectType type : EffectType.values()) {
            CUStatusEffect effect = getEffect(type);
            CompoundTag effectTag = new CompoundTag();
            effectTag.putInt("count", effect.getCount());
            effectTag.putInt("potency", effect.getPotency());
            tag.put(type.name().toLowerCase(), effectTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (EffectType type : EffectType.values()) {
            String key = type.name().toLowerCase();
            if (!nbt.contains(key)) continue;
            CompoundTag effectTag = nbt.getCompound(key);
            int count = effectTag.getInt("count");
            int potency = effectTag.getInt("potency");
            if (count > 0) {
                // Apply directly - bypasses stacking logic since we're restoring state
                getEffect(type).apply(count, potency);
            }
        }
    }

    // --- Effect type enum ---

    public enum EffectType {
        BLEED, BURN, CHARGE, POISE, POWER_DOWN, RUPTURE, SINKING_DELUGE, SINKING, TREMOR_BURST, TREMOR
    }
}
