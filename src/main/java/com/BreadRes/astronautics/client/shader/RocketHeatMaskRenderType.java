package com.BreadRes.astronautics.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class RocketHeatMaskRenderType extends RenderType {

    private static final RenderType TYPE = RenderType.create(
            "astronautics_rocket_heat_mask",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            512,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(new ShaderStateShard(AstronauticsShaders::rocketHeatMask))
                    .setTransparencyState(new TransparencyStateShard(
                            "astronautics_heat_mask_blend",
                            () -> {
                                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                                com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate(
                                        GlStateManager.SourceFactor.SRC_ALPHA,
                                        GlStateManager.DestFactor.ONE,
                                        GlStateManager.SourceFactor.ONE,
                                        GlStateManager.DestFactor.ONE
                                );
                            },
                            () -> {
                                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                            }
                    ))
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .createCompositeState(false)
    );

    private RocketHeatMaskRenderType(String n, VertexFormat f, VertexFormat.Mode m, int b, boolean c, boolean s, Runnable a, Runnable d) {
        super(n, f, m, b, c, s, a, d);
    }

    public static RenderType rocketHeatMask() {
        return TYPE;
    }
}