package com.BreadRes.astronautics.client.screenshake;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class RocketScreenShakeHandler {

    private static float intensity = 0f;

    public static void setEngine(Vec3 enginePos, float throttle) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        double dist = mc.player.position().distanceTo(enginePos);

        float strength;
        if (dist <= 5.0) {
            strength = throttle;
        } else if (dist <= 10.0) {
            strength = throttle * (1f - (float)((dist - 5.0) / 5.0));
        } else {
            strength = 0f;
        }

        if (strength > intensity) intensity = strength;
    }

    public static void tick() {
        intensity *= 0.75f;
        if (intensity < 0.001f) intensity = 0f;
    }

    public static float getIntensity() { return intensity; }
}