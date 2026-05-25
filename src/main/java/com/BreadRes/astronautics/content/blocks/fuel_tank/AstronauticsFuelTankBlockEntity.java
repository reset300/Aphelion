package com.BreadRes.astronautics.content.blocks.fuel_tank;

import com.BreadRes.astronautics.mixin.accessor.FluidTankBlockEntityAccessor;
import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.List;

public class AstronauticsFuelTankBlockEntity extends FluidTankBlockEntity {

    public boolean hasDivider = false;
    public int dividerRatio = 50;

    private SmartFluidTank leftTank = new SmartFluidTank(1, f -> {});
    private SmartFluidTank rightTank = new SmartFluidTank(1, f -> {});

    private final IFluidHandler dividedHandler = new IFluidHandler() {
        @Override
        public int getTanks() { return 2; }

        @Override
        public FluidStack getFluidInTank(int tank) {
            AstronauticsFuelTankBlockEntity ctrl = getAstronauticsController();
            if (ctrl == null) return FluidStack.EMPTY;
            return tank == 0 ? ctrl.leftTank.getFluid() : ctrl.rightTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            AstronauticsFuelTankBlockEntity ctrl = getAstronauticsController();
            if (ctrl == null) return 0;
            return tank == 0 ? ctrl.leftTank.getCapacity() : ctrl.rightTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) { return true; }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            AstronauticsFuelTankBlockEntity ctrl = getAstronauticsController();
            if (ctrl == null) return 0;
            if (!ctrl.leftTank.isEmpty() && ctrl.leftTank.getFluid().is(resource.getFluid()))
                return ctrl.leftTank.fill(resource, action);
            if (!ctrl.rightTank.isEmpty() && ctrl.rightTank.getFluid().is(resource.getFluid()))
                return ctrl.rightTank.fill(resource, action);
            if (ctrl.leftTank.isEmpty()) return ctrl.leftTank.fill(resource, action);
            if (ctrl.rightTank.isEmpty()) return ctrl.rightTank.fill(resource, action);
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            AstronauticsFuelTankBlockEntity ctrl = getAstronauticsController();
            if (ctrl == null) return FluidStack.EMPTY;
            FluidStack left = ctrl.leftTank.drain(resource, action);
            if (!left.isEmpty()) return left;
            return ctrl.rightTank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            AstronauticsFuelTankBlockEntity ctrl = getAstronauticsController();
            if (ctrl == null) return FluidStack.EMPTY;
            FluidStack left = ctrl.leftTank.drain(maxDrain, action);
            if (!left.isEmpty()) return left;
            return ctrl.rightTank.drain(maxDrain, action);
        }
    };

    public AstronauticsFuelTankBlockEntity(BlockPos pos, BlockState state) {
        super(AstronauticsRegistry.FUEL_TANK_BE.get(), pos, state);
    }

    public AstronauticsFuelTankBlockEntity getAstronauticsController() {
        FluidTankBlockEntity ctrl = getControllerBE();
        if (ctrl instanceof AstronauticsFuelTankBlockEntity a) return a;
        return null;
    }

    public void rebuildDividedTanks() {
        int total = getTankInventory().getCapacity();
        if (total <= 0) total = getCapacityMultiplier();
        int leftCap = Math.max(1, (int)(total * (dividerRatio / 100.0)));
        int rightCap = Math.max(1, total - leftCap);

        FluidStack prevLeft = leftTank.getFluid().copy();
        FluidStack prevRight = rightTank.getFluid().copy();

        leftTank = new SmartFluidTank(leftCap, f -> { setChanged(); sendData(); });
        rightTank = new SmartFluidTank(rightCap, f -> { setChanged(); sendData(); });

        if (!prevLeft.isEmpty()) {
            prevLeft.setAmount(Math.min(prevLeft.getAmount(), leftCap));
            leftTank.setFluid(prevLeft);
        }
        if (!prevRight.isEmpty()) {
            prevRight.setAmount(Math.min(prevRight.getAmount(), rightCap));
            rightTank.setFluid(prevRight);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                AstronauticsRegistry.FUEL_TANK_BE.get(),
                (be, context) -> {
                    AstronauticsFuelTankBlockEntity ctrl = be.getAstronauticsController();
                    if (ctrl != null && ctrl.hasDivider) return be.dividedHandler;
                    FluidTankBlockEntityAccessor accessor = (FluidTankBlockEntityAccessor) be;
                    if (accessor.astronautics$getFluidCapability() == null)
                        accessor.astronautics$refreshCapability();
                    return accessor.astronautics$getFluidCapability();
                });
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        AstronauticsFuelTankBlockEntity ctrl = getAstronauticsController();
        if (ctrl == null || !ctrl.hasDivider) return super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        tooltip.add(Component.translatable("goggles.fuel_tank.header").withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(" ")
                .append(Component.literal("L: ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(ctrl.leftTank.getFluidAmount() + "mB / " + ctrl.leftTank.getCapacity() + "mB")
                        .withStyle(ChatFormatting.WHITE)));
        if (!ctrl.leftTank.isEmpty())
            tooltip.add(Component.literal("   ")
                    .append(ctrl.leftTank.getFluid().getHoverName().copy().withStyle(ChatFormatting.GRAY)));

        tooltip.add(Component.literal(" ")
                .append(Component.literal("R: ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(ctrl.rightTank.getFluidAmount() + "mB / " + ctrl.rightTank.getCapacity() + "mB")
                        .withStyle(ChatFormatting.WHITE)));
        if (!ctrl.rightTank.isEmpty())
            tooltip.add(Component.literal("   ")
                    .append(ctrl.rightTank.getFluid().getHoverName().copy().withStyle(ChatFormatting.GRAY)));

        tooltip.add(Component.literal(" ")
                .append(Component.literal("Ratio: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(ctrl.dividerRatio + "% / " + (100 - ctrl.dividerRatio) + "%")
                        .withStyle(ChatFormatting.YELLOW)));

        return true;
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (isController()) {
            tag.putBoolean("HasDivider", hasDivider);
            tag.putInt("DividerRatio", dividerRatio);
            if (hasDivider) {
                tag.put("LeftTank", leftTank.writeToNBT(registries, new CompoundTag()));
                tag.put("RightTank", rightTank.writeToNBT(registries, new CompoundTag()));
            }
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (isController()) {
            hasDivider = tag.getBoolean("HasDivider");
            dividerRatio = tag.contains("DividerRatio") ? tag.getInt("DividerRatio") : 50;
            if (dividerRatio == 0) dividerRatio = 50;
            if (hasDivider) {
                rebuildDividedTanks();
                leftTank.readFromNBT(registries, tag.getCompound("LeftTank"));
                rightTank.readFromNBT(registries, tag.getCompound("RightTank"));
            }
        }
    }

    public SmartFluidTank getLeftTank() { return leftTank; }
    public SmartFluidTank getRightTank() { return rightTank; }
}