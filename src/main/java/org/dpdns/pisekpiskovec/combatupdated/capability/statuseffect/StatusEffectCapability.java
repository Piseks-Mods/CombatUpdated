package org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapabilityProvider;
import org.dpdns.pisekpiskovec.combatupdated.effect.*;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

public class StatusEffectCapability implements INBTSerializable<CompoundTag> {

    // --- Caps ---
    public static final int MAX_POTENCY = 99;
    public static final int MAX_COUNT = 99;
    public static final int MAX_COUNT_CHARGE = 20;

    // --- Effects ---
    private final BleedEffect bleed = new BleedEffect();
    private final BurnEffect burn = new BurnEffect();
    private final ChargeEffect charge = new ChargeEffect();
    private final PoiseEffect poise = new PoiseEffect();
    private final RuptureEffect rupture = new RuptureEffect();
    private final SinkingEffect sinking = new SinkingEffect();
    private final TremorEffect tremor = new TremorEffect();

    // Transient - set by Poise proc, read and cleared by CombatEventHandler in the same hit
    private float poiseDamageBonus = 0f;

    // --- Static accessor ---
    public static LazyOptional<SanityCapability> get(LivingEntity entity) {
        return entity.getCapability(SanityCapabilityProvider.CAPABILITY);
    }

    public static void ifPresent(LivingEntity entity, java.util.function.Consumer<SanityCapability> action) {
        get(entity).ifPresent((NonNullConsumer<? super SanityCapability>) action);
    }

    // --- Apply / Stack ---

    /**
     * Applies or stacks a status effect.
     * Count and potency are clamped to their respective maximums.
     */
    public void apply(EffectType type, int count, int potency) {
        CUStatusEffect effect = getEffect(type);
        int maxCount = (type == EffectType.CHARGE) ? MAX_COUNT_CHARGE : MAX_COUNT;

        int clampedPotency = Math.min(potency, MAX_POTENCY);

        if (effect.isExpired()) {
            // Fresh application
            int clampedCount = Math.min(count, maxCount);
            effect.apply(clampedCount, clampedPotency);
        } else {
            // Stacking - clamp total count after addition
            int newCount = Math.min(effect.getCount() + count, maxCount);
            effect.stack(newCount - effect.getCount(), clampedPotency);
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
            case RUPTURE -> rupture;
            case SINKING -> sinking;
            case TREMOR -> tremor;
        };
    }

    public TremorEffect getTremor() {
        return tremor;
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
        BLEED, BURN, CHARGE, POISE, RUPTURE, SINKING, TREMOR
    }
}
