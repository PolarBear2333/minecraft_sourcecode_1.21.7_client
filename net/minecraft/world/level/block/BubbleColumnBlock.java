/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BubbleColumnBlock
extends Block
implements BucketPickup {
    public static final MapCodec<BubbleColumnBlock> CODEC = BubbleColumnBlock.simpleCodec(BubbleColumnBlock::new);
    public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;
    private static final int CHECK_PERIOD = 5;

    public MapCodec<BubbleColumnBlock> codec() {
        return CODEC;
    }

    public BubbleColumnBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(DRAG_DOWN, true));
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
        boolean bl;
        BlockState blockState2 = level.getBlockState(blockPos.above());
        boolean bl2 = bl = blockState2.getCollisionShape(level, blockPos).isEmpty() && blockState2.getFluidState().isEmpty();
        if (bl) {
            entity.onAboveBubbleColumn(blockState.getValue(DRAG_DOWN), blockPos);
        } else {
            entity.onInsideBubbleColumn(blockState.getValue(DRAG_DOWN));
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BubbleColumnBlock.updateColumn(serverLevel, blockPos, blockState, serverLevel.getBlockState(blockPos.below()));
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        return Fluids.WATER.getSource(false);
    }

    public static void updateColumn(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        BubbleColumnBlock.updateColumn(levelAccessor, blockPos, levelAccessor.getBlockState(blockPos), blockState);
    }

    public static void updateColumn(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        if (!BubbleColumnBlock.canExistIn(blockState)) {
            return;
        }
        BlockState blockState3 = BubbleColumnBlock.getColumnState(blockState2);
        levelAccessor.setBlock(blockPos, blockState3, 2);
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.UP);
        while (BubbleColumnBlock.canExistIn(levelAccessor.getBlockState(mutableBlockPos))) {
            if (!levelAccessor.setBlock(mutableBlockPos, blockState3, 2)) {
                return;
            }
            mutableBlockPos.move(Direction.UP);
        }
    }

    private static boolean canExistIn(BlockState blockState) {
        return blockState.is(Blocks.BUBBLE_COLUMN) || blockState.is(Blocks.WATER) && blockState.getFluidState().getAmount() >= 8 && blockState.getFluidState().isSource();
    }

    private static BlockState getColumnState(BlockState blockState) {
        if (blockState.is(Blocks.BUBBLE_COLUMN)) {
            return blockState;
        }
        if (blockState.is(Blocks.SOUL_SAND)) {
            return (BlockState)Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, false);
        }
        if (blockState.is(Blocks.MAGMA_BLOCK)) {
            return (BlockState)Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, true);
        }
        return Blocks.WATER.defaultBlockState();
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        double d = blockPos.getX();
        double d2 = blockPos.getY();
        double d3 = blockPos.getZ();
        if (blockState.getValue(DRAG_DOWN).booleanValue()) {
            level.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, d + 0.5, d2 + 0.8, d3, 0.0, 0.0, 0.0);
            if (randomSource.nextInt(200) == 0) {
                level.playLocalSound(d, d2, d3, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 0.2f + randomSource.nextFloat() * 0.2f, 0.9f + randomSource.nextFloat() * 0.15f, false);
            }
        } else {
            level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d + 0.5, d2, d3 + 0.5, 0.0, 0.04, 0.0);
            level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d + (double)randomSource.nextFloat(), d2 + (double)randomSource.nextFloat(), d3 + (double)randomSource.nextFloat(), 0.0, 0.04, 0.0);
            if (randomSource.nextInt(200) == 0) {
                level.playLocalSound(d, d2, d3, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.2f + randomSource.nextFloat() * 0.2f, 0.9f + randomSource.nextFloat() * 0.15f, false);
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        if (!blockState.canSurvive(levelReader, blockPos) || direction == Direction.DOWN || direction == Direction.UP && !blockState2.is(Blocks.BUBBLE_COLUMN) && BubbleColumnBlock.canExistIn(blockState2)) {
            scheduledTickAccess.scheduleTick(blockPos, this, 5);
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.below());
        return blockState2.is(Blocks.BUBBLE_COLUMN) || blockState2.is(Blocks.MAGMA_BLOCK) || blockState2.is(Blocks.SOUL_SAND);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DRAG_DOWN);
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity livingEntity, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }
}

