package org.dpdns.pisekpiskovec.combatupdated.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class OnUseEventHandler {
    private static final Map<Integer, Long> lastUseTick = new HashMap<>();

    private static boolean shouldTrigger(LivingEntity entity) {
        long currentTick = entity.level().getGameTime();
        int id = entity.getId();
        if (lastUseTick.getOrDefault(id, -1L) == currentTick) return false;
        lastUseTick.put(id, currentTick);
        return true;
    }

    // Attack swing
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!shouldTrigger(event.getEntity())) return;
        fireOnUse(event.getEntity());
    }

    // Right-click on block (opening chest, going to bed, shielding, etc.)
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!shouldTrigger(event.getEntity())) return;
        fireOnUse(event.getEntity());
    }

    // Right-click in air (activating items, shields))
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!shouldTrigger(event.getEntity())) return;
        fireOnUse(event.getEntity());
    }

    private static void fireOnUse(LivingEntity entity) {
        StatusEffectCapability.ifPresent(entity, cap -> cap.triggerAll(entity, CUStatusEffect.TriggerType.ON_USE));
    }
}
