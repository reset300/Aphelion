package com.BreadRes.astronautics.mixin.client;

import com.BreadRes.astronautics.ducks.IEClientPlayNetworkHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener implements IEClientPlayNetworkHandler {

    @Mutable
    @Shadow
    private ClientLevel level;

    @Override
    public void astro_setLevel(ClientLevel level) {
        this.level = level;
    }
}