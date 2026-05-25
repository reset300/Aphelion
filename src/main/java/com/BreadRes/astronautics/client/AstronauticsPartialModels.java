package com.BreadRes.astronautics.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class AstronauticsPartialModels {
    public static void init() {}
    public static final PartialModel ANTENNA_DISH =
            PartialModel.of(ResourceLocation.fromNamespaceAndPath("astronautics", "block/antenna/antenna_dish"));
}
