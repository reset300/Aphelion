package com.BreadRes.astronautics.mixin.client;

import com.BreadRes.astronautics.ducks.IEGameRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameRenderer.class)
public class MixinGameRenderer implements IEGameRenderer {

    @Shadow
    private LightTexture lightTexture;

    @Override
    public void astro_setLightTexture(LightTexture lightTexture) {
        this.lightTexture = lightTexture;
    }
}