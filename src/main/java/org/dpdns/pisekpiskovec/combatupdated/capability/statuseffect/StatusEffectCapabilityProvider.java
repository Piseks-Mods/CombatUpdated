package org.dpdns.pisekpiskovec.combatupdated.capability.statuseffect;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatusEffectCapabilityProvider implements ICapabilityProvider, net.minecraftforge.common.util.INBTSerializable<CompoundTag> {
    public static final Capability<StatusEffectCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<StatusEffectCapability>() {
    });

    private final StatusEffectCapability instance = new StatusEffectCapability();

    private final LazyOptional<StatusEffectCapability> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserializeNBT(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
