package com.BreadRes.astronautics.client.post;

import com.BreadRes.astronautics.Astronautics;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Astronautics.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class AstronauticsVeilPost {

    public static final ResourceLocation ROCKET_HEAT =
            ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "rocket_engine");

    public static final ResourceLocation CRT =
            ResourceLocation.fromNamespaceAndPath(Astronautics.MOD_ID, "crt_screen");

    private static boolean added;

    private AstronauticsVeilPost() {}

    public static void ensureAdded() {
        if (VeilRenderSystem.renderer() == null) return;

        var ppm = VeilRenderSystem.renderer().getPostProcessingManager();

        ppm.add(2000, ROCKET_HEAT);

        ppm.add(3000, CRT);
    }

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        added = false;
        ensureAdded();
    }
}