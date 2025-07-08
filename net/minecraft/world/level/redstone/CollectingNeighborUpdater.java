/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.redstone;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import org.slf4j.Logger;

public class CollectingNeighborUpdater
implements NeighborUpdater {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Level level;
    private final int maxChainedNeighborUpdates;
    private final ArrayDeque<NeighborUpdates> stack = new ArrayDeque();
    private final List<NeighborUpdates> addedThisLayer = new ArrayList<NeighborUpdates>();
    private int count = 0;

    public CollectingNeighborUpdater(Level level, int n) {
        this.level = level;
        this.maxChainedNeighborUpdates = n;
    }

    @Override
    public void shapeUpdate(Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int n, int n2) {
        this.addAndRun(blockPos, new ShapeUpdate(direction, blockState, blockPos.immutable(), blockPos2.immutable(), n, n2));
    }

    @Override
    public void neighborChanged(BlockPos blockPos, Block block, @Nullable Orientation orientation) {
        this.addAndRun(blockPos, new SimpleNeighborUpdate(blockPos, block, orientation));
    }

    @Override
    public void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        this.addAndRun(blockPos, new FullNeighborUpdate(blockState, blockPos.immutable(), block, orientation, bl));
    }

    @Override
    public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, @Nullable Direction direction, @Nullable Orientation orientation) {
        this.addAndRun(blockPos, new MultiNeighborUpdate(blockPos.immutable(), block, orientation, direction));
    }

    private void addAndRun(BlockPos blockPos, NeighborUpdates neighborUpdates) {
        boolean bl = this.count > 0;
        boolean bl2 = this.maxChainedNeighborUpdates >= 0 && this.count >= this.maxChainedNeighborUpdates;
        ++this.count;
        if (!bl2) {
            if (bl) {
                this.addedThisLayer.add(neighborUpdates);
            } else {
                this.stack.push(neighborUpdates);
            }
        } else if (this.count - 1 == this.maxChainedNeighborUpdates) {
            LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: " + blockPos.toShortString());
        }
        if (!bl) {
            this.runUpdates();
        }
    }

    private void runUpdates() {
        try {
            block3: while (!this.stack.isEmpty() || !this.addedThisLayer.isEmpty()) {
                for (int i = this.addedThisLayer.size() - 1; i >= 0; --i) {
                    this.stack.push(this.addedThisLayer.get(i));
                }
                this.addedThisLayer.clear();
                NeighborUpdates neighborUpdates = this.stack.peek();
                while (this.addedThisLayer.isEmpty()) {
                    if (neighborUpdates.runNext(this.level)) continue;
                    this.stack.pop();
                    continue block3;
                }
            }
        }
        finally {
            this.stack.clear();
            this.addedThisLayer.clear();
            this.count = 0;
        }
    }

    record ShapeUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int updateFlags, int updateLimit) implements NeighborUpdates
    {
        @Override
        public boolean runNext(Level level) {
            NeighborUpdater.executeShapeUpdate(level, this.direction, this.pos, this.neighborPos, this.neighborState, this.updateFlags, this.updateLimit);
            return false;
        }
    }

    static interface NeighborUpdates {
        public boolean runNext(Level var1);
    }

    record SimpleNeighborUpdate(BlockPos pos, Block block, @Nullable Orientation orientation) implements NeighborUpdates
    {
        @Override
        public boolean runNext(Level level) {
            BlockState blockState = level.getBlockState(this.pos);
            NeighborUpdater.executeUpdate(level, blockState, this.pos, this.block, this.orientation, false);
            return false;
        }
    }

    record FullNeighborUpdate(BlockState state, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) implements NeighborUpdates
    {
        @Override
        public boolean runNext(Level level) {
            NeighborUpdater.executeUpdate(level, this.state, this.pos, this.block, this.orientation, this.movedByPiston);
            return false;
        }
    }

    static final class MultiNeighborUpdate
    implements NeighborUpdates {
        private final BlockPos sourcePos;
        private final Block sourceBlock;
        @Nullable
        private Orientation orientation;
        @Nullable
        private final Direction skipDirection;
        private int idx = 0;

        MultiNeighborUpdate(BlockPos blockPos, Block block, @Nullable Orientation orientation, @Nullable Direction direction) {
            this.sourcePos = blockPos;
            this.sourceBlock = block;
            this.orientation = orientation;
            this.skipDirection = direction;
            if (NeighborUpdater.UPDATE_ORDER[this.idx] == direction) {
                ++this.idx;
            }
        }

        @Override
        public boolean runNext(Level level) {
            Direction direction = NeighborUpdater.UPDATE_ORDER[this.idx++];
            BlockPos blockPos = this.sourcePos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos);
            Orientation orientation = null;
            if (level.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS)) {
                if (this.orientation == null) {
                    this.orientation = ExperimentalRedstoneUtils.initialOrientation(level, this.skipDirection == null ? null : this.skipDirection.getOpposite(), null);
                }
                orientation = this.orientation.withFront(direction);
            }
            NeighborUpdater.executeUpdate(level, blockState, blockPos, this.sourceBlock, orientation, false);
            if (this.idx < NeighborUpdater.UPDATE_ORDER.length && NeighborUpdater.UPDATE_ORDER[this.idx] == this.skipDirection) {
                ++this.idx;
            }
            return this.idx < NeighborUpdater.UPDATE_ORDER.length;
        }
    }
}

