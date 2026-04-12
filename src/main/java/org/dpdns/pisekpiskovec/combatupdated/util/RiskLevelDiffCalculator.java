package org.dpdns.pisekpiskovec.combatupdated.util;

import org.dpdns.pisekpiskovec.combatupdated.api.RiskLevel;

public class RiskLevelDiffCalculator {
    // attacker ordinal - defender ordinal
    private static final float[] MULTIPLIERS = {
            2.00f, // diff = +4 (ALEPH attacking ZAYIN)
            1.50f, // diff = +3
            1.20f, // diff = +2
            1.00f, // diff = +1
            1.00f, // diff = 0
            0.80f, // diff = -1
            0.70f, // diff = -2
            0.60f, // diff = -3
            0.40f, // diff = -4 (ZAYIN attacking ALEPH)
    };

    public static float getMultiplier(RiskLevel attacker, RiskLevel defender) {
        int diiff = attacker.ordinal() - defender.ordinal(); // range: -4 to +4
        return MULTIPLIERS[4 - diiff]; // index 0 = diff +4; index 8 = diff -4
    }
}
