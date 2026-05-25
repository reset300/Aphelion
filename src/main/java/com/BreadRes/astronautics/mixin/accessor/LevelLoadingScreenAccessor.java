package com.BreadRes.astronautics.mixin.accessor;

import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelLoadingScreen.class)
public interface LevelLoadingScreenAccessor {

    @Accessor("progressListener")
    StoringChunkProgressListener getProgressListener();

    @Accessor("done")
    boolean isDone();
}