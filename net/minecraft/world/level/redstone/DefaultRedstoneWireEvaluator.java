/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.redstone;

import com.google.common.collect.Sets;
import java.util.HashSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;

public class DefaultRedstoneWireEvaluator
extends RedstoneWireEvaluator {
    public DefaultRedstoneWireEvaluator(RedStoneWireBlock redStoneWireBlock) {
        super(redStoneWireBlock);
    }

    @Override
    public void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState, @Nullable Orientation orientation, boolean bl) {
        int n = this.calculateTargetStrength(level, blockPos);
        if (blockState.getValue(RedStoneWireBlock.POWER) != n) {
            if (level.getBlockState(blockPos) == blockState) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(RedStoneWireBlock.POWER, n), 2);
            }
            HashSet hashSet = Sets.newHashSet();
            hashSet.add(blockPos);
            for (Direction direction : Direction.values()) {
                hashSet.add(blockPos.relative(direction));
            }
            for (BlockPos blockPos2 : hashSet) {
                level.updateNeighborsAt(blockPos2, this.wireBlock);
            }
        }
    }

    private int calculateTargetStrength(Level level, BlockPos blockPos) {
        int n = this.getBlockSignal(level, blockPos);
        if (n == 15) {
            return n;
        }
        return Math.max(n, this.getIncomingWireSignal(level, blockPos));
    }
}

