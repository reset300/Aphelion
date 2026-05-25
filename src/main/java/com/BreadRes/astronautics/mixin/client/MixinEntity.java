package com.BreadRes.astronautics.mixin.client;

import com.BreadRes.astronautics.ducks.IEEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class MixinEntity implements IEEntity {

    @Shadow
    private Level level;

    @Shadow
    private Entity.RemovalReason removalReason;

    @Shadow
    protected void unsetRemoved() {}

    @Override
    public void astro_setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void astro_unsetRemoved() {
        this.unsetRemoved();
    }
}