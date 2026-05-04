package org.dpdns.pisekpiskovec.combatupdated.capability.stagger;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.Config;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class StaggerCapabilityTickHandler {
    /**
     * LivingUpdateEvent fires every tick for every LivingEntity.
     * Used to count down stagger and cooldown timers.
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        StaggerCapability.get(event.getEntity()).ifPresent(stagger -> {
            // One-time init: if the entity still has the compiled default but config
            // differs, sync it. Only fires once since compiled default == config default
            // unsledd the server changed it.
            if (stagger.getBaseThresholdFraction() == StaggerCapability.DEFAULT_THRESHOLD_FRACTION && Config.staggerThreshold != StaggerCapability.DEFAULT_THRESHOLD_FRACTION) {
                stagger.setBaseThresholdFraction(Config.staggerThreshold);
            }
            stagger.tick();
        });
    }
}
