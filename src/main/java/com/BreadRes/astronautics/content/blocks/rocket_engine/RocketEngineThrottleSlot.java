package com.BreadRes.astronautics.content.blocks.rocket_engine;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RocketEngineThrottleSlot extends ValueBoxTransform.Sided {

    @Override
    protected Vec3 getSouthLocation() {
        return new Vec3(0.5, 0.5, 0.78);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = state.getValue(RocketEngineBlock.FACING);
        if (facing.getAxis() != Axis.Y && direction == Direction.DOWN) return false;
        return direction.getAxis() != facing.getAxis();
    }
}