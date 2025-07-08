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
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.DefaultRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.ExperimentalRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedStoneWireBlock
extends Block {
    public static final MapCodec<RedStoneWireBlock> CODEC = RedStoneWireBlock.simpleCodec(RedStoneWireBlock::new);
    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf((Map)Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST)));
    private static final int[] COLORS = Util.make(new int[16], nArray -> {
        for (int i = 0; i <= 15; ++i) {
            float f;
            float f2 = f * 0.6f + ((f = (float)i / 15.0f) > 0.0f ? 0.4f : 0.3f);
            float f3 = Mth.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float f4 = Mth.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            nArray[i] = ARGB.colorFromFloat(1.0f, f2, f3, f4);
        }
    });
    private static final float PARTICLE_DENSITY = 0.2f;
    private final Function<BlockState, VoxelShape> shapes;
    private final BlockState crossState;
    private final RedstoneWireEvaluator evaluator = new DefaultRedstoneWireEvaluator(this);
    private boolean shouldSignal = true;

    public MapCodec<RedStoneWireBlock> codec() {
        return CODEC;
    }

    public RedStoneWireBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, RedstoneSide.NONE)).setValue(EAST, RedstoneSide.NONE)).setValue(SOUTH, RedstoneSide.NONE)).setValue(WEST, RedstoneSide.NONE)).setValue(POWER, 0));
        this.shapes = this.makeShapes();
        this.crossState = (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE)).setValue(EAST, RedstoneSide.SIDE)).setValue(SOUTH, RedstoneSide.SIDE)).setValue(WEST, RedstoneSide.SIDE);
    }

    private Function<BlockState, VoxelShape> makeShapes() {
        boolean bl = true;
        int n = 10;
        VoxelShape voxelShape = Block.column(10.0, 0.0, 1.0);
        Map<Direction, VoxelShape> map = Shapes.rotateHorizontal(Block.boxZ(10.0, 0.0, 1.0, 0.0, 8.0));
        Map<Direction, VoxelShape> map2 = Shapes.rotateHorizontal(Block.boxZ(10.0, 16.0, 0.0, 1.0));
        return this.getShapeForEachState(blockState -> {
            VoxelShape voxelShape2 = voxelShape;
            for (Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : PROPERTY_BY_DIRECTION.entrySet()) {
                voxelShape2 = switch ((RedstoneSide)blockState.getValue(entry.getValue())) {
                    default -> throw new MatchException(null, null);
                    case RedstoneSide.UP -> Shapes.or(voxelShape2, (VoxelShape)map.get(entry.getKey()), (VoxelShape)map2.get(entry.getKey()));
                    case RedstoneSide.SIDE -> Shapes.or(voxelShape2, (VoxelShape)map.get(entry.getKey()));
                    case RedstoneSide.NONE -> voxelShape2;
                };
            }
            return voxelShape2;
        }, POWER);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapes.apply(blockState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.getConnectionState(blockPlaceContext.getLevel(), this.crossState, blockPlaceContext.getClickedPos());
    }

    private BlockState getConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        boolean bl;
        boolean bl2 = RedStoneWireBlock.isDot(blockState);
        blockState = this.getMissingConnections(blockGetter, (BlockState)this.defaultBlockState().setValue(POWER, blockState.getValue(POWER)), blockPos);
        if (bl2 && RedStoneWireBlock.isDot(blockState)) {
            return blockState;
        }
        boolean bl3 = blockState.getValue(NORTH).isConnected();
        boolean bl4 = blockState.getValue(SOUTH).isConnected();
        boolean bl5 = blockState.getValue(EAST).isConnected();
        boolean bl6 = blockState.getValue(WEST).isConnected();
        boolean bl7 = !bl3 && !bl4;
        boolean bl8 = bl = !bl5 && !bl6;
        if (!bl6 && bl7) {
            blockState = (BlockState)blockState.setValue(WEST, RedstoneSide.SIDE);
        }
        if (!bl5 && bl7) {
            blockState = (BlockState)blockState.setValue(EAST, RedstoneSide.SIDE);
        }
        if (!bl3 && bl) {
            blockState = (BlockState)blockState.setValue(NORTH, RedstoneSide.SIDE);
        }
        if (!bl4 && bl) {
            blockState = (BlockState)blockState.setValue(SOUTH, RedstoneSide.SIDE);
        }
        return blockState;
    }

    private BlockState getMissingConnections(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        boolean bl = !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (((RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected()) continue;
            RedstoneSide redstoneSide = this.getConnectingSide(blockGetter, blockPos, direction, bl);
            blockState = (BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneSide);
        }
        return blockState;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == Direction.DOWN) {
            if (!this.canSurviveOn(levelReader, blockPos2, blockState2)) {
                return Blocks.AIR.defaultBlockState();
            }
            return blockState;
        }
        if (direction == Direction.UP) {
            return this.getConnectionState(levelReader, blockState, blockPos);
        }
        RedstoneSide redstoneSide = this.getConnectingSide(levelReader, blockPos, direction);
        if (redstoneSide.isConnected() == ((RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected() && !RedStoneWireBlock.isCross(blockState)) {
            return (BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneSide);
        }
        return this.getConnectionState(levelReader, (BlockState)((BlockState)this.crossState.setValue(POWER, blockState.getValue(POWER))).setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneSide), blockPos);
    }

    private static boolean isCross(BlockState blockState) {
        return blockState.getValue(NORTH).isConnected() && blockState.getValue(SOUTH).isConnected() && blockState.getValue(EAST).isConnected() && blockState.getValue(WEST).isConnected();
    }

    private static boolean isDot(BlockState blockState) {
        return !blockState.getValue(NORTH).isConnected() && !blockState.getValue(SOUTH).isConnected() && !blockState.getValue(EAST).isConnected() && !blockState.getValue(WEST).isConnected();
    }

    @Override
    protected void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int n, int n2) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Object object;
            RedstoneSide redstoneSide = (RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            if (redstoneSide == RedstoneSide.NONE || levelAccessor.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos, direction)).is(this)) continue;
            mutableBlockPos.move(Direction.DOWN);
            BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
            if (blockState2.is(this)) {
                object = mutableBlockPos.relative(direction.getOpposite());
                levelAccessor.neighborShapeChanged(direction.getOpposite(), mutableBlockPos, (BlockPos)object, levelAccessor.getBlockState((BlockPos)object), n, n2);
            }
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction).move(Direction.UP);
            object = levelAccessor.getBlockState(mutableBlockPos);
            if (!((BlockBehaviour.BlockStateBase)object).is(this)) continue;
            Vec3i vec3i = mutableBlockPos.relative(direction.getOpposite());
            levelAccessor.neighborShapeChanged(direction.getOpposite(), mutableBlockPos, (BlockPos)vec3i, levelAccessor.getBlockState((BlockPos)vec3i), n, n2);
        }
    }

    private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return this.getConnectingSide(blockGetter, blockPos, direction, !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos));
    }

    private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction, boolean bl) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        if (bl) {
            boolean bl2;
            boolean bl3 = bl2 = blockState.getBlock() instanceof TrapDoorBlock || this.canSurviveOn(blockGetter, blockPos2, blockState);
            if (bl2 && RedStoneWireBlock.shouldConnectTo(blockGetter.getBlockState(blockPos2.above()))) {
                if (blockState.isFaceSturdy(blockGetter, blockPos2, direction.getOpposite())) {
                    return RedstoneSide.UP;
                }
                return RedstoneSide.SIDE;
            }
        }
        if (RedStoneWireBlock.shouldConnectTo(blockState, direction) || !blockState.isRedstoneConductor(blockGetter, blockPos2) && RedStoneWireBlock.shouldConnectTo(blockGetter.getBlockState(blockPos2.below()))) {
            return RedstoneSide.SIDE;
        }
        return RedstoneSide.NONE;
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        return this.canSurviveOn(levelReader, blockPos2, blockState2);
    }

    private boolean canSurviveOn(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) || blockState.is(Blocks.HOPPER);
    }

    private void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState, @Nullable Orientation orientation, boolean bl) {
        if (RedStoneWireBlock.useExperimentalEvaluator(level)) {
            new ExperimentalRedstoneWireEvaluator(this).updatePowerStrength(level, blockPos, blockState, orientation, bl);
        } else {
            this.evaluator.updatePowerStrength(level, blockPos, blockState, orientation, bl);
        }
    }

    public int getBlockSignal(Level level, BlockPos blockPos) {
        this.shouldSignal = false;
        int n = level.getBestNeighborSignal(blockPos);
        this.shouldSignal = true;
        return n;
    }

    private void checkCornerChangeAt(Level level, BlockPos blockPos) {
        if (!level.getBlockState(blockPos).is(this)) {
            return;
        }
        level.updateNeighborsAt(blockPos, this);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock()) || level.isClientSide) {
            return;
        }
        this.updatePowerStrength(level, blockPos, blockState, null, true);
        for (Direction direction : Direction.Plane.VERTICAL) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
        this.updateNeighborsOfNeighboringWires(level, blockPos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        if (bl) {
            return;
        }
        for (Direction direction : Direction.values()) {
            serverLevel.updateNeighborsAt(blockPos.relative(direction), this);
        }
        this.updatePowerStrength(serverLevel, blockPos, blockState, null, false);
        this.updateNeighborsOfNeighboringWires(serverLevel, blockPos);
    }

    private void updateNeighborsOfNeighboringWires(Level level, BlockPos blockPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            this.checkCornerChangeAt(level, blockPos.relative(direction));
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                this.checkCornerChangeAt(level, blockPos2.above());
                continue;
            }
            this.checkCornerChangeAt(level, blockPos2.below());
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        if (block == this && RedStoneWireBlock.useExperimentalEvaluator(level)) {
            return;
        }
        if (blockState.canSurvive(level, blockPos)) {
            this.updatePowerStrength(level, blockPos, blockState, orientation, false);
        } else {
            RedStoneWireBlock.dropResources(blockState, level, blockPos);
            level.removeBlock(blockPos, false);
        }
    }

    private static boolean useExperimentalEvaluator(Level level) {
        return level.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS);
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!this.shouldSignal) {
            return 0;
        }
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!this.shouldSignal || direction == Direction.DOWN) {
            return 0;
        }
        int n = blockState.getValue(POWER);
        if (n == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((RedstoneSide)this.getConnectionState(blockGetter, blockState, blockPos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()) {
            return n;
        }
        return 0;
    }

    protected static boolean shouldConnectTo(BlockState blockState) {
        return RedStoneWireBlock.shouldConnectTo(blockState, null);
    }

    protected static boolean shouldConnectTo(BlockState blockState, @Nullable Direction direction) {
        if (blockState.is(Blocks.REDSTONE_WIRE)) {
            return true;
        }
        if (blockState.is(Blocks.REPEATER)) {
            Direction direction2 = (Direction)blockState.getValue(RepeaterBlock.FACING);
            return direction2 == direction || direction2.getOpposite() == direction;
        }
        if (blockState.is(Blocks.OBSERVER)) {
            return direction == blockState.getValue(ObserverBlock.FACING);
        }
        return blockState.isSignalSource() && direction != null;
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return this.shouldSignal;
    }

    public static int getColorForPower(int n) {
        return COLORS[n];
    }

    private static void spawnParticlesAlongLine(Level level, RandomSource randomSource, BlockPos blockPos, int n, Direction direction, Direction direction2, float f, float f2) {
        float f3 = f2 - f;
        if (randomSource.nextFloat() >= 0.2f * f3) {
            return;
        }
        float f4 = 0.4375f;
        float f5 = f + f3 * randomSource.nextFloat();
        double d = 0.5 + (double)(0.4375f * (float)direction.getStepX()) + (double)(f5 * (float)direction2.getStepX());
        double d2 = 0.5 + (double)(0.4375f * (float)direction.getStepY()) + (double)(f5 * (float)direction2.getStepY());
        double d3 = 0.5 + (double)(0.4375f * (float)direction.getStepZ()) + (double)(f5 * (float)direction2.getStepZ());
        level.addParticle(new DustParticleOptions(n, 1.0f), (double)blockPos.getX() + d, (double)blockPos.getY() + d2, (double)blockPos.getZ() + d3, 0.0, 0.0, 0.0);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        int n = blockState.getValue(POWER);
        if (n == 0) {
            return;
        }
        block4: for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneSide = (RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            switch (redstoneSide) {
                case UP: {
                    RedStoneWireBlock.spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[n], direction, Direction.UP, -0.5f, 0.5f);
                }
                case SIDE: {
                    RedStoneWireBlock.spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[n], Direction.DOWN, direction, 0.0f, 0.5f);
                    continue block4;
                }
            }
            RedStoneWireBlock.spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[n], Direction.DOWN, direction, 0.0f, 0.3f);
        }
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(EAST, blockState.getValue(WEST))).setValue(SOUTH, blockState.getValue(NORTH))).setValue(WEST, blockState.getValue(EAST));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(EAST))).setValue(EAST, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(NORTH));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(WEST))).setValue(EAST, blockState.getValue(NORTH))).setValue(SOUTH, blockState.getValue(EAST))).setValue(WEST, blockState.getValue(SOUTH));
            }
        }
        return blockState;
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(NORTH));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)blockState.setValue(EAST, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(EAST));
            }
        }
        return super.mirror(blockState, mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, POWER);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        if (RedStoneWireBlock.isCross(blockState) || RedStoneWireBlock.isDot(blockState)) {
            BlockState blockState2 = RedStoneWireBlock.isCross(blockState) ? this.defaultBlockState() : this.crossState;
            blockState2 = (BlockState)blockState2.setValue(POWER, blockState.getValue(POWER));
            if ((blockState2 = this.getConnectionState(level, blockState2, blockPos)) != blockState) {
                level.setBlock(blockPos, blockState2, 3);
                this.updatesOnShapeChange(level, blockPos, blockState, blockState2);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private void updatesOnShapeChange(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, null, Direction.UP);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (((RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected() == ((RedstoneSide)blockState2.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected() || !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) continue;
            level.updateNeighborsAtExceptFromFacing(blockPos2, blockState2.getBlock(), direction.getOpposite(), ExperimentalRedstoneUtils.withFront(orientation, direction));
        }
    }
}

