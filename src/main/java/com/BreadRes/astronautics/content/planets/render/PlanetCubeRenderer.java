package com.BreadRes.astronautics.content.planets.render;

import com.BreadRes.astronautics.content.planets.Planet;
import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@EventBusSubscriber(value = Dist.CLIENT)
public class PlanetCubeRenderer {

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        var dim = mc.level.dimension();
        if (!PlanetRegistry.isOrbitDimension(dim) && !isSpaceDimension(dim)) return;

        Camera camera = mc.gameRenderer.getMainCamera();

        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        double planetX = 0;
        double planetY = 0;
        double planetZ = 0;

        float relX = (float)(planetX - camX);
        float relY = (float)(planetY - camY);
        float relZ = (float)(planetZ - camZ);

        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(event.getModelViewMatrix());

        for (Planet planet : PlanetRegistry.getAllPlanets()) {
            renderPlanet(poseStack.last().pose(), planet, relX, relY, relZ);
        }
    }

    private static void renderPlanet(Matrix4f matrix, Planet planet, float relX, float relY, float relZ) {
        float size = planet.getVisualSize();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        renderFace(matrix, planet, relX, relY, relZ, size, Face.BOTTOM);
        renderFace(matrix, planet, relX, relY, relZ, size, Face.TOP);
        renderFace(matrix, planet, relX, relY, relZ, size, Face.NORTH);
        renderFace(matrix, planet, relX, relY, relZ, size, Face.SOUTH);
        renderFace(matrix, planet, relX, relY, relZ, size, Face.WEST);
        renderFace(matrix, planet, relX, relY, relZ, size, Face.EAST);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderFace(Matrix4f matrix, Planet planet,
                                   float cx, float cy, float cz,
                                   float size, Face face) {
        ResourceLocation baseTexture = planet.getTexture();
        ResourceLocation dynTexture = PlanetTextureCache.getTextureId(planet.getId());

        Tesselator tess = Tesselator.getInstance();

        renderQuad(matrix, tess, baseTexture, cx, cy, cz, size, face, 1f);
        if (dynTexture != null) {
            renderQuad(matrix, tess, dynTexture, cx, cy, cz, size, face, 1f);
        }
    }

    private static void renderQuad(Matrix4f matrix, Tesselator tess, ResourceLocation texture,
                                    float cx, float cy, float cz, float size, Face face, float alpha) {
        RenderSystem.setShaderTexture(0, texture);

        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float x0 = cx - size, x1 = cx + size;
        float y0 = cy - size, y1 = cy + size;
        float z0 = cz - size, z1 = cz + size;

        switch (face) {
            case BOTTOM -> {
                buf.addVertex(matrix, x0, y0, z0).setUv(0, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x0, y0, z1).setUv(0, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y0, z1).setUv(1, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y0, z0).setUv(1, 0).setColor(1f, 1f, 1f, alpha);
            }
            case TOP -> {
                buf.addVertex(matrix, x0, y1, z0).setUv(0, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y1, z0).setUv(1, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y1, z1).setUv(1, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x0, y1, z1).setUv(0, 1).setColor(1f, 1f, 1f, alpha);
            }
            case NORTH -> {
                buf.addVertex(matrix, x0, y0, z0).setUv(0, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y0, z0).setUv(1, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y1, z0).setUv(1, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x0, y1, z0).setUv(0, 0).setColor(1f, 1f, 1f, alpha);
            }
            case SOUTH -> {
                buf.addVertex(matrix, x0, y0, z1).setUv(1, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x0, y1, z1).setUv(1, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y1, z1).setUv(0, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y0, z1).setUv(0, 1).setColor(1f, 1f, 1f, alpha);
            }
            case WEST -> {
                buf.addVertex(matrix, x0, y0, z0).setUv(1, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x0, y1, z0).setUv(1, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x0, y1, z1).setUv(0, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x0, y0, z1).setUv(0, 1).setColor(1f, 1f, 1f, alpha);
            }
            case EAST -> {
                buf.addVertex(matrix, x1, y0, z0).setUv(0, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y0, z1).setUv(1, 1).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y1, z1).setUv(1, 0).setColor(1f, 1f, 1f, alpha);
                buf.addVertex(matrix, x1, y1, z0).setUv(0, 0).setColor(1f, 1f, 1f, alpha);
            }
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private static boolean isSpaceDimension(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim) {
        return dim.location().getNamespace().equals("astronautics")
            && dim.location().getPath().equals("space");
    }

    private enum Face {
        TOP, BOTTOM, NORTH, SOUTH, WEST, EAST
    }
}