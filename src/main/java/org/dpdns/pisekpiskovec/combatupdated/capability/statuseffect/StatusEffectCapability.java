package org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.effect.*;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.DarkFlameEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.MagicBullet.MagicAmmoEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament.ButterflyEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament.TheLivingAndTheDepartedEffect;
import org.dpdns.pisekpiskovec.combatupdated.effect.Thoracalgia.NebulizerAlphaEffect;
import org.dpdns.pisekpiskovec.combatupdated.util.CUMath;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
    // --- Keyword ---
    private final BleedEffect bleed = new BleedEffect();
    private final BurnEffect burn = new BurnEffect();
    private final ChargeEffect charge = new ChargeEffect();
    private final PoiseEffect poise = new PoiseEffect();
    private final RuptureEffect rupture = new RuptureEffect();
    private final SinkingEffect sinking = new SinkingEffect();
    private final TremorEffect tremor = new TremorEffect();

    // --- Buffs ---
    private final AttackPowerUpEffect attack_power_up = new AttackPowerUpEffect();
    private final DefenseLevelUpEffect defense_level_up = new DefenseLevelUpEffect();
    private final NebulizerAlphaEffect nebulizer_alpha = new NebulizerAlphaEffect();

    // --- Neutral ---
    private final AmmoEffect ammo = new AmmoEffect();
    private final MagicAmmoEffect magic_ammo = new MagicAmmoEffect();
    private final ReloadEffect reload = new ReloadEffect();
    private final org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament.ReloadEffect reload_sl = new org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament.ReloadEffect();
    private final TheLivingAndTheDepartedEffect the_living_and_the_departed = new TheLivingAndTheDepartedEffect();

    // --- Debuffs ---
    private final AttackPowerDownEffect attack_power_down = new AttackPowerDownEffect();
    private final ButterflyEffect butterfly = new ButterflyEffect();
    private final DarkFlameEffect dark_flame = new DarkFlameEffect();
    private final DefenseLevelDownEffect defense_level_down = new DefenseLevelDownEffect();
    private final FragileEffect fragile = new FragileEffect();
    private final NailsEffect nails = new NailsEffect();
    private final ParalyzeEffect paralyze = new ParalyzeEffect();
    private final SinkingDelugeEffect sinking_deluge = new SinkingDelugeEffect();
    private final TremorBurstEffect tremor_burst = new TremorBurstEffect();
    private final TremorEverlastingEffect tremor_everlasting = new TremorEverlastingEffect();

    // --- Static accessor ---
    public static LazyOptional<StatusEffectCapability> get(LivingEntity entity) {
        return entity.getCapability(StatusEffectCapabilityProvider.CAPABILITY);
    }

    public static void ifPresent(LivingEntity entity, Consumer<StatusEffectCapability> action) {
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
            int freshPotency = potency > 0 ? Math.min(potency, maxPotency) : count > 0 ? 1 : effect.getDefaultPotency();
            int freshCount = count > 0 ? Math.min(count, maxCount) : potency > 0 ? 1 : effect.getDefaultCount();
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

    public void applyInstant(EffectType type, LivingEntity entity, AttackType attackType) {
        CUStatusEffect effect = getEffect(type);
        if (effect.getStackType() != CUStatusEffect.StackType.INSTANT) return;
        switch (type) {
            case SINKING_DELUGE -> SinkingDelugeEffect.apply(entity, attackType);
            case TREMOR_BURST -> TremorBurstEffect.apply(entity);
            case RELOAD -> ReloadEffect.apply(entity);
            case THE_LIVING_AND_THE_DEPARTED_RELOAD ->
                    org.dpdns.pisekpiskovec.combatupdated.effect.SolemnLament.ReloadEffect.apply(entity);
            default -> {
            }
        }
    }

    // --- Trigger dispatch ---

    /**
     * Fire all effects that respond to the given trigger. Removes expired effects.
     */
    public void triggerAll(LivingEntity entity, CUStatusEffect.TriggerType type) {
        for (EffectType et : EffectType.values()) {
            CUStatusEffect effect = getEffect(et);
            if (effect.isExpired()) continue;
            if (!effect.hasTrigger(type)) continue;

            int decrement = effect.managesOwnCount() ? 0 : 1;
            effect.trigger(entity, type, decrement);
        }
    }

    // --- Direct accessors ---

    public CUStatusEffect getEffect(EffectType type) {
        return switch (type) {
            case AMMO -> ammo;
            case ATTACK_POWER_DOWN -> attack_power_down;
            case ATTACK_POWER_UP -> attack_power_up;
            case BLEED -> bleed;
            case BURN -> burn;
            case BUTTERFLY -> butterfly;
            case CHARGE -> charge;
            case DARK_FLAME -> dark_flame;
            case DEFENSE_LEVEL_DOWN -> defense_level_down;
            case DEFENSE_LEVEL_UP -> defense_level_up;
            case FRAGILE -> fragile;
            case MAGIC_AMMO -> magic_ammo;
            case NAILS -> nails;
            case NEBULIZER_ALPHA -> nebulizer_alpha;
            case PARALYZE -> paralyze;
            case POISE -> poise;
            case RELOAD -> reload;
            case RUPTURE -> rupture;
            case SINKING_DELUGE -> sinking_deluge;
            case SINKING -> sinking;
            case THE_LIVING_AND_THE_DEPARTED -> the_living_and_the_departed;
            case THE_LIVING_AND_THE_DEPARTED_RELOAD -> reload_sl;
            case TREMOR_BURST -> tremor_burst;
            case TREMOR -> tremor;
            case TREMOR_EVERLASTING -> tremor_everlasting;
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
        AMMO, ATTACK_POWER_DOWN, ATTACK_POWER_UP, BLEED, BURN, BUTTERFLY, CHARGE, DARK_FLAME, DEFENSE_LEVEL_DOWN, DEFENSE_LEVEL_UP, FRAGILE, MAGIC_AMMO, NAILS, NEBULIZER_ALPHA, PARALYZE, POISE, RELOAD, RUPTURE, SINKING_DELUGE, SINKING, THE_LIVING_AND_THE_DEPARTED, THE_LIVING_AND_THE_DEPARTED_RELOAD, TREMOR_BURST, TREMOR_EVERLASTING, TREMOR
    }
}
