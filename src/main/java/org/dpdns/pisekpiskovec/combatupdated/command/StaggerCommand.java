package org.dpdns.pisekpiskovec.combatupdated.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;

import java.util.Collection;

public class StaggerCommand {

    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("stagger").then(Commands.argument("target", EntityArgument.entities())
                // get
                .then(Commands.literal("get").executes(ctx -> execute(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), "get", 0)))

                // set <ticks>
                .then(Commands.literal("set").then(Commands.argument("ticks", IntegerArgumentType.integer(1, 72000)).executes(ctx -> execute(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), "set", IntegerArgumentType.getInteger(ctx, "ticks")))))

                // clear
                .then(Commands.literal("clear").executes(ctx -> execute(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), "clear", 0))));
    }

    private static int execute(CommandSourceStack source, Collection<? extends net.minecraft.world.entity.Entity> targets, String op, int ticks) {
        int affected = 0;
        for (var entity : targets) {
            if (!(entity instanceof LivingEntity living)) continue;

            StaggerCapability.get(living).ifPresent(cap -> {
                switch (op) {
                    case "set" -> {
                        cap.applyStagger(ticks);
                        source.sendSuccess(() -> Component.literal(living.getName().getString() + " staggered for " + ticks + " ticks.").withStyle(ChatFormatting.WHITE), false);
                    }
                    case "clear" -> {
                        cap.clearStagger();
                        source.sendSuccess(() -> Component.literal("Cleared stagger from " + living.getName().getString()).withStyle(ChatFormatting.WHITE), false);
                    }
                    case "get" -> {
                        float threshold = cap.getEffectiveThreshold(living);
                        if (cap.isStaggered()) {
                            source.sendSuccess(() -> Component.literal(living.getName().getString() + " is STAGGERed  Threshold: " + String.format("%.1f", threshold) + " HP").withStyle(ChatFormatting.YELLOW), false);
                        } else {
                            source.sendSuccess(() -> Component.literal(living.getName().getString() + " is not staggered  Threshold: " + String.format("%.1f", threshold) + " HP").withStyle(ChatFormatting.WHITE), false);
                        }
                    }
                }
            });

            affected++;
        }

        if (affected == 0) source.sendFailure(Component.literal("No living entities matched."));

        int fin = affected;
        if (affected > 1 && !op.equals("get"))
            source.sendSuccess(() -> Component.literal("Affected " + fin + " entities.").withStyle(ChatFormatting.WHITE), false);

        return affected;
    }
}
