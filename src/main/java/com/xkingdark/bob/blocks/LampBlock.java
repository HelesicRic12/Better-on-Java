package com.xkingdark.bob.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class LampBlock extends Block {
    public static final MapCodec<LampBlock> CODEC = createCodec(LampBlock::new);
    public static final BooleanProperty LIT = Properties.LIT;

    @Override
    public MapCodec<LampBlock> getCodec() {
        return CODEC;
    }

    public LampBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(LIT, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        if (!world.isClient()) {
            boolean isLit = state.get(LIT);
            if (isLit != world.isReceivingRedstonePower(pos)) {
                if (isLit) {
                    world.scheduleBlockTick(pos, this, 4);
                }
                else {
                    world.setBlockState(pos, state.cycle(LIT), Block.NOTIFY_LISTENERS);
                }
            }
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LIT) && !world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.cycle(LIT), Block.NOTIFY_LISTENERS);
        }
    }
}
