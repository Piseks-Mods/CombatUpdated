package org.dpdns.pisekpiskovec.combatupdated.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.util.CUMath;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class InflictParser {

    public static List<InflictEntry> parse(JsonObject json, String arrayKey, ResourceLocation fileId) {
        if (!json.has(arrayKey)) return List.of();
        return parseArray(json.getAsJsonArray(arrayKey), fileId); // reuse existing logic but reading json.getAsJsonArray(arrayKey)
    }

    public static List<InflictEntry> parseArray(JsonArray json, ResourceLocation fileId) {
        List<InflictEntry> result = new ArrayList<>(json.size());
        for (int i = 0; i < json.size(); i++) {
            try {
                JsonObject entry = json.get(i).getAsJsonObject();
                if (!entry.has("effect")) {
                    CombatUpdated.LOGGER.warn("[CombatUpdated] inflicts[{}] in '{}' missing 'effect', skipping.", i, fileId);
                    continue;
                }

                String raw = entry.get("effect").getAsString().toUpperCase(Locale.ROOT);
                StatusEffectCapability.EffectType effectType;
                try {
                    effectType = StatusEffectCapability.EffectType.valueOf(raw);
                } catch (IllegalArgumentException e) {
                    CombatUpdated.LOGGER.warn("[CombatUpdated] inflicts[{}] in '{}': unknown effect '{}', skipping.", i, fileId, raw);
                    continue;
                }

                int count = entry.has("count") ? CUMath.clamp(0, entry.get("count").getAsInt(), 99) : 0;
                int potency = entry.has("potency") ? CUMath.clamp(0, entry.get("potency").getAsInt(), 99) : 0;

                ConsumeCondition consume = entry.has("consume") ? parseCondition(entry.getAsJsonObject("consume"), "consume", i, fileId) : null;
                ConsumeCondition drain = entry.has("drain") ? parseCondition(entry.getAsJsonObject("drain"), "consume", i, fileId) : null;

                result.add(new InflictEntry(effectType, count, potency, consume, drain));
            } catch (Exception e) {
                CombatUpdated.LOGGER.warn("[CombatUpdated] inflicts[{}] in '{}' failed to parse: {}", i, fileId, e.getMessage());
            }
        }

        return Collections.unmodifiableList(result);
    }

    private static @Nullable ConsumeCondition parseCondition(JsonObject json, String key, int idx, ResourceLocation fileId) {
        if (!json.has("effect")) {
            CombatUpdated.LOGGER.warn("[CombatUpdate] {}[{}] in '{}' missing 'effect', skipping,", key, idx, fileId);
            return null;
        }
        String raw = json.get("effect").getAsString().toUpperCase(Locale.ROOT);
        StatusEffectCapability.EffectType effectType;
        try {
            effectType = StatusEffectCapability.EffectType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            CombatUpdated.LOGGER.warn("[CombatUpdate] {}[{}] in '{}': unknown effect '{}', skipping,", key, idx, fileId, raw);
            return null;
        }

        int potency = json.has("potency") ? CUMath.clamp(0, json.get("potency").getAsInt(), 99) : 0;
        int count = json.has("count") ? CUMath.clamp(0, json.get("count").getAsInt(), 99) : 0;

        if (potency == 0 && count == 0) {
            CombatUpdated.LOGGER.warn("[CombatUpdate] {}[{}] in '{}' has no potency and count, skipping,", key, idx, fileId);
            return null;
        }
        return new ConsumeCondition(effectType, potency, count);
    }

    /**
     * Parses an optional "inflicts" array from a JSON object.
     * Return an unmodified list, empty if the field is absent or all entries fail.
     * <p>
     * Example JSON:
     * "inflicts": [
     * { "effect: "RUPTURE", "count": 1, "potency": 3 },
     * { "effect: "BLEED", "count": 2, "potency": 0 }
     * ]
     */
    public static List<InflictEntry> parse(JsonObject json, ResourceLocation fileId) {
        if (!json.has("inflicts")) return List.of();
        return parseArray(json.getAsJsonArray("inflicts"), fileId);
    }
}
