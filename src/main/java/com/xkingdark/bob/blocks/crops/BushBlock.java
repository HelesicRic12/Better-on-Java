package com.xkingdark.bob.blocks.crops;

import com.mojang.serialization.MapCodec;
import com.xkingdark.bob.entities.DamageTypes;
import com.xkingdark.bob.items.Items;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

import java.util.function.Function;

public class BushBlock extends PlantBlock implements Fertilizable {
    public static final int MAX_AGE = Properties.AGE_3_MAX;
    public static final IntProperty AGE = Properties.AGE_3;
    private static final VoxelShape SMALL_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape LARGE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public BushBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
    }

    public Item getDrop() {
        return net.minecraft.item.Items.AIR;
    }

    public boolean shouldDamage() {
        return false;
    }

    public RegistryKey<DamageType> damageType() {
        return net.minecraft.entity.damage.DamageTypes.GENERIC;
    }

    @Override
    public MapCodec<BushBlock> getCodec() {
        return createCodec(BushBlock::new);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this.getDrop());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(AGE) == 0)
            return SMALL_SHAPE;

        return state.get(AGE) < 3 ? LARGE_SHAPE : super.getOutlineShape(state, world, pos, context);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.get(AGE) < 3;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int i = state.get(AGE);
        if (i < MAX_AGE && random.nextInt(5) == 0 && world.getBaseLightLevel(pos.up(), 0) >= 9) {
            BlockState blockState = state.with(AGE, i + 1);
            world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        if (!(entity instanceof LivingEntity))
            return;

        if (entity.getType() == EntityType.FOX && entity.getType() == EntityType.BEE)
            return;

        entity.slowMovement(state, new Vec3d(0.8F, 0.75, 0.8F));
        if (!this.shouldDamage())
            return;

        if (world instanceof ServerWorld serverWorld && state.get(AGE) != 0) {
            Vec3d vec3d = entity.isControlledByPlayer() ? entity.getMovement() : entity.getLastRenderPos().subtract(entity.getEntityPos());
            if (vec3d.horizontalLengthSquared() > 0.0) {
                double d = Math.abs(vec3d.getX());
                double e = Math.abs(vec3d.getZ());
                if (d >= 0.003F || e >= 0.003F) {
                    entity.damage(serverWorld, DamageTypes.getSource(world, this.damageType()), 1.0F);
                }
            }
        }
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int i = state.get(AGE);
        Item item = net.minecraft.item.Items.BONE_MEAL;
        if (i != MAX_AGE && stack.isOf(item))
            return ActionResult.PASS;

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        int i = state.get(AGE);
        if (i < MAX_AGE - 1)
            return super.onUse(state, world, pos, player, hit);

        int j = 1 + world.random.nextInt(2);
        dropStack(world, pos, new ItemStack(this.getDrop(), j + (i == MAX_AGE ? 1 : 0)));
        world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);

        BlockState blockState = state.with(AGE, 1);
        world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, blockState));
        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return state.get(AGE) < 3;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockState newState = state.with(AGE, Math.min(3, state.get(AGE) + 1));
        world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
    }
}