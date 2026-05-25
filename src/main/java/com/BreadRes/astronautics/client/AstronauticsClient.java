package com.BreadRes.astronautics.client;

import com.BreadRes.astronautics.client.space.SpaceSkyEffects;
import com.BreadRes.astronautics.content.blocks.fuel_tank.AstronauticsFuelTankBlockEntity;
import com.BreadRes.astronautics.content.blocks.rocket_engine.RocketEngineRenderer;
import com.BreadRes.astronautics.content.planets.AstronauticsPlanets;
import com.BreadRes.astronautics.content.planets.Planet;
import com.BreadRes.astronautics.content.planets.PlanetRegistry;
import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.BreadRes.astronautics.content.blocks.fuel_tank.FuelTankRenderer;
import com.BreadRes.astronautics.content.blocks.antenna.AntennaControllerRenderer;
@EventBusSubscriber(modid = "astronautics", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AstronauticsClient {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                AstronauticsRegistry.ROCKET_ENGINE_BE.get(),
                RocketEngineRenderer::new
        );
        event.registerBlockEntityRenderer(
                AstronauticsRegistry.FUEL_TANK_BE.get(),
                FuelTankRenderer::new
        );
        event.registerBlockEntityRenderer(
                AstronauticsRegistry.ANTENNA_CONTROLLER_BE.get(),
                AntennaControllerRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        AstronauticsPlanets.init();

        SpaceSkyEffects effects = new SpaceSkyEffects();

        event.register(
                ResourceLocation.fromNamespaceAndPath("astronautics", "space"),
                effects
        );

        for (Planet planet : PlanetRegistry.getAllPlanets()) {
            event.register(
                    planet.getOrbitDimension().location(),
                    new SpaceSkyEffects()
            );
        }
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureAtlasStitchedEvent event) {
    }

    @EventBusSubscriber(modid = "astronautics", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public class AstronauticsClientSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            AstronauticsPartialModels.init();
        }
    }

//    @SubscribeEvent
//    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
//        event.register(ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath("astronautics", "block/antenna/antenna_dish")));
//    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        AstronauticsFuelTankBlockEntity.registerCapabilities(event);
    }

    @SubscribeEvent
    public static void onRegisterRenderTypes(RegisterNamedRenderTypesEvent event) {
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_still");
            }
            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_flow");
            }
            @Override
            public int getTintColor() { return 0xCCD4C85A; }
        }, AstronauticsRegistry.UDMH_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_still");
            }
            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_flow");
            }
            @Override
            public int getTintColor() { return 0xCCB84A00; }
        }, AstronauticsRegistry.N2O4_TYPE.get());
    }
}