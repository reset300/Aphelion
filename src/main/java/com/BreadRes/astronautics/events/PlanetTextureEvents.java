package com.BreadRes.astronautics.events;

import com.BreadRes.astronautics.content.planets.AstronauticsPlanets;
import com.BreadRes.astronautics.content.planets.Planet;
import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import com.BreadRes.astronautics.content.planets.render.PlanetTextureGenerator;
import com.BreadRes.astronautics.content.planets.render.PlanetTexturePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class PlanetTextureEvents {

    private static final Map<String, byte[]> CACHE = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AstronauticsPlanets.init();

        for (Planet planet : PlanetRegistry.getAllPlanets()) {
            ServerLevel level = player.getServer().getLevel(planet.getDimension());
            if (level == null) continue;

            String planetId = planet.getId();

            if (CACHE.containsKey(planetId)) {
                PacketDistributor.sendToPlayer(player,
                    new PlanetTexturePayload(planetId, CACHE.get(planetId))
                );
                continue;
            }

            final ServerLevel finalLevel = level;
            CompletableFuture.runAsync(() -> {
                byte[] data = PlanetTextureGenerator.generate(finalLevel, planet);
                CACHE.put(planetId, data);
                player.getServer().execute(() ->
                    PacketDistributor.sendToPlayer(player,
                        new PlanetTexturePayload(planetId, data)
                    )
                );
            });
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }
}