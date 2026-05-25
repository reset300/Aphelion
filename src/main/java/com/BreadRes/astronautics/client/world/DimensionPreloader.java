package com.BreadRes.astronautics.client.world;

import com.BreadRes.astronautics.IP.ClientWorldLoader;
import com.BreadRes.astronautics.content.planets.Planet;
import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class DimensionPreloader {

    private static final int PRELOAD_START_DISTANCE = 300;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getEntity() != mc.player) return;

        tickCounter++;
        if (tickCounter % 20 != 0) return;

        ResourceKey<Level> dim = mc.player.level().dimension();
        double y = mc.player.getY();

        Planet planet = PlanetRegistry.getByDimension(dim);
        if (planet != null) {
            int launchHeight = planet.getLaunchHeight();
            if (y >= launchHeight - PRELOAD_START_DISTANCE) {
                tryPreload(planet.getOrbitDimension());
            }
            return;
        }

        Planet orbitPlanet = PlanetRegistry.getByOrbit(dim);
        if (orbitPlanet != null) {
            float size = orbitPlanet.getVisualSize();
            double px = mc.player.getX();
            double py = y;
            double pz = mc.player.getZ();
            int orbitRadius = orbitPlanet.getOrbitRadius();

            boolean nearExit = py >= orbitPlanet.getLandingHeight() + orbitRadius - PRELOAD_START_DISTANCE
                    || pz <= -(size + orbitRadius - PRELOAD_START_DISTANCE)
                    || pz >= size + orbitRadius - PRELOAD_START_DISTANCE
                    || px <= -(size + orbitRadius - PRELOAD_START_DISTANCE)
                    || px >= size + orbitRadius - PRELOAD_START_DISTANCE;

            if (nearExit) {
                tryPreload(ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("astronautics", "space")
                ));
            }
        }
    }

    private static void tryPreload(ResourceKey<Level> dimension) {
        Minecraft mc = Minecraft.getInstance();
        org.slf4j.LoggerFactory.getLogger("AstroPreload").info(
                "tryPreload: dim={} initialized={} y={}",
                dimension.location(),
                ClientWorldLoader.getIsInitialized(),
                mc.player != null ? mc.player.getY() : "null"
        );
        if (!ClientWorldLoader.getIsInitialized()) {
            ClientWorldLoader.initializeIfNeeded();
            if (!ClientWorldLoader.getIsInitialized()) return;
        }
        try {
            ClientLevel world = ClientWorldLoader.getOptionalWorld(dimension);
            org.slf4j.LoggerFactory.getLogger("AstroPreload").info("result: {}", world != null ? "ok" : "null");
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger("AstroPreload").error("error: {}", e.getMessage());
        }
    }
}