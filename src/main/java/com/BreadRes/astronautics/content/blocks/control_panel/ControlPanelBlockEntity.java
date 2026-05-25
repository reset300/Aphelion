package com.BreadRes.astronautics.content.blocks.control_panel;

import com.BreadRes.astronautics.content.blocks.rocket_engine.RocketEngineBlockEntity;
import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class ControlPanelBlockEntity extends SmartBlockEntity {

    public static class EngineEntry {
        public BlockPos pos;
        public String name;
        public boolean active;
        public int throttle;
        public boolean hasFuel;
        public int udmh;
        public int n2o4;
        public float flow;
        public double ignition;

        public EngineEntry(BlockPos pos) {
            this.pos = pos;
            this.name = "Engine " + pos.toShortString();
            this.active = false;
            this.throttle = 100;
            this.hasFuel = false;
            this.udmh = 0;
            this.n2o4 = 0;
            this.flow = 0f;
        }

        public RocketEngineBlockEntity getBE(Level level, BlockPos panelPos) {
            if (level == null) return null;

            var sub = Sable.HELPER.getContaining(level, panelPos);
            if (!(sub instanceof ServerSubLevel serverSub)) return null;

            BlockEntity be = serverSub.getLevel().getBlockEntity(pos);
            if (be instanceof RocketEngineBlockEntity engine) {
                return engine;
            }

            return null;
        }
    }

    private final List<EngineEntry> engines = new ArrayList<>();
    private int scanTimer = 0;
    private static final int SCAN_INTERVAL = 40;

    public ControlPanelBlockEntity(BlockPos pos, BlockState state) {
        super(AstronauticsRegistry.CONTROL_PANEL_BE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public int getTotalUDMH() {
        int sum = 0;

        for (var e : engines) {
            var engine = e.getBE(level, worldPosition);
            if (engine != null) {
                sum += engine.getUDMH();
            }
        }

        return sum;
    }

    public int getTotalN2O4() {
        int sum = 0;

        for (var e : engines) {
            var engine = e.getBE(level, worldPosition);
            if (engine != null) {
                sum += engine.getN2O4();
            }
        }

        return sum;
    }

    public int getMaxUDMH() {
        int sum = 0;

        for (var e : engines) {
            var engine = e.getBE(level, worldPosition);
            if (engine != null) {
                sum += engine.getUDMHCapacity();
            }
        }

        return sum;
    }

    public int getMaxN2O4() {
        int sum = 0;

        for (var e : engines) {
            var engine = e.getBE(level, worldPosition);
            if (engine != null) {
                sum += engine.getN2O4Capacity();
            }
        }

        return sum;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ControlPanelBlockEntity be) {
        if (level.isClientSide) return;

        be.scanTimer++;
        if (be.scanTimer >= SCAN_INTERVAL) {
            be.scanTimer = 0;
            be.scanEngines();
        }

        be.applyToEngines();
    }

    private void scanEngines() {
        if (level == null) return;

        var sub = Sable.HELPER.getContaining(level, worldPosition);
        if (!(sub instanceof ServerSubLevel serverSub)) return;

        ServerLevelPlot plot = serverSub.getPlot();
        Set<BlockPos> found = new HashSet<>();

        for (var chunkHolder : plot.getLoadedChunks()) {
            LevelChunk chunk = chunkHolder.getChunk();
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof RocketEngineBlockEntity) {
                    found.add(be.getBlockPos());
                }
            }
        }

        for (BlockPos epos : found) {
            boolean exists = engines.stream().anyMatch(e -> e.pos.equals(epos));
            if (!exists) engines.add(new EngineEntry(epos));
        }

        engines.removeIf(e -> !found.contains(e.pos));

        setChanged();
        sendData();
    }

    public void forceScan() {
        scanTimer = 0;
        scanEngines();
    }

    private void applyToEngines() {
        if (level == null) return;

        var sub = Sable.HELPER.getContaining(level, worldPosition);
        if (!(sub instanceof ServerSubLevel serverSub)) return;

        for (EngineEntry entry : engines) {
            BlockEntity be = serverSub.getLevel().getBlockEntity(entry.pos);
            if (be instanceof RocketEngineBlockEntity engine) {
                entry.hasFuel = engine.hasFuel;
                entry.udmh = engine.getUDMH();
                entry.n2o4 = engine.getN2O4();
                entry.ignition = engine.getIgnitionFactor();
                entry.flow = (float)(engine.getCurrentFlow() * 20.0);
                engine.setPanelActive(entry.active);
                engine.setPanelThrottle(entry.throttle);
            }
        }
    }

    public List<EngineEntry> getEngines() {
        return Collections.unmodifiableList(engines);
    }

    public void setEngineActive(BlockPos pos, boolean active) {
        engines.stream().filter(e -> e.pos.equals(pos)).findFirst()
                .ifPresent(e -> {
                    e.active = active;
                    setChanged();
                    sendData();
                });
    }

    public void setEngineThrottle(BlockPos pos, int throttle) {
        engines.stream().filter(e -> e.pos.equals(pos)).findFirst()
                .ifPresent(e -> { e.throttle = throttle; setChanged(); sendData(); });
    }

    public void setEngineName(BlockPos pos, String name) {
        engines.stream().filter(e -> e.pos.equals(pos)).findFirst()
                .ifPresent(e -> { e.name = name; setChanged(); sendData(); });
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        ListTag list = new ListTag();
        for (EngineEntry e : engines) {
            CompoundTag t = new CompoundTag();
            t.putInt("X", e.pos.getX());
            t.putInt("Y", e.pos.getY());
            t.putInt("Z", e.pos.getZ());
            t.putString("Name", e.name);
            t.putBoolean("Active", e.active);
            t.putInt("Throttle", e.throttle);
            t.putBoolean("HasFuel", e.hasFuel);
            t.putInt("UDMH", e.udmh);
            t.putInt("N2O4", e.n2o4);
            t.putFloat("Flow", e.flow);
            list.add(t);
        }
        tag.put("Engines", list);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        engines.clear();
        ListTag list = tag.getList("Engines", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {

                CompoundTag t = list.getCompound(i);
                BlockPos p = new BlockPos(t.getInt("X"), t.getInt("Y"), t.getInt("Z"));
                EngineEntry e = new EngineEntry(p);
                e.name = t.getString("Name");
                e.active = t.getBoolean("Active");
                e.throttle = t.getInt("Throttle");
                e.hasFuel = t.getBoolean("HasFuel");
                e.udmh = t.getInt("UDMH");
                e.n2o4 = t.getInt("N2O4");
                e.flow = t.getFloat("Flow");
                engines.add(e);
            }
    }
}