package com.BreadRes.astronautics.content.planets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public abstract class Planet {

    // Identification
    public abstract String getId();
    public abstract ResourceKey<Level> getDimension();
    public abstract ResourceKey<Level> getOrbitDimension();

    // Physics
    public abstract double getGravity();
    public abstract double getAtmospherePressure();
    public abstract double getWindSpeed();
    public abstract double getMagneticField();

    // Temperature
    public abstract double getAvgTemperature();
    public abstract double getDayTemperature();
    public abstract double getNightTemperature();
    public abstract double getTemperatureVariance();

    // Atmosphere
    public abstract boolean hasOxygen();
    public abstract Map<String, Float> getAtmosphereComposition();
    public abstract boolean hasWeather();
    public abstract List<String> getWeatherTypes();
    public abstract int getSkyColor();
    public abstract int getFogColor();
    public abstract double getFogDensity();

    // Water
    public abstract boolean hasWater();
    public abstract ResourceLocation getWaterType();
    public abstract float getOceanCoverage();

    // Generation
    public abstract double getSurfaceNoise();
    public abstract int getMountainHeight();
    public abstract float getCaveFrequency();
    public abstract List<String> getBiomeSet();

    // Resources
    public abstract Map<ResourceLocation, Float> getOreTable();
    public abstract List<ResourceLocation> getSurfaceResources();
    public abstract List<ResourceLocation> getUniqueResources();

    // Size and Orbit
    public abstract int getPlanetRadius();
    public abstract int getOrbitRadius();
    public abstract int getOrbitScale();
    public abstract double getRotationSpeed();
    public abstract int getDayLength();
    public abstract float getAxialTilt();
    public abstract int getOrbitExitHeight();

    // Hazards
    public abstract float getRadiationLevel();
    public abstract float getToxicityLevel();
    public abstract float getMeteorFrequency();
    public abstract boolean hasElectricalStorms();

    // Technical
    public abstract int getLaunchHeight();
    public abstract int getLandingHeight();
    public abstract Vec3 getSpawnPosition();
    public abstract float getVisualSize();
    public abstract ResourceLocation getTexture();

    // Moons
    public abstract List<Planet> getMoons();

    // Helpers
    public boolean hasMoons() {
        return !getMoons().isEmpty();
    }

    public boolean isHabitable() {
        return hasOxygen()
            && getAvgTemperature() > -50
            && getAvgTemperature() < 60
            && getRadiationLevel() < 0.3f
            && getToxicityLevel() < 0.3f;
    }

    public boolean isInOrbit(double y) {
        return y >= getLaunchHeight() && y <= getLaunchHeight() + getOrbitRadius();
    }

    public double convertOrbitToSurface(double orbitCoord) {
        return orbitCoord * getOrbitScale();
    }

    public double convertSurfaceToOrbit(double surfaceCoord) {
        return surfaceCoord / getOrbitScale();
    }
}