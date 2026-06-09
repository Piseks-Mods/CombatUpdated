package org.dpdns.pisekpiskovec.combatupdated.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.base.CUStatusEffect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class EffectCommand {

    private static final SuggestionProvider<CommandSourceStack> EFFECT_SUGGESTIONS = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(StatusEffectCapability.EffectType.values()).map(Enum::name), builder);

    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("effect").then(Commands.argument("target", EntityArgument.entities()).then(Commands.argument("effect", StringArgumentType.word()).suggests(EFFECT_SUGGESTIONS)

                // get
                .then(Commands.literal("get").executes(ctx -> executeGet(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), StringArgumentType.getString(ctx, "effect"))))

                // set <count> <potency>
                .then(Commands.literal("set").then(Commands.argument("count", IntegerArgumentType.integer(0, 99)).then(Commands.argument("potency", IntegerArgumentType.integer(0, 99)).executes(ctx -> executeSet(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), StringArgumentType.getString(ctx, "effect"), IntegerArgumentType.getInteger(ctx, "count"), IntegerArgumentType.getInteger(ctx, "potency"))))))

                .then(Commands.literal("add")
                        // add count <n>
                        .then(Commands.literal("count").then(Commands.argument("n", IntegerArgumentType.integer(1, 99)).executes(ctx -> executeAdd(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), StringArgumentType.getString(ctx, "effect"), IntegerArgumentType.getInteger(ctx, "n"), 0))))
                        // add potency <n>
                        .then(Commands.literal("potency").then(Commands.argument("n", IntegerArgumentType.integer(1, 99)).executes(ctx -> executeAdd(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), StringArgumentType.getString(ctx, "effect"), 0, IntegerArgumentType.getInteger(ctx, "n"))))))

                // clear
                .then(Commands.literal("clear").executes(ctx -> executeClear(ctx.getSource(), EntityArgument.getEntities(ctx, "target"), StringArgumentType.getString(ctx, "effect"))))));
    }

    private static StatusEffectCapability.EffectType parseType(CommandSourceStack source, String raw) {
        try {
            return StatusEffectCapability.EffectType.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Unknown effect: " + raw));
            return null;
        }
    }

    private static int executeGet(CommandSourceStack source, Collection<? extends net.minecraft.world.entity.Entity> targets, String effectName) {
        StatusEffectCapability.EffectType type = parseType(source, effectName);
        if (type == null) return 0;

        int count = 0;
        for (var entity : targets) {
            if (!(entity instanceof LivingEntity living)) continue;
            StatusEffectCapability.get(living).ifPresent(cap -> {
                CUStatusEffect effect = cap.getEffect(type);
                ChatFormatting color = StatusCommand.colorFor(type);
                if (effect.isExpired()) {
                    source.sendSuccess(() -> Component.literal(living.getName().getString() + " - " + type.name() + ": inactive").withStyle(ChatFormatting.DARK_GRAY), false);
                } else {
                    source.sendSuccess(() -> Component.literal(living.getName().getString() + " - " + type.name() + " Count: " + effect.getCount() + " Potency: " + effect.getPotency()).withStyle(color), false);
                }
            });
            count++;
        }
        return count;
    }

    private static int executeSet(CommandSourceStack source, Collection<? extends net.minecraft.world.entity.Entity> targets, String effectName, int count, int potency) {
        StatusEffectCapability.EffectType type = parseType(source, effectName);
        if (type == null) return 0;

        int affected = 0;
        for (var entity : targets) {
            if (!(entity instanceof LivingEntity living)) continue;
            StatusEffectCapability.get(living).ifPresent(cap -> {
                // Clear first, then apply fresh
                cap.getEffect(type).apply(0, 0); // expire
                if (count > 0 || potency > 0) cap.apply(type, count, potency);
                source.sendSuccess(() -> Component.literal("Set " + type.name() + " on " + living.getName().getString() + " → Count: " + count + " Potency: " + potency).withStyle(StatusCommand.colorFor(type)), false);
            });
            affected++;
        }

        int fin = affected;
        if (affected > 1)
            source.sendSuccess(() -> Component.literal("Applied to " + fin + " entities.").withStyle(ChatFormatting.WHITE), false);
        return affected;
    }

    private static int executeAdd(CommandSourceStack source, Collection<? extends net.minecraft.world.entity.Entity> targets, String effectName, int count, int potency) {
        StatusEffectCapability.EffectType type = parseType(source, effectName);
        if (type == null) return 0;

        int affected = 0;
        for (var entity : targets) {
            if (!(entity instanceof LivingEntity living)) continue;
            StatusEffectCapability.get(living).ifPresent(cap -> {
                cap.apply(type, count, potency);
                CUStatusEffect effect = cap.getEffect(type);
                source.sendSuccess(() -> Component.literal("Updated " + type.name() + " on " + living.getName().getString() + " → Count: " + effect.getCount() + " Potency: " + effect.getPotency()).withStyle(StatusCommand.colorFor(type)), false);
            });
            affected++;
        }

        int fin = affected;
        if (affected > 1)
            source.sendSuccess(() -> Component.literal("Applied to " + fin + " entities.").withStyle(ChatFormatting.WHITE), false);
        return affected;
    }

    private static int executeClear(CommandSourceStack source, Collection<? extends net.minecraft.world.entity.Entity> targets, String effectName) {
        StatusEffectCapability.EffectType type = parseType(source, effectName);
        if (type == null) return 0;

        int affected = 0;
        for (var entity : targets) {
            if (!(entity instanceof LivingEntity living)) continue;
            StatusEffectCapability.get(living).ifPresent(cap -> {
                cap.getEffect(type).apply(0, 0);
                source.sendSuccess(() -> Component.literal("Cleared " + type.name() + " from " + living.getName().getString()).withStyle(ChatFormatting.DARK_GRAY), false);
            });
            affected++;
        }
        int fin = affected;
        if (affected > 1)
            source.sendSuccess(() -> Component.literal("Cleared from " + fin + " entities.").withStyle(ChatFormatting.WHITE), false);
        return affected;
    }
}
