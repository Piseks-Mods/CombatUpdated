package org.dpdns.pisekpiskovec.combatupdated.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapabilityProvider;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapabilityProvider;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class CapabilityEvents {

    // Called on the MOD bus - register capability types
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        // event.register(StatusEffectCapability.class);
        event.register(StaggerCapability.class);
        event.register(SanityCapability.class);
    }

    // Called on the FORGE bus - attach to players
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (!(event.getObject() instanceof Player)) return;

        // event.addCapability(
        //         new ResourceLocation(CombatUpdated.MODID, "status_effects"),
        //         new StatusEffectCapabilityProvider()
        // );
        event.addCapability(new ResourceLocation(CombatUpdated.MODID, "stagger"), new StaggerCapabilityProvider());
        event.addCapability(ResourceLocation.fromNamespaceAndPath(CombatUpdated.MODID, "sanity"), new SanityCapabilityProvider());
    }

    // Copy capabilities on player respawn / dimensional travel
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();

        // Status effects - intentionally NOT copied on death, only on dimension travel

        // Stagger - intentionally NOT copied on death, only on dimension travel
        if (!event.isWasDeath()) {
            event.getOriginal().getCapability(StaggerCapabilityProvider.CAPABILITY).ifPresent(old -> event.getEntity().getCapability(StaggerCapabilityProvider.CAPABILITY).ifPresent(fresh -> fresh.deserializeNBT(old.serializeNBT())));
        }

        // Sanity - intentionally NOT copied on death, only on dimension travel
        if (!event.isWasDeath()) {
            event.getOriginal().getCapability(SanityCapabilityProvider.CAPABILITY).ifPresent(old -> event.getEntity().getCapability(SanityCapabilityProvider.CAPABILITY).ifPresent(fresh -> fresh.deserializeNBT(old.serializeNBT())));
        }

        event.getOriginal().invalidateCaps();
    }
}
