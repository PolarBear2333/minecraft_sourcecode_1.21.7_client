/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailState {
    private final Level level;
    private final BlockPos pos;
    private final BaseRailBlock block;
    private BlockState state;
    private final boolean isStraight;
    private final List<BlockPos> connections = Lists.newArrayList();

    public RailState(Level level, BlockPos blockPos, BlockState blockState) {
        this.level = level;
        this.pos = blockPos;
        this.state = blockState;
        this.block = (BaseRailBlock)blockState.getBlock();
        RailShape railShape = blockState.getValue(this.block.getShapeProperty());
        this.isStraight = this.block.isStraight();
        this.updateConnections(railShape);
    }

    public List<BlockPos> getConnections() {
        return this.connections;
    }

    private void updateConnections(RailShape railShape) {
        this.connections.clear();
        switch (railShape) {
            case NORTH_SOUTH: {
                this.connections.add(this.pos.north());
                this.connections.add(this.pos.south());
                break;
            }
            case EAST_WEST: {
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.east());
                break;
            }
            case ASCENDING_EAST: {
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.east().above());
                break;
            }
            case ASCENDING_WEST: {
                this.connections.add(this.pos.west().above());
                this.connections.add(this.pos.east());
                break;
            }
            case ASCENDING_NORTH: {
                this.connections.add(this.pos.north().above());
                this.connections.add(this.pos.south());
                break;
            }
            case ASCENDING_SOUTH: {
                this.connections.add(this.pos.north());
                this.connections.add(this.pos.south().above());
                break;
            }
            case SOUTH_EAST: {
                this.connections.add(this.pos.east());
                this.connections.add(this.pos.south());
                break;
            }
            case SOUTH_WEST: {
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.south());
                break;
            }
            case NORTH_WEST: {
                this.connections.add(this.pos.west());
                this.connections.add(this.pos.north());
                break;
            }
            case NORTH_EAST: {
                this.connections.add(this.pos.east());
                this.connections.add(this.pos.north());
            }
        }
    }

    private void removeSoftConnections() {
        for (int i = 0; i < this.connections.size(); ++i) {
            RailState railState = this.getRail(this.connections.get(i));
            if (railState == null || !railState.connectsTo(this)) {
                this.connections.remove(i--);
                continue;
            }
            this.connections.set(i, railState.pos);
        }
    }

    private boolean hasRail(BlockPos blockPos) {
        return BaseRailBlock.isRail(this.level, blockPos) || BaseRailBlock.isRail(this.level, blockPos.above()) || BaseRailBlock.isRail(this.level, blockPos.below());
    }

    @Nullable
    private RailState getRail(BlockPos blockPos) {
        BlockPos blockPos2 = blockPos;
        BlockState blockState = this.level.getBlockState(blockPos2);
        if (BaseRailBlock.isRail(blockState)) {
            return new RailState(this.level, blockPos2, blockState);
        }
        blockPos2 = blockPos.above();
        blockState = this.level.getBlockState(blockPos2);
        if (BaseRailBlock.isRail(blockState)) {
            return new RailState(this.level, blockPos2, blockState);
        }
        blockPos2 = blockPos.below();
        blockState = this.level.getBlockState(blockPos2);
        if (BaseRailBlock.isRail(blockState)) {
            return new RailState(this.level, blockPos2, blockState);
        }
        return null;
    }

    private boolean connectsTo(RailState railState) {
        return this.hasConnection(railState.pos);
    }

    private boolean hasConnection(BlockPos blockPos) {
        for (int i = 0; i < this.connections.size(); ++i) {
            BlockPos blockPos2 = this.connections.get(i);
            if (blockPos2.getX() != blockPos.getX() || blockPos2.getZ() != blockPos.getZ()) continue;
            return true;
        }
        return false;
    }

    protected int countPotentialConnections() {
        int n = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!this.hasRail(this.pos.relative(direction))) continue;
            ++n;
        }
        return n;
    }

    private boolean canConnectTo(RailState railState) {
        return this.connectsTo(railState) || this.connections.size() != 2;
    }

    private void connectTo(RailState railState) {
        this.connections.add(railState.pos);
        BlockPos blockPos = this.pos.north();
        BlockPos blockPos2 = this.pos.south();
        BlockPos blockPos3 = this.pos.west();
        BlockPos blockPos4 = this.pos.east();
        boolean bl = this.hasConnection(blockPos);
        boolean bl2 = this.hasConnection(blockPos2);
        boolean bl3 = this.hasConnection(blockPos3);
        boolean bl4 = this.hasConnection(blockPos4);
        RailShape railShape = null;
        if (bl || bl2) {
            railShape = RailShape.NORTH_SOUTH;
        }
        if (bl3 || bl4) {
            railShape = RailShape.EAST_WEST;
        }
        if (!this.isStraight) {
            if (bl2 && bl4 && !bl && !bl3) {
                railShape = RailShape.SOUTH_EAST;
            }
            if (bl2 && bl3 && !bl && !bl4) {
                railShape = RailShape.SOUTH_WEST;
            }
            if (bl && bl3 && !bl2 && !bl4) {
                railShape = RailShape.NORTH_WEST;
            }
            if (bl && bl4 && !bl2 && !bl3) {
                railShape = RailShape.NORTH_EAST;
            }
        }
        if (railShape == RailShape.NORTH_SOUTH) {
            if (BaseRailBlock.isRail(this.level, blockPos.above())) {
                railShape = RailShape.ASCENDING_NORTH;
            }
            if (BaseRailBlock.isRail(this.level, blockPos2.above())) {
                railShape = RailShape.ASCENDING_SOUTH;
            }
        }
        if (railShape == RailShape.EAST_WEST) {
            if (BaseRailBlock.isRail(this.level, blockPos4.above())) {
                railShape = RailShape.ASCENDING_EAST;
            }
            if (BaseRailBlock.isRail(this.level, blockPos3.above())) {
                railShape = RailShape.ASCENDING_WEST;
            }
        }
        if (railShape == null) {
            railShape = RailShape.NORTH_SOUTH;
        }
        this.state = (BlockState)this.state.setValue(this.block.getShapeProperty(), railShape);
        this.level.setBlock(this.pos, this.state, 3);
    }

    private boolean hasNeighborRail(BlockPos blockPos) {
        RailState railState = this.getRail(blockPos);
        if (railState == null) {
            return false;
        }
        railState.removeSoftConnections();
        return railState.canConnectTo(this);
    }

    public RailState place(boolean bl, boolean bl2, RailShape railShape) {
        boolean bl3;
        boolean bl4;
        BlockPos blockPos = this.pos.north();
        BlockPos blockPos2 = this.pos.south();
        BlockPos blockPos3 = this.pos.west();
        BlockPos blockPos4 = this.pos.east();
        boolean bl5 = this.hasNeighborRail(blockPos);
        boolean bl6 = this.hasNeighborRail(blockPos2);
        boolean bl7 = this.hasNeighborRail(blockPos3);
        boolean bl8 = this.hasNeighborRail(blockPos4);
        RailShape railShape2 = null;
        boolean bl9 = bl5 || bl6;
        boolean bl10 = bl4 = bl7 || bl8;
        if (bl9 && !bl4) {
            railShape2 = RailShape.NORTH_SOUTH;
        }
        if (bl4 && !bl9) {
            railShape2 = RailShape.EAST_WEST;
        }
        boolean bl11 = bl6 && bl8;
        boolean bl12 = bl6 && bl7;
        boolean bl13 = bl5 && bl8;
        boolean bl14 = bl3 = bl5 && bl7;
        if (!this.isStraight) {
            if (bl11 && !bl5 && !bl7) {
                railShape2 = RailShape.SOUTH_EAST;
            }
            if (bl12 && !bl5 && !bl8) {
                railShape2 = RailShape.SOUTH_WEST;
            }
            if (bl3 && !bl6 && !bl8) {
                railShape2 = RailShape.NORTH_WEST;
            }
            if (bl13 && !bl6 && !bl7) {
                railShape2 = RailShape.NORTH_EAST;
            }
        }
        if (railShape2 == null) {
            if (bl9 && bl4) {
                railShape2 = railShape;
            } else if (bl9) {
                railShape2 = RailShape.NORTH_SOUTH;
            } else if (bl4) {
                railShape2 = RailShape.EAST_WEST;
            }
            if (!this.isStraight) {
                if (bl) {
                    if (bl11) {
                        railShape2 = RailShape.SOUTH_EAST;
                    }
                    if (bl12) {
                        railShape2 = RailShape.SOUTH_WEST;
                    }
                    if (bl13) {
                        railShape2 = RailShape.NORTH_EAST;
                    }
                    if (bl3) {
                        railShape2 = RailShape.NORTH_WEST;
                    }
                } else {
                    if (bl3) {
                        railShape2 = RailShape.NORTH_WEST;
                    }
                    if (bl13) {
                        railShape2 = RailShape.NORTH_EAST;
                    }
                    if (bl12) {
                        railShape2 = RailShape.SOUTH_WEST;
                    }
                    if (bl11) {
                        railShape2 = RailShape.SOUTH_EAST;
                    }
                }
            }
        }
        if (railShape2 == RailShape.NORTH_SOUTH) {
            if (BaseRailBlock.isRail(this.level, blockPos.above())) {
                railShape2 = RailShape.ASCENDING_NORTH;
            }
            if (BaseRailBlock.isRail(this.level, blockPos2.above())) {
                railShape2 = RailShape.ASCENDING_SOUTH;
            }
        }
        if (railShape2 == RailShape.EAST_WEST) {
            if (BaseRailBlock.isRail(this.level, blockPos4.above())) {
                railShape2 = RailShape.ASCENDING_EAST;
            }
            if (BaseRailBlock.isRail(this.level, blockPos3.above())) {
                railShape2 = RailShape.ASCENDING_WEST;
            }
        }
        if (railShape2 == null) {
            railShape2 = railShape;
        }
        this.updateConnections(railShape2);
        this.state = (BlockState)this.state.setValue(this.block.getShapeProperty(), railShape2);
        if (bl2 || this.level.getBlockState(this.pos) != this.state) {
            this.level.setBlock(this.pos, this.state, 3);
            for (int i = 0; i < this.connections.size(); ++i) {
                RailState railState = this.getRail(this.connections.get(i));
                if (railState == null) continue;
                railState.removeSoftConnections();
                if (!railState.canConnectTo(this)) continue;
                railState.connectTo(this);
            }
        }
        return this;
    }

    public BlockState getState() {
        return this.state;
    }
}

