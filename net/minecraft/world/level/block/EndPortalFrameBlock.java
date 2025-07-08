/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicates
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.google.common.base.Predicates;
import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EndPortalFrameBlock
extends Block {
    public static final MapCodec<EndPortalFrameBlock> CODEC = EndPortalFrameBlock.simpleCodec(EndPortalFrameBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_EYE = BlockStateProperties.EYE;
    private static final VoxelShape SHAPE_EMPTY = Block.column(16.0, 0.0, 13.0);
    private static final VoxelShape SHAPE_FULL = Shapes.or(SHAPE_EMPTY, Block.column(8.0, 13.0, 16.0));
    private static BlockPattern portalShape;

    public MapCodec<EndPortalFrameBlock> codec() {
        return CODEC;
    }

    public EndPortalFrameBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(HAS_EYE, false));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return blockState.getValue(HAS_EYE) != false ? SHAPE_FULL : SHAPE_EMPTY;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())).setValue(HAS_EYE, false);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (blockState.getValue(HAS_EYE).booleanValue()) {
            return 15;
        }
        return 0;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_EYE);
    }

    public static BlockPattern getOrCreatePortalShape() {
        if (portalShape == null) {
            portalShape = BlockPatternBuilder.start().aisle("?vvv?", ">???<", ">???<", ">???<", "?^^^?").where('?', BlockInWorld.hasState(BlockStatePredicate.ANY)).where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(HAS_EYE, (Predicate<Object>)Predicates.equalTo((Object)true)).where(FACING, (Predicate<Object>)Predicates.equalTo((Object)Direction.SOUTH)))).where('>', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(HAS_EYE, (Predicate<Object>)Predicates.equalTo((Object)true)).where(FACING, (Predicate<Object>)Predicates.equalTo((Object)Direction.WEST)))).where('v', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(HAS_EYE, (Predicate<Object>)Predicates.equalTo((Object)true)).where(FACING, (Predicate<Object>)Predicates.equalTo((Object)Direction.NORTH)))).where('<', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(HAS_EYE, (Predicate<Object>)Predicates.equalTo((Object)true)).where(FACING, (Predicate<Object>)Predicates.equalTo((Object)Direction.EAST)))).build();
        }
        return portalShape;
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

