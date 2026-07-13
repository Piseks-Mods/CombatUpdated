package org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.dpdns.pisekpiskovec.combatupdated.effect.*;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;
import org.dpdns.pisekpiskovec.combatupdated.util.CUMath;
import org.jetbrains.annotations.Nullable;

public class StatusEffectCapability implements INBTSerializable<CompoundTag> {

    @Nullable
    private LivingEntity attackerContext = null;

    public void setAttackerContext(@Nullable LivingEntity attacker) {
        this.attackerContext = attacker;
    }

    @Nullable
    public LivingEntity getAttackerContext() {
        return attackerContext;
    }

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
    private final ButterflyEffect butterfly = new ButterflyEffect();
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

        if (effect.isExpired()) {
            // Fresh application - 0 means "use this effect's default"
            int freshCount = count > 0 ? Math.min(count, maxCount) : effect.getDefaultCount();
            int freshPotency = potency > 0 ? Math.min(potency, maxPotency) : effect.getDefaultPotency();
            if (freshCount == 0 && freshPotency == 0) return;
            effect.apply(freshCount, freshPotency);
        } else {
            int clampedCount = CUMath.clamp(0, count, maxCount);
            int clampedPotency = CUMath.clamp(0, potency, maxPotency);
            if (clampedCount == 0 && clampedPotency == 0) return;

            switch (effect.getStackType()) {
                case STACKABLE -> {
                    if (clampedCount > 0) {
                        // Add count, leave potency untouched
                        int newCount = Math.min(effect.getCount() + clampedCount, maxCount);
                        effect.addCount(newCount - effect.getCount());
                    }
                    if (clampedPotency > 0) {
                        // Add potency, leave count untouched
                        int newPotency = Math.min(effect.getPotency() + clampedPotency, maxPotency);
                        effect.addPotency(newPotency - effect.getPotency());
                    }
                }
                case REPLACEABLE -> {
                    // Each field only updates if the incoming value beats the current
                    int newCount = clampedCount > 0 ? Math.max(effect.getCount(), clampedCount) : effect.getCount();
                    int newPotency = clampedPotency > 0 ? Math.max(effect.getPotency(), clampedPotency) : effect.getPotency();
                    effect.apply(newCount, newPotency);
                }
                case LOCKED -> {
                    // External stacking blocked - InflictHelper handles BUTTERFLY redirect.
                    // Commands force-expire first via effect.apply(0, 0), so fresh works here.
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

            int decrement;
            if (et == EffectType.TREMOR && type == CUStatusEffect.TriggerType.TURN_END) decrement = 3;
            else if (et == EffectType.BUTTERFLY) decrement = 0; // Handled inside onTrigger
            else decrement = 1;
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
            case BUTTERFLY -> butterfly;
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

            if (effect.getStackType() == CUStatusEffect.StackType.INSTANT) continue; // Skip INSTANT

            if (effect.isExpired()) continue; // No point in storing zeroes

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
            // Apply directly - bypasses stacking logic since we're restoring state
            getEffect(type).apply(count, potency);
        }
    }

    // --- Effect type enum ---

    public enum EffectType {
        BLEED, BURN, BUTTERFLY, CHARGE, POISE, POWER_DOWN, RUPTURE, SINKING_DELUGE, SINKING, TREMOR_BURST, TREMOR
    }
}
