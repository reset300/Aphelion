package com.BreadRes.astronautics.content.blocks.rocket_engine;

import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import com.mojang.serialization.MapCodec;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.ryanhcode.sable.mixinhelpers.sublevel_render.vanilla.VanillaSubLevelBlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.*;
import org.jetbrains.annotations.Nullable;

public class RocketEngineBlock extends BaseEntityBlock implements BlockSubLevelAssemblyListener {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape LOWER_SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 4, 14),
            Block.box(3, 4, 3, 13, 10, 13),
            Block.box(4, 10, 4, 12, 16, 12)
    );

    private static final VoxelShape UPPER_SHAPE = Shapes.or(
            Block.box(4, 0, 4, 12, 8, 12),
            Block.box(5, 8, 5, 11, 16, 11)
    );

    public RocketEngineBlock() {
        super(Properties.of().mapColor(MapColor.METAL).strength(3.5f));
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(ACTIVE, false));
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(p -> new RocketEngineBlock());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, HALF, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getClickedFace().getOpposite();
        BlockPos clicked = ctx.getClickedPos();
        Level level = ctx.getLevel();
        BlockPos lowerPos = clicked.relative(facing.getOpposite());

        if (!level.getBlockState(clicked).canBeReplaced(ctx)) return null;
        if (!level.getBlockState(lowerPos).canBeReplaced(ctx)) return null;

        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(HALF, DoubleBlockHalf.UPPER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        BlockPos lowerPos = pos.relative(facing.getOpposite());
        level.setBlock(lowerPos, state.setValue(HALF, DoubleBlockHalf.LOWER), 3);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) {
            Direction f = state.getValue(FACING);
            BlockPos other = state.getValue(HALF) == DoubleBlockHalf.UPPER
                    ? pos.relative(f.getOpposite())
                    : pos.relative(f);
            if (level.getBlockState(other).getBlock() == this)
                level.destroyBlock(other, false);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void beforeMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState state, BlockPos oldPos, BlockPos newPos) {
        Direction facing = state.getValue(FACING);
        DoubleBlockHalf half = state.getValue(HALF);

        BlockPos otherOld = half == DoubleBlockHalf.LOWER
                ? oldPos.relative(facing)
                : oldPos.relative(facing.getOpposite());

        BlockState otherState = originLevel.getBlockState(otherOld);
        if (otherState.getBlock() != this) return;

        BlockPos otherNew = half == DoubleBlockHalf.LOWER
                ? newPos.relative(facing)
                : newPos.relative(facing.getOpposite());

        if (resultingLevel.getBlockState(otherNew).getBlock() == this) return;

        originLevel.removeBlock(otherOld, false);
        resultingLevel.setBlock(otherNew, otherState, 3);
    }

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState state, BlockPos oldPos, BlockPos newPos) {
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter lvl, BlockPos pos, CollisionContext ctx) {
        Direction facing = state.getValue(FACING);
        VoxelShape base = state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : UPPER_SHAPE;
        boolean lower = state.getValue(HALF) == DoubleBlockHalf.LOWER;
        return switch (facing) {
            case UP    -> lower ? flipY(base) : base;
            case DOWN  -> lower ? base : flipY(base);
            case NORTH -> lower ? rotX_cw(base) : rotX_ccw(base);
            case SOUTH -> lower ? rotX_ccw(base) : rotX_cw(base);
            case EAST  -> rotate(facing, base);
            case WEST  -> rotate(facing, base);
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER
                ? RenderShape.INVISIBLE
                : RenderShape.MODEL;
    }

    private static VoxelShape rotate(Direction dir, VoxelShape s) {
        return switch (dir) {
            case UP    -> s;
            case DOWN  -> flipY(s);
            case NORTH -> rotX_ccw(s);
            case SOUTH -> rotX_cw(s);
            case EAST  -> rotZ_ccw(s);
            case WEST  -> rotZ_cw(s);
        };
    }

    private static VoxelShape rotX_cw(VoxelShape s) {
        VoxelShape[] b = {Shapes.empty()};
        s.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                b[0] = Shapes.or(b[0], Shapes.box(x1, z1, 1-y2, x2, z2, 1-y1)));
        return b[0];
    }

    private static VoxelShape rotX_ccw(VoxelShape s) {
        VoxelShape[] b = {Shapes.empty()};
        s.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                b[0] = Shapes.or(b[0], Shapes.box(x1, 1-z2, y1, x2, 1-z1, y2)));
        return b[0];
    }

    private static VoxelShape rotZ_cw(VoxelShape s) {
        VoxelShape[] b = {Shapes.empty()};
        s.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                b[0] = Shapes.or(b[0], Shapes.box(1-y2, x1, z1, 1-y1, x2, z2)));
        return b[0];
    }

    private static VoxelShape rotZ_ccw(VoxelShape s) {
        VoxelShape[] b = {Shapes.empty()};
        s.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                b[0] = Shapes.or(b[0], Shapes.box(y1, 1-x2, z1, y2, 1-x1, z2)));
        return b[0];
    }

    private static VoxelShape flipY(VoxelShape s) {
        VoxelShape[] b = {Shapes.empty()};
        s.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                b[0] = Shapes.or(b[0], Shapes.box(x1, 1-y2, z1, x2, 1-y1, z2)));
        return b[0];
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER
                ? new RocketEngineBlockEntity(pos, state)
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        return s.getValue(HALF) == DoubleBlockHalf.UPPER
                ? createTickerHelper(t, AstronauticsRegistry.ROCKET_ENGINE_BE.get(), RocketEngineBlockEntity::tick)
                : null;
    }
}