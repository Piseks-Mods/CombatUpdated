package org.dpdns.pisekpiskovec.combatupdated.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.MobSanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.sanity.SanityCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.stagger.StaggerCapability;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.effect.CUStatusEffect;

import java.util.Collection;

public class StatusCommand {

    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("status").then(Commands.argument("target", EntityArgument.entities()).executes(ctx -> {
            Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "target");

            int count = 0;
            for (var entity : targets) {
                if (!(entity instanceof LivingEntity living)) continue;
                printStatus(ctx.getSource(), living);
                count++;
            }
            if (count == 0) {
                ctx.getSource().sendFailure(Component.literal("No living entities matched."));
            }
            return count;
        }));
    }

    private static void printStatus(CommandSourceStack source, LivingEntity entity) {
        String name = entity.getName().getString();

        source.sendSuccess(() -> Component.literal("=== " + name + " ===").withStyle(ChatFormatting.WHITE), false);

        // --- Status effects ---
        StatusEffectCapability.get(entity).ifPresent(cap -> {
            boolean anyActive = false;
            for (var type : StatusEffectCapability.EffectType.values()) {
                CUStatusEffect effect = cap.getEffect(type);
                if (effect.isExpired()) continue;
                anyActive = true;

                ChatFormatting color = colorFor(cap, type);
                MutableComponent line = Component.literal(String.format("  %-14s Count: %2d  Potency: %2d", type.name(), effect.getCount(), effect.getPotency())).withStyle(color);
                source.sendSuccess(() -> line, false);
            }
            if (!anyActive) {
                source.sendSuccess(() -> Component.literal("  No active effects.").withStyle(ChatFormatting.DARK_GRAY), false);
            }
        });

        // --- Player sanity ---
        SanityCapability.get(entity).ifPresent(cap -> {
            ChatFormatting color = cap.getSanity() >= 0 ? ChatFormatting.AQUA : ChatFormatting.RED;
            source.sendSuccess(() -> Component.literal("  Sanity: " + cap.getSanity()).withStyle(color), false);
        });

        // --- Mob sanity ---
        MobSanityCapability.get(entity).ifPresent(cap -> {
            ChatFormatting color = cap.getSanity() >= 0 ? ChatFormatting.AQUA : ChatFormatting.RED;
            source.sendSuccess(() -> Component.literal("  Sanity: " + cap.getSanity() + (cap.hasPanicked() ? " [PANICKED]" : "")).withStyle(color), false);
        });

        // --- Stagger ---
        StaggerCapability.get(entity).ifPresent(cap -> {
            if (cap.isStaggered()) {
                source.sendSuccess(() -> Component.literal("  STAGGERED (threshold: " + String.format("%.1f", cap.getEffectiveThreshold(entity)) + " HP)").withStyle(ChatFormatting.GOLD), false);
            } else {
                source.sendSuccess(() -> Component.literal("  Not staggered (threshold: " + String.format("%.1f", cap.getEffectiveThreshold(entity)) + " HP)").withStyle(ChatFormatting.WHITE), false);
            }
        });
    }

    static ChatFormatting colorFor(StatusEffectCapability cap, StatusEffectCapability.EffectType type) {
        return switch (cap.getEffect(type).getCategory()) {
            case POSITIVE -> ChatFormatting.GOLD;
            case NEUTRAL -> ChatFormatting.GRAY;
            case NEGATIVE -> ChatFormatting.RED;
        };
    }
}
