package org.dpdns.pisekpiskovec.combatupdated.data;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.dpdns.pisekpiskovec.combatupdated.api.AttackType;
import org.dpdns.pisekpiskovec.combatupdated.api.RiskLevel;

import java.util.HashMap;
import java.util.Map;

public class ItemDataManager {
    public record ItemData(RiskLevel riskLevel, AttackType attackType) {
        public static final ItemData DEFAULT = new ItemData(RiskLevel.ZAYIN, AttackType.BLUNT);
    }

    private static final Map<String, ItemData> registry = new HashMap<>();

    public static ItemData get(ItemStack stack) {
        if (stack.isEmpty()) return ItemData.DEFAULT;
        String key = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        return registry.getOrDefault(key, ItemData.DEFAULT);
    }

    public static void register(String itemId, ItemData data) {
        registry.put(itemId, data);
    }
}
