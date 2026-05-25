package com.BreadRes.astronautics.client.space;

import com.BreadRes.astronautics.client.shader.AstronauticsShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class SpaceSkyEffects extends DimensionSpecialEffects {

    public static float currentAlpha = 1.0f;

    public SpaceSkyEffects() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick,
                             Matrix4f modelViewMatrix, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy,
                             Runnable setupFog) {
        org.slf4j.LoggerFactory.getLogger("AstroSky").info("renderSky called for {}", level.dimension().location());
        renderSkyDirect(modelViewMatrix, projectionMatrix);
        return true;
    }

    public static void renderSkyDirect(Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        var shader = AstronauticsShaders.spaceSky();
        if (shader == null) return;

        var uTime = shader.getUniform("Time");
        if (uTime != null) uTime.set((float)(System.currentTimeMillis() % 1000000L) * 0.001f);

        Matrix4f invView = new Matrix4f(modelViewMatrix);
        invView.m30(0); invView.m31(0); invView.m32(0);
        invView.invert();
        var uInvView = shader.getUniform("InvViewMat");
        if (uInvView != null) uInvView.set(invView);

        Matrix4f invProj = new Matrix4f(projectionMatrix);
        invProj.invert();
        var uInvProj = shader.getUniform("InvProjMat");
        if (uInvProj != null) uInvProj.set(invProj);

        var uAlpha = shader.getUniform("Alpha");
        if (uAlpha != null) uAlpha.set(currentAlpha);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);

        var tess = Tesselator.getInstance();
        var buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f identity = new Matrix4f().identity();

        buf.addVertex(identity, -1, -1, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        buf.addVertex(identity,  1, -1, 0).setUv(1, 0).setColor(255, 255, 255, 255);
        buf.addVertex(identity,  1,  1, 0).setUv(1, 1).setColor(255, 255, 255, 255);
        buf.addVertex(identity, -1,  1, 0).setUv(0, 1).setColor(255, 255, 255, 255);

        BufferUploader.drawWithShader(buf.buildOrThrow());

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}