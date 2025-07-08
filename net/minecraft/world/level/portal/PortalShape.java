/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

public class PortalShape {
    private static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;
    private static final BlockBehaviour.StatePredicate FRAME = (blockState, blockGetter, blockPos) -> blockState.is(Blocks.OBSIDIAN);
    private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0f;
    private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private final int numPortalBlocks;
    private final BlockPos bottomLeft;
    private final int height;
    private final int width;

    private PortalShape(Direction.Axis axis, int n, Direction direction, BlockPos blockPos, int n2, int n3) {
        this.axis = axis;
        this.numPortalBlocks = n;
        this.rightDir = direction;
        this.bottomLeft = blockPos;
        this.width = n2;
        this.height = n3;
    }

    public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
        return PortalShape.findPortalShape(levelAccessor, blockPos, portalShape -> portalShape.isValid() && portalShape.numPortalBlocks == 0, axis);
    }

    public static Optional<PortalShape> findPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Predicate<PortalShape> predicate, Direction.Axis axis) {
        Optional<PortalShape> optional = Optional.of(PortalShape.findAnyShape(levelAccessor, blockPos, axis)).filter(predicate);
        if (optional.isPresent()) {
            return optional;
        }
        Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(PortalShape.findAnyShape(levelAccessor, blockPos, axis2)).filter(predicate);
    }

    public static PortalShape findAnyShape(BlockGetter blockGetter, BlockPos blockPos, Direction.Axis axis) {
        Direction direction = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        BlockPos blockPos2 = PortalShape.calculateBottomLeft(blockGetter, direction, blockPos);
        if (blockPos2 == null) {
            return new PortalShape(axis, 0, direction, blockPos, 0, 0);
        }
        int n = PortalShape.calculateWidth(blockGetter, blockPos2, direction);
        if (n == 0) {
            return new PortalShape(axis, 0, direction, blockPos2, 0, 0);
        }
        MutableInt mutableInt = new MutableInt();
        int n2 = PortalShape.calculateHeight(blockGetter, blockPos2, direction, n, mutableInt);
        return new PortalShape(axis, mutableInt.getValue(), direction, blockPos2, n, n2);
    }

    @Nullable
    private static BlockPos calculateBottomLeft(BlockGetter blockGetter, Direction direction, BlockPos blockPos) {
        int n = Math.max(blockGetter.getMinY(), blockPos.getY() - 21);
        while (blockPos.getY() > n && PortalShape.isEmpty(blockGetter.getBlockState(blockPos.below()))) {
            blockPos = blockPos.below();
        }
        Direction direction2 = direction.getOpposite();
        int n2 = PortalShape.getDistanceUntilEdgeAboveFrame(blockGetter, blockPos, direction2) - 1;
        if (n2 < 0) {
            return null;
        }
        return blockPos.relative(direction2, n2);
    }

    private static int calculateWidth(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        int n = PortalShape.getDistanceUntilEdgeAboveFrame(blockGetter, blockPos, direction);
        if (n < 2 || n > 21) {
            return 0;
        }
        return n;
    }

    private static int getDistanceUntilEdgeAboveFrame(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= 21; ++i) {
            mutableBlockPos.set(blockPos).move(direction, i);
            BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
            if (!PortalShape.isEmpty(blockState)) {
                if (!FRAME.test(blockState, blockGetter, mutableBlockPos)) break;
                return i;
            }
            BlockState blockState2 = blockGetter.getBlockState(mutableBlockPos.move(Direction.DOWN));
            if (!FRAME.test(blockState2, blockGetter, mutableBlockPos)) break;
        }
        return 0;
    }

    private static int calculateHeight(BlockGetter blockGetter, BlockPos blockPos, Direction direction, int n, MutableInt mutableInt) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n2 = PortalShape.getDistanceUntilTop(blockGetter, blockPos, direction, mutableBlockPos, n, mutableInt);
        if (n2 < 3 || n2 > 21 || !PortalShape.hasTopFrame(blockGetter, blockPos, direction, mutableBlockPos, n, n2)) {
            return 0;
        }
        return n2;
    }

    private static boolean hasTopFrame(BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos.MutableBlockPos mutableBlockPos, int n, int n2) {
        for (int i = 0; i < n; ++i) {
            BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.set(blockPos).move(Direction.UP, n2).move(direction, i);
            if (FRAME.test(blockGetter.getBlockState(mutableBlockPos2), blockGetter, mutableBlockPos2)) continue;
            return false;
        }
        return true;
    }

    private static int getDistanceUntilTop(BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos.MutableBlockPos mutableBlockPos, int n, MutableInt mutableInt) {
        for (int i = 0; i < 21; ++i) {
            mutableBlockPos.set(blockPos).move(Direction.UP, i).move(direction, -1);
            if (!FRAME.test(blockGetter.getBlockState(mutableBlockPos), blockGetter, mutableBlockPos)) {
                return i;
            }
            mutableBlockPos.set(blockPos).move(Direction.UP, i).move(direction, n);
            if (!FRAME.test(blockGetter.getBlockState(mutableBlockPos), blockGetter, mutableBlockPos)) {
                return i;
            }
            for (int j = 0; j < n; ++j) {
                mutableBlockPos.set(blockPos).move(Direction.UP, i).move(direction, j);
                BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
                if (!PortalShape.isEmpty(blockState)) {
                    return i;
                }
                if (!blockState.is(Blocks.NETHER_PORTAL)) continue;
                mutableInt.increment();
            }
        }
        return 21;
    }

    private static boolean isEmpty(BlockState blockState) {
        return blockState.isAir() || blockState.is(BlockTags.FIRE) || blockState.is(Blocks.NETHER_PORTAL);
    }

    public boolean isValid() {
        return this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortalBlocks(LevelAccessor levelAccessor) {
        BlockState blockState = (BlockState)Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach(blockPos -> levelAccessor.setBlock((BlockPos)blockPos, blockState, 18));
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }

    public static Vec3 getRelativePosition(BlockUtil.FoundRectangle foundRectangle, Direction.Axis axis, Vec3 vec3, EntityDimensions entityDimensions) {
        Direction.Axis axis2;
        double d;
        double d2;
        double d3 = (double)foundRectangle.axis1Size - (double)entityDimensions.width();
        double d4 = (double)foundRectangle.axis2Size - (double)entityDimensions.height();
        BlockPos blockPos = foundRectangle.minCorner;
        if (d3 > 0.0) {
            d2 = (double)blockPos.get(axis) + (double)entityDimensions.width() / 2.0;
            d = Mth.clamp(Mth.inverseLerp(vec3.get(axis) - d2, 0.0, d3), 0.0, 1.0);
        } else {
            d = 0.5;
        }
        if (d4 > 0.0) {
            axis2 = Direction.Axis.Y;
            d2 = Mth.clamp(Mth.inverseLerp(vec3.get(axis2) - (double)blockPos.get(axis2), 0.0, d4), 0.0, 1.0);
        } else {
            d2 = 0.0;
        }
        axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        double d5 = vec3.get(axis2) - ((double)blockPos.get(axis2) + 0.5);
        return new Vec3(d, d2, d5);
    }

    public static Vec3 findCollisionFreePosition(Vec3 vec32, ServerLevel serverLevel, Entity entity, EntityDimensions entityDimensions) {
        if (entityDimensions.width() > 4.0f || entityDimensions.height() > 4.0f) {
            return vec32;
        }
        double d = (double)entityDimensions.height() / 2.0;
        Vec3 vec33 = vec32.add(0.0, d, 0.0);
        VoxelShape voxelShape = Shapes.create(AABB.ofSize(vec33, entityDimensions.width(), 0.0, entityDimensions.width()).expandTowards(0.0, 1.0, 0.0).inflate(1.0E-6));
        Optional<Vec3> optional = serverLevel.findFreePosition(entity, voxelShape, vec33, entityDimensions.width(), entityDimensions.height(), entityDimensions.width());
        Optional<Vec3> optional2 = optional.map(vec3 -> vec3.subtract(0.0, d, 0.0));
        return optional2.orElse(vec32);
    }
}

