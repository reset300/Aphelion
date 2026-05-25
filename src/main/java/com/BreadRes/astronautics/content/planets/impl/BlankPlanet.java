package com.BreadRes.astronautics.content.planets.impl;

import com.BreadRes.astronautics.content.planets.Planet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlankPlanet extends Planet {

    private final String id;
    private final ResourceKey<Level> dimension;
    private final ResourceKey<Level> orbitDimension;

    private double gravity = 1.0;
    private double atmospherePressure = 1.0;
    private double windSpeed = 0.0;
    private double magneticField = 1.0;

    private double avgTemperature = 20.0;
    private double dayTemperature = 25.0;
    private double nightTemperature = 15.0;
    private double temperatureVariance = 5.0;

    private boolean hasOxygen = true;
    private Map<String, Float> atmosphereComposition = new HashMap<>();
    private boolean hasWeather = false;
    private List<String> weatherTypes = new ArrayList<>();
    private int skyColor = 0x78A7FF;
    private int fogColor = 0xC0D8FF;
    private double fogDensity = 0.0;

    private boolean hasWater = false;
    private ResourceLocation waterType = ResourceLocation.withDefaultNamespace("water");
    private float oceanCoverage = 0.0f;

    private double surfaceNoise = 1.0;
    private int mountainHeight = 64;
    private float caveFrequency = 0.5f;
    private List<String> biomeSet = new ArrayList<>();

    private Map<ResourceLocation, Float> oreTable = new HashMap<>();
    private List<ResourceLocation> surfaceResources = new ArrayList<>();
    private List<ResourceLocation> uniqueResources = new ArrayList<>();

    private int planetRadius = 6400;
    private int orbitRadius = 1000;
    private int orbitScale = 16;
    private double rotationSpeed = 1.0;
    private int dayLength = 24000;
    private float axialTilt = 0.0f;
    private int orbitExitHeight = 2000;

    private float radiationLevel = 0.0f;
    private float toxicityLevel = 0.0f;
    private float meteorFrequency = 0.0f;
    private boolean hasElectricalStorms = false;

    private int launchHeight = 10000;
    private int landingHeight = 200;
    private Vec3 spawnPosition = new Vec3(0, 64, 0);
    private float visualSize = 50.0f;
    private ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("astronautics", "textures/planet/blank.png");

    private List<Planet> moons = new ArrayList<>();

    public BlankPlanet(String id, ResourceKey<Level> dimension, ResourceKey<Level> orbitDimension) {
        this.id = id;
        this.dimension = dimension;
        this.orbitDimension = orbitDimension;
    }

    @Override public String getId() { return id; }
    @Override public ResourceKey<Level> getDimension() { return dimension; }
    @Override public ResourceKey<Level> getOrbitDimension() { return orbitDimension; }

    @Override public double getGravity() { return gravity; }
    @Override public double getAtmospherePressure() { return atmospherePressure; }
    @Override public double getWindSpeed() { return windSpeed; }
    @Override public double getMagneticField() { return magneticField; }

    @Override public double getAvgTemperature() { return avgTemperature; }
    @Override public double getDayTemperature() { return dayTemperature; }
    @Override public double getNightTemperature() { return nightTemperature; }
    @Override public double getTemperatureVariance() { return temperatureVariance; }

    @Override public boolean hasOxygen() { return hasOxygen; }
    @Override public Map<String, Float> getAtmosphereComposition() { return atmosphereComposition; }
    @Override public boolean hasWeather() { return hasWeather; }
    @Override public List<String> getWeatherTypes() { return weatherTypes; }
    @Override public int getSkyColor() { return skyColor; }
    @Override public int getFogColor() { return fogColor; }
    @Override public double getFogDensity() { return fogDensity; }

    @Override public boolean hasWater() { return hasWater; }
    @Override public ResourceLocation getWaterType() { return waterType; }
    @Override public float getOceanCoverage() { return oceanCoverage; }

    @Override public double getSurfaceNoise() { return surfaceNoise; }
    @Override public int getMountainHeight() { return mountainHeight; }
    @Override public float getCaveFrequency() { return caveFrequency; }
    @Override public List<String> getBiomeSet() { return biomeSet; }

    @Override public Map<ResourceLocation, Float> getOreTable() { return oreTable; }
    @Override public List<ResourceLocation> getSurfaceResources() { return surfaceResources; }
    @Override public List<ResourceLocation> getUniqueResources() { return uniqueResources; }

    @Override public int getPlanetRadius() { return planetRadius; }
    @Override public int getOrbitRadius() { return orbitRadius; }
    @Override public int getOrbitScale() { return orbitScale; }
    @Override public double getRotationSpeed() { return rotationSpeed; }
    @Override public int getDayLength() { return dayLength; }
    @Override public float getAxialTilt() { return axialTilt; }

    @Override public float getRadiationLevel() { return radiationLevel; }
    @Override public float getToxicityLevel() { return toxicityLevel; }
    @Override public float getMeteorFrequency() { return meteorFrequency; }
    @Override public boolean hasElectricalStorms() { return hasElectricalStorms; }

    @Override public int getLaunchHeight() { return launchHeight; }
    @Override public int getLandingHeight() { return landingHeight; }
    @Override public Vec3 getSpawnPosition() { return spawnPosition; }
    @Override public float getVisualSize() { return visualSize; }
    @Override public ResourceLocation getTexture() { return texture; }

    @Override public List<Planet> getMoons() { return moons; }

    public BlankPlanet gravity(double v) { gravity = v; return this; }
    public BlankPlanet atmospherePressure(double v) { atmospherePressure = v; return this; }
    public BlankPlanet windSpeed(double v) { windSpeed = v; return this; }
    public BlankPlanet magneticField(double v) { magneticField = v; return this; }

    public BlankPlanet avgTemperature(double v) { avgTemperature = v; return this; }
    public BlankPlanet dayTemperature(double v) { dayTemperature = v; return this; }
    public BlankPlanet nightTemperature(double v) { nightTemperature = v; return this; }
    public BlankPlanet temperatureVariance(double v) { temperatureVariance = v; return this; }

    public BlankPlanet hasOxygen(boolean v) { hasOxygen = v; return this; }
    public BlankPlanet atmosphereComposition(Map<String, Float> v) { atmosphereComposition = v; return this; }
    public BlankPlanet hasWeather(boolean v) { hasWeather = v; return this; }
    public BlankPlanet weatherTypes(List<String> v) { weatherTypes = v; return this; }
    public BlankPlanet skyColor(int v) { skyColor = v; return this; }
    public BlankPlanet fogColor(int v) { fogColor = v; return this; }
    public BlankPlanet fogDensity(double v) { fogDensity = v; return this; }

    public BlankPlanet hasWater(boolean v) { hasWater = v; return this; }
    public BlankPlanet waterType(ResourceLocation v) { waterType = v; return this; }
    public BlankPlanet oceanCoverage(float v) { oceanCoverage = v; return this; }

    public BlankPlanet surfaceNoise(double v) { surfaceNoise = v; return this; }
    public BlankPlanet mountainHeight(int v) { mountainHeight = v; return this; }
    public BlankPlanet caveFrequency(float v) { caveFrequency = v; return this; }
    public BlankPlanet biomeSet(List<String> v) { biomeSet = v; return this; }

    public BlankPlanet oreTable(Map<ResourceLocation, Float> v) { oreTable = v; return this; }
    public BlankPlanet surfaceResources(List<ResourceLocation> v) { surfaceResources = v; return this; }
    public BlankPlanet uniqueResources(List<ResourceLocation> v) { uniqueResources = v; return this; }

    public BlankPlanet planetRadius(int v) { planetRadius = v; return this; }
    public BlankPlanet orbitRadius(int v) { orbitRadius = v; return this; }
    public BlankPlanet orbitScale(int v) { orbitScale = v; return this; }
    public BlankPlanet rotationSpeed(double v) { rotationSpeed = v; return this; }
    public BlankPlanet dayLength(int v) { dayLength = v; return this; }
    public BlankPlanet axialTilt(float v) { axialTilt = v; return this; }

    public BlankPlanet radiationLevel(float v) { radiationLevel = v; return this; }
    public BlankPlanet toxicityLevel(float v) { toxicityLevel = v; return this; }
    public BlankPlanet meteorFrequency(float v) { meteorFrequency = v; return this; }
    public BlankPlanet hasElectricalStorms(boolean v) { hasElectricalStorms = v; return this; }

    public BlankPlanet launchHeight(int v) { launchHeight = v; return this; }
    public BlankPlanet landingHeight(int v) { landingHeight = v; return this; }


    @Override public int getOrbitExitHeight() { return orbitExitHeight; }

    public BlankPlanet orbitExitHeight(int v) { orbitExitHeight = v; return this; }
    public BlankPlanet spawnPosition(Vec3 v) { spawnPosition = v; return this; }
    public BlankPlanet visualSize(float v) { visualSize = v; return this; }
    public BlankPlanet texture(ResourceLocation v) { texture = v; return this; }

    public BlankPlanet addMoon(Planet moon) { moons.add(moon); return this; }
    public BlankPlanet moons(List<Planet> v) { moons = v; return this; }
}