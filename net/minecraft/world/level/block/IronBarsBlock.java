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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IronBarsBlock
extends CrossCollisionBlock {
    public static final MapCodec<IronBarsBlock> CODEC = IronBarsBlock.simpleCodec(IronBarsBlock::new);

    public MapCodec<? extends IronBarsBlock> codec() {
        return CODEC;
    }

    protected IronBarsBlock(BlockBehaviour.Properties properties) {
        super(2.0f, 16.0f, 2.0f, 16.0f, 16.0f, properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.south();
        BlockPos blockPos4 = blockPos.west();
        BlockPos blockPos5 = blockPos.east();
        BlockState blockState = level.getBlockState(blockPos2);
        BlockState blockState2 = level.getBlockState(blockPos3);
        BlockState blockState3 = level.getBlockState(blockPos4);
        BlockState blockState4 = level.getBlockState(blockPos5);
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(NORTH, this.attachsTo(blockState, blockState.isFaceSturdy(level, blockPos2, Direction.SOUTH)))).setValue(SOUTH, this.attachsTo(blockState2, blockState2.isFaceSturdy(level, blockPos3, Direction.NORTH)))).setValue(WEST, this.attachsTo(blockState3, blockState3.isFaceSturdy(level, blockPos4, Direction.EAST)))).setValue(EAST, this.attachsTo(blockState4, blockState4.isFaceSturdy(level, blockPos5, Direction.WEST)))).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), this.attachsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, direction.getOpposite())));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    protected boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        if (blockState2.is(this)) {
            if (!direction.getAxis().isHorizontal()) {
                return true;
            }
            if (((Boolean)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).booleanValue() && ((Boolean)blockState2.getValue((Property)PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).booleanValue()) {
                return true;
            }
        }
        return super.skipRendering(blockState, blockState2, direction);
    }

    public final boolean attachsTo(BlockState blockState, boolean bl) {
        return !IronBarsBlock.isExceptionForConnection(blockState) && bl || blockState.getBlock() instanceof IronBarsBlock || blockState.is(BlockTags.WALLS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}

