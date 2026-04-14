package org.dpdns.pisekpiskovec.combatupdated.capability.sanity;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class SanityLuckModifier {
    public static final UUID MODIFIER_ID = UUID.fromString("a3f7c821-4d02-4e1b-bc3a-1e6f8d9c0b25");

    public static AttributeModifier forSanity(int sanity, float scalar) {
        return new AttributeModifier(MODIFIER_ID, "Sanity Luck Modifier", sanity * scalar, AttributeModifier.Operation.ADDITION);
    }
}
