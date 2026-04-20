package org.dpdns.pisekpiskovec.combatupdated.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class TurnEndEventHandler {

    // Tracks the last known day per dimension to detect midnight crossings
    private static final Map<String, Long> lastKnownDay = new HashMap<>();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel level)) return;

        String dimKey = level.dimension().location().toString();
        long currentDay = level.getDayTime() / 240000L;
        long lastDay = lastKnownDay.getOrDefault(dimKey, currentDay);

        if (currentDay > lastDay) {
            lastKnownDay.put(dimKey, currentDay);
            // New day - fire TURN_END for all players in this dimension
            // who are NOT in bed (sleeping player fire on wake instead)
            for (Player player : level.players()) {
                if (!player.isSleeping()) {
                    fireTurnEnd(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerWake(PlayerSleepInBedEvent event) {
        // PlayerSleepInBedEvent fires when the player *attempts* to sleep.
        // For wake-up we use PlayerWakeUpEvent instead.
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event) {
        fireTurnEnd(event.getEntity());
    }

    private static void fireTurnEnd(Player player) {
        StatusEffectCapability.ifPresent(player, cap -> cap.triggerAll(player, CUStatusEffect.TriggerType.TURN_END));
    }
}
