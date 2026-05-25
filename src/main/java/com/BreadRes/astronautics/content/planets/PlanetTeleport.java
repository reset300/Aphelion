package com.BreadRes.astronautics.content.planets;

import com.BreadRes.astronautics.ducks.IEEntity;
import com.BreadRes.astronautics.network.AstroTeleportPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.ryanhcode.sable.Sable.LOGGER;

public class PlanetTeleport {

    private static final Map<UUID, TeleportRequest> pending = new HashMap<>();

    public record TeleportRequest(
            ServerLevel destination,
            double x, double y, double z,
            float yRot, float xRot
    ) {}

    private static final Map<UUID, Long> teleportCooldown = new HashMap<>();
    private static final long COOLDOWN_MS = 3000;

    private static boolean isOnCooldown(ServerPlayer player) {
        Long last = teleportCooldown.get(player.getUUID());
        if (last == null) return false;
        return System.currentTimeMillis() - last < COOLDOWN_MS;
    }

    private static void setCooldown(ServerPlayer player) {
        teleportCooldown.put(player.getUUID(), System.currentTimeMillis());
    }

    public static void tryLaunch(ServerPlayer player) {
        var currentDim = player.level().dimension();
        var planet = PlanetRegistry.getByDimension(currentDim);

        if (planet == null) return;
        if (player.getY() < planet.getLaunchHeight()) return;
        if (pending.containsKey(player.getUUID())) return;

        ServerLevel orbitLevel = player.getServer().getLevel(planet.getOrbitDimension());
        if (orbitLevel == null) return;

        double orbitX = planet.convertSurfaceToOrbit(player.getX());
        double orbitZ = planet.convertSurfaceToOrbit(player.getZ());
        double orbitY = planet.getVisualSize() + 10;

        scheduleWithPreload(player, orbitLevel, orbitX, orbitY, orbitZ,
                player.getYRot(), player.getXRot());
    }

    public static void tryLand(ServerPlayer player) {
        var currentDim = player.level().dimension();
        var planet = PlanetRegistry.getByOrbit(currentDim);

        if (planet == null) return;
        if (pending.containsKey(player.getUUID())) return;

        ServerLevel planetLevel = player.getServer().getLevel(planet.getDimension());
        if (planetLevel == null) return;

        float size = planet.getVisualSize();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        boolean hitBottom = py <= planet.getLandingHeight();
        boolean hitNorth  = pz <= -size;
        boolean hitSouth  = pz >= size;
        boolean hitWest   = px <= -size;
        boolean hitEast   = px >= size;

        if (!hitBottom && !hitNorth && !hitSouth && !hitWest && !hitEast) return;

        double surfaceX = planet.convertOrbitToSurface(px);
        double surfaceZ = planet.convertOrbitToSurface(pz);
        double surfaceY = planet.getLaunchHeight() - 50;

        scheduleWithPreload(player, planetLevel, surfaceX, surfaceY, surfaceZ,
                player.getYRot(), player.getXRot());
    }

    public static void tryEnterOrbit(ServerPlayer player) {
        var currentDim = player.level().dimension();
        if (!PlanetRegistry.isOrbitDimension(currentDim)) return;
        if (pending.containsKey(player.getUUID())) return;

        var planet = PlanetRegistry.getByOrbit(currentDim);
        if (planet == null) return;

        float size = planet.getVisualSize();
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        int orbitRadius = planet.getOrbitRadius();

        boolean exitTop = py >= planet.getOrbitExitHeight();
        boolean exitNorth = pz <= -(size + orbitRadius);
        boolean exitSouth = pz >= size + orbitRadius;
        boolean exitWest  = px <= -(size + orbitRadius);
        boolean exitEast  = px >= size + orbitRadius;

        if (!exitTop && !exitNorth && !exitSouth && !exitWest && !exitEast) return;

        var spaceDim = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("astronautics", "space")
        );

        ServerLevel spaceLevel = player.getServer().getLevel(spaceDim);
        if (spaceLevel == null) return;

