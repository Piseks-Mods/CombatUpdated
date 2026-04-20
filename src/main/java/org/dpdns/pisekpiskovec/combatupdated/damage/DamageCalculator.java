package org.dpdns.pisekpiskovec.combatupdated.damage;

import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.api.RiskLevel;
import org.dpdns.pisekpiskovec.combatupdated.util.RiskLevelDiffCalculator;

public class DamageCalculator {
    public static float calculate(float baseDamage, RiskLevel attackerRisk, RiskLevel defenderRisk, ResistanceType resistance, boolean isStaggered, float poiseBonus) {
        // Damage modifier: Risk Level difference
        float riskMod = RiskLevelDiffCalculator.getMultiplier(attackerRisk, defenderRisk);

        // Damage modifier: Attack type resistance (or Stagger)
        ResistanceType effectiveResistance = isStaggered ? ResistanceType.FATAL : resistance;
        float resMod = (float) (effectiveResistance.getMultiplier() - 1.0);

        // Final calculation
        return Math.max(0f, baseDamage * (riskMod + resMod + poiseBonus));
    }

    public static float calculate(float baseDamage, RiskLevel attackerRisk, RiskLevel defenderRisk, ResistanceType resistance, boolean isStaggered) {
        return calculate(baseDamage, attackerRisk, defenderRisk, resistance, isStaggered, 0f);
    }
}
