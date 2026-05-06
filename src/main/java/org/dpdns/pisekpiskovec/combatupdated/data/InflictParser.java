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

        JsonArray arr = json.getAsJsonArray("inflicts");
        List<InflictEntry> result = new ArrayList<>(arr.size());

        for (int i = 0; i < arr.size(); i++) {
            try {
                JsonObject entry = arr.get(i).getAsJsonObject();

                if (!entry.has("effect")) {
                    CombatUpdated.LOGGER.warn("[CombatUpdated] inflicts[{}] in '{}' missing 'effect', skipping.", i, fileId);
                    continue;
                }

                String raw = entry.get("effect").getAsString().toLowerCase(Locale.ROOT);
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
}
