package com.BreadRes.astronautics.events;

import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import com.BreadRes.astronautics.content.planets.PlanetTeleport;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class PlanetTickEvents {

    private static final ResourceKey<Level> SPACE_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("astronautics", "space")
    );

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var dim = player.level().dimension();

        if (PlanetTeleport.isPending(player)) {
            PlanetTeleport.tick(player);
            return;
        }

        if (PlanetRegistry.isPlanetDimension(dim)) {
            PlanetTeleport.tryLaunch(player);
            return;
        }

        if (PlanetRegistry.isOrbitDimension(dim)) {
            PlanetTeleport.tryLand(player);
            PlanetTeleport.tryEnterOrbit(player);
            return;
        }

        if (dim.equals(SPACE_KEY)) {
            PlanetTeleport.tryEnterOrbitFromSpace(player);
            return;
        }
    }
}