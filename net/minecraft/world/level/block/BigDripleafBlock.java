/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BigDripleafStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafBlock
extends HorizontalDirectionalBlock
implements BonemealableBlock,
SimpleWaterloggedBlock {
    public static final MapCodec<BigDripleafBlock> CODEC = BigDripleafBlock.simpleCodec(BigDripleafBlock::new);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final EnumProperty<Tilt> TILT = BlockStateProperties.TILT;
    private static final int NO_TICK = -1;
    private static final Object2IntMap<Tilt> DELAY_UNTIL_NEXT_TILT_STATE = (Object2IntMap)Util.make(new Object2IntArrayMap(), object2IntArrayMap -> {
        object2IntArrayMap.defaultReturnValue(-1);
        object2IntArrayMap.put((Object)Tilt.UNSTABLE, 10);
        object2IntArrayMap.put((Object)Tilt.PARTIAL, 10);
        object2IntArrayMap.put((Object)Tilt.FULL, 100);
    });
    private static final int MAX_GEN_HEIGHT = 5;
    private static final int ENTITY_DETECTION_MIN_Y = 11;
    private static final int LOWEST_LEAF_TOP = 13;
    private static final Map<Tilt, VoxelShape> SHAPE_LEAF = Maps.newEnumMap(Map.of(Tilt.NONE, Block.column(16.0, 11.0, 15.0), Tilt.UNSTABLE, Block.column(16.0, 11.0, 15.0), Tilt.PARTIAL, Block.column(16.0, 11.0, 13.0), Tilt.FULL, Shapes.empty()));
    private final Function<BlockState, VoxelShape> shapes;

    public MapCodec<BigDripleafBlock> codec() {
        return CODEC;
    }

    protected BigDripleafBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, false)).setValue(FACING, Direction.NORTH)).setValue(TILT, Tilt.NONE));
        this.shapes = this.makeShapes();
    }

    private Function<BlockState, VoxelShape> makeShapes() {
        Map<Direction, VoxelShape> map = Shapes.rotateHorizontal(Block.column(6.0, 0.0, 13.0).move(0.0, 0.0, 0.25).optimize());
        return this.getShapeForEachState(blockState -> Shapes.or(SHAPE_LEAF.get(blockState.getValue(TILT)), (VoxelShape)map.get(blockState.getValue(FACING))), WATERLOGGED);
    }

    public static void placeWithRandomHeight(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, Direction direction) {
        int n;
        int n2 = Mth.nextInt(randomSource, 2, 5);
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (n = 0; n < n2 && BigDripleafBlock.canPlaceAt(levelAccessor, mutableBlockPos, levelAccessor.getBlockState(mutableBlockPos)); ++n) {
            mutableBlockPos.move(Direction.UP);
        }
        int n3 = blockPos.getY() + n - 1;
        mutableBlockPos.setY(blockPos.getY());
        while (mutableBlockPos.getY() < n3) {
            BigDripleafStemBlock.place(levelAccessor, mutableBlockPos, levelAccessor.getFluidState(mutableBlockPos), direction);
            mutableBlockPos.move(Direction.UP);
        }
        BigDripleafBlock.place(levelAccessor, mutableBlockPos, levelAccessor.getFluidState(mutableBlockPos), direction);
    }

    private static boolean canReplace(BlockState blockState) {
        return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.SMALL_DRIPLEAF);
    }

    protected static boolean canPlaceAt(LevelHeightAccessor levelHeightAccessor, BlockPos blockPos, BlockState blockState) {
        return !levelHeightAccessor.isOutsideBuildHeight(blockPos) && BigDripleafBlock.canReplace(blockState);
    }

    protected static boolean place(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState, Direction direction) {
        BlockState blockState = (BlockState)((BlockState)Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(WATERLOGGED, fluidState.isSourceOfType(Fluids.WATER))).setValue(FACING, direction);
        return levelAccessor.setBlock(blockPos, blockState, 3);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        this.setTiltAndScheduleTick(blockState, level, blockHitResult.getBlockPos(), Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        return blockState2.is(this) || blockState2.is(Blocks.BIG_DRIPLEAF_STEM) || blockState2.is(BlockTags.BIG_DRIPLEAF_PLACEABLE);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if (direction == Direction.UP && blockState2.is(this)) {
            return Blocks.BIG_DRIPLEAF_STEM.withPropertiesOf(blockState);
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.above());
        return BigDripleafBlock.canReplace(blockState2);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockState blockState2;
        BlockPos blockPos2 = blockPos.above();
        if (BigDripleafBlock.canPlaceAt(serverLevel, blockPos2, blockState2 = serverLevel.getBlockState(blockPos2))) {
            Direction direction = (Direction)blockState.getValue(FACING);
            BigDripleafStemBlock.place(serverLevel, blockPos, blockState.getFluidState(), direction);
            BigDripleafBlock.place(serverLevel, blockPos2, blockState2.getFluidState(), direction);
        }
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
        if (level.isClientSide) {
            return;
        }
        if (blockState.getValue(TILT) == Tilt.NONE && BigDripleafBlock.canEntityTilt(blockPos, entity) && !level.hasNeighborSignal(blockPos)) {
            this.setTiltAndScheduleTick(blockState, level, blockPos, Tilt.UNSTABLE, null);
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (serverLevel.hasNeighborSignal(blockPos)) {
            BigDripleafBlock.resetTilt(blockState, serverLevel, blockPos);
            return;
        }
        Tilt tilt = blockState.getValue(TILT);
        if (tilt == Tilt.UNSTABLE) {
            this.setTiltAndScheduleTick(blockState, serverLevel, blockPos, Tilt.PARTIAL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
        } else if (tilt == Tilt.PARTIAL) {
            this.setTiltAndScheduleTick(blockState, serverLevel, blockPos, Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
        } else if (tilt == Tilt.FULL) {
            BigDripleafBlock.resetTilt(blockState, serverLevel, blockPos);
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (level.hasNeighborSignal(blockPos)) {
            BigDripleafBlock.resetTilt(blockState, level, blockPos);
        }
    }

    private static void playTiltSound(Level level, BlockPos blockPos, SoundEvent soundEvent) {
        float f = Mth.randomBetween(level.random, 0.8f, 1.2f);
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, f);
    }

    private static boolean canEntityTilt(BlockPos blockPos, Entity entity) {
        return entity.onGround() && entity.position().y > (double)((float)blockPos.getY() + 0.6875f);
    }

    private void setTiltAndScheduleTick(BlockState blockState, Level level, BlockPos blockPos, Tilt tilt, @Nullable SoundEvent soundEvent) {
        int n;
        BigDripleafBlock.setTilt(blockState, level, blockPos, tilt);
        if (soundEvent != null) {
            BigDripleafBlock.playTiltSound(level, blockPos, soundEvent);
        }
        if ((n = DELAY_UNTIL_NEXT_TILT_STATE.getInt((Object)tilt)) != -1) {
            level.scheduleTick(blockPos, this, n);
        }
    }

    private static void resetTilt(BlockState blockState, Level level, BlockPos blockPos) {
        BigDripleafBlock.setTilt(blockState, level, blockPos, Tilt.NONE);
        if (blockState.getValue(TILT) != Tilt.NONE) {
            BigDripleafBlock.playTiltSound(level, blockPos, SoundEvents.BIG_DRIPLEAF_TILT_UP);
        }
    }

    private static void setTilt(BlockState blockState, Level level, BlockPos blockPos, Tilt tilt) {
        Tilt tilt2 = blockState.getValue(TILT);
        level.setBlock(blockPos, (BlockState)blockState.setValue(TILT, tilt), 2);
        if (tilt.causesVibration() && tilt != tilt2) {
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos);
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_LEAF.get(blockState.getValue(TILT));
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapes.apply(blockState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().below());
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = blockState.is(Blocks.BIG_DRIPLEAF) || blockState.is(Blocks.BIG_DRIPLEAF_STEM);
        return (BlockState)((BlockState)this.defaultBlockState().setValue(WATERLOGGED, fluidState.isSourceOfType(Fluids.WATER))).setValue(FACING, bl ? (Direction)blockState.getValue(FACING) : blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, TILT);
    }
}

