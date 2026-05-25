package com.BreadRes.astronautics.client.shader;

import com.BreadRes.astronautics.Astronautics;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Astronautics.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AstronauticsShaders {

    private static ShaderInstance rocketFlameShader;
    private static ShaderInstance rocketHeatMaskShader;
    private static ShaderInstance crtShader;
    private static ShaderInstance spaceSkyShader;

    private AstronauticsShaders() {}

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "rocket_flame"),
                        DefaultVertexFormat.POSITION_TEX_COLOR
                ),
                shader -> rocketFlameShader = shader
        );

        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "space_sky"),
                        DefaultVertexFormat.POSITION_TEX_COLOR
                ),
                shader -> spaceSkyShader = shader
        );

        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "rocket_heat_mask"),
                        DefaultVertexFormat.POSITION_TEX_COLOR
                ),
                shader -> rocketHeatMaskShader = shader
        );

        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "crt"),
                        DefaultVertexFormat.POSITION_TEX_COLOR
                ),
                shader -> crtShader = shader
        );


    }
    public static ShaderInstance rocketFlame() {
        return rocketFlameShader;
    }

    public static ShaderInstance spaceSky() {
        if (spaceSkyShader == null) System.out.println("=== spaceSky shader is NULL ===");
        return spaceSkyShader;
    }

    public static ShaderInstance rocketHeatMask() {
        return rocketHeatMaskShader;
    }

    public static ShaderInstance crt() {
        return crtShader;
    }
}