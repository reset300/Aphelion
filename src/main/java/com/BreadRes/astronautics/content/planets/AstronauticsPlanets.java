package com.BreadRes.astronautics.content.planets;

import com.BreadRes.astronautics.Astronautics;
import com.BreadRes.astronautics.content.planets.impl.BlankPlanet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class AstronauticsPlanets {

    public static final Planet EARTH = new BlankPlanet(
        "earth",
        Level.OVERWORLD,
        orbitKey("earth")
    )
        .gravity(1.0)
        .atmospherePressure(1.0)
        .windSpeed(0.3)
        .magneticField(1.0)

        .avgTemperature(15.0)
        .dayTemperature(25.0)
        .nightTemperature(5.0)
        .temperatureVariance(30.0)

        .hasOxygen(true)
        .hasWeather(true)
        .weatherTypes(java.util.List.of("rain", "snow", "thunder"))
        .skyColor(0x78A7FF)
        .fogColor(0xC0D8FF)
        .fogDensity(0.0)

        .hasWater(true)
        .waterType(ResourceLocation.withDefaultNamespace("water"))
        .oceanCoverage(0.71f)

        .surfaceNoise(1.0)
        .mountainHeight(256)
        .caveFrequency(0.5f)

        .planetRadius(12443)
        .orbitRadius(778 + 778)
        .orbitScale(16)
        .rotationSpeed(1.0)
        .dayLength(24000)
        .axialTilt(23.5f)

        .radiationLevel(0.0f)
        .toxicityLevel(0.0f)
        .meteorFrequency(0.001f)
        .hasElectricalStorms(true)

        .launchHeight(8000)
        .landingHeight(778)
        .orbitExitHeight(1778)
        .spawnPosition(new net.minecraft.world.phys.Vec3(0, 64, 0))
        .visualSize(778)
        .texture(ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "textures/planet/earth.png"));

    public static void init() {
        PlanetRegistry.register(EARTH);
    }

    private static ResourceKey<Level> orbitKey(String planetId) {
        return ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, planetId + "_orbit")
        );
    }
}