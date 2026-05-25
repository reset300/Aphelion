package com.BreadRes.astronautics.client.space;

import com.BreadRes.astronautics.Astronautics;
import com.BreadRes.astronautics.client.TransitionRenderer;
import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Astronautics.MOD_ID, value = Dist.CLIENT)
public class SpaceSkyOverlayRenderer {

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        var dim = mc.player.level().dimension();

        if (!dim.equals(Level.OVERWORLD)) return;

        float alpha = TransitionRenderer.getSkyboxAlpha();
        if (alpha <= 0f) return;

        SpaceSkyEffects.currentAlpha = alpha;

        SpaceSkyEffects.renderSkyDirect(
                event.getModelViewMatrix(),
                event.getProjectionMatrix()
        );

        SpaceSkyEffects.currentAlpha = 1.0f;
    }
}