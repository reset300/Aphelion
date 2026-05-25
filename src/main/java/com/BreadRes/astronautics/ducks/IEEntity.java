package com.BreadRes.astronautics.ducks;

import net.minecraft.world.level.Level;

public interface IEEntity {
    void astro_setLevel(Level level);
    void astro_unsetRemoved();
}