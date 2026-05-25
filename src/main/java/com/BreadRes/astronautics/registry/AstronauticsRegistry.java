package com.BreadRes.astronautics.registry;

import com.BreadRes.astronautics.Astronautics;
import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlock;
import com.BreadRes.astronautics.content.blocks.control_panel.ControlPanelBlockEntity;
import com.BreadRes.astronautics.content.blocks.fuel_tank.AstronauticsFuelTankBlockEntity;
import com.BreadRes.astronautics.content.blocks.fuel_tank.FuelTankBlock;
import com.BreadRes.astronautics.content.blocks.rocket_engine.RocketEngineBlock;
import com.BreadRes.astronautics.content.blocks.rocket_engine.RocketEngineBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.BreadRes.astronautics.content.blocks.antenna.*;
import java.util.function.Supplier;
import com.BreadRes.astronautics.content.blocks.fuel_tank.*;

public class AstronauticsRegistry {

    // Misc
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, Astronautics.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Astronautics.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Astronautics.MOD_ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Astronautics.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, Astronautics.MOD_ID);

    // Rocket Engine
    public static final Supplier<RocketEngineBlock> ROCKET_ENGINE_BLOCK =
            BLOCKS.register("rocket_engine", RocketEngineBlock::new);
    public static final Supplier<Item> ROCKET_ENGINE_ITEM =
            ITEMS.register("rocket_engine", () -> new BlockItem(ROCKET_ENGINE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<RocketEngineBlockEntity>> ROCKET_ENGINE_BE =
            BLOCK_ENTITIES.register("rocket_engine", () ->
                    BlockEntityType.Builder.of(RocketEngineBlockEntity::new, ROCKET_ENGINE_BLOCK.get()).build(null));

    // UDMH
    public static final Supplier<FluidType> UDMH_TYPE = FLUID_TYPES.register("udmh",
            () -> new FluidType(FluidType.Properties.create().density(791).viscosity(791).temperature(293)) {});
    public static final Supplier<FlowingFluid> UDMH_SOURCE = FLUIDS.register("udmh",
            () -> new BaseFlowingFluid.Source(udmhProps()));
    public static final Supplier<FlowingFluid> UDMH_FLOWING = FLUIDS.register("flowing_udmh",
            () -> new BaseFlowingFluid.Flowing(udmhProps()));
    public static final Supplier<LiquidBlock> UDMH_BLOCK = BLOCKS.register("udmh_fluid",
            () -> new LiquidBlock(UDMH_SOURCE.get(), Block.Properties.of().noCollission().strength(100f).noLootTable()));
    public static final Supplier<Item> UDMH_BUCKET = ITEMS.register("udmh_bucket",
            () -> new BucketItem(UDMH_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    // N2O4
    public static final Supplier<FluidType> N2O4_TYPE = FLUID_TYPES.register("n2o4",
            () -> new FluidType(FluidType.Properties.create().density(1450).viscosity(1450).temperature(294)) {});
    public static final Supplier<FlowingFluid> N2O4_SOURCE = FLUIDS.register("n2o4",
            () -> new BaseFlowingFluid.Source(n2o4Props()));
    public static final Supplier<FlowingFluid> N2O4_FLOWING = FLUIDS.register("flowing_n2o4",
            () -> new BaseFlowingFluid.Flowing(n2o4Props()));
    public static final Supplier<LiquidBlock> N2O4_BLOCK = BLOCKS.register("n2o4_fluid",
            () -> new LiquidBlock(N2O4_SOURCE.get(), Block.Properties.of().noCollission().strength(100f).noLootTable()));
    public static final Supplier<Item> N2O4_BUCKET = ITEMS.register("n2o4_bucket",
            () -> new BucketItem(N2O4_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    // Fuel Tank
    public static final Supplier<FuelTankBlock> FUEL_TANK_BLOCK =
            BLOCKS.register("fuel_tank", FuelTankBlock::new);
    public static final Supplier<Item> FUEL_TANK_ITEM =
            ITEMS.register("fuel_tank", () -> new BlockItem(FUEL_TANK_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<AstronauticsFuelTankBlockEntity>> FUEL_TANK_BE =
            BLOCK_ENTITIES.register("fuel_tank", () ->
                    BlockEntityType.Builder.of(AstronauticsFuelTankBlockEntity::new, FUEL_TANK_BLOCK.get()).build(null));
    public static final Supplier<Item> DIVIDER =
            ITEMS.register("divider", DividerItem::new);

    // Control Panel
    public static final Supplier<ControlPanelBlock> CONTROL_PANEL_BLOCK =
            BLOCKS.register("control_panel", ControlPanelBlock::new);

    public static final Supplier<Item> CONTROL_PANEL_ITEM =
            ITEMS.register("control_panel", () ->
                    new BlockItem(CONTROL_PANEL_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<ControlPanelBlockEntity>> CONTROL_PANEL_BE =
            BLOCK_ENTITIES.register("control_panel", () ->
                    BlockEntityType.Builder.of(ControlPanelBlockEntity::new, CONTROL_PANEL_BLOCK.get()).build(null));

    // Antenna
    public static final Supplier<AntennaControllerBlock> ANTENNA_CONTROLLER_BLOCK =
            BLOCKS.register("antenna_controller",
                    () -> new AntennaControllerBlock(Block.Properties.of()));

    public static final Supplier<Item> ANTENNA_CONTROLLER_ITEM =
            ITEMS.register("antenna_controller",
                    () -> new BlockItem(ANTENNA_CONTROLLER_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<AntennaControllerBlockEntity>> ANTENNA_CONTROLLER_BE =
            BLOCK_ENTITIES.register("antenna_controller", () ->
                    BlockEntityType.Builder.of(
                            AntennaControllerBlockEntity::new,
                            ANTENNA_CONTROLLER_BLOCK.get()
                    ).build(null));

    private static BaseFlowingFluid.Properties udmhProps() {
        return new BaseFlowingFluid.Properties(UDMH_TYPE, UDMH_SOURCE, UDMH_FLOWING)
                .bucket(UDMH_BUCKET).block(UDMH_BLOCK);
    }

    private static BaseFlowingFluid.Properties n2o4Props() {
        return new BaseFlowingFluid.Properties(N2O4_TYPE, N2O4_SOURCE, N2O4_FLOWING)
                .bucket(N2O4_BUCKET).block(N2O4_BLOCK);
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}