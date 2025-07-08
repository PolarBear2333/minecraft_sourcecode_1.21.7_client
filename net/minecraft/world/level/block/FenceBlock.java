/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceBlock
extends CrossCollisionBlock {
    public static final MapCodec<FenceBlock> CODEC = FenceBlock.simpleCodec(FenceBlock::new);
    private final Function<BlockState, VoxelShape> occlusionShapes;

    public MapCodec<FenceBlock> codec() {
        return CODEC;
    }

    public FenceBlock(BlockBehaviour.Properties properties) {
        super(4.0f, 16.0f, 4.0f, 16.0f, 24.0f, properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
        this.occlusionShapes = this.makeShapes(4.0f, 16.0f, 2.0f, 6.0f, 15.0f);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState blockState) {
        return this.occlusionShapes.apply(blockState);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    public boolean connectsTo(BlockState blockState, boolean bl, Direction direction) {
        Block block = blockState.getBlock();
        boolean bl2 = this.isSameFence(blockState);
        boolean bl3 = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(blockState, direction);
        return !FenceBlock.isExceptionForConnection(blockState) && bl || bl2 || bl3;
    }

    private boolean isSameFence(BlockState blockState) {
        return blockState.is(BlockTags.FENCES) && blockState.is(BlockTags.WOODEN_FENCES) == this.defaultBlockState().is(BlockTags.WOODEN_FENCES);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        return !level.isClientSide() ? LeadItem.bindPlayerMobs(player, level, blockPos) : InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.east();
        BlockPos blockPos4 = blockPos.south();
        BlockPos blockPos5 = blockPos.west();
        BlockState blockState = level.getBlockState(blockPos2);
        BlockState blockState2 = level.getBlockState(blockPos3);
        BlockState blockState3 = level.getBlockState(blockPos4);
        BlockState blockState4 = level.getBlockState(blockPos5);
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)super.getStateForPlacement(blockPlaceContext).setValue(NORTH, this.connectsTo(blockState, blockState.isFaceSturdy(level, blockPos2, Direction.SOUTH), Direction.SOUTH))).setValue(EAST, this.connectsTo(blockState2, blockState2.isFaceSturdy(level, blockPos3, Direction.WEST), Direction.WEST))).setValue(SOUTH, this.connectsTo(blockState3, blockState3.isFaceSturdy(level, blockPos4, Direction.NORTH), Direction.NORTH))).setValue(WEST, this.connectsTo(blockState4, blockState4.isFaceSturdy(level, blockPos5, Direction.EAST), Direction.EAST))).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), this.connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, direction.getOpposite()), direction.getOpposite()));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}

