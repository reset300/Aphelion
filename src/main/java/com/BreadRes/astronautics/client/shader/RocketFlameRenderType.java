package com.BreadRes.astronautics.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class RocketFlameRenderType extends RenderType {

    private static final RenderType ROCKET_FLAME = RenderType.create(
            "astronautics_rocket_flame",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            2048,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(new ShaderStateShard(AstronauticsShaders::rocketFlame))
                    .setTransparencyState(new TransparencyStateShard(
                            "astronautics_additive_transparency",
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
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .createCompositeState(false)
    );

    private RocketFlameRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType rocketFlame() {
        return ROCKET_FLAME;
    }
}