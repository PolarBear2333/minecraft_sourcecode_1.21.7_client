/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface BlockGetter
extends LevelHeightAccessor {
    public static final int MAX_BLOCK_ITERATIONS_ALONG_TRAVEL = 16;

    @Nullable
    public BlockEntity getBlockEntity(BlockPos var1);

    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity == null || blockEntity.getType() != blockEntityType) {
            return Optional.empty();
        }
        return Optional.of(blockEntity);
    }

    public BlockState getBlockState(BlockPos var1);

    public FluidState getFluidState(BlockPos var1);

    default public int getLightEmission(BlockPos blockPos) {
        return this.getBlockState(blockPos).getLightEmission();
    }

    default public Stream<BlockState> getBlockStates(AABB aABB) {
        return BlockPos.betweenClosedStream(aABB).map(this::getBlockState);
    }

    default public BlockHitResult isBlockInLine(ClipBlockStateContext clipBlockStateContext2) {
        return BlockGetter.traverseBlocks(clipBlockStateContext2.getFrom(), clipBlockStateContext2.getTo(), clipBlockStateContext2, (clipBlockStateContext, blockPos) -> {
            BlockState blockState = this.getBlockState((BlockPos)blockPos);
            Vec3 vec3 = clipBlockStateContext.getFrom().subtract(clipBlockStateContext.getTo());
            return clipBlockStateContext.isTargetBlock().test(blockState) ? new BlockHitResult(clipBlockStateContext.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipBlockStateContext.getTo()), false) : null;
        }, clipBlockStateContext -> {
            Vec3 vec3 = clipBlockStateContext.getFrom().subtract(clipBlockStateContext.getTo());
            return BlockHitResult.miss(clipBlockStateContext.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipBlockStateContext.getTo()));
        });
    }

    default public BlockHitResult clip(ClipContext clipContext2) {
        return BlockGetter.traverseBlocks(clipContext2.getFrom(), clipContext2.getTo(), clipContext2, (clipContext, blockPos) -> {
            BlockState blockState = this.getBlockState((BlockPos)blockPos);
            FluidState fluidState = this.getFluidState((BlockPos)blockPos);
            Vec3 vec3 = clipContext.getFrom();
            Vec3 vec32 = clipContext.getTo();
            VoxelShape voxelShape = clipContext.getBlockShape(blockState, this, (BlockPos)blockPos);
            BlockHitResult blockHitResult = this.clipWithInteractionOverride(vec3, vec32, (BlockPos)blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = clipContext.getFluidShape(fluidState, this, (BlockPos)blockPos);
            BlockHitResult blockHitResult2 = voxelShape2.clip(vec3, vec32, (BlockPos)blockPos);
            double d = blockHitResult == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockHitResult.getLocation());
            double d2 = blockHitResult2 == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockHitResult2.getLocation());
            return d <= d2 ? blockHitResult : blockHitResult2;
        }, clipContext -> {
            Vec3 vec3 = clipContext.getFrom().subtract(clipContext.getTo());
            return BlockHitResult.miss(clipContext.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipContext.getTo()));
        });
    }

    @Nullable
    default public BlockHitResult clipWithInteractionOverride(Vec3 vec3, Vec3 vec32, BlockPos blockPos, VoxelShape voxelShape, BlockState blockState) {
        BlockHitResult blockHitResult;
        BlockHitResult blockHitResult2 = voxelShape.clip(vec3, vec32, blockPos);
        if (blockHitResult2 != null && (blockHitResult = blockState.getInteractionShape(this, blockPos).clip(vec3, vec32, blockPos)) != null && blockHitResult.getLocation().subtract(vec3).lengthSqr() < blockHitResult2.getLocation().subtract(vec3).lengthSqr()) {
            return blockHitResult2.withDirection(blockHitResult.getDirection());
        }
        return blockHitResult2;
    }

    default public double getBlockFloorHeight(VoxelShape voxelShape, Supplier<VoxelShape> supplier) {
        if (!voxelShape.isEmpty()) {
            return voxelShape.max(Direction.Axis.Y);
        }
        double d = supplier.get().max(Direction.Axis.Y);
        if (d >= 1.0) {
            return d - 1.0;
        }
        return Double.NEGATIVE_INFINITY;
    }

    default public double getBlockFloorHeight(BlockPos blockPos) {
        return this.getBlockFloorHeight(this.getBlockState(blockPos).getCollisionShape(this, blockPos), () -> {
            BlockPos blockPos2 = blockPos.below();
            return this.getBlockState(blockPos2).getCollisionShape(this, blockPos2);
        });
    }

    public static <T, C> T traverseBlocks(Vec3 vec3, Vec3 vec32, C c, BiFunction<C, BlockPos, T> biFunction, Function<C, T> function) {
        int n;
        int n2;
        if (vec3.equals(vec32)) {
            return function.apply(c);
        }
        double d = Mth.lerp(-1.0E-7, vec32.x, vec3.x);
        double d2 = Mth.lerp(-1.0E-7, vec32.y, vec3.y);
        double d3 = Mth.lerp(-1.0E-7, vec32.z, vec3.z);
        double d4 = Mth.lerp(-1.0E-7, vec3.x, vec32.x);
        double d5 = Mth.lerp(-1.0E-7, vec3.y, vec32.y);
        double d6 = Mth.lerp(-1.0E-7, vec3.z, vec32.z);
        int n3 = Mth.floor(d4);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(n3, n2 = Mth.floor(d5), n = Mth.floor(d6));
        T t = biFunction.apply(c, mutableBlockPos);
        if (t != null) {
            return t;
        }
        double d7 = d - d4;
        double d8 = d2 - d5;
        double d9 = d3 - d6;
        int n4 = Mth.sign(d7);
        int n5 = Mth.sign(d8);
        int n6 = Mth.sign(d9);
        double d10 = n4 == 0 ? Double.MAX_VALUE : (double)n4 / d7;
        double d11 = n5 == 0 ? Double.MAX_VALUE : (double)n5 / d8;
        double d12 = n6 == 0 ? Double.MAX_VALUE : (double)n6 / d9;
        double d13 = d10 * (n4 > 0 ? 1.0 - Mth.frac(d4) : Mth.frac(d4));
        double d14 = d11 * (n5 > 0 ? 1.0 - Mth.frac(d5) : Mth.frac(d5));
        double d15 = d12 * (n6 > 0 ? 1.0 - Mth.frac(d6) : Mth.frac(d6));
        while (d13 <= 1.0 || d14 <= 1.0 || d15 <= 1.0) {
            T t2;
            if (d13 < d14) {
                if (d13 < d15) {
                    n3 += n4;
                    d13 += d10;
                } else {
                    n += n6;
                    d15 += d12;
                }
            } else if (d14 < d15) {
                n2 += n5;
                d14 += d11;
            } else {
                n += n6;
                d15 += d12;
            }
            if ((t2 = biFunction.apply(c, mutableBlockPos.set(n3, n2, n))) == null) continue;
            return t2;
        }
        return function.apply(c);
    }

    public static boolean forEachBlockIntersectedBetween(Vec3 vec3, Vec3 vec32, AABB aABB, BlockStepVisitor blockStepVisitor) {
        Vec3 vec33 = vec32.subtract(vec3);
        if (vec33.lengthSqr() < (double)Mth.square(0.99999f)) {
            for (BlockPos blockPos : BlockPos.betweenClosed(aABB)) {
                if (blockStepVisitor.visit(blockPos, 0)) continue;
                return false;
            }
            return true;
        }
        LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
        Vec3 vec34 = aABB.getMinPosition();
        Vec3 vec35 = vec34.subtract(vec33);
        int n = BlockGetter.addCollisionsAlongTravel((LongSet)longOpenHashSet, vec35, vec34, aABB, blockStepVisitor);
        if (n < 0) {
            return false;
        }
        for (BlockPos blockPos : BlockPos.betweenClosed(aABB)) {
            if (longOpenHashSet.contains(blockPos.asLong()) || blockStepVisitor.visit(blockPos, n + 1)) continue;
            return false;
        }
        return true;
    }

    private static int addCollisionsAlongTravel(LongSet longSet, Vec3 vec3, Vec3 vec32, AABB aABB, BlockStepVisitor blockStepVisitor) {
        Vec3 vec33 = vec32.subtract(vec3);
        int n = Mth.floor(vec3.x);
        int n2 = Mth.floor(vec3.y);
        int n3 = Mth.floor(vec3.z);
        int n4 = Mth.sign(vec33.x);
        int n5 = Mth.sign(vec33.y);
        int n6 = Mth.sign(vec33.z);
        double d = n4 == 0 ? Double.MAX_VALUE : (double)n4 / vec33.x;
        double d2 = n5 == 0 ? Double.MAX_VALUE : (double)n5 / vec33.y;
        double d3 = n6 == 0 ? Double.MAX_VALUE : (double)n6 / vec33.z;
        double d4 = d * (n4 > 0 ? 1.0 - Mth.frac(vec3.x) : Mth.frac(vec3.x));
        double d5 = d2 * (n5 > 0 ? 1.0 - Mth.frac(vec3.y) : Mth.frac(vec3.y));
        double d6 = d3 * (n6 > 0 ? 1.0 - Mth.frac(vec3.z) : Mth.frac(vec3.z));
        int n7 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        while (d4 <= 1.0 || d5 <= 1.0 || d6 <= 1.0) {
            if (d4 < d5) {
                if (d4 < d6) {
                    n += n4;
                    d4 += d;
                } else {
                    n3 += n6;
                    d6 += d3;
                }
            } else if (d5 < d6) {
                n2 += n5;
                d5 += d2;
            } else {
                n3 += n6;
                d6 += d3;
            }
            if (n7++ > 16) break;
            Optional<Vec3> optional = AABB.clip(n, n2, n3, n + 1, n2 + 1, n3 + 1, vec3, vec32);
            if (optional.isEmpty()) continue;
            Vec3 vec34 = optional.get();
            double d7 = Mth.clamp(vec34.x, (double)n + (double)1.0E-5f, (double)n + 1.0 - (double)1.0E-5f);
            double d8 = Mth.clamp(vec34.y, (double)n2 + (double)1.0E-5f, (double)n2 + 1.0 - (double)1.0E-5f);
            double d9 = Mth.clamp(vec34.z, (double)n3 + (double)1.0E-5f, (double)n3 + 1.0 - (double)1.0E-5f);
            int n8 = Mth.floor(d7 + aABB.getXsize());
            int n9 = Mth.floor(d8 + aABB.getYsize());
            int n10 = Mth.floor(d9 + aABB.getZsize());
            for (int i = n; i <= n8; ++i) {
                for (int j = n2; j <= n9; ++j) {
                    for (int k = n3; k <= n10; ++k) {
                        if (!longSet.add(BlockPos.asLong(i, j, k)) || blockStepVisitor.visit(mutableBlockPos.set(i, j, k), n7)) continue;
                        return -1;
                    }
                }
            }
        }
        return n7;
    }

    @FunctionalInterface
    public static interface BlockStepVisitor {
        public boolean visit(BlockPos var1, int var2);
    }
}

