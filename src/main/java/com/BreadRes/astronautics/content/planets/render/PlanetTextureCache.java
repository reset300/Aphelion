package com.BreadRes.astronautics.content.planets.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PlanetTextureCache {

    private static final Map<String, DynamicTexture> TEXTURES = new HashMap<>();
    private static final Map<String, ResourceLocation> TEXTURE_IDS = new HashMap<>();

    public static void update(String planetId, byte[] data) {
        Minecraft mc = Minecraft.getInstance();

        int size = PlanetTextureGenerator.SIZE;
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, size, size, false);

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                byte idx = data[x + z * size];
                int rgba = PlanetTextureGenerator.colorIdxToRGBA(idx);

                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;

                image.setPixelRGBA(x, z, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }

        if (TEXTURES.containsKey(planetId)) {
            TEXTURES.get(planetId).setPixels(image);
            TEXTURES.get(planetId).upload();
        } else {
            DynamicTexture tex = new DynamicTexture(image);
            ResourceLocation id = mc.getTextureManager().register(
                "astronautics_planet_" + planetId, tex
            );
            tex.setFilter(false, false);
            TEXTURES.put(planetId, tex);
            TEXTURE_IDS.put(planetId, id);
        }
    }

    @Nullable
    public static ResourceLocation getTextureId(String planetId) {
        return TEXTURE_IDS.get(planetId);
    }

    public static boolean hasTexture(String planetId) {
        return TEXTURE_IDS.containsKey(planetId);
    }

    public static void clear() {
        TEXTURES.clear();
        TEXTURE_IDS.clear();
    }
}