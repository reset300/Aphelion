package com.BreadRes.astronautics.content.blocks.rocket_engine;

import com.BreadRes.astronautics.client.*;
import com.BreadRes.astronautics.client.post.AstronauticsVeilPost;
import com.BreadRes.astronautics.client.screenshake.RocketScreenShakeHandler;
import com.BreadRes.astronautics.client.shader.RocketFlameRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import static com.BreadRes.astronautics.client.shader.AstronauticsShaders.rocketFlame;

public class RocketEngineRenderer implements BlockEntityRenderer<RocketEngineBlockEntity> {

    public RocketEngineRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static void renderFlame(RocketEngineBlockEntity be, float pt, PoseStack ps, MultiBufferSource buffer) {
        if (be.getBlockState().getValue(RocketEngineBlock.HALF) != DoubleBlockHalf.UPPER) return;
        if (be.getLevel() == null) return;

        float throttle = be.clientThrottle / 100.0f;
        float ignition = be.getClientIgnitionProgress(pt);
        float smoke = be.getClientSmokeProgress(pt);
        float power = Math.max(throttle * ignition, smoke * 0.18f);

        if (be.clientFlameScale <= 0.01f) return;

        float time = (be.getLevel().getGameTime() + pt) / 20.0f;
        float flicker = 0.94f
                + 0.035f * Mth.sin(time * 31.0f)
                + 0.025f * Mth.sin(time * 73.0f)
                + 0.015f * Mth.sin(time * 151.0f);

        float maxThrust = 415.0f;
        float thrustPct = Mth.clamp(be.clientThrust / maxThrust, 0f, 1f);

        float flameLength = Mth.lerp(Math.max(power, be.clientFlameScale * 0.1f), 5.0f, 30.0f) * flicker * be.clientFlameScale;
        float baseRadius  = Mth.lerp(power, 0.22f, 0.98f);
        float brightness  = Mth.lerp(throttle * ignition, 0.8f, 1.1f);

        ShaderInstance shader = rocketFlame();
        if (shader != null) {
            set(shader, "Time",          time);
            set(shader, "Throttle",      thrustPct);
            set(shader, "FlameLength",   flameLength);
            set(shader, "FlameRadius",   baseRadius);
            set(shader, "CoreRadius",    Mth.lerp(power, 0.06f, 0.16f));
            set(shader, "OuterStrength", 1.35f);
            set(shader, "Brightness",    brightness);
            set(shader, "Startup",       be.getClientStartupProgress(pt));
            set(shader, "SmokePhase",    smoke);
        }

        Direction dir = be.getBlockState().getValue(RocketEngineBlock.FACING);
        BlockPos pos  = be.getBlockPos();

        ps.pushPose();

        VertexConsumer vc = buffer.getBuffer(RocketFlameRenderType.rocketFlame());
        drawPlumeMesh(ps.last().pose(), vc, flameLength, baseRadius,        16, 12, 255, time);
        drawPlumeMesh(ps.last().pose(), vc, flameLength, baseRadius * 0.6f, 12, 10, 220, time);
        drawPlumeMesh(ps.last().pose(), vc, flameLength, baseRadius * 0.3f, 8,  8,  180, time);
        drawPlumeMesh(ps.last().pose(), vc, flameLength, baseRadius * 0.1f, 6,  6,  140, time);

        Level clientLevel = be.getLevel();
        Vec3 start  = Vec3.atCenterOf(pos).add(0.5, 0.5, 0.5);
        Vec3 dirVec = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
        Vec3 end    = start.add(dirVec.scale(flameLength));

        BlockHitResult hit = clientLevel.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity) null
        ));

        boolean groundHit = hit.getType() == HitResult.Type.BLOCK;
        float groundDist  = groundHit ? (float) hit.getLocation().distanceTo(start) : flameLength;

        if (groundHit && groundDist < flameLength) {
            float groundRadius = baseRadius * Mth.lerp(groundDist / flameLength, 0.3f, 1.8f);
            float splashAlpha  = Mth.clamp(1.0f - groundDist / flameLength, 0.3f, 1.0f);
            ps.pushPose();
            ps.translate(0, -groundDist, 0);
            drawSplashMesh(ps.last().pose(), vc, groundRadius, 16, (int)(splashAlpha * 255));
            ps.popPose();
        }

        ps.popPose();

        Vec3 worldEnginePos;
        var level = be.getLevel();
        var sub = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, pos);
        if (sub instanceof ClientSubLevel clientSub) {
            Pose3dc pose = clientSub.renderPose(pt);
            worldEnginePos = pose.transformPosition(Vec3.atCenterOf(pos));
        } else {
            worldEnginePos = Vec3.atCenterOf(pos);
        }
        RocketScreenShakeHandler.setEngine(worldEnginePos, thrustPct);
    }

    @Override
    public void render(RocketEngineBlockEntity be, float pt, PoseStack ps, MultiBufferSource buffer, int light, int overlay) {
        if (be == null || be.getLevel() == null) return;
        if (be.getBlockState().getValue(RocketEngineBlock.HALF) != DoubleBlockHalf.UPPER) return;
        if (!be.clientActive) return;

        float throttle = be.clientThrottle / 100.0f;
        if (throttle <= 0.01f) return;

        float ignition = be.getClientIgnitionProgress(pt);
        BlockPos pos = be.getBlockPos();

        Vec3 worldEnginePos;
        var level = be.getLevel();
        var sub = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, pos);
        if (sub instanceof ClientSubLevel clientSub) {
            Pose3dc pose = clientSub.renderPose(pt);
            worldEnginePos = pose.transformPosition(Vec3.atCenterOf(pos));
        } else {
            worldEnginePos = Vec3.atCenterOf(pos);
        }
        RocketScreenShakeHandler.setEngine(worldEnginePos, throttle * ignition);
    }

    private static void drawPlumeMesh(Matrix4f m, VertexConsumer vc,
                                      float length, float radius,
                                      int segments, int rings, int alpha, float time) {
        for (int j = 0; j < rings; j++) {
            float t0 = (float) j / rings;
            float t1 = (float) (j + 1) / rings;

            float y0 = -length * t0;
            float y1 = -length * t1;

            float r0 = radiusAt(radius, t0, time);
            float r1 = radiusAt(radius, t1, time);

            float wobbleX0 = 0.04f * baseRadius(radius, t0) * Mth.sin(time * 5.2f + t0 * 9.0f);
            float wobbleZ0 = 0.04f * baseRadius(radius, t0) * Mth.sin(time * 6.7f + t0 * 11.0f);
            float wobbleX1 = 0.04f * baseRadius(radius, t1) * Mth.sin(time * 5.2f + t1 * 9.0f);
            float wobbleZ1 = 0.04f * baseRadius(radius, t1) * Mth.sin(time * 6.7f + t1 * 11.0f);

            for (int i = 0; i < segments; i++) {
                float u0 = (float) i / segments;
                float u1 = (float) (i + 1) / segments;
                float aAng0 = Mth.TWO_PI * u0;
                float aAng1 = Mth.TWO_PI * u1;

                float x00 = Mth.cos(aAng0) * r0 + wobbleX0, z00 = Mth.sin(aAng0) * r0 + wobbleZ0;
                float x01 = Mth.cos(aAng1) * r0 + wobbleX0, z01 = Mth.sin(aAng1) * r0 + wobbleZ0;
                float x10 = Mth.cos(aAng0) * r1 + wobbleX1, z10 = Mth.sin(aAng0) * r1 + wobbleZ1;
                float x11 = Mth.cos(aAng1) * r1 + wobbleX1, z11 = Mth.sin(aAng1) * r1 + wobbleZ1;

                v(vc, m, x00, y0, z00, u0, t0, alphaAt(255, t0));
                v(vc, m, x10, y1, z10, u0, t1, alphaAt(255, t1));
                v(vc, m, x11, y1, z11, u1, t1, alphaAt(255, t1));
                v(vc, m, x01, y0, z01, u1, t0, alphaAt(255, t0));
            }
        }
    }

    private static float baseRadius(float baseRadius, float t) {
        float expansion = 1.18f + 0.22f * Mth.sin(t * Mth.PI);
        float tip = 1.0f - smoothstep(0.72f, 1.0f, t) * 0.88f;
        return baseRadius * Mth.lerp(t, 0.72f, expansion) * tip;
    }

    private static void drawSplashMesh(Matrix4f m, VertexConsumer vc,
                                       float radius, int segments, int alpha) {
        for (int i = 0; i < segments; i++) {
            float u0 = (float) i / segments;
            float u1 = (float) (i + 1) / segments;
            float a0 = Mth.TWO_PI * u0;
            float a1 = Mth.TWO_PI * u1;
            for (int r = 0; r < 4; r++) {
                float rt0 = (float) r / 4;
                float rt1 = (float) (r + 1) / 4;
                float rad0 = radius * rt0;
                float rad1 = radius * rt1;
                int a = Mth.clamp((int)((1.0f - rt0) * alpha), 0, 255);
                float x00 = Mth.cos(a0) * rad0, z00 = Mth.sin(a0) * rad0;
                float x01 = Mth.cos(a1) * rad0, z01 = Mth.sin(a1) * rad0;
                float x10 = Mth.cos(a0) * rad1, z10 = Mth.sin(a0) * rad1;
                float x11 = Mth.cos(a1) * rad1, z11 = Mth.sin(a1) * rad1;
                v(vc, m, x00, 0, z00, u0, rt0, a);
                v(vc, m, x10, 0, z10, u0, rt1, a);
                v(vc, m, x11, 0, z11, u1, rt1, a);
                v(vc, m, x01, 0, z01, u1, rt0, a);
            }
        }
    }

    private static float radiusAt(float baseRadius, float t, float time) {
        float expansion = 1.18f + 0.22f * Mth.sin(t * Mth.PI);
        float tip = 1.0f - smoothstep(0.72f, 1.0f, t) * 0.88f;
        float flutter = 1.0f + 0.06f * Mth.sin(time * 8.3f + t * 12.0f)
                + 0.03f * Mth.sin(time * 17.1f + t * 7.0f);
        return baseRadius * Mth.lerp(t, 0.72f, expansion) * tip * flutter;
    }

    private static int alphaAt(int alpha, float t) {
        float fadeIn  = smoothstep(0.0f, 0.08f, t);
        float fadeOut = 1.0f - smoothstep(0.74f, 1.0f, t);
        return Mth.clamp((int)(alpha * fadeIn * fadeOut), 0, 255);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private static void v(VertexConsumer vc, Matrix4f m,
                          float x, float y, float z,
                          float u, float v, int alpha) {
        int cr = Mth.clamp((int)((x + 2.0f) / 4.0f * 255), 0, 255);
        int cg = Mth.clamp((int)((z + 2.0f) / 4.0f * 255), 0, 255);
        vc.addVertex(m, x, y, z)
                .setUv(u, v)
                .setColor(cr, cg, 0, alpha);
    }

    public static void rotate(PoseStack ps, Direction d) {
        switch (d) {
            case DOWN  -> ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180));
            case UP    -> {}
            case NORTH -> ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
            case SOUTH -> ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            case WEST  -> ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90));
            case EAST  -> ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90));
        }
    }

    private static void set(ShaderInstance s, String n, float v) {
        var u = s.getUniform(n);
        if (u != null) u.set(v);
    }

    @Override
    public boolean shouldRenderOffScreen(RocketEngineBlockEntity be) { return true; }

    @Override
    public int getViewDistance() { return 4096 * 4; }
}