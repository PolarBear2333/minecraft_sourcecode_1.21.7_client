/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FallenTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class FallenTreeFeature
extends Feature<FallenTreeConfiguration> {
    private static final int STUMP_HEIGHT = 1;
    private static final int STUMP_HEIGHT_PLUS_EMPTY_SPACE = 2;
    private static final int FALLEN_LOG_MAX_FALL_HEIGHT_TO_GROUND = 5;
    private static final int FALLEN_LOG_MAX_GROUND_GAP = 2;
    private static final int FALLEN_LOG_MAX_SPACE_FROM_STUMP = 2;
    private static final int BLOCK_UPDATE_FLAGS = 19;

    public FallenTreeFeature(Codec<FallenTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FallenTreeConfiguration> featurePlaceContext) {
        this.placeFallenTree(featurePlaceContext.config(), featurePlaceContext.origin(), featurePlaceContext.level(), featurePlaceContext.random());
        return true;
    }

    private void placeFallenTree(FallenTreeConfiguration fallenTreeConfiguration, BlockPos blockPos, WorldGenLevel worldGenLevel, RandomSource randomSource) {
        this.placeStump(fallenTreeConfiguration, worldGenLevel, randomSource, blockPos.mutable());
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
        int n = fallenTreeConfiguration.logLength.sample(randomSource) - 2;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.relative(direction, 2 + randomSource.nextInt(2)).mutable();
        this.setGroundHeightForFallenLogStartPos(worldGenLevel, mutableBlockPos);
        if (this.canPlaceEntireFallenLog(worldGenLevel, n, mutableBlockPos, direction)) {
            this.placeFallenLog(fallenTreeConfiguration, worldGenLevel, randomSource, n, mutableBlockPos, direction);
        }
    }

    private void setGroundHeightForFallenLogStartPos(WorldGenLevel worldGenLevel, BlockPos.MutableBlockPos mutableBlockPos) {
        mutableBlockPos.move(Direction.UP, 1);
        for (int i = 0; i < 6; ++i) {
            if (this.mayPlaceOn(worldGenLevel, mutableBlockPos)) {
                return;
            }
            mutableBlockPos.move(Direction.DOWN);
        }
    }

    private void placeStump(FallenTreeConfiguration fallenTreeConfiguration, WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos) {
        BlockPos blockPos = this.placeLogBlock(fallenTreeConfiguration, worldGenLevel, randomSource, mutableBlockPos, Function.identity());
        this.decorateLogs(worldGenLevel, randomSource, Set.of(blockPos), fallenTreeConfiguration.stumpDecorators);
    }

    private boolean canPlaceEntireFallenLog(WorldGenLevel worldGenLevel, int n, BlockPos.MutableBlockPos mutableBlockPos, Direction direction) {
        int n2 = 0;
        for (int i = 0; i < n; ++i) {
            if (!TreeFeature.validTreePos(worldGenLevel, mutableBlockPos)) {
                return false;
            }
            if (!this.isOverSolidGround(worldGenLevel, mutableBlockPos)) {
                if (++n2 > 2) {
                    return false;
                }
            } else {
                n2 = 0;
            }
            mutableBlockPos.move(direction);
        }
        mutableBlockPos.move(direction.getOpposite(), n);
        return true;
    }

    private void placeFallenLog(FallenTreeConfiguration fallenTreeConfiguration, WorldGenLevel worldGenLevel, RandomSource randomSource, int n, BlockPos.MutableBlockPos mutableBlockPos, Direction direction) {
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        for (int i = 0; i < n; ++i) {
            hashSet.add(this.placeLogBlock(fallenTreeConfiguration, worldGenLevel, randomSource, mutableBlockPos, FallenTreeFeature.getSidewaysStateModifier(direction)));
            mutableBlockPos.move(direction);
        }
        this.decorateLogs(worldGenLevel, randomSource, hashSet, fallenTreeConfiguration.logDecorators);
    }

    private boolean mayPlaceOn(LevelAccessor levelAccessor, BlockPos blockPos) {
        return TreeFeature.validTreePos(levelAccessor, blockPos) && this.isOverSolidGround(levelAccessor, blockPos);
    }

    private boolean isOverSolidGround(LevelAccessor levelAccessor, BlockPos blockPos) {
        return levelAccessor.getBlockState(blockPos.below()).isFaceSturdy(levelAccessor, blockPos, Direction.UP);
    }

    private BlockPos placeLogBlock(FallenTreeConfiguration fallenTreeConfiguration, WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, Function<BlockState, BlockState> function) {
        worldGenLevel.setBlock(mutableBlockPos, function.apply(fallenTreeConfiguration.trunkProvider.getState(randomSource, mutableBlockPos)), 3);
        this.markAboveForPostProcessing(worldGenLevel, mutableBlockPos);
        return mutableBlockPos.immutable();
    }

    private void decorateLogs(WorldGenLevel worldGenLevel, RandomSource randomSource, Set<BlockPos> set, List<TreeDecorator> list) {
        if (!list.isEmpty()) {
            TreeDecorator.Context context = new TreeDecorator.Context(worldGenLevel, this.getDecorationSetter(worldGenLevel), randomSource, set, Set.of(), Set.of());
            list.forEach(treeDecorator -> treeDecorator.place(context));
        }
    }

    private BiConsumer<BlockPos, BlockState> getDecorationSetter(WorldGenLevel worldGenLevel) {
        return (blockPos, blockState) -> worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
    }

    private static Function<BlockState, BlockState> getSidewaysStateModifier(Direction direction) {
        return blockState -> (BlockState)blockState.trySetValue(RotatedPillarBlock.AXIS, direction.getAxis());
    }
}

