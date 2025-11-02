package com.xkingdark.bob.blocks.pedestal;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.util.*;

public class Pedestal extends BlockWithEntity {
    public Pedestal(Settings settings) {
        super(settings);
    };

    @Override
    public MapCodec<Pedestal> getCodec() {
        return createCodec(Pedestal::new);
    };

    @Override
    public PedestalBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    };

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(
            4.0, 0.0, 4.0,
            12.0, 16.0, 12.0
        );
    };

    @Override
    public ActionResult.Success onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient)
            return ActionResult.SUCCESS;

        if (!(world.getBlockEntity(pos) instanceof PedestalBlockEntity blockEntity))
            return ActionResult.SUCCESS;

        if (!player.getStackInHand(hand).isEmpty()) {
            if (!blockEntity.getStack(0).isEmpty()) {
                player.sendMessage(
                    Text.literal("The pedestal already has enough items.").formatted(Formatting.RED),
                    true
                );
                return ActionResult.SUCCESS;
            }

            // Copy the item stack from the player's hand into the pedestal's inventory
            blockEntity.setStack(0, player.getStackInHand(hand).copy());
            player.getStackInHand(hand).setCount(0);
        }
        else {
            if (blockEntity.getStack(0).isEmpty())
                return ActionResult.SUCCESS;

            player.getInventory().offerOrDrop(blockEntity.getStack(0));
            blockEntity.removeStack(0);
        };

        blockEntity.markDirty();
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
        super.onStateReplaced(state, world, pos, moved);
    };

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    };

    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    };
};