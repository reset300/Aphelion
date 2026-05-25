package com.BreadRes.astronautics;

import com.BreadRes.astronautics.content.planets.AstronauticsPlanets;
import com.BreadRes.astronautics.content.utils.FluidDensityDataHandler;
import com.BreadRes.astronautics.integration.AstronauticsTabInit;
import com.BreadRes.astronautics.network.AstronauticsNetwork;
import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(Astronautics.MOD_ID)
public class Astronautics {
    public static final String MOD_ID = "astronautics";

    public Astronautics(IEventBus modBus) {
        AstronauticsRegistry.register(modBus);
        AstronauticsTabInit.register();
        AstronauticsNetwork.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        AstronauticsPlanets.init();
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(FluidDensityDataHandler.INSTANCE);
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientSetup {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(
                        AstronauticsRegistry.FUEL_TANK_BLOCK.get(),
                        RenderType.cutout()
                );
            });
        }
    }
}