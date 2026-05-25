package com.BreadRes.astronautics.events;

import com.BreadRes.astronautics.content.planets.AstronauticsPlanets;
import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class SpaceEvents {

    private static final ResourceKey<Level> SPACE_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("astronautics", "space")
    );

    private static boolean isSpaceLike(ResourceKey<Level> dim) {
        if (dim.equals(SPACE_KEY)) return true;
        AstronauticsPlanets.init();
        return PlanetRegistry.isOrbitDimension(dim);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isSpaceLike(player.level().dimension())) return;

        var source = event.getSource();
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            event.setCanceled(true);
        }
    }
}