/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MossyCarpetBlock
extends Block
implements BonemealableBlock {
    public static final MapCodec<MossyCarpetBlock> CODEC = MossyCarpetBlock.simpleCodec(MossyCarpetBlock::new);
    public static final BooleanProperty BASE = BlockStateProperties.BOTTOM;
    public static final EnumProperty<WallSide> NORTH = BlockStateProperties.NORTH_WALL;
    public static final EnumProperty<WallSide> EAST = BlockStateProperties.EAST_WALL;
    public static final EnumProperty<WallSide> SOUTH = BlockStateProperties.SOUTH_WALL;
    public static final EnumProperty<WallSide> WEST = BlockStateProperties.WEST_WALL;
    public static final Map<Direction, EnumProperty<WallSide>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf((Map)Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST)));
    private final Function<BlockState, VoxelShape> shapes;

    public MapCodec<MossyCarpetBlock> codec() {
        return CODEC;
    }

    public MossyCarpetBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(BASE, true)).setValue(NORTH, WallSide.NONE)).setValue(EAST, WallSide.NONE)).setValue(SOUTH, WallSide.NONE)).setValue(WEST, WallSide.NONE));
        this.shapes = this.makeShapes();
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState blockState) {
        return Shapes.empty();
    }

    public Function<BlockState, VoxelShape> makeShapes() {
        Map<Direction, VoxelShape> map = Shapes.rotateHorizontal(Block.boxZ(16.0, 0.0, 10.0, 0.0, 1.0));
        Map<Direction, VoxelShape> map2 = Shapes.rotateAll(Block.boxZ(16.0, 0.0, 1.0));
        return this.getShapeForEachState(blockState -> {
            VoxelShape voxelShape = blockState.getValue(BASE) != false ? (VoxelShape)map2.get(Direction.DOWN) : Shapes.empty();
            for (Map.Entry<Direction, EnumProperty<WallSide>> entry : PROPERTY_BY_DIRECTION.entrySet()) {
                switch ((WallSide)blockState.getValue(entry.getValue())) {
                    case NONE: {
                        break;
                    }
                    case LOW: {
                        voxelShape = Shapes.or(voxelShape, (VoxelShape)map.get(entry.getKey()));
                        break;
                    }
                    case TALL: {
                        voxelShape = Shapes.or(voxelShape, (VoxelShape)map2.get(entry.getKey()));
                    }
                }
            }
            return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
        });
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapes.apply(blockState);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return blockState.getValue(BASE) != false ? this.shapes.apply(this.defaultBlockState()) : Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.below());
        if (blockState.getValue(BASE).booleanValue()) {
            return !blockState2.isAir();
        }
        return blockState2.is(this) && blockState2.getValue(BASE) != false;
    }

    private static boolean hasFaces(BlockState blockState) {
        if (blockState.getValue(BASE).booleanValue()) {
            return true;
        }
        for (EnumProperty<WallSide> enumProperty : PROPERTY_BY_DIRECTION.values()) {
            if (blockState.getValue(enumProperty) == WallSide.NONE) continue;
            return true;
        }
        return false;
    }

    private static boolean canSupportAtFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.UP) {
            return false;
        }
        return MultifaceBlock.canAttachTo(blockGetter, blockPos, direction);
    }

    private static BlockState getUpdatedState(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, boolean bl) {
        BlockBehaviour.BlockStateBase blockStateBase = null;
        BlockBehaviour.BlockStateBase blockStateBase2 = null;
        bl |= blockState.getValue(BASE).booleanValue();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            WallSide wallSide;
            EnumProperty<WallSide> enumProperty = MossyCarpetBlock.getPropertyForFace(direction);
            WallSide wallSide2 = MossyCarpetBlock.canSupportAtFace(blockGetter, blockPos, direction) ? (bl ? WallSide.LOW : blockState.getValue(enumProperty)) : (wallSide = WallSide.NONE);
            if (wallSide == WallSide.LOW) {
                if (blockStateBase == null) {
                    blockStateBase = blockGetter.getBlockState(blockPos.above());
                }
                if (blockStateBase.is(Blocks.PALE_MOSS_CARPET) && blockStateBase.getValue(enumProperty) != WallSide.NONE && !blockStateBase.getValue(BASE).booleanValue()) {
                    wallSide = WallSide.TALL;
                }
                if (!blockState.getValue(BASE).booleanValue()) {
                    if (blockStateBase2 == null) {
                        blockStateBase2 = blockGetter.getBlockState(blockPos.below());
                    }
                    if (blockStateBase2.is(Blocks.PALE_MOSS_CARPET) && blockStateBase2.getValue(enumProperty) == WallSide.NONE) {
                        wallSide = WallSide.NONE;
                    }
                }
            }
            blockState = (BlockState)blockState.setValue(enumProperty, wallSide);
        }
        return blockState;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return MossyCarpetBlock.getUpdatedState(this.defaultBlockState(), blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), true);
    }

    public static void placeAt(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, int n) {
        BlockState blockState = Blocks.PALE_MOSS_CARPET.defaultBlockState();
        BlockState blockState2 = MossyCarpetBlock.getUpdatedState(blockState, levelAccessor, blockPos, true);
        levelAccessor.setBlock(blockPos, blockState2, n);
        BlockState blockState3 = MossyCarpetBlock.createTopperWithSideChance(levelAccessor, blockPos, randomSource::nextBoolean);
        if (!blockState3.isAir()) {
            levelAccessor.setBlock(blockPos.above(), blockState3, n);
            BlockState blockState4 = MossyCarpetBlock.getUpdatedState(blockState2, levelAccessor, blockPos, true);
            levelAccessor.setBlock(blockPos, blockState4, n);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (level.isClientSide) {
            return;
        }
        RandomSource randomSource = level.getRandom();
        BlockState blockState2 = MossyCarpetBlock.createTopperWithSideChance(level, blockPos, randomSource::nextBoolean);
        if (!blockState2.isAir()) {
            level.setBlock(blockPos.above(), blockState2, 3);
        }
    }

    private static BlockState createTopperWithSideChance(BlockGetter blockGetter, BlockPos blockPos, BooleanSupplier booleanSupplier) {
        BlockPos blockPos2 = blockPos.above();
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        boolean bl = blockState.is(Blocks.PALE_MOSS_CARPET);
        if (bl && blockState.getValue(BASE).booleanValue() || !bl && !blockState.canBeReplaced()) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState blockState2 = (BlockState)Blocks.PALE_MOSS_CARPET.defaultBlockState().setValue(BASE, false);
        BlockState blockState3 = MossyCarpetBlock.getUpdatedState(blockState2, blockGetter, blockPos.above(), true);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            EnumProperty<WallSide> enumProperty = MossyCarpetBlock.getPropertyForFace(direction);
            if (blockState3.getValue(enumProperty) == WallSide.NONE || booleanSupplier.getAsBoolean()) continue;
            blockState3 = (BlockState)blockState3.setValue(enumProperty, WallSide.NONE);
        }
        if (MossyCarpetBlock.hasFaces(blockState3) && blockState3 != blockState) {
            return blockState3;
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (!blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState blockState3 = MossyCarpetBlock.getUpdatedState(blockState, levelReader, blockPos, false);
        if (!MossyCarpetBlock.hasFaces(blockState3)) {
            return Blocks.AIR.defaultBlockState();
        }
        return blockState3;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BASE, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return switch (rotation) {
            case Rotation.CLOCKWISE_180 -> (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(EAST, blockState.getValue(WEST))).setValue(SOUTH, blockState.getValue(NORTH))).setValue(WEST, blockState.getValue(EAST));
            case Rotation.COUNTERCLOCKWISE_90 -> (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(EAST))).setValue(EAST, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(NORTH));
            case Rotation.CLOCKWISE_90 -> (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(WEST))).setValue(EAST, blockState.getValue(NORTH))).setValue(SOUTH, blockState.getValue(EAST))).setValue(WEST, blockState.getValue(SOUTH));
            default -> blockState;
        };
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return switch (mirror) {
            case Mirror.LEFT_RIGHT -> (BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(NORTH));
            case Mirror.FRONT_BACK -> (BlockState)((BlockState)blockState.setValue(EAST, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(EAST));
            default -> super.mirror(blockState, mirror);
        };
    }

    @Nullable
    public static EnumProperty<WallSide> getPropertyForFace(Direction direction) {
        return PROPERTY_BY_DIRECTION.get(direction);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return blockState.getValue(BASE) != false && !MossyCarpetBlock.createTopperWithSideChance(levelReader, blockPos, () -> true).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockState blockState2 = MossyCarpetBlock.createTopperWithSideChance(serverLevel, blockPos, () -> true);
        if (!blockState2.isAir()) {
            serverLevel.setBlock(blockPos.above(), blockState2, 3);
        }
    }
}

