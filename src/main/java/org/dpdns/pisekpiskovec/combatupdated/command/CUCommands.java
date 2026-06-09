package org.dpdns.pisekpiskovec.combatupdated.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class CUCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("cu").requires(source -> source.hasPermission(2)).then(StatusCommand.register()).then(SanityCommand.register()).then(EffectCommand.register()).then(StaggerCommand.register()).then(RiskCommand.register()));
    }
}
