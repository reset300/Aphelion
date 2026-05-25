package com.BreadRes.astronautics.content.blocks.fuel_tank;

import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlock extends FluidTankBlock implements IWrenchable {

    public FuelTankBlock() {
        super(Properties.of().mapColor(MapColor.METAL).strength(3.5f).noOcclusion(), false);
    }

    @Override
    public MapCodec<? extends FluidTankBlock> codec() {
        return simpleCodec(p -> new FuelTankBlock());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstronauticsFuelTankBlockEntity(pos, state);
    }

    @Override
    public BlockEntityType<AstronauticsFuelTankBlockEntity> getBlockEntityType() {
        return AstronauticsRegistry.FUEL_TANK_BE.get();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            if (!level.isClientSide) {
                AstronauticsFuelTankBlockEntity fuelTank = (AstronauticsFuelTankBlockEntity) ConnectivityHandler.partAt(AstronauticsRegistry.FUEL_TANK_BE.get(), level, pos);
                if (fuelTank != null) {
                    AstronauticsFuelTankBlockEntity controller = fuelTank.getAstronauticsController();
                    if (controller != null && controller.hasDivider) {
                        boolean leftHasFluid = !controller.getLeftTank().isEmpty();
                        boolean rightHasFluid = !controller.getRightTank().isEmpty();
                        if (leftHasFluid && rightHasFluid) {
                            player.displayClientMessage(
                                    Component.translatable("astronautics.divider.cannot_remove").withStyle(ChatFormatting.RED), true
                            );
                            return ItemInteractionResult.SUCCESS;
                        }
                        FluidStack remaining = leftHasFluid
                                ? controller.getLeftTank().getFluid().copy()
                                : controller.getRightTank().getFluid().copy();

                        controller.hasDivider = false;
                        controller.dividerRatio = 50;

                        if (!remaining.isEmpty()) {
                            controller.getTankInventory().fill(remaining, IFluidHandler.FluidAction.EXECUTE);
                        }

                        controller.sendData();
                        controller.setChanged();
                        player.getInventory().placeItemBackInInventory(new ItemStack(AstronauticsRegistry.DIVIDER.get()));
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(AstronauticsRegistry.DIVIDER.get())) {
            if (!level.isClientSide) {
                AstronauticsFuelTankBlockEntity fuelTank = (AstronauticsFuelTankBlockEntity) ConnectivityHandler.partAt(AstronauticsRegistry.FUEL_TANK_BE.get(), level, pos);
                if (fuelTank != null) {
                    AstronauticsFuelTankBlockEntity controller = fuelTank.getAstronauticsController();
                    if (controller != null) {
                        if (!controller.hasDivider) {
                            controller.hasDivider = true;
                            controller.dividerRatio = 50;
                            controller.rebuildDividedTanks();
                            controller.sendData();
                            controller.setChanged();
                            if (!player.isCreative()) stack.shrink(1);
                        } else {
                            boolean isShift = player.isSecondaryUseActive();
                            int delta = isShift ? -10 : 10;
                            controller.dividerRatio = Mth.clamp(controller.dividerRatio + delta, 1, 99);
                            controller.rebuildDividedTanks();
                            controller.sendData();
                            controller.setChanged();
                        }
                    }
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        FluidTankBlockEntity be = (FluidTankBlockEntity) ConnectivityHandler.partAt(AstronauticsRegistry.FUEL_TANK_BE.get(), level, pos);
        if (be == null) return ItemInteractionResult.FAIL;

        IFluidHandler tankCapability = level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
        if (tankCapability == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be)) return ItemInteractionResult.SUCCESS;
        if (FluidHelper.tryFillItemFromBE(level, player, hand, stack, be)) return ItemInteractionResult.SUCCESS;

        return !GenericItemEmptying.canItemBeEmptied(level, stack) && !GenericItemFilling.canItemBeFilled(level, stack)
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SUCCESS;
    }
}