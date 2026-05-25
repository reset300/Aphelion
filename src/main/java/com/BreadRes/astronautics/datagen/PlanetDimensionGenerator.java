package com.BreadRes.astronautics.datagen;

import com.BreadRes.astronautics.Astronautics;
import com.BreadRes.astronautics.content.planets.AstronauticsPlanets;
import com.BreadRes.astronautics.content.planets.Planet;
import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PlanetDimensionGenerator extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(Registries.DIMENSION_TYPE, ctx -> {
            AstronauticsPlanets.init();
            for (Planet planet : PlanetRegistry.getAllPlanets()) {
                ctx.register(
                    dimensionTypeKey(planet),
                        new DimensionType(
                                OptionalLong.of(18000),
                                false,
                                false,
                                false,
                                false,
                                1.0,
                                false,
                                false,
                                -64,
                                384,
                                384,
                                BlockTags.INFINIBURN_OVERWORLD,
                                ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "space"),
                                0.0f,
                                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)
                        )
                );
            }
        })
        .add(Registries.BIOME, ctx -> {
            for (Planet planet : PlanetRegistry.getAllPlanets()) {
                ctx.register(
                    biomeKey(planet),
                    new Biome.BiomeBuilder()
                        .hasPrecipitation(false)
                        .temperature(0.0f)
                        .downfall(0.0f)
                        .specialEffects(new BiomeSpecialEffects.Builder()
                            .skyColor(0)
                            .fogColor(0)
                            .waterColor(0)
                            .waterFogColor(0)
                            .build())
                        .mobSpawnSettings(MobSpawnSettings.EMPTY)
                        .generationSettings(BiomeGenerationSettings.EMPTY)
                        .build()
                );
            }
        })
        .add(Registries.LEVEL_STEM, ctx -> {
            var dimTypes = ctx.lookup(Registries.DIMENSION_TYPE);
            var biomes   = ctx.lookup(Registries.BIOME);

            for (Planet planet : PlanetRegistry.getAllPlanets()) {
                var dimTypeHolder = dimTypes.getOrThrow(dimensionTypeKey(planet));
                var biomeHolder   = biomes.getOrThrow(biomeKey(planet));

                FlatLevelGeneratorSettings flatSettings = new FlatLevelGeneratorSettings(
                    Optional.empty(),
                    biomeHolder,
                    List.of()
                );
                flatSettings.getLayersInfo().add(new FlatLayerInfo(1, net.minecraft.world.level.block.Blocks.AIR));

                ChunkGenerator generator = new FlatLevelSource(flatSettings);

                ctx.register(
                    levelStemKey(planet),
                    new LevelStem(dimTypeHolder, generator)
                );
            }
        });

    public PlanetDimensionGenerator(PackOutput output,
                                     CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Astronautics.MOD_ID));
    }

    public static ResourceKey<DimensionType> dimensionTypeKey(Planet planet) {
        return ResourceKey.create(
            Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, planet.getId() + "_orbit")
        );
    }

    public static ResourceKey<Biome> biomeKey(Planet planet) {
        return ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, planet.getId() + "_orbit")
        );
    }

    public static ResourceKey<LevelStem> levelStemKey(Planet planet) {
        return ResourceKey.create(
            Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, planet.getId() + "_orbit")
        );
    }

    @Override
    public String getName() {
        return "Astronautics Planet Dimensions";
    }
}