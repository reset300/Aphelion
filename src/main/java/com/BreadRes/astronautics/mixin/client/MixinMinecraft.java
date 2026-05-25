package com.BreadRes.astronautics.mixin.client;

import com.BreadRes.astronautics.ducks.IEMinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public class MixinMinecraft implements IEMinecraftClient {

    @Shadow
    public LevelRenderer levelRenderer;

    @Override
    public void astro_setWorldRenderer(LevelRenderer renderer) {
        this.levelRenderer = renderer;
    }

    @Override
    public LevelRenderer astro_getWorldRenderer() {
        return this.levelRenderer;
    }
}