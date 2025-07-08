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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

public abstract class DiodeBlock
extends HorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 2.0);

    protected DiodeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected abstract MapCodec<? extends DiodeBlock> codec();

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        return this.canSurviveOn(levelReader, blockPos2, levelReader.getBlockState(blockPos2));
    }

    protected boolean canSurviveOn(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return blockState.isFaceSturdy(levelReader, blockPos, Direction.UP, SupportType.RIGID);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (this.isLocked(serverLevel, blockPos, blockState)) {
            return;
        }
        boolean bl = blockState.getValue(POWERED);
        boolean bl2 = this.shouldTurnOn(serverLevel, blockPos, blockState);
        if (bl && !bl2) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, false), 2);
        } else if (!bl) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, true), 2);
            if (!bl2) {
                serverLevel.scheduleTick(blockPos, this, this.getDelay(blockState), TickPriority.VERY_HIGH);
            }
        }
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!blockState.getValue(POWERED).booleanValue()) {
            return 0;
        }
        if (blockState.getValue(FACING) == direction) {
            return this.getOutputSignal(blockGetter, blockPos, blockState);
        }
        return 0;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (blockState.canSurvive(level, blockPos)) {
            this.checkTickOnNeighbor(level, blockPos, blockState);
            return;
        }
        BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
        DiodeBlock.dropResources(blockState, level, blockPos, blockEntity);
        level.removeBlock(blockPos, false);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    protected void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState) {
        boolean bl;
        if (this.isLocked(level, blockPos, blockState)) {
            return;
        }
        boolean bl2 = blockState.getValue(POWERED);
        if (bl2 != (bl = this.shouldTurnOn(level, blockPos, blockState)) && !level.getBlockTicks().willTickThisTick(blockPos, this)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (this.shouldPrioritize(level, blockPos, blockState)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (bl2) {
                tickPriority = TickPriority.VERY_HIGH;
            }
            level.scheduleTick(blockPos, this, this.getDelay(blockState), tickPriority);
        }
    }

    public boolean isLocked(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return false;
    }

    protected boolean shouldTurnOn(Level level, BlockPos blockPos, BlockState blockState) {
        return this.getInputSignal(level, blockPos, blockState) > 0;
    }

    protected int getInputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction)blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction);
        int n = level.getSignal(blockPos2, direction);
        if (n >= 15) {
            return n;
        }
        BlockState blockState2 = level.getBlockState(blockPos2);
        return Math.max(n, blockState2.is(Blocks.REDSTONE_WIRE) ? blockState2.getValue(RedStoneWireBlock.POWER) : 0);
    }

    protected int getAlternateSignal(SignalGetter signalGetter, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction)blockState.getValue(FACING);
        Direction direction2 = direction.getClockWise();
        Direction direction3 = direction.getCounterClockWise();
        boolean bl = this.sideInputDiodesOnly();
        return Math.max(signalGetter.getControlInputSignal(blockPos.relative(direction2), direction2, bl), signalGetter.getControlInputSignal(blockPos.relative(direction3), direction3, bl));
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (this.shouldTurnOn(level, blockPos, blockState)) {
            level.scheduleTick(blockPos, this, 1);
        }
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.updateNeighborsInFront(level, blockPos, blockState);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        if (!bl) {
            this.updateNeighborsInFront(serverLevel, blockPos, blockState);
        }
    }

    protected void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction)blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, direction.getOpposite(), Direction.UP);
        level.neighborChanged(blockPos2, this, orientation);
        level.updateNeighborsAtExceptFromFacing(blockPos2, this, direction, orientation);
    }

    protected boolean sideInputDiodesOnly() {
        return false;
    }

    protected int getOutputSignal(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return 15;
    }

    public static boolean isDiode(BlockState blockState) {
        return blockState.getBlock() instanceof DiodeBlock;
    }

    public boolean shouldPrioritize(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        Direction direction = ((Direction)blockState.getValue(FACING)).getOpposite();
        BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
        return DiodeBlock.isDiode(blockState2) && blockState2.getValue(FACING) != direction;
    }

    protected abstract int getDelay(BlockState var1);
}

