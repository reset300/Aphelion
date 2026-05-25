package com.BreadRes.astronautics.client.screenshake;

import com.BreadRes.astronautics.Astronautics;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.plot.ClientLevelPlot;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Astronautics.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class RocketScreenShakeEvents {

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float intensity = RocketScreenShakeHandler.getIntensity();
        if (intensity <= 0) return;
        long t = System.currentTimeMillis();
        float yaw   = (float)(Math.sin(t * 0.042) * intensity * 0.8);
        float pitch = (float)(Math.sin(t * 0.037) * intensity * 0.6);
        event.setYaw(event.getYaw()     + yaw);
        event.setPitch(event.getPitch() + pitch);
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide()) return;
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        RocketScreenShakeHandler.tick();

    }
}