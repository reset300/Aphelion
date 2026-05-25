package com.BreadRes.astronautics.content.planets.render;

import com.BreadRes.astronautics.content.planets.Planet;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class PlanetTextureGenerator {

    public static final int SIZE = 128;

    public static byte[] generate(ServerLevel level, Planet planet) {
        byte[] data = new byte[SIZE * SIZE];

        BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();

        int scale = planet.getOrbitScale();

        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = (x - SIZE / 2) * scale * 16;
                int worldZ = (z - SIZE / 2) * scale * 16;

                Holder<Biome> biome = source.getNoiseBiome(
                    worldX >> 2, 64 >> 2, worldZ >> 2, sampler
                );

                data[x + z * SIZE] = biomeToColor(biome);
            }
        }

        return data;
    }

    private static byte biomeToColor(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN)) return 0;
        if (biome.is(BiomeTags.IS_RIVER)) return 0;

        if (biome.is(BiomeTags.IS_BEACH)) return 2;

        if (biome.is(BiomeTags.HAS_VILLAGE_SNOWY)
                || biome.is(BiomeTags.SPAWNS_SNOW_FOXES)
                || biome.is(BiomeTags.SNOW_GOLEM_MELTS) == false
        ) return 8;

        if (biome.is(BiomeTags.IS_BADLANDS)) return 9;

        if (biome.is(BiomeTags.HAS_DESERT_PYRAMID)
                || biome.is(BiomeTags.SPAWNS_GOLD_RABBITS)
        ) return 3;

        if (biome.is(BiomeTags.IS_JUNGLE)) return 6;

        if (biome.is(BiomeTags.IS_TAIGA)) return 7;

        if (biome.is(BiomeTags.IS_FOREST)) return 5;

        if (biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL)) return 10;

        if (biome.is(BiomeTags.IS_SAVANNA)) return 3;

        return 4;
    }

    public static int colorIdxToRGBA(byte idx) {
        return switch (idx) {
            case 0  -> 0x00000000;
            case 3  -> 0xFFC8B464;
            case 4  -> 0xFF3C7828;
            case 5  -> 0xFF1E5A1E;
            case 6  -> 0xFF0F461E;
            case 7  -> 0xFF143C28;
            case 8  -> 0xFFDCDCE6;
            case 10 -> 0xFF787878;
            default -> 0xFF3C7828;
        };
    }
}