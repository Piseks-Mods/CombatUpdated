package org.dpdns.pisekpiskovec.combatupdated;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CombatUpdated.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue STAGGER_THRESHOLD = BUILDER.comment("Default stagger threshold as a fraction of max HP (0.18 = 18 %)").defineInRange("staggerThreshold", 0.18, 0.0, 1.0);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static float staggerThreshold;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        staggerThreshold = STAGGER_THRESHOLD.get().floatValue();
    }
}
