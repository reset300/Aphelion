package com.BreadRes.astronautics.content.blocks.fuel_tank;

import com.BreadRes.astronautics.mixin.accessor.FluidTankBlockEntityAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FuelTankRenderer extends SafeBlockEntityRenderer<FluidTankBlockEntity> {

    public FuelTankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(FluidTankBlockEntity te, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        if (!te.isController()) return;

        FluidTankBlockEntityAccessor accessor = (FluidTankBlockEntityAccessor) te;
        if (!accessor.astronautics$getWindow()) return;

        LerpedFloat fluidLevel = te.getFluidLevel();
        if (fluidLevel == null) return;

        float capHeight = 0.25f;
        float tankHullWidth = 0.0703125f;
        float minPuddleHeight = 0.0625f;
        int height = accessor.astronautics$getHeight();
        int width = accessor.astronautics$getWidth();
        float totalHeight = height - 2f * capHeight - minPuddleHeight;

        if (te instanceof AstronauticsFuelTankBlockEntity fuelTank && fuelTank.hasDivider) {
            renderDivided(fuelTank, partialTicks, ms, buffer, light, tankHullWidth, capHeight, minPuddleHeight, totalHeight, width);

        } else {
            renderSingle(te, accessor, partialTicks, ms, buffer, light, tankHullWidth, capHeight, minPuddleHeight, totalHeight, width, fluidLevel);
        }
    }



    private void renderSingle(FluidTankBlockEntity te, FluidTankBlockEntityAccessor accessor,
                              float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, float tankHullWidth, float capHeight,
                              float minPuddleHeight, float totalHeight, int width, LerpedFloat fluidLevel) {

        if (fluidLevel == null) return;

        float level = fluidLevel.getValue(partialTicks);
        if (level < 1f / (512f * totalHeight)) return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0f, totalHeight);

        FluidTank tank = accessor.astronautics$getTankInventory();
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack.isEmpty()) return;

        boolean top = fluidStack.getFluid().getFluidType().isLighterThanAir();

        float xMin = tankHullWidth;
        float xMax = tankHullWidth + width - 2f * tankHullWidth;
        float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;

        if (top) {
            yMin += totalHeight - clampedLevel;
            yMax += totalHeight - clampedLevel;
        }

        float zMax = tankHullWidth + width - 2f * tankHullWidth;

        ms.pushPose();
        ms.translate(0f, clampedLevel - totalHeight, 0f);
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack,
                xMin, yMin, tankHullWidth, xMax, yMax, zMax,
                buffer, ms, light, false, true);
        ms.popPose();
    }

    private void renderDivided(AstronauticsFuelTankBlockEntity te,
                               float partialTicks, PoseStack ms, MultiBufferSource buffer,
                               int light, float tankHullWidth, float capHeight,
                               float minPuddleHeight, float totalHeight, int width) {
        float zMin = tankHullWidth;
        float zMax = tankHullWidth + width - 2f * tankHullWidth;
        float yBase = capHeight + minPuddleHeight;

        float dividerX = tankHullWidth + (width - 2f * tankHullWidth) * (te.dividerRatio / 100.0f);

        renderFluidSide(te.getLeftTank().getFluid(), te.getLeftTank().getCapacity(),
                ms, buffer, light,
                tankHullWidth, dividerX, zMin, zMax,
                yBase, totalHeight, false);

        renderFluidSide(te.getRightTank().getFluid(), te.getRightTank().getCapacity(),
                ms, buffer, light,
                dividerX, tankHullWidth + width - 2f * tankHullWidth, zMin, zMax,
                yBase, totalHeight, false);

    }

    private void renderFluidSide(FluidStack fluidStack, int capacity,
                                 PoseStack ms, MultiBufferSource buffer, int light,
                                 float xMin, float xMax, float zMin, float zMax,
                                 float yBase, float totalHeight, boolean top) {
        if (fluidStack.isEmpty()) return;

        float fillRatio = (float) fluidStack.getAmount() / capacity;
        float fluidHeight = Mth.clamp(fillRatio * totalHeight, 0f, totalHeight);
        if (fluidHeight < 1f / 512f) return;

        boolean lighterThanAir = fluidStack.getFluid().getFluidType().isLighterThanAir();

        float yMin, yMax;
        if (lighterThanAir) {
            yMax = yBase + totalHeight;
            yMin = yMax - fluidHeight;
        } else {
            yMin = yBase;
            yMax = yBase + fluidHeight;
        }

        ms.pushPose();
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack,
                xMin, yMin, zMin, xMax, yMax, zMax,
                buffer, ms, light, false, true);
        ms.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(FluidTankBlockEntity te) {
        return te.isController();
    }
}