/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DirtPathBlock
extends Block {
    public static final MapCodec<DirtPathBlock> CODEC = DirtPathBlock.simpleCodec(DirtPathBlock::new);
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 15.0);

    public MapCodec<DirtPathBlock> codec() {
        return CODEC;
    }

    protected DirtPathBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        if (!this.defaultBlockState().canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
            return Block.pushEntitiesUp(this.defaultBlockState(), Blocks.DIRT.defaultBlockState(), blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == Direction.UP && !blockState.canSurvive(levelReader, blockPos)) {
            scheduledTickAccess.scheduleTick(blockPos, this, 1);
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        FarmBlock.turnToDirt(null, blockState, serverLevel, blockPos);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.above());
        return !blockState2.isSolid() || blockState2.getBlock() instanceof FenceGateBlock;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

