package com.BreadRes.astronautics.content.blocks.rocket_engine;

import com.BreadRes.astronautics.client.ClientRocketEngines;
import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlockEntity;
import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.propeller.BlockEntityPropeller;
import dev.ryanhcode.sable.api.block.propeller.BlockEntitySubLevelPropellerActor;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.List;

public class RocketEngineBlockEntity extends SmartBlockEntity implements BlockEntitySubLevelPropellerActor, IHaveGoggleInformation {

    private static final double ISP = 1790.0;
    private static final double G0 = 9.81;
    private static final double MIX_RATIO = 2.0;
    private static final int UDMH_FLOW_PER_TICK = 10;
    private static final int N2O4_FLOW_PER_TICK = 20;
    private static final int INTERNAL_CAPACITY = 1000;
    private static final int IGNITION_TICKS = 70;

    public float clientThrust = 0f;
    public int clientSignal = 0;
    public float clientFlameScale = 0.0f;

    public boolean clientGroundHit = false;
    public float clientGroundDist = 40f;

    private ScrollValueBehaviour throttle;
    private boolean active = false;
    private int signalStrength = 0;

    public float clientRenderedIgnition = 0f;
    private int internalUDMH = 0;
    private int internalN2O4 = 0;
    public boolean hasFuel = false;

    private int startupTicks = 0;

    public boolean clientActive = false;
    public int clientThrottle = 100;
    public int clientStartupTicks = 0;
    public boolean clientHasFuel = false;

    private final BlockEntityPropeller propeller = new BlockEntityPropeller() {
        @Override
        public Direction getBlockDirection() {
            return getBlockState().getValue(RocketEngineBlock.FACING);
        }

        @Override
        public double getScaledThrust() {
            return -getThrust() * getAirflowScaling();
        }

        @Override
        public double getAirflowScaling() {
            return 1.0;
        }

        @Override
        public double getAirflow() {
            double throttlePct = throttle != null ? throttle.getValue() / 100.0 : 1.0;
            double signalPct = signalStrength / 15.0;
            double ignition = getIgnitionFactor();
            return 3330.0 * throttlePct * signalPct * ignition;
        }

        @Override
        public double getThrust() {
            if (!active || !hasFuel) return 0;

            double throttlePct = throttle != null ? throttle.getValue() / 100.0 : 1.0;
            double signalPct = signalStrength / 15.0;
            double ignition = getIgnitionFactor();
            double effectivePct = throttlePct * signalPct * ignition;

            double fuelPct = Math.min(
                    internalUDMH / (double) INTERNAL_CAPACITY,
                    internalN2O4 / (double) (INTERNAL_CAPACITY * 2)
            );

            double mdotUDMH = (UDMH_FLOW_PER_TICK / 1000.0) * effectivePct;
            double mdot = mdotUDMH * (1.0 + MIX_RATIO) * 0.791;
            return -(mdot * ISP * G0 * fuelPct);
        }

        @Override
        public boolean isActive() {
            return active && signalStrength > 0 && hasFuel && getIgnitionFactor() > 0.2;
        }

        @Override
        public Level getLevel() {
            return level;
        }

        @Override
        public BlockPos getBlockPos() {
            return worldPosition;
        }
    };

    public RocketEngineBlockEntity(BlockPos pos, BlockState state) {
        super(AstronauticsRegistry.ROCKET_ENGINE_BE.get(), pos, state);
    }

    public void setPanelActive(boolean active) {
        this.panelActive = active;
    }

