package com.xkingdark.bob.blocks;

import com.xkingdark.bob.network.packets.WaystoneOpenPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.block.*;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class WaystoneBlock extends Block implements Waterloggable {
    private static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    private static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public WaystoneBlock(AbstractBlock.Settings settings) {
        super(settings);

        BlockState state = this.stateManager.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(HALF, DoubleBlockHalf.LOWER)
            .with(WATERLOGGED, false);
        this.setDefaultState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, WATERLOGGED);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        FluidState fluidState = world.getFluidState(pos.up());
        BlockState blockState = state
            .with(HALF, DoubleBlockHalf.UPPER)
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        world.setBlockState(pos.up(), blockState);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighborPos,
        BlockState neighborState,
        Random random
    ) {
        DoubleBlockHalf half = state.get(HALF);
        if (direction.getAxis() != Direction.Axis.Y
            || half == DoubleBlockHalf.LOWER != (direction == Direction.UP)
            || (neighborState.isOf(this) && neighborState.get(HALF) != half)
        ) {
            return half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }

        return Blocks.AIR.getDefaultState();
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            if (player.isCreative()) {
                onBreakInCreative(world, pos, state, player);
            } else {
                dropStacks(state, world, pos, null, player, player.getMainHandStack());
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    protected static void onBreakInCreative(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        DoubleBlockHalf half = state.get(HALF);
        if (half != DoubleBlockHalf.UPPER)
            return;

        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.isOf(state.getBlock()) && blockState.get(HALF) == DoubleBlockHalf.LOWER) {
            BlockState blockState2 = blockState.getFluidState()
                .isOf(Fluids.WATER) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();

            world.setBlockState(blockPos, blockState2, Block.NOTIFY_ALL | Block.SKIP_DROPS);
            world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, blockPos, Block.getRawIdFromState(blockState));
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState floor = world.getBlockState(pos.down());
        //return !world.isAir(pos.down())
        return floor.isOpaqueFullCube();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            ServerPlayNetworking.send((ServerPlayerEntity) player, new WaystoneOpenPacket(true, pos));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        DoubleBlockHalf doubleBlockHalf = state.get(HALF);
        double maxY = doubleBlockHalf == DoubleBlockHalf.LOWER ? 16 : 14;
        return Block.createCuboidShape(1f, 0f, 1f, 15f, maxY, 15f);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos blockPos = context.getBlockPos();
        BlockPos up = blockPos.up();
        World world = context.getWorld();
        if (up.getY() >= world.getTopYInclusive())
            return null;

        FluidState fluidState = world.getFluidState(blockPos);
        if (world.getBlockState(up).canReplace(context)) {
            return this.getDefaultState()
                .with(FACING, context.getHorizontalPlayerFacing().getOpposite())
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        }

        return null;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }
}