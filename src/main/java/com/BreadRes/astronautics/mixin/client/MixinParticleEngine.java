package com.BreadRes.astronautics.mixin.client;

import com.BreadRes.astronautics.ducks.IEParticleManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine implements IEParticleManager {

    @Mutable
    @Shadow
    protected ClientLevel level;

    @Override
    public void astro_setLevel(ClientLevel level) {
        this.level = level;
    }
}