    public void setPanelThrottle(int value) {
        if (this.throttle != null) {
            this.throttle.setValue(value);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.translatable("goggles.rocket_engine.header").withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(" ").append(
                        Component.translatable("goggles.rocket_engine.throttle").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": " + (throttle != null ? throttle.getValue() : 100) + "%").withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.literal(" ").append(
                        Component.translatable("goggles.rocket_engine.signal").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": " + signalStrength + "/15").withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.literal(" ").append(
                        Component.translatable("goggles.rocket_engine.udmh").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(": " + internalUDMH + "mB").withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.literal(" ").append(
                        Component.translatable("goggles.rocket_engine.n2o4").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(": " + internalN2O4 + "mB").withStyle(ChatFormatting.WHITE)));

        if (active && startupTicks < IGNITION_TICKS) {
            tooltip.add(Component.literal(" Ignition: " + (int) (getIgnitionFactor() * 100.0) + "%").withStyle(ChatFormatting.YELLOW));
        }

        if (hasFuel && active) {
            double throttlePct = (throttle != null ? throttle.getValue() : 100) / 100.0;
            double signalPct = signalStrength / 15.0;
            double ignition = getIgnitionFactor();
            double fuelPct = Math.min(internalUDMH / (double) INTERNAL_CAPACITY, internalN2O4 / (double) (INTERNAL_CAPACITY * 2));
            double mdot = (UDMH_FLOW_PER_TICK / 1000.0) * throttlePct * signalPct * ignition * (1.0 + MIX_RATIO) * 0.791;
            double thrust = mdot * ISP * G0 * fuelPct;
            tooltip.add(Component.literal(" ").append(
                            Component.translatable("goggles.rocket_engine.thrust").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(": " + (int) thrust + "N").withStyle(ChatFormatting.WHITE)));
        } else if (!hasFuel) {
            tooltip.add(Component.translatable("goggles.rocket_engine.no_fuel").withStyle(ChatFormatting.RED));
        }

        return true;
    }

    @Override
    public BlockEntityPropeller getPropeller() {
        return propeller;
    }

    @Override
    public Iterable<SubLevel> sable$getConnectionDependencies() {
        if (level == null) return List.of();

        Direction facing = getBlockState().getValue(RocketEngineBlock.FACING);

        BlockPos pos1 = worldPosition;
        BlockPos pos2 = worldPosition.relative(facing);
        BlockPos pos3 = worldPosition.relative(facing.getOpposite());

        var sub =
                Sable.HELPER.getContaining(level, pos1) != null ? Sable.HELPER.getContaining(level, pos1) :
                        Sable.HELPER.getContaining(level, pos2) != null ? Sable.HELPER.getContaining(level, pos2) :
                                Sable.HELPER.getContaining(level, pos3);

        return sub instanceof SubLevel s ? List.of(s) : List.of();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        throttle = new ScrollValueBehaviour(
                Component.translatable("astronautics.throttle"),
                this,
                new RocketEngineThrottleSlot()
        );
        throttle.between(0, 100);
        throttle.value = 100;
        throttle.withFormatter(v -> v + "%");
        behaviours.add(throttle);
    }

    public static double getIgnition(RocketEngineBlockEntity instance) {
        return instance.getIgnitionFactor();
    }

    public double getIgnitionFactor() {
        float p = Mth.clamp(startupTicks / (float) IGNITION_TICKS, 0.0f, 1.0f);
        return p * p * (3.0f - 2.0f * p);
    }

    public float getClientStartupProgress(float partialTick) {
        return Mth.clamp((clientStartupTicks + partialTick) / (float) IGNITION_TICKS, 0.0f, 1.0f);
    }

    public float getClientIgnitionProgress(float partialTick) {
        float p = getClientStartupProgress(partialTick);
        p = Mth.clamp((p - 0.28f) / 0.72f, 0.0f, 1.0f);
        return p * p * (3.0f - 2.0f * p);
    }

    public float getClientSmokeProgress(float partialTick) {
        return 1.0f - getClientIgnitionProgress(partialTick);
    }

    private void refuelFromContraption(ServerSubLevel subLevel) {
        if (internalUDMH >= INTERNAL_CAPACITY && internalN2O4 >= INTERNAL_CAPACITY * 2) {
            hasFuel = internalUDMH > 0 && internalN2O4 > 0;
            return;
        }

        ServerLevelPlot plot = subLevel.getPlot();

        for (var chunkHolder : plot.getLoadedChunks()) {
            LevelChunk chunk = chunkHolder.getChunk();
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be == this) continue;

                IFluidHandler handler = be.getLevel().getCapability(
                        net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                        be.getBlockPos(),
                        null
                );

                if (handler == null) continue;

                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fluid = handler.getFluidInTank(i);
                    if (fluid.isEmpty()) continue;

                    var id = fluid.getFluid().builtInRegistryHolder().key().location();
                    var udmhId = AstronauticsRegistry.UDMH_SOURCE.get().builtInRegistryHolder().key().location();
                    var n2o4Id = AstronauticsRegistry.N2O4_SOURCE.get().builtInRegistryHolder().key().location();

                    if (id.equals(udmhId) && internalUDMH < INTERNAL_CAPACITY) {
                        int needed = INTERNAL_CAPACITY - internalUDMH;
                        FluidStack drain = handler.drain(new FluidStack(AstronauticsRegistry.UDMH_SOURCE.get(), needed), FluidAction.EXECUTE);
                        internalUDMH += drain.getAmount();
                    }

                    if (id.equals(n2o4Id) && internalN2O4 < INTERNAL_CAPACITY * 2) {
                        int needed = INTERNAL_CAPACITY * 2 - internalN2O4;
                        FluidStack drain = handler.drain(new FluidStack(AstronauticsRegistry.N2O4_SOURCE.get(), needed), FluidAction.EXECUTE);
                        internalN2O4 += drain.getAmount();
                    }
                }
            }
        }

        hasFuel = internalUDMH > 0 && internalN2O4 > 0;
    }

    private void consumeFuel(float effectiveThrottle) {
        int udmhConsume = (int) (UDMH_FLOW_PER_TICK * effectiveThrottle);
        int n2o4Consume = (int) (N2O4_FLOW_PER_TICK * effectiveThrottle);

        udmhConsume = Math.min(udmhConsume, internalUDMH);
        n2o4Consume = Math.min(n2o4Consume, internalN2O4);

        int actualUDMH = Math.min(udmhConsume, (int) (n2o4Consume / MIX_RATIO));
        int actualN2O4 = (int) (actualUDMH * MIX_RATIO);

        internalUDMH -= actualUDMH;
        internalN2O4 -= actualN2O4;

        hasFuel = internalUDMH > 0 && internalN2O4 > 0;
    }

    private boolean panelActive = false;



    public static void tick(Level level, BlockPos pos, BlockState state, RocketEngineBlockEntity be) {
        if (level.isClientSide) return;

        var sub = Sable.HELPER.getContaining(level, pos);
        Direction facing = state.getValue(RocketEngineBlock.FACING);
        BlockPos lowerPos = pos.relative(facing.getOpposite());

        int redstone = Math.max(
                level.getBestNeighborSignal(pos),
                level.getBestNeighborSignal(lowerPos)
        );

        int signal = be.panelActive ? 15 : redstone;
        boolean powered = signal > 0;

        boolean changed = false;

        if (signal != be.signalStrength) {
            be.signalStrength = signal;
            changed = true;
        }

        if (sub instanceof ServerSubLevel serverSubLevel) {
            be.refuelFromContraption(serverSubLevel);
        }

        boolean canRun = powered && be.hasFuel;

        if (canRun != be.active) {
            be.active = canRun;
            level.setBlock(pos, state.setValue(RocketEngineBlock.ACTIVE, be.active), 3);
            changed = true;
        }

        if (be.active) {
            if (be.startupTicks < IGNITION_TICKS) {
                be.startupTicks++;
                changed = true;
            }
        } else {
            if (be.startupTicks > 0) {
                be.startupTicks = Math.max(0, be.startupTicks - 1);
                changed = true;
            }
        }

        if (be.active && level instanceof ServerLevel serverLevel) {
            float throttlePct = be.throttle != null ? be.throttle.getValue() / 100.0f : 1.0f;
            float signalPct = be.panelActive ? 1.0f : (be.signalStrength / 15.0f);
            float ignition = (float) be.getIgnitionFactor();
            float effectiveThrottle = throttlePct * signalPct * ignition;

            if (effectiveThrottle > 0 && be.hasFuel) {
                be.consumeFuel(effectiveThrottle);
                changed = true;

                double nx = -facing.getStepX();
                double ny = -facing.getStepY();
                double nz = -facing.getStepZ();
                double cx = lowerPos.getX() + 0.5 + nx * 0.5;
                double cy = lowerPos.getY() + 0.5 + ny * 0.5;
                double cz = lowerPos.getZ() + 0.5 + nz * 0.5;
                double radius = 5.5 * effectiveThrottle;
                AABB box = new AABB(cx - radius, cy - radius, cz - radius, cx + radius, cy + radius, cz + radius);

                for (Entity entity : serverLevel.getEntities((Entity) null, box, e -> true)) {
                    double dx = entity.getX() - cx;
                    double dy = entity.getY() - cy;
                    double dz = entity.getZ() - cz;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > radius) continue;
                    double dot = (dx * nx + dy * ny + dz * nz) / (dist + 0.001);
                    if (dot < 0.3) continue;
                    double falloff = 1.0 - (dist / radius);
                    double force = falloff * effectiveThrottle * 4.2;
                    entity.setDeltaMovement(entity.getDeltaMovement().add(nx * force, ny * force, nz * force));
                    entity.hurtMarked = true;
                    if (dist < radius * 0.6) {
                        entity.setRemainingFireTicks((int)(80 * effectiveThrottle * falloff));
                    }
                }
            }
        }

        if (changed) {
            be.sendData();
            be.setChanged();
        }
    }

    public double getCurrentFlow() {
        if (!active || !hasFuel) return 0;

        double throttlePct = throttle != null ? throttle.getValue() / 100.0 : 1.0;
        double signalPct = signalStrength / 15.0;
        double ignition = getIgnitionFactor();

        double effectivePct = throttlePct * signalPct * ignition;

        double mdotUDMH = (UDMH_FLOW_PER_TICK / 1000.0) * effectivePct;

        return mdotUDMH * (1.0 + MIX_RATIO);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("Active", active);
        tag.putInt("Throttle", throttle != null ? throttle.getValue() : 100);
        tag.putInt("Signal", signalStrength);
        tag.putInt("InternalUDMH", internalUDMH);
        tag.putInt("InternalN2O4", internalN2O4);
        tag.putBoolean("HasFuel", hasFuel);
        tag.putInt("StartupTicks", startupTicks);
        tag.putBoolean("GroundHit", clientGroundHit);
        tag.putFloat("GroundDist", clientGroundDist);
        tag.putFloat("Thrust", (float) Math.abs(propeller.getThrust()));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        active = tag.getBoolean("Active");
        clientThrottle = tag.getInt("Throttle");
        clientActive = active;
        signalStrength = tag.getInt("Signal");
        internalUDMH = tag.getInt("InternalUDMH");
        internalN2O4 = tag.getInt("InternalN2O4");
        hasFuel = tag.getBoolean("HasFuel");
        clientHasFuel = hasFuel;
        startupTicks = tag.getInt("StartupTicks");
        clientStartupTicks = startupTicks;
        clientGroundHit = tag.getBoolean("GroundHit");
        clientGroundDist = tag.getFloat("GroundDist");
        clientSignal = tag.getInt("Signal");
        clientThrust = tag.getFloat("Thrust");
    }

    public boolean isActive() {
        return active;
    }

    public int getThrottleValue() {
        return throttle != null ? throttle.getValue() : 100;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) {
            ClientRocketEngines.ENGINES.add(this);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return AABB.INFINITE;
    }

    public int getUDMH() {
        return internalUDMH;
    }

    public int getN2O4() {
        return internalN2O4;
    }

    public int getUDMHCapacity() {
        return INTERNAL_CAPACITY;
    }

    public int getN2O4Capacity() {
        return INTERNAL_CAPACITY * 2;
    }
}