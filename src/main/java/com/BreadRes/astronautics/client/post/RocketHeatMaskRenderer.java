package com.BreadRes.astronautics.client.post;

import com.BreadRes.astronautics.Astronautics;
import com.BreadRes.astronautics.client.ClientRocketEngines;
import com.BreadRes.astronautics.client.shader.RocketFlameRenderType;
import com.BreadRes.astronautics.client.shader.RocketHeatMaskRenderType;
import com.BreadRes.astronautics.content.blocks.rocket_engine.RocketEngineBlock;
import com.BreadRes.astronautics.content.blocks.rocket_engine.RocketEngineBlockEntity;
import com.BreadRes.astronautics.content.blocks.rocket_engine.RocketEngineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;

@EventBusSubscriber(modid = Astronautics.MOD_ID, value = Dist.CLIENT)
public class RocketHeatMaskRenderer {

    private static final ResourceLocation FBO =
            ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "rocket_heat_mask");

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ClientRocketEngines.ENGINES.removeIf(engine -> engine == null || engine.isRemoved());
        if (ClientRocketEngines.ENGINES.isEmpty()) return;

        AstronauticsVeilPost.ensureAdded();
        float pt = mc.getTimer().getGameTimeDeltaPartialTick(true);
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();

        AdvancedFbo.getMainFramebuffer().bind(true);

        for (RocketEngineBlockEntity engine : ClientRocketEngines.ENGINES) {
            if (engine.getBlockState().getValue(RocketEngineBlock.HALF) != DoubleBlockHalf.UPPER) continue;
            if (engine.getLevel() == null) continue;

            float targetScale = engine.clientActive ? (engine.clientSignal / 15.0f) : 0f;
            engine.clientFlameScale += (targetScale - engine.clientFlameScale) * 0.03f;
            if (engine.clientFlameScale < 0.005f) engine.clientFlameScale = 0f;

            if (engine.clientFlameScale <= 0.005f) continue;

            PoseStack flamePose = new PoseStack();

            var sub = dev.ryanhcode.sable.Sable.HELPER.getContaining(engine.getLevel(), engine.getBlockPos());
            Vec3 worldPos;
            if (sub instanceof dev.ryanhcode.sable.sublevel.ClientSubLevel clientSub) {
                var pose = clientSub.renderPose(pt);
                worldPos = pose.transformPosition(new Vec3(
                        engine.getBlockPos().getX() + 0.5,
                        engine.getBlockPos().getY() + 0.5,
                        engine.getBlockPos().getZ() + 0.5
                ));
                flamePose.translate(worldPos.x - cam.x, worldPos.y - cam.y, worldPos.z - cam.z);
                org.joml.Quaternionf quat = new org.joml.Quaternionf(
                        (float) pose.orientation().x(),
                        (float) pose.orientation().y(),
                        (float) pose.orientation().z(),
                        (float) pose.orientation().w()
                );
                flamePose.mulPose(quat);
            } else {
                worldPos = Vec3.atCenterOf(engine.getBlockPos());
                flamePose.translate(worldPos.x - cam.x, worldPos.y - cam.y, worldPos.z - cam.z);
            }

            RocketEngineRenderer.rotate(flamePose, engine.getBlockState().getValue(RocketEngineBlock.FACING));
            flamePose.translate(0.0, -0.92, 0.0);

            RocketEngineRenderer.renderFlame(engine, pt, flamePose, source);
        }
        source.endBatch(RocketFlameRenderType.rocketFlame());

        var renderer = VeilRenderSystem.renderer();
        if (renderer == null) return;

        var fbo = renderer.getFramebufferManager().getFramebuffer(FBO);
        if (fbo == null) return;

        PoseStack ps = new PoseStack();
        fbo.bind(true);
        fbo.clear(0, 0, 0, 0, 1, GL_COLOR_BUFFER_BIT);
        VertexConsumer vc = source.getBuffer(RocketHeatMaskRenderType.rocketHeatMask());

        for (RocketEngineBlockEntity engine : ClientRocketEngines.ENGINES) {
            if (!engine.clientActive && engine.clientStartupTicks <= 0) continue;
            if (engine.getBlockState().getValue(RocketEngineBlock.HALF) != DoubleBlockHalf.UPPER) continue;

            float throttle = engine.clientThrottle / 100f;
            float ignition = engine.getClientIgnitionProgress(pt);
            float smoke = engine.getClientSmokeProgress(pt);
            float power = Math.max(throttle * ignition, smoke * 0.18f);
            if (power <= 0.01f) continue;

            float length = Mth.lerp(power, 5f, 30f);
            float radius = Mth.lerp(power, 0.32f, 1.0f);
            int alpha = Mth.clamp((int)((ignition * 0.95f + smoke * 0.35f) * throttle * 255.0f), 0, 255);
            if (alpha <= 0) continue;

            BlockPos pos = engine.getBlockPos();
            Direction dir = engine.getBlockState().getValue(RocketEngineBlock.FACING);

            var sub = dev.ryanhcode.sable.Sable.HELPER.getContaining(engine.getLevel(), pos);
            Vec3 worldPos;
            if (sub instanceof dev.ryanhcode.sable.sublevel.ClientSubLevel clientSub) {
                var pose = clientSub.renderPose(pt);
                worldPos = pose.transformPosition(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                ps.pushPose();
                ps.translate(worldPos.x - cam.x, worldPos.y - cam.y, worldPos.z - cam.z);
                org.joml.Quaternionf quat = new org.joml.Quaternionf(
                        (float) pose.orientation().x(),
                        (float) pose.orientation().y(),
                        (float) pose.orientation().z(),
                        (float) pose.orientation().w()
                );
                ps.mulPose(quat);
            } else {
                worldPos = Vec3.atCenterOf(pos);
                ps.pushPose();
                ps.translate(worldPos.x - cam.x, worldPos.y - cam.y, worldPos.z - cam.z);
            }

            RocketEngineRenderer.rotate(ps, dir);
            ps.translate(0.0, -0.92, 0.0);
            drawMaskMesh(ps.last().pose(), vc, length, radius, 12, 8, alpha);
            ps.popPose();
        }

        source.endBatch(RocketHeatMaskRenderType.rocketHeatMask());
        AdvancedFbo.getMainFramebuffer().bind(true);
    }

    private static void drawMaskMesh(Matrix4f m, VertexConsumer vc, float length, float radius, int segments, int rings, int alpha) {
        for (int j = 0; j < rings; j++) {
            float t0 = (float) j / rings;
            float t1 = (float) (j + 1) / rings;
            float y0 = -length * t0;
            float y1 = -length * t1;
            float r0 = radiusAt(radius, t0);
            float r1 = radiusAt(radius, t1);
            int a0 = alphaAt(alpha, t0);
            int a1 = alphaAt(alpha, t1);
            for (int i = 0; i < segments; i++) {
                float u0 = (float) i / segments;
                float u1 = (float) (i + 1) / segments;
                float aAng0 = Mth.TWO_PI * u0;
                float aAng1 = Mth.TWO_PI * u1;
                float x00 = Mth.cos(aAng0) * r0, z00 = Mth.sin(aAng0) * r0;
                float x01 = Mth.cos(aAng1) * r0, z01 = Mth.sin(aAng1) * r0;
                float x10 = Mth.cos(aAng0) * r1, z10 = Mth.sin(aAng0) * r1;
                float x11 = Mth.cos(aAng1) * r1, z11 = Mth.sin(aAng1) * r1;
                v(vc, m, x00, y0, z00, u0, t0, a0);
                v(vc, m, x10, y1, z10, u0, t1, a1);
                v(vc, m, x11, y1, z11, u1, t1, a1);
                v(vc, m, x01, y0, z01, u1, t0, a0);
            }
        }
    }

    private static float radiusAt(float baseRadius, float t) {
        float expansion = 1.2f + 0.32f * Mth.sin(t * Mth.PI);
        float tip = 1.0f - smoothstep(0.72f, 1.0f, t) * 0.78f;
        return baseRadius * expansion * tip;
    }

    private static int alphaAt(int alpha, float t) {
        float fadeIn = smoothstep(0.0f, 0.08f, t);
        float fadeOut = 1.0f - smoothstep(0.76f, 1.0f, t);
        return Mth.clamp((int)(alpha * fadeIn * fadeOut), 0, 255);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private static void v(VertexConsumer vc, Matrix4f m, float x, float y, float z, float u, float v, int a) {
        vc.addVertex(m, x, y, z).setUv(u, v).setColor(255, 255, 255, a);
    }

    private static void rotate(PoseStack ps, Direction d) {
        switch (d) {
            case DOWN -> {}
            case UP    -> ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180));
            case NORTH -> ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
            case SOUTH -> ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            case WEST  -> ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90));
            case EAST  -> ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90));
        }
    }
}