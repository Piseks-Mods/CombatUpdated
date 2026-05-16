package org.dpdns.pisekpiskovec.combatupdated.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect.StatusEffectCapability;
import org.dpdns.pisekpiskovec.combatupdated.util.CUMath;

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

                int count = entry.has("count") ? entry.get("count").getAsInt() : 0;
                int potency = entry.has("potency") ? entry.get("potency").getAsInt() : 0;

                // Clamp
                count = CUMath.clamp(0, count, 99);
                potency = CUMath.clamp(0, potency, 99);

                result.add(new InflictEntry(effectType, count, potency));
            } catch (Exception e) {
                CombatUpdated.LOGGER.warn("[CombatUpdated] inflicts[{}] in '{}' failed to parse: {}", i, fileId, e.getMessage());
            }
        }

        return Collections.unmodifiableList(result);
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
