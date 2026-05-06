package org.dpdns.pisekpiskovec.combatupdated.capability.stagger;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class StaggerCapabilityTickHandler {
    /**
     * LivingUpdateEvent fires every tick for every LivingEntity.
     * Used to count down stagger and cooldown timers.
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        StaggerCapability.get(entity).ifPresent(stagger -> {
            // One-time threshold init per entity spawn:
            // If still holding the compiled default, resovle it from data pack
            // (per-entity override) then config (global default).
            if (stagger.getBaseThresholdFraction() == StaggerCapability.DEFAULT_THRESHOLD_FRACTION) {
                MobDataManager.MobData data = MobDataManager.get(entity);
                float resolved = data.resolvedStaggerThreshold();
                if (resolved != StaggerCapability.DEFAULT_THRESHOLD_FRACTION) {
                    stagger.setBaseThresholdFraction(resolved);
                }
            }
            stagger.tick();
        });
    }
}
