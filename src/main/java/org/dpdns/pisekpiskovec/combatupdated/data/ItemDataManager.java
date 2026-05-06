package org.dpdns.pisekpiskovec.combatupdated.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.dpdns.pisekpiskovec.combatupdated.CombatUpdated;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.RiskLevel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID)
public class ItemDataManager extends SimpleJsonResourceReloadListener {
    // --- Data record ---

    public record ItemData(RiskLevel riskLevel, AttackType attackType) {
        public static final ItemData DEFAULT = new ItemData(RiskLevel.ZAYIN, AttackType.BLUNT);
    }

    // --- Singleton ---

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ItemDataManager INSTANCE = new ItemDataManager();

    private static final Map<String, ItemData> registry = new HashMap<>();

    private ItemDataManager() {
        super(GSON, "item_data");
    }

    // --- Reload listener registration ---

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    // --- JSON loading ---

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        registry.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                ItemData data = parse(json, fileId);
                if (data == null) continue;

                String itemId = json.get("item").getAsString();
                registry.put(itemId, data);
            } catch (Exception e) {
                CombatUpdated.LOGGER.error("[CombatUpdated] Failed to load item_data entry '{}': {}", fileId, e.getMessage());
            }
        }
        CombatUpdated.LOGGER.info("[CombatUpdated] Loaded {} item data entries.", registry.size());
    }

    private static ItemData parse(JsonObject json, ResourceLocation fileId) {
        // Required: item id
        if (!json.has("item")) {
            CombatUpdated.LOGGER.warn("[CombatUpdated] item_data '{}' missing 'item' field, skipping.", fileId);
            return null;
        }

        // Required: risk_level
        RiskLevel riskLevel = parseEnum(json, "risk_level", RiskLevel.class, RiskLevel.ZAYIN, fileId);

        // Required: attack_type
        AttackType attackType = parseEnum(json, "attack_type", AttackType.class, AttackType.BLUNT, fileId);

        return new ItemData(riskLevel, attackType);
    }

    // --- Lookup ---

    public static ItemData get(ItemStack stack) {
        if (stack.isEmpty()) return ItemData.DEFAULT;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return ItemData.DEFAULT;
        return registry.getOrDefault(key.toString(), ItemData.DEFAULT);
    }

    /**
     * Direct key lookup
     */
    public static ItemData get(String itemId) {
        return registry.getOrDefault(itemId, ItemData.DEFAULT);
    }

    // --- Helpers ---

    private static <E extends Enum<E>> E parseEnum(JsonObject json, String key, Class<E> enumClass, E fallback, ResourceLocation fileId) {
        if (!json.has(key)) return fallback;
        String raw = json.get(key).getAsString().toUpperCase(Locale.ROOT);
        try {
            return Enum.valueOf(enumClass, raw);
        } catch (IllegalArgumentException e) {
            CombatUpdated.LOGGER.warn("[CombatUpdated] item_data '{}': unknown {} '{}', using {}.", fileId, enumClass.getSimpleName(), raw, fallback.name());
            return fallback;
        }
    }
}