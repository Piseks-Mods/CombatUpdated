package org.dpdns.pisekpiskovec.combatupdated.effect;

import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

public class TremorAmplitudeHelper {

    /**
     * Amplitude Conversion: converts current single Tremor type to newType, preserving P/C.
     * If Superposition is active, adds newType to it (Entanglement behavior).
     * If no Tremor is active, does nothing.
     */
    public static void applyConversion(StatusEffectCapability cap, TremorType newType) {
        // Superposition -> add new type (acts as Entanglement)
        CUStatusEffect sup = cap.getEffect(StatusEffectCapability.EffectType.TREMOR_SUPERPOSITION);
        if (!sup.isExpired() && sup instanceof TremorSuperpositionEffect tSup) {
            tSup.addType(newType);
            return;
        }

        TremorType current = TremorType.getActive(cap);
        if (current == null) return;

        int savedCount = cap.getEffect(current.effectType).getCount();
        int savedPotency = cap.getEffect(current.effectType).getPotency();
        cap.getEffect(current.effectType).apply(0, 0);
        cap.apply(newType.effectType, savedCount, savedPotency);
    }

    /**
     * Amplitude Entanglement: fuses current Tremor type and newType into Superposition.
     * If Superposition is already active, adds newType to it.
     * If no Tremor is active, does nothing.
     */
    public static void applyEntanglement(StatusEffectCapability cap, TremorType newType) {
        // Superposition -> add new type
        CUStatusEffect sup = cap.getEffect(StatusEffectCapability.EffectType.TREMOR_SUPERPOSITION);
        if (!sup.isExpired() && sup instanceof TremorSuperpositionEffect tSup) {
            tSup.addType(newType);
            return;
        }

        TremorType current = TremorType.getActive(cap);
        if (current == null) return;

        int savedCount = cap.getEffect(current.effectType).getCount();
        int savedPotency = cap.getEffect(current.effectType).getPotency();
        cap.getEffect(current.effectType).apply(0, 0);

        // Create Superposition with both types
        CUStatusEffect supEff = cap.getEffect(StatusEffectCapability.EffectType.TREMOR_SUPERPOSITION);
        supEff.apply(savedCount, savedPotency);
        if (supEff instanceof TremorSuperpositionEffect tSup) {
            tSup.addType(current);
            tSup.addType(newType);
        }
    }
}
