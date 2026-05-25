package com.BreadRes.astronautics.content.blocks.antenna;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AntennaControllerRenderer implements BlockEntityRenderer<AntennaControllerBlockEntity> {

    private static final int RADIAL_SEGMENTS = 96;
    private static final int RING_SEGMENTS = 64;

    private static final float RADIUS = 1.2f;
    private static final float PARABOLA_DIV = 1.3f;
    private static final float THICKNESS = 0.05f;
    private static final float SCALE = 5.0f;

    public AntennaControllerRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(AntennaControllerBlockEntity be, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int light, int overlay) {

        if (!be.isFormed()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        VertexConsumer vc = buffer.getBuffer(RenderType.solid());
        PoseStack.Pose pose = poseStack.last();

        float s = SCALE / RADIUS;

        for (int i = 0; i < RADIAL_SEGMENTS; i++) {
            float a0 = (float)(2 * Math.PI * i / RADIAL_SEGMENTS);
            float a1 = (float)(2 * Math.PI * (i + 1) / RADIAL_SEGMENTS);

            for (int j = 0; j < RING_SEGMENTS; j++) {
                float r0 = RADIUS * j / RING_SEGMENTS;
                float r1 = RADIUS * (j + 1) / RING_SEGMENTS;

                float x00 = r0 * (float)Math.cos(a0) * s;
                float z00 = r0 * (float)Math.sin(a0) * s;
                float x01 = r0 * (float)Math.cos(a1) * s;
                float z01 = r0 * (float)Math.sin(a1) * s;

                float x10 = r1 * (float)Math.cos(a0) * s;
                float z10 = r1 * (float)Math.sin(a0) * s;
                float x11 = r1 * (float)Math.cos(a1) * s;
                float z11 = r1 * (float)Math.sin(a1) * s;

                float y00 = parabola(r0) * s;
                float y01 = parabola(r0) * s;
                float y10 = parabola(r1) * s;
                float y11 = parabola(r1) * s;

                float ny0 = normalY(r0);
                float ny1 = normalY(r1);

                float nx00 = (float)Math.cos(a0) * (1 - ny0);
                float nz00 = (float)Math.sin(a0) * (1 - ny0);

                float nx01 = (float)Math.cos(a1) * (1 - ny0);
                float nz01 = (float)Math.sin(a1) * (1 - ny0);

                float nx10 = (float)Math.cos(a0) * (1 - ny1);
                float nz10 = (float)Math.sin(a0) * (1 - ny1);

                float nx11 = (float)Math.cos(a1) * (1 - ny1);
                float nz11 = (float)Math.sin(a1) * (1 - ny1);

                quad(vc, pose,
                        x00, y00, z00,
                        x10, y10, z10,
                        x11, y11, z11,
                        x01, y01, z01,
                        nx00, ny0, nz00,
                        nx10, ny1, nz10,
                        nx11, ny1, nz11,
                        nx01, ny0, nz01,
                        light, overlay
                );

                float yt00 = y00 + THICKNESS;
                float yt01 = y01 + THICKNESS;
                float yt10 = y10 + THICKNESS;
                float yt11 = y11 + THICKNESS;

                quad(vc, pose,
                        x01, yt01, z01,
                        x11, yt11, z11,
                        x10, yt10, z10,
                        x00, yt00, z00,
                        -nx01, -ny0, -nz01,
                        -nx11, -ny1, -nz11,
                        -nx10, -ny1, -nz10,
                        -nx00, -ny0, -nz00,
                        light, overlay
                );

                if (j == RING_SEGMENTS - 1) {
                    quad(vc, pose,
                            x10, y10, z10,
                            x11, y11, z11,
                            x11, yt11, z11,
                            x10, yt10, z10,
                            nx10, ny1, nz10,
                            nx11, ny1, nz11,
                            nx11, ny1, nz11,
                            nx10, ny1, nz10,
                            light, overlay
                    );
                }
            }
        }

        renderFocus(vc, pose, light, overlay);

        poseStack.popPose();
    }

    private void quad(VertexConsumer vc, PoseStack.Pose pose,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float nx0, float ny0, float nz0,
                      float nx1, float ny1, float nz1,
                      float nx2, float ny2, float nz2,
                      float nx3, float ny3, float nz3,
                      int light, int overlay) {

        vertex(vc, pose, x0, y0, z0, nx0, ny0, nz0, light, overlay);
        vertex(vc, pose, x1, y1, z1, nx1, ny1, nz1, light, overlay);
        vertex(vc, pose, x2, y2, z2, nx2, ny2, nz2, light, overlay);
        vertex(vc, pose, x3, y3, z3, nx3, ny3, nz3, light, overlay);
    }

    private void vertex(VertexConsumer vc, PoseStack.Pose pose,
                        float x, float y, float z,
                        float nx, float ny, float nz,
                        int light, int overlay) {

        vc.addVertex(pose.pose(), x, y, z)
                .setColor(200, 200, 200, 255)
                .setUv(0f, 0f)
                .setLight(light)
                .setOverlay(overlay)
                .setNormal(pose, nx, ny, nz);
    }

    private float parabola(float r) {
        return (r * r) / PARABOLA_DIV;
    }

    private float normalY(float r) {
        float dydr = (2 * r) / PARABOLA_DIV;
        float len = (float)Math.sqrt(1 + dydr * dydr);
        return 1f / len;
    }

    private void renderFocus(VertexConsumer vc, PoseStack.Pose pose, int light, int overlay) {
        float s = SCALE / RADIUS;
        float y = parabola(0) * s + 0.5f;

        int seg = 16;
        float r = 0.1f;
        float h = 0.3f;

        for (int i = 0; i < seg; i++) {
            float a0 = (float)(2 * Math.PI * i / seg);
            float a1 = (float)(2 * Math.PI * (i + 1) / seg);

            float x0 = r * (float)Math.cos(a0);
            float z0 = r * (float)Math.sin(a0);
            float x1 = r * (float)Math.cos(a1);
            float z1 = r * (float)Math.sin(a1);

            vertex(vc, pose, x0, y, z0, 0, 1, 0, light, overlay);
            vertex(vc, pose, x1, y, z1, 0, 1, 0, light, overlay);
            vertex(vc, pose, x1, y + h, z1, 0, 1, 0, light, overlay);
            vertex(vc, pose, x0, y + h, z0, 0, 1, 0, light, overlay);
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}