package org.dpdns.pisekpiskovec.combatupdated.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.ICUEntity;
import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.api.RiskLevel;
import org.dpdns.pisekpiskovec.combatupdated.data.ItemDataManager;
import org.dpdns.pisekpiskovec.combatupdated.data.MobDataManager;

import java.util.Collection;

public class RiskCommand {


    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("risk").then(Commands.argument("target", EntityArgument.entities()).executes(ctx -> {
            Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "target");

            int count = 0;
            for (var entity : targets) {
                if (!(entity instanceof LivingEntity living)) continue;
                printRisk(ctx.getSource(), living);
                count++;
            }
            if (count == 0) ctx.getSource().sendFailure(Component.literal("No living entities matched."));
            return count;
        }));
    }

    private static void printRisk(CommandSourceStack source, LivingEntity entity) {
        String name = entity.getName().getString();
        source.sendSuccess(() -> Component.literal("=== " + name + " ===").withStyle(ChatFormatting.WHITE), false);

        RiskLevel risk;
        AttackType attackType;

        if (entity instanceof Player player) {
            ItemStack held = player.getMainHandItem();
            ItemDataManager.ItemData itemData = ItemDataManager.get(held);
            boolean hasItem = !held.isEmpty() && itemData != ItemDataManager.ItemData.DEFAULT;

            if (hasItem) {
                MobDataManager.MobData mobData = MobDataManager.get(entity);
                risk = itemData.riskLevel().max(mobData.riskLevel());
                attackType = itemData.attackType();
                source.sendSuccess(() -> Component.literal("  Source: held item (" + held.getItem().getDescriptionId() + ")").withStyle(ChatFormatting.WHITE), false);
            } else {
                risk = RiskLevel.ZAYIN;
                attackType = AttackType.BLUNT;
                source.sendSuccess(() -> Component.literal("  Source: default (no item entry)").withStyle(ChatFormatting.WHITE), false);
            }
        } else if (entity instanceof ICUEntity adv) {
            risk = adv.getRiskLevel();
            attackType = adv.getAttackType();
            source.sendSuccess(() -> Component.literal("  Source: ICUEntity interface").withStyle(ChatFormatting.WHITE), false);
        } else {
            MobDataManager.MobData data = MobDataManager.get(entity);
            risk = data.riskLevel();
            attackType = data.attackType();

            ItemStack held = entity.getMainHandItem();
            if (!held.isEmpty()) {
                ItemDataManager.ItemData itemData = ItemDataManager.get(held);
                if (itemData != ItemDataManager.ItemData.DEFAULT) {
                    risk = itemData.riskLevel().max(data.riskLevel());
                    attackType = itemData.attackType();
                    source.sendSuccess(() -> Component.literal("  Source: mob data + held item (" + held.getItem().getDescriptionId() + ")").withStyle(ChatFormatting.WHITE), false);
                } else {
                    source.sendSuccess(() -> Component.literal("  Source: mob data pack").withStyle(ChatFormatting.WHITE), false);
                }
            } else {
                source.sendSuccess(() -> Component.literal("  Source: mob data pack").withStyle(ChatFormatting.WHITE), false);
            }
        }

        // Printing final data
        final RiskLevel finalRisk = risk;
        final AttackType finalAttackType = attackType;

        source.sendSuccess(() -> Component.literal("  Risk Level: " + finalRisk.name()).withStyle(ChatFormatting.YELLOW), false);

        source.sendSuccess(() -> Component.literal("  Attack Type: " + finalAttackType.name()).withStyle(ChatFormatting.YELLOW), false);

        if (!(entity instanceof Player)) {
            MobDataManager.MobData data = MobDataManager.get(entity);
            source.sendSuccess(() -> Component.literal("  Resistances:").withStyle(ChatFormatting.WHITE), false);

            for (AttackType at : AttackType.values()) {
                ResistanceType res = data.getResistance(at);
                ChatFormatting resColor = switch (res) {
                    case FATAL -> ChatFormatting.DARK_RED;
                    case WEAK -> ChatFormatting.RED;
                    case NORMAL -> ChatFormatting.WHITE;
                    case ENDURED -> ChatFormatting.GRAY;
                    case INEFFECTIVE -> ChatFormatting.DARK_GRAY;
                };
                source.sendSuccess(() -> Component.literal("    " + at.name() + ": " + res.name()).withStyle(resColor), false);
            }
        }
    }
}
