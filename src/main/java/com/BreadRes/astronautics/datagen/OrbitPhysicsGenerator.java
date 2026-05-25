package com.BreadRes.astronautics.datagen;

import com.BreadRes.astronautics.content.planets.AstronauticsPlanets;
import com.BreadRes.astronautics.content.planets.Planet;
import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrbitPhysicsGenerator implements DataProvider {

    private final PackOutput output;

    public OrbitPhysicsGenerator(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        AstronauticsPlanets.init();

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Planet planet : PlanetRegistry.getAllPlanets()) {
            JsonObject json = new JsonObject();
            json.addProperty("dimension", "astronautics:" + planet.getId() + "_orbit");
            json.addProperty("priority", 1000);
            json.addProperty("universal_drag", 0.0);

            JsonArray gravity = new JsonArray();
            gravity.add(0.0);
            gravity.add(0.0);
            gravity.add(0.0);
            json.add("base_gravity", gravity);

            json.addProperty("base_pressure", 0.0);

            JsonArray magnetic = new JsonArray();
            magnetic.add(0.0);
            magnetic.add(0.0);
            magnetic.add(0.0);
            json.add("magnetic_north", magnetic);

            Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                    .resolve("astronautics")
                    .resolve("sable")
                    .resolve("dimension_physics")
                    .resolve(planet.getId() + "_orbit.json");

            futures.add(DataProvider.saveStable(cache, json, path));
        }

        JsonObject spaceJson = new JsonObject();
        spaceJson.addProperty("dimension", "astronautics:space");
        spaceJson.addProperty("priority", 1000);
        spaceJson.addProperty("universal_drag", 0.0);

        JsonArray spaceGravity = new JsonArray();
        spaceGravity.add(0.0);
        spaceGravity.add(0.0);
        spaceGravity.add(0.0);
        spaceJson.add("base_gravity", spaceGravity);

        spaceJson.addProperty("base_pressure", 0.0);

        JsonArray spaceMagnetic = new JsonArray();
        spaceMagnetic.add(0.0);
        spaceMagnetic.add(0.0);
        spaceMagnetic.add(0.0);
        spaceJson.add("magnetic_north", spaceMagnetic);

        Path spacePath = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("astronautics")
                .resolve("sable")
                .resolve("dimension_physics")
                .resolve("space.json");

        futures.add(DataProvider.saveStable(cache, spaceJson, spacePath));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "Astronautics Orbit Physics";
    }
}