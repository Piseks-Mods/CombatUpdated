package org.dpdns.pisekpiskovec.combatupdated.capability.stagger;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StaggerCapabilityProvider implements ICapabilityProvider, net.minecraftforge.common.util.INBTSerializable<CompoundTag> {
    public static final Capability<StaggerCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<StaggerCapability>() {
    });

    private final StaggerCapability instance = new StaggerCapability();
    private final LazyOptional<StaggerCapability> optional = LazyOptional.of(() -> instance);

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