        scheduleWithPreload(player, spaceLevel,
                player.getX(), 200, player.getZ(),
                player.getYRot(), player.getXRot());
    }

    private static void scheduleWithPreload(ServerPlayer player, ServerLevel destination,
                                            double x, double y, double z, float yRot, float xRot) {
        int chunkX = (int) x >> 4;
        int chunkZ = (int) z >> 4;
        int dist = Math.min(player.getServer().getPlayerList().getViewDistance(), 5);

        for (int dx = -dist; dx <= dist; dx++) {
            for (int dz = -dist; dz <= dist; dz++) {
                destination.getChunkSource().getChunkFuture(
                        chunkX + dx, chunkZ + dz,
                        net.minecraft.world.level.chunk.status.ChunkStatus.FULL,
                        true
                );
            }
        }

        pending.put(player.getUUID(), new TeleportRequest(destination, x, y, z, yRot, xRot));
    }

    public static void tick(ServerPlayer player) {
        TeleportRequest req = pending.get(player.getUUID());
        if (req == null) return;

        int chunkX = (int) req.x() >> 4;
        int chunkZ = (int) req.z() >> 4;
        int dist = Math.min(player.getServer().getPlayerList().getViewDistance(), 5);

        boolean ready = true;
        outer:
        for (int dx = -dist; dx <= dist; dx++) {
            for (int dz = -dist; dz <= dist; dz++) {
                if (!req.destination().getChunkSource().hasChunk(chunkX + dx, chunkZ + dz)) {
                    ready = false;
                    break outer;
                }
            }
        }

        if (!ready) return;

        pending.remove(player.getUUID());
        setCooldown(player);

        Vec3 vel = player.getDeltaMovement();

        serverChangePlayerDimension(player, req.destination(),
                new Vec3(req.x(), req.y(), req.z()), req.yRot(), req.xRot());
        LOGGER.info("server tp: to={} pos={},{},{}",
                req.destination().dimension().location(), req.x(), req.y(), req.z());
        player.setDeltaMovement(vel);
        player.connection.resetPosition();

        PacketDistributor.sendToPlayer(player, new AstroTeleportPayload(
                req.destination().dimension(),
                req.x(), req.y(), req.z()
        ));
    }

    private static void serverChangePlayerDimension(
            ServerPlayer player,
            ServerLevel toWorld,
            Vec3 newPos,
            float yRot,
            float xRot
    ) {
        ServerLevel fromWorld = (ServerLevel) player.level();

        if (fromWorld == toWorld) {
            player.setPos(newPos.x, newPos.y, newPos.z);
            return;
        }

        fromWorld.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
        ((IEEntity) player).astro_unsetRemoved();

        player.setPos(newPos.x, newPos.y, newPos.z);
        player.setYRot(yRot);
        player.setXRot(xRot);

        player.setServerLevel(toWorld);
        toWorld.addDuringTeleport(player);
    }

    public static boolean isPending(ServerPlayer player) {
        return pending.containsKey(player.getUUID());
    }

    public static void tryEnterOrbitFromSpace(ServerPlayer player) {
        if (pending.containsKey(player.getUUID())) return;
        if (isOnCooldown(player)) return;

        var spaceDim = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath("astronautics", "space")
        );
        if (!player.level().dimension().equals(spaceDim)) return;

        for (Planet planet : PlanetRegistry.getAllPlanets()) {
            ServerLevel orbitLevel = player.getServer().getLevel(planet.getOrbitDimension());
            if (orbitLevel == null) continue;

            float size = planet.getVisualSize();
            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();

            boolean enterBottom = py <= 100;
            boolean enterNorth  = pz >= -(size + planet.getOrbitRadius()) && pz < -size;
            boolean enterSouth  = pz <= size + planet.getOrbitRadius() && pz > size;
            boolean enterWest   = px >= -(size + planet.getOrbitRadius()) && px < -size;
            boolean enterEast   = px <= size + planet.getOrbitRadius() && px > size;

            if (!enterBottom && !enterNorth && !enterSouth && !enterWest && !enterEast) continue;

            scheduleWithPreload(player, orbitLevel,
                    player.getX(), planet.getOrbitExitHeight() - 50, player.getZ(),
                    player.getYRot(), player.getXRot());
            return;
        }
    }
    public static void cancel(ServerPlayer player) {
        pending.remove(player.getUUID());
    }

    private static double findSafeY(ServerLevel level, double x, double z) {
        int bx = (int) x;
        int bz = (int) z;
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            var pos = new BlockPos(bx, y, bz);
            if (!level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()
                    && level.getBlockState(pos.above(2)).isAir()) {
                return y + 1;
            }
        }
        return 64;
    }
}