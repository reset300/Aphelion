package com.BreadRes.astronautics.mixin.client;

import com.BreadRes.astronautics.content.blocks.control_panel.screen.ControlPanelScreen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class BlurMixin {

    @Inject(method = "processBlurEffect", at = @At("HEAD"), cancellable = true)
    private void cancelBlurForControlPanel(float partialTicks, CallbackInfo ci) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.screen instanceof ControlPanelScreen) {
            ci.cancel();
        }
    }
}