package org.dpdns.pisekpiskovec.combatupdated.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.MobSanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class SanityEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity killed = event.getEntity();
        var source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity killer)) return;

        // Player handled separately (their sanity system is different)
        if (killer instanceof Player) return;

        // Killer must have mob sanity
        if (!MobDataManager.get(killer).hasSanity()) return;

        MobSanityCapability.get(killer).ifPresent(cap -> {
            var killedData = MobDataManager.get(killed);
            var killerData = MobDataManager.get(killer);

            int riskDiff = killedData.riskLevel().diffFrom(killerData.riskLevel());

            // +5 base, +riskDiff (clamped so minimum gain is always 1)
            int gain = Math.max(1, 5 + riskDiff);
            cap.increase(gain);
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        SanityCapability.ifPresent(player, cap -> cap.syncAttributes(player));
    }
}
