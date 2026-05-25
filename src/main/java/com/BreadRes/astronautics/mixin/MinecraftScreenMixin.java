package com.BreadRes.astronautics.mixin;

import com.BreadRes.astronautics.IP.ClientWorldLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftScreenMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void astronautics$replaceLoadingScreen(Screen screen, CallbackInfo ci) {
        if (!ClientWorldLoader.getIsInitialized()) return;
        if (screen instanceof ReceivingLevelScreen || screen instanceof LevelLoadingScreen) {
            ci.cancel();
        }
    }
}