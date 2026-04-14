package org.dpdns.pisekpiskovec.combatupdated.capability.sanity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.dpdns.pisekpiskovec.combatupdated.util.ApotheosisCompat;

public class SanityCapability implements INBTSerializable<CompoundTag> {
    public static final int MIN_SANITY = -45;
    public static final int MAX_SANITY = 45;

    // Sanity -> Luck conversion: 1 sanity = this many Luck points
    // At +-45 Sanity, Luck = +-LUCK_SCALAR
    // Vanilla Luck affects loot table rolls - keep scalar modest (default: 0.1)
    private static final float LUCK_SCALAR = 0.1f;

    private int sanity = 0;

    // --- Static accessor ---
    public static LazyOptional<SanityCapability> get(LivingEntity entity) {
        return entity.getCapability(SanityCapabilityProvider.CAPABILITY);
    }

    public static void ifPresent(LivingEntity entity, java.util.function.Consumer<SanityCapability> action) {
        get(entity).ifPresent((NonNullConsumer<? super SanityCapability>) action);
    }

    // --- Sanity modifications ---

    public void increase(int amount) {
        setSanity(this.sanity + amount);
    }

    public void reduce(int amount) {
        setSanity(this.sanity - amount);
    }

    public void setSanity(int value) {
        this.sanity = Math.max(MIN_SANITY, Math.min(MAX_SANITY, value));
    }

    public int getSanity() {
        return sanity;
    }

    /**
     * Coin flip chance: (50 + sanity) %
     * Sanity  45 -> 95 % chance
     * Sanity   0 -> 50 % chance
     * Sanity -45 ->  5 % chance
     */
    public float getCoinFlipChance() {
        return (50f + sanity) / 100f;
    }

    // --- Attribute sync ---

    /**
     * Syncs sanity to the LUCK attribute.
     * If Apothic Attributes is present, synced to CRIT_CHANCE as well.
     * Call this whenever sanity changes.
     */
    public void syncAttributes(Player player) {
        syncLuck(player);
        if (ApotheosisCompat.isLoaded()) {
            ApotheosisCompat.syncCritChance(player, sanity);
        }
    }

    private void syncLuck(Player player) {
        AttributeInstance luck = player.getAttribute(Attributes.LUCK);
        if (luck == null) return;

        // Remove our previous modifier if present, then re-add
        luck.removeModifier(SanityLuckModifier.MODIFIER_ID);
        luck.addTransientModifier(SanityLuckModifier.forSanity(sanity, LUCK_SCALAR));
    }

    // --- NBT ---


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("sanity", sanity);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setSanity(nbt.getInt("sanity"));
    }
}
