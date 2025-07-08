/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.redstone;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

public abstract class RedstoneWireEvaluator {
    protected final RedStoneWireBlock wireBlock;

    protected RedstoneWireEvaluator(RedStoneWireBlock redStoneWireBlock) {
        this.wireBlock = redStoneWireBlock;
    }

    public abstract void updatePowerStrength(Level var1, BlockPos var2, BlockState var3, @Nullable Orientation var4, boolean var5);

    protected int getBlockSignal(Level level, BlockPos blockPos) {
        return this.wireBlock.getBlockSignal(level, blockPos);
    }

    protected int getWireSignal(BlockPos blockPos, BlockState blockState) {
        return blockState.is(this.wireBlock) ? blockState.getValue(RedStoneWireBlock.POWER) : 0;
    }

    protected int getIncomingWireSignal(Level level, BlockPos blockPos) {
        int n = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2;
            BlockPos blockPos3 = blockPos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos3);
            n = Math.max(n, this.getWireSignal(blockPos3, blockState));
            BlockPos blockPos4 = blockPos.above();
            if (blockState.isRedstoneConductor(level, blockPos3) && !level.getBlockState(blockPos4).isRedstoneConductor(level, blockPos4)) {
                blockPos2 = blockPos3.above();
                n = Math.max(n, this.getWireSignal(blockPos2, level.getBlockState(blockPos2)));
                continue;
            }
            if (blockState.isRedstoneConductor(level, blockPos3)) continue;
            blockPos2 = blockPos3.below();
            n = Math.max(n, this.getWireSignal(blockPos2, level.getBlockState(blockPos2)));
        }
        return Math.max(0, n - 1);
    }
}

