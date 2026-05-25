package com.BreadRes.astronautics.mixin.accessor;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FluidTankBlockEntity.class)
public interface FluidTankBlockEntityAccessor {
    @Accessor("window")
    boolean astronautics$getWindow();

    @Accessor("height")
    int astronautics$getHeight();

    @Accessor("width")
    int astronautics$getWidth();

    @Accessor("tankInventory")
    FluidTank astronautics$getTankInventory();

    @Accessor("fluidCapability")
    IFluidHandler astronautics$getFluidCapability();

    @Invoker("refreshCapability")
    void astronautics$refreshCapability();
}