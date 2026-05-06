package org.dpdns.pisekpiskovec.combatupdated.data;

import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;

import java.util.List;

public class InflictHelper {

    public static void apply(LivingEntity target, List<InflictEntry> inflicts) {
        if (inflicts.isEmpty()) return;

        StatusEffectCapability.ifPresent(target, cap -> {
            for (InflictEntry entry : inflicts) {
                cap.apply(entry.effect(), entry.count(), entry.potency());
            }
        });
    }
}
