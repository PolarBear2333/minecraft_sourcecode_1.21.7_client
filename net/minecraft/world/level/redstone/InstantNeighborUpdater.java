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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;

public class InstantNeighborUpdater
implements NeighborUpdater {
    private final Level level;

    public InstantNeighborUpdater(Level level) {
        this.level = level;
    }

    @Override
    public void shapeUpdate(Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int n, int n2) {
        NeighborUpdater.executeShapeUpdate(this.level, direction, blockPos, blockPos2, blockState, n, n2 - 1);
    }

    @Override
    public void neighborChanged(BlockPos blockPos, Block block, @Nullable Orientation orientation) {
        BlockState blockState = this.level.getBlockState(blockPos);
        this.neighborChanged(blockState, blockPos, block, orientation, false);
    }

    @Override
    public void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        NeighborUpdater.executeUpdate(this.level, blockState, blockPos, block, orientation, bl);
    }
}

