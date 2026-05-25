package com.BreadRes.astronautics.mixin;

import com.BreadRes.astronautics.content.utils.FluidDensityDataHandler;
import com.BreadRes.astronautics.content.utils.FluidMassContributor;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ServerSubLevel.class)
public class ServerSubLevelMixin {

    private static final Field MASS_FIELD;
    private static final Field INVERSE_MASS_FIELD;
    private static final Field MERGED_COM_FIELD;
    private static final Field MERGED_MASS_FIELD;
    private static final Field MERGED_INVERSE_MASS_FIELD;

    static {
        try {
            MASS_FIELD = MassTracker.class.getDeclaredField("mass");
            MASS_FIELD.setAccessible(true);
            INVERSE_MASS_FIELD = MassTracker.class.getDeclaredField("inverseMass");
            INVERSE_MASS_FIELD.setAccessible(true);

            Class<?> mergedClass = Class.forName("dev.ryanhcode.sable.api.physics.mass.MergedMassTracker");
            MERGED_COM_FIELD = mergedClass.getDeclaredField("centerOfMass");
            MERGED_COM_FIELD.setAccessible(true);
            MERGED_MASS_FIELD = mergedClass.getDeclaredField("mass");
            MERGED_MASS_FIELD.setAccessible(true);
            MERGED_INVERSE_MASS_FIELD = mergedClass.getDeclaredField("inverseMass");
            MERGED_INVERSE_MASS_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Unique private double astronautics$cachedFluidMass = 0.0;
    @Unique private int astronautics$tickCounter = 0;
    private static final int RECALC_INTERVAL = 20;

    @Unique
    private double astronautics$calcFluidMass(ServerSubLevel subLevel) {
        double total = 0.0;
        ServerLevelPlot plot = subLevel.getPlot();
        for (var chunkHolder : plot.getLoadedChunks()) {
            LevelChunk chunk = chunkHolder.getChunk();
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof FluidMassContributor contributor) {
                    total += contributor.getFluidMassKg();
                    continue;
                }
                if (be instanceof com.simibubi.create.content.fluids.tank.FluidTankBlockEntity tank) {
                    if (!tank.isController()) continue;
                    var fluid = tank.getTankInventory().getFluid();
                    if (fluid.isEmpty()) continue;
                    total += (fluid.getAmount() / 1000.0) * FluidDensityDataHandler.INSTANCE.getDensity(fluid);
                    continue;
                }
                var cap = be.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
                if (cap == null) continue;
                for (int i = 0; i < cap.getTanks(); i++) {
                    var fluid = cap.getFluidInTank(i);
                    if (fluid.isEmpty()) continue;
                    total += (fluid.getAmount() / 1000.0) * FluidDensityDataHandler.INSTANCE.getDensity(fluid);
                }
            }
        }
        return total;
    }

