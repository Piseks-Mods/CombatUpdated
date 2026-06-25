package org.dpdns.pisekpiskovec.combatupdated.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.Config;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.ResistanceType;
import org.dpdns.pisekpiskovec.combatupdated.api.RiskLevel;
import org.dpdns.pisekpiskovec.combatupdated.util.CUMath;

import java.util.*;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class MobDataManager extends SimpleJsonResourceReloadListener {

    // --- Data record ---

    public record MobData(RiskLevel riskLevel, AttackType attackType, Map<AttackType, ResistanceType> resistances,
                          float staggerThreshold, List<InflictEntry> inflicts, List<InflictEntry> gains, MobSanityData sanity) {
        public boolean hasSanity() {
            return sanity.isPresent();
        }

        public ResistanceType getResistance(AttackType type) {
            return resistances.getOrDefault(type, ResistanceType.NORMAL);
        }

        public float resolvedStaggerThreshold() {
            return staggerThreshold < 0 ? Config.staggerThreshold : staggerThreshold;
        }

        public static final MobData DEFAULT = new MobData(RiskLevel.ZAYIN, AttackType.BLUNT, Map.of(AttackType.SLASH, ResistanceType.NORMAL, AttackType.PIERCE, ResistanceType.NORMAL, AttackType.BLUNT, ResistanceType.NORMAL), -1, List.of(), List.of(), MobSanityData.NONE);
    }

    // --- Singleton ---

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final MobDataManager INSTANCE = new MobDataManager();

    private static final Map<String, MobData> registry = new HashMap<>();

    private MobDataManager() {
        super(GSON, "mob_data");
    }

    // --- Reload listener registration ---

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    // --- JSON loading ---

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        registry.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation fieldId = entry.getKey();
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                MobData data = parse(json, fieldId);
                if (data == null) continue;

                String entityId = json.get("entity").getAsString();
                registry.put(entityId, data);
            } catch (Exception e) {
                CombatUpdated.LOGGER.error("[CombatUpdated] Failed to load mob_data entry '{}': {}", fieldId, e.getMessage());
            }
        }

        CombatUpdated.LOGGER.info("[CombatUpdated] Loaded {} mob data entries.", registry.size());
    }

    private static MobData parse(JsonObject json, ResourceLocation fileId) {
        // Required: entity_id
        if (!json.has("entity")) {
            CombatUpdated.LOGGER.warn("[CompatUpdated] mob_data '{}' missing 'entity' field, skipping...", fileId);
            return null;
        }

        RiskLevel riskLevel = parseEnum(json, "risk_level", RiskLevel.class, RiskLevel.ZAYIN, fileId); // Required: risk_level
        AttackType attackType = parseEnum(json, "attack_type", AttackType.class, AttackType.BLUNT, fileId); // Required: attack_type

        // Optional: resistance block - missing attack types default to NORMAL
        Map<AttackType, ResistanceType> resistances = new EnumMap<>(AttackType.class);
        if (json.has("resistances")) {
            JsonObject resJson = json.getAsJsonObject("resistances");
            for (AttackType at : AttackType.values()) {
                String key = at.name();
                if (resJson.has(key)) {
                    ResistanceType rt = parseEnum(resJson, key, ResistanceType.class, ResistanceType.NORMAL, fileId);
                    resistances.put(at, rt);
                } else {
                    resistances.put(at, ResistanceType.NORMAL);
                }
            }
        } else {
            // No resistances block - all NORMAL
            for (AttackType at : AttackType.values()) {
                resistances.put(at, ResistanceType.NORMAL);
            }
        }

        // Optional: stagger_threshold (0.0-1.0), -1 if absent
        float staggerThreshold = -1f;
        if (json.has("stagger_threshold")) {
            staggerThreshold = CUMath.clamp(0f, json.get("stagger_threshold").getAsFloat(), 1f);
        }

        List<InflictEntry> inflicts = InflictParser.parse(json, fileId);
        List<InflictEntry> gains = InflictParser.parse(json, "gains", fileId);

        MobSanityData sanity = MobSanityData.NONE;
        if (json.has("sanity")) {
            JsonObject sanityJson = json.getAsJsonObject("sanity");
            List<InflictEntry> panicGains = InflictParser.parse(sanityJson, "panic-gains", fileId);
            sanity = new MobSanityData(panicGains);
        }

        return new MobData(riskLevel, attackType, Collections.unmodifiableMap(resistances), staggerThreshold, inflicts, gains, sanity);
    }

    // --- Lookup ---

    public static MobData get(LivingEntity entity) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (key == null) return MobData.DEFAULT;
        return registry.getOrDefault(key.toString(), MobData.DEFAULT);
    }

    /**
     * Direct key lookup - useful for testing.
     */
    public static MobData get(String entityId) {
        return registry.getOrDefault(entityId, MobData.DEFAULT);
    }

    // --- Helpers ---

    private static <E extends Enum<E>> E parseEnum(JsonObject json, String key, Class<E> enumClass, E fallback, ResourceLocation fileId) {
        if (!json.has(key)) return fallback;
        String raw = json.get(key).getAsString().toUpperCase(Locale.ROOT);
        try {
            return Enum.valueOf(enumClass, raw);
        } catch (IllegalArgumentException e) {
            CombatUpdated.LOGGER.warn("[CombatUpdated] mob_data '{}': unknown {} '{}', using {}.", fileId, enumClass.getSimpleName(), raw, fallback.name());
            return fallback;
        }
    }
}
