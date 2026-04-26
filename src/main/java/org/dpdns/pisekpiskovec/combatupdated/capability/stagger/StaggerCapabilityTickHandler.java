package org.dpdns.pisekpiskovec.combatupdated.capability.stagger;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class StaggerCapabilityTickHandler {
    /**
     * LivingUpdateEvent fires every tick for every LivingEntity.
     * Used to count down stagger and cooldown timers.
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        StaggerCapability.get(event.getEntity()).ifPresent(StaggerCapability::tick);
    }
}
