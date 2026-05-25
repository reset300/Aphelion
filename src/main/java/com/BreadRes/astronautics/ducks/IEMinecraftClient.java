package com.BreadRes.astronautics.ducks;

import net.minecraft.client.renderer.LevelRenderer;

public interface IEMinecraftClient {
    void astro_setWorldRenderer(LevelRenderer renderer);
    LevelRenderer astro_getWorldRenderer();
}