    @Unique private double astronautics$baseBlockMass = -1.0;

//    @Inject(method = "updateMergedMassData", at = @At("HEAD"), remap = false)
    private void onUpdateMergedMassData(float partialPhysicsTick, CallbackInfo ci) {
        ServerSubLevel subLevel = (ServerSubLevel)(Object)this;
        if (subLevel.getMassTracker() == null) return;

        MassTracker selfTracker = subLevel.getSelfMassTracker();
        if (selfTracker == null || selfTracker.getMass() <= 0) return;

        if (astronautics$baseBlockMass <= 0) {
            astronautics$baseBlockMass = selfTracker.getMass();
        }

        astronautics$tickCounter++;
        if (astronautics$tickCounter >= RECALC_INTERVAL) {
            astronautics$tickCounter = 0;
            astronautics$cachedFluidMass = astronautics$calcFluidMass(subLevel);
        }

        if (astronautics$cachedFluidMass <= 0) return;

        try {
            MASS_FIELD.set(selfTracker, astronautics$baseBlockMass + astronautics$cachedFluidMass);
            INVERSE_MASS_FIELD.set(selfTracker, 1.0 / (astronautics$baseBlockMass + astronautics$cachedFluidMass));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

//    @Inject(method = "updateMergedMassData", at = @At("RETURN"), remap = false)
    private void onUpdateMergedMassDataReturn(float partialPhysicsTick, CallbackInfo ci) {
        ServerSubLevel subLevel = (ServerSubLevel)(Object)this;
        if (subLevel.getMassTracker() == null || astronautics$cachedFluidMass <= 0) return;

        var blockCOM = subLevel.getSelfMassTracker().getCenterOfMass();
        if (blockCOM == null) return;

        MassData merged = subLevel.getMassTracker();

        try {
            double currentMass = (double) MERGED_MASS_FIELD.get(merged);
            double newMass = currentMass + astronautics$cachedFluidMass;
            MERGED_MASS_FIELD.set(merged, newMass);
            MERGED_INVERSE_MASS_FIELD.set(merged, 1.0 / newMass);
            MERGED_COM_FIELD.set(merged, new Vector3d(blockCOM));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        SubLevelPhysicsSystem physicsSystem = SubLevelPhysicsSystem.get(subLevel.getLevel());
        if (physicsSystem != null) {
            physicsSystem.getPipeline().onStatsChanged(subLevel);
        }
    }

//    @Inject(method = "prePhysicsTick", at = @At("HEAD"), remap = false)
//    private void onPrePhysicsTick(SubLevelPhysicsSystem physicsSystem, RigidBodyHandle handle, double timeStep, CallbackInfo ci) {
//        if (astronautics$cachedFluidMass <= 0) return;
//
//        ServerSubLevel subLevel = (ServerSubLevel)(Object)this;
//        if (subLevel.getMassTracker() == null || subLevel.getMassTracker().getCenterOfMass() == null) return;
//
//        ServerLevelPlot plot = subLevel.getPlot();
//        var pose = subLevel.logicalPose();
//
//        Vector3d worldGravity = new Vector3d(DimensionPhysicsData.getGravity(subLevel.getLevel()));
//        Vector3d localGravity = pose.transformNormalInverse(worldGravity, new Vector3d());
//
//        for (var chunkHolder : plot.getLoadedChunks()) {
//            LevelChunk chunk = chunkHolder.getChunk();
//            for (BlockEntity be : chunk.getBlockEntities().values()) {
//                double fluidMass = 0;
//                Vector3d worldCenter = null;
//
//                if (be instanceof FluidMassContributor contributor) {
//                    fluidMass = contributor.getFluidMassKg();
//                    worldCenter = new Vector3d(be.getBlockPos().getX() + 0.5,
//                            be.getBlockPos().getY() + 0.5,
//                            be.getBlockPos().getZ() + 0.5);
//                } else if (be instanceof com.simibubi.create.content.fluids.tank.FluidTankBlockEntity tank) {
//                    if (!tank.isController()) continue;
//                    var fluid = tank.getTankInventory().getFluid();
//                    if (fluid.isEmpty()) continue;
//                    fluidMass = (fluid.getAmount() / 1000.0) * FluidDensityDataHandler.INSTANCE.getDensity(fluid);
//                    BlockPos ctrl = be.getBlockPos();
//                    int w = tank.getWidth();
//                    int h = tank.getHeight();
//                    double fill = (double) fluid.getAmount() / tank.getTankInventory().getCapacity();
//                    double filledHeight = h * fill;
//                    worldCenter = new Vector3d(
//                            ctrl.getX() + (w - 1) * 0.5 + 0.5,
//                            ctrl.getY() + filledHeight * 0.5 + 0.5,
//                            ctrl.getZ() + (w - 1) * 0.5 + 0.5);
//                } else {
//                    var cap = be.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
//                    if (cap == null) continue;
//                    for (int i = 0; i < cap.getTanks(); i++) {
//                        var fluid = cap.getFluidInTank(i);
//                        if (fluid.isEmpty()) continue;
//                        fluidMass += (fluid.getAmount() / 1000.0) * FluidDensityDataHandler.INSTANCE.getDensity(fluid);
//                    }
//                    if (fluidMass <= 0) continue;
//                    worldCenter = new Vector3d(be.getBlockPos().getX() + 0.5,
//                            be.getBlockPos().getY() + 0.5,
//                            be.getBlockPos().getZ() + 0.5);
//                }
//
//                if (fluidMass <= 0 || worldCenter == null) continue;
//
//                Vector3d localCenter = pose.transformPositionInverse(worldCenter, new Vector3d());
//                Vector3d fluidGravityForce = new Vector3d(localGravity).mul(fluidMass * timeStep);
//
//                subLevel.getOrCreateQueuedForceGroup(
//                        ForceGroups.REGISTRY.get(Sable.sablePath("gravity"))
//                ).applyAndRecordPointForce(localCenter, fluidGravityForce);
//            }
//        }
//    }
}