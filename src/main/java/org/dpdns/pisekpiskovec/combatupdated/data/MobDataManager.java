package org.dpdns.pisekpiskovec.combatupdated.data;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.api.RiskLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MobDataManager {
    public record MobData(RiskLevel riskLevel, AttackType attackType, Map<AttackType, ResistanceType> resistances) {
        public ResistanceType getResistance(AttackType type) {
            return resistances.getOrDefault(type, ResistanceType.NORMAL);
        }

        public static final MobData DEFAULT = new MobData(RiskLevel.ZAYIN, AttackType.BLUNT, Map.of(AttackType.SLASH, ResistanceType.NORMAL, AttackType.PIERCE, ResistanceType.NORMAL, AttackType.BLUNT, ResistanceType.NORMAL));
    }

    // Populated by JSON loading - keyed by "minecraft:zombie" etc.
    private static final Map<String, MobData> registry = new HashMap<>();

    public static MobData get(LivingEntity entity) {
        String key = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())).toString();
        return registry.getOrDefault(key, MobData.DEFAULT);
    }

    public static void register(String entityId, MobData data) {
        registry.put(entityId, data);
    }
}
