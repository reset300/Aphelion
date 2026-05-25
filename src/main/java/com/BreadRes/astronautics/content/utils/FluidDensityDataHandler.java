package com.BreadRes.astronautics.content.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public class FluidDensityDataHandler extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public static final FluidDensityDataHandler INSTANCE = new FluidDensityDataHandler();
    private static final double DEFAULT_DENSITY = 1.0;

    private final Map<ResourceLocation, Double> densities = new HashMap<>();

    private FluidDensityDataHandler() {
        super(GSON, "fluid_densities");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        densities.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            JsonObject json = entry.getValue().getAsJsonObject();
            for (Map.Entry<String, JsonElement> fluidEntry : json.entrySet()) {
                ResourceLocation fluid = ResourceLocation.parse(fluidEntry.getKey());
                double density = fluidEntry.getValue().getAsDouble();
                densities.put(fluid, density);
            }
        }
    }

    public double getDensity(FluidStack fluid) {
        return densities.getOrDefault(fluid.getFluid().builtInRegistryHolder().key().location(), DEFAULT_DENSITY);
    }

    public double getDensity(ResourceLocation fluidId) {
        return densities.getOrDefault(fluidId, DEFAULT_DENSITY);
    }
}