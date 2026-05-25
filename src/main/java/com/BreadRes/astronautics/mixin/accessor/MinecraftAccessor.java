package com.BreadRes.astronautics.mixin.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("levelRenderer")
    LevelRenderer getLevelRenderer();

    @Mutable
    @Accessor("levelRenderer")
    void setLevelRenderer(LevelRenderer renderer);
}