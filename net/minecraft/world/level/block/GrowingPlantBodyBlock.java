/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBodyBlock
extends GrowingPlantBlock
implements BonemealableBlock {
    protected GrowingPlantBodyBlock(BlockBehaviour.Properties properties, Direction direction, VoxelShape voxelShape, boolean bl) {
        super(properties, direction, voxelShape, bl);
    }

    protected abstract MapCodec<? extends GrowingPlantBodyBlock> codec();

    protected BlockState updateHeadAfterConvertedFromBody(BlockState blockState, BlockState blockState2) {
        return blockState2;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == this.growthDirection.getOpposite() && !blockState.canSurvive(levelReader, blockPos)) {
            scheduledTickAccess.scheduleTick(blockPos, this, 1);
        }
        GrowingPlantHeadBlock growingPlantHeadBlock = this.getHeadBlock();
        if (direction == this.growthDirection && !blockState2.is(this) && !blockState2.is(growingPlantHeadBlock)) {
            return this.updateHeadAfterConvertedFromBody(blockState, growingPlantHeadBlock.getStateForPlacement(randomSource));
        }
        if (this.scheduleFluidTicks) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
        return new ItemStack(this.getHeadBlock());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        Optional<BlockPos> optional = this.getHeadPos(levelReader, blockPos, blockState.getBlock());
        return optional.isPresent() && this.getHeadBlock().canGrowInto(levelReader.getBlockState(optional.get().relative(this.growthDirection)));
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        Optional<BlockPos> optional = this.getHeadPos(serverLevel, blockPos, blockState.getBlock());
        if (optional.isPresent()) {
            BlockState blockState2 = serverLevel.getBlockState(optional.get());
            ((GrowingPlantHeadBlock)blockState2.getBlock()).performBonemeal(serverLevel, randomSource, optional.get(), blockState2);
        }
    }

    private Optional<BlockPos> getHeadPos(BlockGetter blockGetter, BlockPos blockPos, Block block) {
        return BlockUtil.getTopConnectedBlock(blockGetter, blockPos, block, this.growthDirection, this.getHeadBlock());
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        boolean bl = super.canBeReplaced(blockState, blockPlaceContext);
        if (bl && blockPlaceContext.getItemInHand().is(this.getHeadBlock().asItem())) {
            return false;
        }
        return bl;
    }

    @Override
    protected Block getBodyBlock() {
        return this;
    }
}

