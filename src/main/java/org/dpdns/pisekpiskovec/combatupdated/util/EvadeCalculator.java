package org.dpdns.pisekpiskovec.combatupdated.util;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.EvadeResult;

public class EvadeCalculator {

    public static EvadeResult calculate(LivingEntity attacker, LivingEntity target, boolean isStaggered) {
        int diff = SpeedCalculator.getEffectiveSpeed(target) - SpeedCalculator.getEffectiveSpeed(attacker);

        if (diff > 10 && !isStaggered) return EvadeResult.FULL;
        if (diff > 06 && !isStaggered) return EvadeResult.PARTIAL;
        return EvadeResult.NONE;
    }
}
