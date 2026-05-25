package com.BreadRes.astronautics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class TransitionRenderer {

    public static final ResourceKey<Level> ORBIT_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("astronautics", "earth_orbit")
    );

    public static final int LAUNCH_HEIGHT = 8000;
    public static final int SKY_FADE_START = 6000;

    private static float skyboxAlpha = 0f;
    private static long skyTime = 0;
    private static ResourceKey<Level> lastDimension = null;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (event.getEntity() != mc.player) return;

        ResourceKey<Level> current = mc.player.level().dimension();

        if (lastDimension != null && !lastDimension.equals(current)) {
            skyboxAlpha = 0f;
        }
        lastDimension = current;

        if (current.equals(Level.OVERWORLD)) {
            double y = mc.player.getY();
            float target;
            if (y <= SKY_FADE_START) {
                target = 0f;
            } else if (y >= LAUNCH_HEIGHT) {
                target = 1f;
            } else {
                target = (float)((y - SKY_FADE_START) / (double)(LAUNCH_HEIGHT - SKY_FADE_START));
            }
            skyboxAlpha += (target - skyboxAlpha) * 0.1f;
            if (skyboxAlpha < 0.001f) skyboxAlpha = 0f;
            if (skyboxAlpha > 0.999f) skyboxAlpha = 1f;
        } else {
            skyboxAlpha = 0f;
        }

        skyTime++;
    }

    public static float getSkyboxAlpha() { return skyboxAlpha; }
    public static long getSkyTime() { return skyTime; }
}