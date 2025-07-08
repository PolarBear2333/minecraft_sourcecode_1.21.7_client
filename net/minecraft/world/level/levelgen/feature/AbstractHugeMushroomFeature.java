/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeMushroomFeature
extends Feature<HugeMushroomFeatureConfiguration> {
    public AbstractHugeMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
        super(codec);
    }

    protected void placeTrunk(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration, int n, BlockPos.MutableBlockPos mutableBlockPos) {
        for (int i = 0; i < n; ++i) {
            mutableBlockPos.set(blockPos).move(Direction.UP, i);
            this.placeMushroomBlock(levelAccessor, mutableBlockPos, hugeMushroomFeatureConfiguration.stemProvider.getState(randomSource, blockPos));
        }
    }

    protected void placeMushroomBlock(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, BlockState blockState) {
        BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
        if (blockState2.isAir() || blockState2.is(BlockTags.REPLACEABLE_BY_MUSHROOMS)) {
            this.setBlock(levelAccessor, mutableBlockPos, blockState);
        }
    }

    protected int getTreeHeight(RandomSource randomSource) {
        int n = randomSource.nextInt(3) + 4;
        if (randomSource.nextInt(12) == 0) {
            n *= 2;
        }
        return n;
    }

    protected boolean isValidPosition(LevelAccessor levelAccessor, BlockPos blockPos, int n, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int n2 = blockPos.getY();
        if (n2 < levelAccessor.getMinY() + 1 || n2 + n + 1 > levelAccessor.getMaxY()) {
            return false;
        }
        BlockState blockState = levelAccessor.getBlockState(blockPos.below());
        if (!AbstractHugeMushroomFeature.isDirt(blockState) && !blockState.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return false;
        }
        for (int i = 0; i <= n; ++i) {
            int n3 = this.getTreeRadiusForHeight(-1, -1, hugeMushroomFeatureConfiguration.foliageRadius, i);
            for (int j = -n3; j <= n3; ++j) {
                for (int k = -n3; k <= n3; ++k) {
                    BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, j, i, k));
                    if (blockState2.isAir() || blockState2.is(BlockTags.LEAVES)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> featurePlaceContext) {
        BlockPos.MutableBlockPos mutableBlockPos;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        RandomSource randomSource = featurePlaceContext.random();
        HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration = featurePlaceContext.config();
        int n = this.getTreeHeight(randomSource);
        if (!this.isValidPosition(worldGenLevel, blockPos, n, mutableBlockPos = new BlockPos.MutableBlockPos(), hugeMushroomFeatureConfiguration)) {
            return false;
        }
        this.makeCap(worldGenLevel, randomSource, blockPos, n, mutableBlockPos, hugeMushroomFeatureConfiguration);
        this.placeTrunk(worldGenLevel, randomSource, blockPos, hugeMushroomFeatureConfiguration, n, mutableBlockPos);
        return true;
    }

    protected abstract int getTreeRadiusForHeight(int var1, int var2, int var3, int var4);

    protected abstract void makeCap(LevelAccessor var1, RandomSource var2, BlockPos var3, int var4, BlockPos.MutableBlockPos var5, HugeMushroomFeatureConfiguration var6);
}

