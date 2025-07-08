/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SeaPickleBlock
extends VegetationBlock
implements BonemealableBlock,
SimpleWaterloggedBlock {
    public static final MapCodec<SeaPickleBlock> CODEC = SeaPickleBlock.simpleCodec(SeaPickleBlock::new);
    public static final int MAX_PICKLES = 4;
    public static final IntegerProperty PICKLES = BlockStateProperties.PICKLES;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_ONE = Block.column(4.0, 0.0, 6.0);
    private static final VoxelShape SHAPE_TWO = Block.column(10.0, 0.0, 6.0);
    private static final VoxelShape SHAPE_THREE = Block.column(12.0, 0.0, 6.0);
    private static final VoxelShape SHAPE_FOUR = Block.column(12.0, 0.0, 7.0);

    public MapCodec<SeaPickleBlock> codec() {
        return CODEC;
    }

    protected SeaPickleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PICKLES, 1)).setValue(WATERLOGGED, true));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(this)) {
            return (BlockState)blockState.setValue(PICKLES, Math.min(4, blockState.getValue(PICKLES) + 1));
        }
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = fluidState.getType() == Fluids.WATER;
        return (BlockState)super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, bl);
    }

    public static boolean isDead(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) == false;
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !blockState.getCollisionShape(blockGetter, blockPos).getFaceShape(Direction.UP).isEmpty() || blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        return this.mayPlaceOn(levelReader.getBlockState(blockPos2), levelReader, blockPos2);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (!blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        if (!blockPlaceContext.isSecondaryUseActive() && blockPlaceContext.getItemInHand().is(this.asItem()) && blockState.getValue(PICKLES) < 4) {
            return true;
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return switch (blockState.getValue(PICKLES)) {
            default -> SHAPE_ONE;
            case 2 -> SHAPE_TWO;
            case 3 -> SHAPE_THREE;
            case 4 -> SHAPE_FOUR;
        };
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PICKLES, WATERLOGGED);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return !SeaPickleBlock.isDead(blockState) && levelReader.getBlockState(blockPos.below()).is(BlockTags.CORAL_BLOCKS);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        int n = 5;
        int n2 = 1;
        int n3 = 2;
        int n4 = 0;
        int n5 = blockPos.getX() - 2;
        int n6 = 0;
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < n2; ++j) {
                int n7 = 2 + blockPos.getY() - 1;
                for (int k = n7 - 2; k < n7; ++k) {
                    BlockState blockState2;
                    BlockPos blockPos2 = new BlockPos(n5 + i, k, blockPos.getZ() - n6 + j);
                    if (blockPos2 == blockPos || randomSource.nextInt(6) != 0 || !serverLevel.getBlockState(blockPos2).is(Blocks.WATER) || !(blockState2 = serverLevel.getBlockState(blockPos2.below())).is(BlockTags.CORAL_BLOCKS)) continue;
                    serverLevel.setBlock(blockPos2, (BlockState)Blocks.SEA_PICKLE.defaultBlockState().setValue(PICKLES, randomSource.nextInt(4) + 1), 3);
                }
            }
            if (n4 < 2) {
                n2 += 2;
                ++n6;
            } else {
                n2 -= 2;
                --n6;
            }
            ++n4;
        }
        serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(PICKLES, 4), 2);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

