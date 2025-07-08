/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class RandomPos {
    private static final int RANDOM_POS_ATTEMPTS = 10;

    public static BlockPos generateRandomDirection(RandomSource randomSource, int n, int n2) {
        int n3 = randomSource.nextInt(2 * n + 1) - n;
        int n4 = randomSource.nextInt(2 * n2 + 1) - n2;
        int n5 = randomSource.nextInt(2 * n + 1) - n;
        return new BlockPos(n3, n4, n5);
    }

    @Nullable
    public static BlockPos generateRandomDirectionWithinRadians(RandomSource randomSource, int n, int n2, int n3, double d, double d2, double d3) {
        double d4 = Mth.atan2(d2, d) - 1.5707963705062866;
        double d5 = d4 + (double)(2.0f * randomSource.nextFloat() - 1.0f) * d3;
        double d6 = Math.sqrt(randomSource.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)n;
        double d7 = -d6 * Math.sin(d5);
        double d8 = d6 * Math.cos(d5);
        if (Math.abs(d7) > (double)n || Math.abs(d8) > (double)n) {
            return null;
        }
        int n4 = randomSource.nextInt(2 * n2 + 1) - n2 + n3;
        return BlockPos.containing(d7, n4, d8);
    }

    @VisibleForTesting
    public static BlockPos moveUpOutOfSolid(BlockPos blockPos, int n, Predicate<BlockPos> predicate) {
        if (predicate.test(blockPos)) {
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.UP);
            while (mutableBlockPos.getY() <= n && predicate.test(mutableBlockPos)) {
                mutableBlockPos.move(Direction.UP);
            }
            return mutableBlockPos.immutable();
        }
        return blockPos;
    }

    @VisibleForTesting
    public static BlockPos moveUpToAboveSolid(BlockPos blockPos, int n, int n2, Predicate<BlockPos> predicate) {
        if (n < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + n + ", expected >= 0");
        }
        if (predicate.test(blockPos)) {
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.UP);
            while (mutableBlockPos.getY() <= n2 && predicate.test(mutableBlockPos)) {
                mutableBlockPos.move(Direction.UP);
            }
            int n3 = mutableBlockPos.getY();
            while (mutableBlockPos.getY() <= n2 && mutableBlockPos.getY() - n3 < n) {
                mutableBlockPos.move(Direction.UP);
                if (!predicate.test(mutableBlockPos)) continue;
                mutableBlockPos.move(Direction.DOWN);
                break;
            }
            return mutableBlockPos.immutable();
        }
        return blockPos;
    }

    @Nullable
    public static Vec3 generateRandomPos(PathfinderMob pathfinderMob, Supplier<BlockPos> supplier) {
        return RandomPos.generateRandomPos(supplier, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 generateRandomPos(Supplier<BlockPos> supplier, ToDoubleFunction<BlockPos> toDoubleFunction) {
        double d = Double.NEGATIVE_INFINITY;
        BlockPos blockPos = null;
        for (int i = 0; i < 10; ++i) {
            double d2;
            BlockPos blockPos2 = supplier.get();
            if (blockPos2 == null || !((d2 = toDoubleFunction.applyAsDouble(blockPos2)) > d)) continue;
            d = d2;
            blockPos = blockPos2;
        }
        return blockPos != null ? Vec3.atBottomCenterOf(blockPos) : null;
    }

    public static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfinderMob, int n, RandomSource randomSource, BlockPos blockPos) {
        int n2 = blockPos.getX();
        int n3 = blockPos.getZ();
        if (pathfinderMob.hasHome() && n > 1) {
            BlockPos blockPos2 = pathfinderMob.getHomePosition();
            n2 = pathfinderMob.getX() > (double)blockPos2.getX() ? (n2 -= randomSource.nextInt(n / 2)) : (n2 += randomSource.nextInt(n / 2));
            n3 = pathfinderMob.getZ() > (double)blockPos2.getZ() ? (n3 -= randomSource.nextInt(n / 2)) : (n3 += randomSource.nextInt(n / 2));
        }
        return BlockPos.containing((double)n2 + pathfinderMob.getX(), (double)blockPos.getY() + pathfinderMob.getY(), (double)n3 + pathfinderMob.getZ());
    }
}

