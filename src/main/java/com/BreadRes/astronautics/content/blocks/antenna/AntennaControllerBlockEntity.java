package com.BreadRes.astronautics.content.blocks.antenna;

import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AntennaControllerBlockEntity extends BlockEntity {

    private boolean formed = false;

    public AntennaControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AstronauticsRegistry.ANTENNA_CONTROLLER_BE.get(), pos, state);
    }

    public boolean isFormed() {
        return true;
    }

    public void setFormed(boolean formed) {
        this.formed = formed;

        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition,
                getBlockState().setValue(AntennaControllerBlock.FORMED, formed),
                3);
        }

        setChanged();
    }
}