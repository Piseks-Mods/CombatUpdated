package org.dpdns.pisekpiskovec.combatupdated.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class ApotheosisCompat {
    private static final String MODID = "apotheosis";

    // Fixed UUID for the crit chance modifier
    private static final UUID CRIT_MODIFIER_ID = UUID.fromString("b9e2d410-7f13-4a2c-9d1b-3c8e7f6a0d44");

    // Sanity +-45 maps to +-CRIT_SCALAR crit chance (0.45 = 45% swing)
    private static final float CRIT_SCALAR = 0.01f; // 1 % per sanity point

    private static Boolean loaded = null;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(MODID);
        }
        return loaded;
    }

    /**
     * Syncs sanity to Apotheosis's CRIT_CHANCE attribute.
     * Wrapped in a try/catch - if the attribute doesn't exist
     * (different Apoth version), it fails silently rather than crashing.
     */
    public static void syncCritChance(Player player, int sanity) {
        try {
            var critAttr = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.fromNamespaceAndPath(MODID, "crit_chance"));
            if (critAttr == null) return;

            AttributeInstance instance = player.getAttribute(critAttr);
            if (instance == null) return;

            instance.removeModifier(CRIT_MODIFIER_ID);
            instance.addTransientModifier(new AttributeModifier(CRIT_MODIFIER_ID, "Sanity Crit Chance", sanity * CRIT_SCALAR, AttributeModifier.Operation.ADDITION));
        } catch (Exception ignored) {
            // Apotheosis version mismatch - degrade gracefully
        }
    }
}
