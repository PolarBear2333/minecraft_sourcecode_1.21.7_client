/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeBrownMushroomFeature
extends AbstractHugeMushroomFeature {
    public HugeBrownMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    protected void makeCap(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, int n, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int n2 = hugeMushroomFeatureConfiguration.foliageRadius;
        for (int i = -n2; i <= n2; ++i) {
            for (int j = -n2; j <= n2; ++j) {
                boolean bl;
                boolean bl2 = i == -n2;
                boolean bl3 = i == n2;
                boolean bl4 = j == -n2;
                boolean bl5 = j == n2;
                boolean bl6 = bl2 || bl3;
                boolean bl7 = bl = bl4 || bl5;
                if (bl6 && bl) continue;
                mutableBlockPos.setWithOffset(blockPos, i, n, j);
                boolean bl8 = bl2 || bl && i == 1 - n2;
                boolean bl9 = bl3 || bl && i == n2 - 1;
                boolean bl10 = bl4 || bl6 && j == 1 - n2;
                boolean bl11 = bl5 || bl6 && j == n2 - 1;
                BlockState blockState = hugeMushroomFeatureConfiguration.capProvider.getState(randomSource, blockPos);
                if (blockState.hasProperty(HugeMushroomBlock.WEST) && blockState.hasProperty(HugeMushroomBlock.EAST) && blockState.hasProperty(HugeMushroomBlock.NORTH) && blockState.hasProperty(HugeMushroomBlock.SOUTH)) {
                    blockState = (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(HugeMushroomBlock.WEST, bl8)).setValue(HugeMushroomBlock.EAST, bl9)).setValue(HugeMushroomBlock.NORTH, bl10)).setValue(HugeMushroomBlock.SOUTH, bl11);
                }
                this.placeMushroomBlock(levelAccessor, mutableBlockPos, blockState);
            }
        }
    }

    @Override
    protected int getTreeRadiusForHeight(int n, int n2, int n3, int n4) {
        return n4 <= 3 ? 0 : n3;
    }
}

