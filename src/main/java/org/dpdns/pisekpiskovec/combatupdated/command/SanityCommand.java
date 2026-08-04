package org.dpdns.pisekpiskovec.combatupdated.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.SanityAPI;

import java.util.Collection;

public class SanityCommand {

    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("sanity").then(Commands.argument("target", EntityArgument.entities())

                // get
                .then(Commands.literal("get").executes(ctx -> execute(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), "get", 0)))
                // set <value>
                .then(Commands.literal("set").then(Commands.argument("value", IntegerArgumentType.integer(-45, 45)).executes(ctx -> execute(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), "set", IntegerArgumentType.getInteger(ctx, "value")))))

                // add <value>
                .then(Commands.literal("add").then(Commands.argument("value", IntegerArgumentType.integer(1, 90)).executes(ctx -> execute(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), "add", IntegerArgumentType.getInteger(ctx, "value")))))

                // reduce <value>
                .then(Commands.literal("reduce").then(Commands.argument("value", IntegerArgumentType.integer(1, 90)).executes(ctx -> execute(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), "reduce", IntegerArgumentType.getInteger(ctx, "value"))))));
    }

    private static int execute(CommandSourceStack source, Collection<? extends net.minecraft.world.entity.Entity> targets, String op, int value) {
        int affected = 0;
        for (var entity : targets) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (SanityAPI.hasSanity(living)) {
                applyOp(op, value, living);
                int s = SanityAPI.getSanity(living);
                source.sendSuccess(() -> Component.literal(living.getName().getString() + " sanity: " + s).withStyle(s >= 0 ? ChatFormatting.AQUA : ChatFormatting.RED), false);
                affected++;
            } else
                source.sendSuccess(() -> Component.literal(living.getName().getString() + " has no sanity capability.").withStyle(ChatFormatting.DARK_GRAY), false);
        }

        if (affected == 0) source.sendFailure(Component.literal("No valid sanity targets found."));
        return affected;
    }

    private static void applyOp(String op, int value, LivingEntity entity) {
        switch (op) {
            case "set" -> SanityAPI.set(entity, value);
            case "add" -> SanityAPI.increase(entity, value);
            case "reduce" -> SanityAPI.reduce(entity, value);
            case "get" -> {
            }
        }
    }
}
