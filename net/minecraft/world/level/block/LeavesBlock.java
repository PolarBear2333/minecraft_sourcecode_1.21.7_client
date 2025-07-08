/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class LeavesBlock
extends Block
implements SimpleWaterloggedBlock {
    public static final int DECAY_DISTANCE = 7;
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected final float leafParticleChance;
    private static final int TICK_DELAY = 1;

    public abstract MapCodec<? extends LeavesBlock> codec();

    public LeavesBlock(float f, BlockBehaviour.Properties properties) {
        super(properties);
        this.leafParticleChance = f;
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(DISTANCE, 7)).setValue(PERSISTENT, false)).setValue(WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(DISTANCE) == 7 && blockState.getValue(PERSISTENT) == false;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (this.decaying(blockState)) {
            LeavesBlock.dropResources(blockState, serverLevel, blockPos);
            serverLevel.removeBlock(blockPos, false);
        }
    }

    protected boolean decaying(BlockState blockState) {
        return blockState.getValue(PERSISTENT) == false && blockState.getValue(DISTANCE) == 7;
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        serverLevel.setBlock(blockPos, LeavesBlock.updateDistance(blockState, serverLevel, blockPos), 3);
    }

    @Override
    protected int getLightBlock(BlockState blockState) {
        return 1;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        int n;
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if ((n = LeavesBlock.getDistanceAt(blockState2) + 1) != 1 || blockState.getValue(DISTANCE) != n) {
            scheduledTickAccess.scheduleTick(blockPos, this, 1);
        }
        return blockState;
    }

    private static BlockState updateDistance(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        int n = 7;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            n = Math.min(n, LeavesBlock.getDistanceAt(levelAccessor.getBlockState(mutableBlockPos)) + 1);
            if (n == 1) break;
        }
        return (BlockState)blockState.setValue(DISTANCE, n);
    }

    private static int getDistanceAt(BlockState blockState) {
        return LeavesBlock.getOptionalDistanceAt(blockState).orElse(7);
    }

    public static OptionalInt getOptionalDistanceAt(BlockState blockState) {
        if (blockState.is(BlockTags.LOGS)) {
            return OptionalInt.of(0);
        }
        if (blockState.hasProperty(DISTANCE)) {
            return OptionalInt.of(blockState.getValue(DISTANCE));
        }
        return OptionalInt.empty();
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        super.animateTick(blockState, level, blockPos, randomSource);
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = level.getBlockState(blockPos2);
        LeavesBlock.makeDrippingWaterParticles(level, blockPos, randomSource, blockState2, blockPos2);
        this.makeFallingLeavesParticles(level, blockPos, randomSource, blockState2, blockPos2);
    }

    private static void makeDrippingWaterParticles(Level level, BlockPos blockPos, RandomSource randomSource, BlockState blockState, BlockPos blockPos2) {
        if (!level.isRainingAt(blockPos.above())) {
            return;
        }
        if (randomSource.nextInt(15) != 1) {
            return;
        }
        if (blockState.canOcclude() && blockState.isFaceSturdy(level, blockPos2, Direction.UP)) {
            return;
        }
        ParticleUtils.spawnParticleBelow(level, blockPos, randomSource, ParticleTypes.DRIPPING_WATER);
    }

    private void makeFallingLeavesParticles(Level level, BlockPos blockPos, RandomSource randomSource, BlockState blockState, BlockPos blockPos2) {
        if (randomSource.nextFloat() >= this.leafParticleChance) {
            return;
        }
        if (LeavesBlock.isFaceFull(blockState.getCollisionShape(level, blockPos2), Direction.UP)) {
            return;
        }
        this.spawnFallingLeavesParticle(level, blockPos, randomSource);
    }

    protected abstract void spawnFallingLeavesParticle(Level var1, BlockPos var2, RandomSource var3);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockState blockState = (BlockState)((BlockState)this.defaultBlockState().setValue(PERSISTENT, true)).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        return LeavesBlock.updateDistance(blockState, blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }
}

