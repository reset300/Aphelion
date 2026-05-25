package com.BreadRes.astronautics.integration;

import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class AstronauticsTabInit {

    private static final ResourceLocation SECTION_ID =
            ResourceLocation.fromNamespaceAndPath("astronautics", "astronautics");

    public static void register() {

        add("rocket_engine");
        add("fuel_tank");
        add("divider");

        add("udmh_bucket");
        add("n2o4_bucket");
        add("control_panel");
    }

    private static void add(String path) {

        ResourceLocation itemId =
                ResourceLocation.fromNamespaceAndPath("astronautics", path);

        Supplier<Item> supplier =
                () -> BuiltInRegistries.ITEM.get(itemId);

        SimulatedRegistrate.TAB_ITEMS.add(supplier);

        SimulatedRegistrate.ITEM_TO_SECTION.put(itemId, SECTION_ID);
    }
}