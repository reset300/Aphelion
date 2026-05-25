package com.BreadRes.astronautics.content.planets;

import com.BreadRes.astronautics.Astronautics;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanetRegistry {

    private static final Map<String, Planet> PLANETS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Planet> BY_DIMENSION = new HashMap<>();
    private static final Map<ResourceKey<Level>, Planet> BY_ORBIT = new HashMap<>();

    public static void register(Planet planet) {
        PLANETS.put(planet.getId(), planet);
        BY_DIMENSION.put(planet.getDimension(), planet);
        BY_ORBIT.put(planet.getOrbitDimension(), planet);

        for (Planet moon : planet.getMoons()) {
            register(moon);
        }
    }

    @Nullable
    public static Planet getById(String id) {
        return PLANETS.get(id);
    }

    @Nullable
    public static Planet getByDimension(ResourceKey<Level> dimension) {
        return BY_DIMENSION.get(dimension);
    }

    @Nullable
    public static Planet getByOrbit(ResourceKey<Level> dimension) {
        return BY_ORBIT.get(dimension);
    }

    public static boolean isPlanetDimension(ResourceKey<Level> dimension) {
        return BY_DIMENSION.containsKey(dimension);
    }

    public static boolean isOrbitDimension(ResourceKey<Level> dimension) {
        return BY_ORBIT.containsKey(dimension);
    }

    public static boolean isSpaceDimension(ResourceKey<Level> dimension) {
        return isPlanetDimension(dimension) || isOrbitDimension(dimension);
    }

    public static Collection<Planet> getAllPlanets() {
        return PLANETS.values();
    }

    public static List<Planet> getRootPlanets() {
        List<Planet> roots = new ArrayList<>();
        for (Planet planet : PLANETS.values()) {
            boolean isMoon = false;
            for (Planet other : PLANETS.values()) {
                if (other.getMoons().contains(planet)) {
                    isMoon = true;
                    break;
                }
            }
            if (!isMoon) roots.add(planet);
        }
        return roots;
    }

    @Nullable
    public static Planet getParent(Planet moon) {
        for (Planet planet : PLANETS.values()) {
            if (planet.getMoons().contains(moon)) {
                return planet;
            }
        }
        return null;
    }

    public static void clear() {
        PLANETS.clear();
        BY_DIMENSION.clear();
        BY_ORBIT.clear();
    }
}