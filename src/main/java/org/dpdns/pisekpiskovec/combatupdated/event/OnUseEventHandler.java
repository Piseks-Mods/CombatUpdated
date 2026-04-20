package org.dpdns.pisekpiskovec.combatupdated.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class OnUseEventHandler {

    // Attack swing
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        fireOnUse(player);
    }

    // Right-click on block (opening chest, going to bed, shielding, etc.)
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        fireOnUse(event.getEntity());
    }

    // Right-click in air (activating items, shields))
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        fireOnUse(event.getEntity());
    }

    private static void fireOnUse(Player player) {
        StatusEffectCapability.ifPresent(player, cap -> cap.triggerAll(player, CUStatusEffect.TriggerType.ON_USE));
    }
}
