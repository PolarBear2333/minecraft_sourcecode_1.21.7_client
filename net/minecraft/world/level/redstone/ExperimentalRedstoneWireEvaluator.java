/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.redstone;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.RedstoneWireOrientationsDebugPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;

public class ExperimentalRedstoneWireEvaluator
extends RedstoneWireEvaluator {
    private final Deque<BlockPos> wiresToTurnOff = new ArrayDeque<BlockPos>();
    private final Deque<BlockPos> wiresToTurnOn = new ArrayDeque<BlockPos>();
    private final Object2IntMap<BlockPos> updatedWires = new Object2IntLinkedOpenHashMap();

    public ExperimentalRedstoneWireEvaluator(RedStoneWireBlock redStoneWireBlock) {
        super(redStoneWireBlock);
    }

    @Override
    public void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState, @Nullable Orientation orientation, boolean bl) {
        Orientation orientation2 = ExperimentalRedstoneWireEvaluator.getInitialOrientation(level, orientation);
        this.calculateCurrentChanges(level, blockPos, orientation2);
        ObjectIterator objectIterator = this.updatedWires.object2IntEntrySet().iterator();
        boolean bl2 = true;
        while (objectIterator.hasNext()) {
            Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
            BlockPos blockPos2 = (BlockPos)entry.getKey();
            int n = entry.getIntValue();
            int n2 = ExperimentalRedstoneWireEvaluator.unpackPower(n);
            BlockState blockState2 = level.getBlockState(blockPos2);
            if (blockState2.is(this.wireBlock) && !blockState2.getValue(RedStoneWireBlock.POWER).equals(n2)) {
                int n3 = 2;
                if (!bl || !bl2) {
                    n3 |= 0x80;
                }
                level.setBlock(blockPos2, (BlockState)blockState2.setValue(RedStoneWireBlock.POWER, n2), n3);
            } else {
                objectIterator.remove();
            }
            bl2 = false;
        }
        this.causeNeighborUpdates(level);
    }

    private void causeNeighborUpdates(Level level) {
        this.updatedWires.forEach((blockPos, n) -> {
            Orientation orientation = ExperimentalRedstoneWireEvaluator.unpackOrientation(n);
            BlockState blockState = level.getBlockState((BlockPos)blockPos);
            for (Direction direction : orientation.getDirections()) {
                if (!ExperimentalRedstoneWireEvaluator.isConnected(blockState, direction)) continue;
                BlockPos blockPos2 = blockPos.relative(direction);
                BlockState blockState2 = level.getBlockState(blockPos2);
                Orientation orientation2 = orientation.withFrontPreserveUp(direction);
                level.neighborChanged(blockState2, blockPos2, this.wireBlock, orientation2, false);
                if (!blockState2.isRedstoneConductor(level, blockPos2)) continue;
                for (Direction direction2 : orientation2.getDirections()) {
                    if (direction2 == direction.getOpposite()) continue;
                    level.neighborChanged(blockPos2.relative(direction2), this.wireBlock, orientation2.withFrontPreserveUp(direction2));
                }
            }
        });
    }

    private static boolean isConnected(BlockState blockState, Direction direction) {
        EnumProperty<RedstoneSide> enumProperty = RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(direction);
        if (enumProperty == null) {
            return direction == Direction.DOWN;
        }
        return blockState.getValue(enumProperty).isConnected();
    }

    private static Orientation getInitialOrientation(Level level, @Nullable Orientation orientation) {
        Orientation orientation2 = orientation != null ? orientation : Orientation.random(level.random);
        return orientation2.withUp(Direction.UP).withSideBias(Orientation.SideBias.LEFT);
    }

    private void calculateCurrentChanges(Level level, BlockPos blockPos, Orientation orientation) {
        int n;
        int n2;
        int n3;
        int n4;
        BlockPos blockPos2;
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(this.wireBlock)) {
            this.setPower(blockPos, blockState.getValue(RedStoneWireBlock.POWER), orientation);
            this.wiresToTurnOff.add(blockPos);
        } else {
            this.propagateChangeToNeighbors(level, blockPos, 0, orientation, true);
        }
        while (!this.wiresToTurnOff.isEmpty()) {
            int n5;
            blockPos2 = this.wiresToTurnOff.removeFirst();
            n4 = this.updatedWires.getInt((Object)blockPos2);
            Orientation orientation2 = ExperimentalRedstoneWireEvaluator.unpackOrientation(n4);
            n3 = ExperimentalRedstoneWireEvaluator.unpackPower(n4);
            n2 = this.getBlockSignal(level, blockPos2);
            int n6 = Math.max(n2, n = this.getIncomingWireSignal(level, blockPos2));
            if (n6 < n3) {
                if (n2 > 0 && !this.wiresToTurnOn.contains(blockPos2)) {
                    this.wiresToTurnOn.add(blockPos2);
                }
                n5 = 0;
            } else {
                n5 = n6;
            }
            if (n5 != n3) {
                this.setPower(blockPos2, n5, orientation2);
            }
            this.propagateChangeToNeighbors(level, blockPos2, n5, orientation2, n3 > n6);
        }
        while (!this.wiresToTurnOn.isEmpty()) {
            blockPos2 = this.wiresToTurnOn.removeFirst();
            n4 = this.updatedWires.getInt((Object)blockPos2);
            int n7 = ExperimentalRedstoneWireEvaluator.unpackPower(n4);
            n3 = this.getBlockSignal(level, blockPos2);
            n2 = this.getIncomingWireSignal(level, blockPos2);
            n = Math.max(n3, n2);
            Orientation orientation3 = ExperimentalRedstoneWireEvaluator.unpackOrientation(n4);
            if (n > n7) {
                this.setPower(blockPos2, n, orientation3);
            } else if (n < n7) {
                throw new IllegalStateException("Turning off wire while trying to turn it on. Should not happen.");
            }
            this.propagateChangeToNeighbors(level, blockPos2, n, orientation3, false);
        }
    }

    private static int packOrientationAndPower(Orientation orientation, int n) {
        return orientation.getIndex() << 4 | n;
    }

    private static Orientation unpackOrientation(int n) {
        return Orientation.fromIndex(n >> 4);
    }

    private static int unpackPower(int n) {
        return n & 0xF;
    }

    private void setPower(BlockPos blockPos2, int n, Orientation orientation) {
        this.updatedWires.compute((Object)blockPos2, (blockPos, n2) -> {
            if (n2 == null) {
                return ExperimentalRedstoneWireEvaluator.packOrientationAndPower(orientation, n);
            }
            return ExperimentalRedstoneWireEvaluator.packOrientationAndPower(ExperimentalRedstoneWireEvaluator.unpackOrientation(n2), n);
        });
    }

    private void propagateChangeToNeighbors(Level level, BlockPos blockPos, int n, Orientation orientation, boolean bl) {
        BlockPos blockPos2;
        for (Direction direction : orientation.getHorizontalDirections()) {
            blockPos2 = blockPos.relative(direction);
            this.enqueueNeighborWire(level, blockPos2, n, orientation.withFront(direction), bl);
        }
        for (Direction direction : orientation.getVerticalDirections()) {
            blockPos2 = blockPos.relative(direction);
            boolean bl2 = level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2);
            for (Direction direction2 : orientation.getHorizontalDirections()) {
                BlockPos blockPos3;
                BlockPos blockPos4 = blockPos.relative(direction2);
                if (direction == Direction.UP && !bl2) {
                    blockPos3 = blockPos2.relative(direction2);
                    this.enqueueNeighborWire(level, blockPos3, n, orientation.withFront(direction2), bl);
                    continue;
                }
                if (direction != Direction.DOWN || level.getBlockState(blockPos4).isRedstoneConductor(level, blockPos4)) continue;
                blockPos3 = blockPos2.relative(direction2);
                this.enqueueNeighborWire(level, blockPos3, n, orientation.withFront(direction2), bl);
            }
        }
    }

    private void enqueueNeighborWire(Level level, BlockPos blockPos, int n, Orientation orientation, boolean bl) {
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(this.wireBlock)) {
            int n2 = this.getWireSignal(blockPos, blockState);
            if (n2 < n - 1 && !this.wiresToTurnOn.contains(blockPos)) {
                this.wiresToTurnOn.add(blockPos);
                this.setPower(blockPos, n2, orientation);
            }
            if (bl && n2 > n && !this.wiresToTurnOff.contains(blockPos)) {
                this.wiresToTurnOff.add(blockPos);
                this.setPower(blockPos, n2, orientation);
            }
        }
    }

    @Override
    protected int getWireSignal(BlockPos blockPos, BlockState blockState) {
        int n = this.updatedWires.getOrDefault((Object)blockPos, -1);
        if (n != -1) {
            return ExperimentalRedstoneWireEvaluator.unpackPower(n);
        }
        return super.getWireSignal(blockPos, blockState);
    }

    private static /* synthetic */ void lambda$causeNeighborUpdates$1(List list, BlockPos blockPos, Integer n) {
        Orientation orientation = ExperimentalRedstoneWireEvaluator.unpackOrientation(n);
        list.add(new RedstoneWireOrientationsDebugPayload.Wire(blockPos, orientation));
    }
}

