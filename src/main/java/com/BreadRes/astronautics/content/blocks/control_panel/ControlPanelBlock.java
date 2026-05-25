package com.BreadRes.astronautics.content.blocks.control_panel;

import com.BreadRes.astronautics.content.blocks.control_panel.screen.ControlPanelScreen;
import com.BreadRes.astronautics.registry.AstronauticsRegistry;
import com.mojang.serialization.MapCodec;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class ControlPanelBlock extends BaseEntityBlock implements BlockSubLevelAssemblyListener {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    public ControlPanelBlock() {
        super(Properties.of().mapColor(MapColor.METAL).strength(3.5f).noOcclusion());
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
        calculateShapes();
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(p -> new ControlPanelBlock());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPES.get(Direction.NORTH));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            return InteractionResult.CONSUME;
        }

        if (level.getBlockEntity(pos) instanceof ControlPanelBlockEntity be) {

            double localX = hit.getLocation().x - pos.getX();
            double localY = hit.getLocation().y - pos.getY();
            double localZ = hit.getLocation().z - pos.getZ();

            Direction facing = state.getValue(FACING);

            double u = switch (facing) {
                case NORTH -> 1 - localX;
                case SOUTH -> localX;
                case WEST  -> localZ;
                case EAST  -> 1 - localZ;
                default -> localX;
            };

            int screen;

            if (u < 0.2) screen = 0;
            else if (u < 0.4) screen = 1;
            else if (u < 0.6) screen = 2;
            else if (u < 0.8) screen = 3;
            else screen = 4;

            be.forceScan();

            Minecraft.getInstance().setScreen(new ControlPanelScreen(be, screen));
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControlPanelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        return createTickerHelper(t, AstronauticsRegistry.CONTROL_PANEL_BE.get(),
                ControlPanelBlockEntity::tick);
    }

    @Override
    public void beforeMove(ServerLevel originLevel, ServerLevel resultingLevel,
                           BlockState state, BlockPos oldPos, BlockPos newPos) {}

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel,
                          BlockState state, BlockPos oldPos, BlockPos newPos) {}

    private void calculateShapes() {
        VoxelShape base = Shapes.empty();

        base = Shapes.or(base, Block.box(4.5, 2, 4, 11.5, 3, 13));
        base = Shapes.or(base, Block.box(11.5, 2, 5, 12.5, 3, 12));
        base = Shapes.or(base, Block.box(3.5, 2, 5, 4.5, 3, 12));
        base = Shapes.or(base, Block.box(2, 0, 0, 14, 2, 16));
        base = Shapes.or(base, Block.box(14, 0, 1, 15, 2, 15));
        base = Shapes.or(base, Block.box(1, 0, 1, 2, 2, 15));
        base = Shapes.or(base, Block.box(6, 2, 9.5, 10, 15, 10.5));
        base = Shapes.or(base, Block.box(5, 2, 5.5, 11, 15, 9.5));
        base = Shapes.or(base, Block.box(6.5, 15, 9, 9.5, 20, 10));
        base = Shapes.or(base, Block.box(6, 15, 6, 10, 21, 9));
        base = Shapes.or(base, Block.box(3, 11.25, 8, 7, 13.25, 9));
        base = Shapes.or(base, Block.box(9, 11.25, 8, 13, 13.25, 9));
        base = Shapes.or(base, Block.box(6, 21, 6, 10, 27, 8));
        base = Shapes.or(base, Block.box(6, 27, 6.5, 10, 28, 7.5));
        base = Shapes.or(base, Block.box(6, 28, 7, 10, 29, 8));
        base = Shapes.or(base, Block.box(6, 2, 5, 10, 14, 6));
        base = Shapes.or(base, Block.box(7, 14, 5, 9, 15, 6));
        base = Shapes.or(base, Block.box(7, 15, 5.5, 9, 26, 6.5));
        base = Shapes.or(base, Block.box(9, 14.25, 7, 11, 27.25, 9));
        base = Shapes.or(base, Block.box(5, 14.25, 7, 7, 27.25, 9));
        base = Shapes.or(base, Block.box(15, 18.25, 7.75, 17, 21.25, 9.75));
        base = Shapes.or(base, Block.box(-1, 18.25, 7.75, 1, 21.25, 9.75));
        base = Shapes.or(base, Block.box(1, 6.25, 8.25, 7, 11.25, 9.25));
        base = Shapes.or(base, Block.box(9, 6.25, 8.25, 15, 11.25, 9.25));
        base = Shapes.or(base, Block.box(16, 21, 8.25, 28, 30, 9.25));
        base = Shapes.or(base, Block.box(0, 21, 7.25, 16, 31, 9.25));
        base = Shapes.or(base, Block.box(-12, 21, 8.25, 0, 30, 9.25));
        base = Shapes.or(base, Block.box(12.75, 10.5, 8.25, 23.75, 19.5, 9.25));
        base = Shapes.or(base, Block.box(-7.75, 10.5, 8.25, 3.25, 19.5, 9.25));

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            SHAPES.put(dir, rotateShape(base, dir));
        }
    }

    private static VoxelShape rotateShape(VoxelShape shape, Direction to) {
        VoxelShape[] buffer = {shape, Shapes.empty()};
        int rotations = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        for (int i = 0; i < rotations; i++) {
            buffer[1] = Shapes.empty();
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Block.box(
                            16 * (1 - maxZ), 16 * minY, 16 * minX,
                            16 * (1 - minZ), 16 * maxY, 16 * maxX))
            );
            buffer[0] = buffer[1];
        }
        return buffer[0];
    }
}