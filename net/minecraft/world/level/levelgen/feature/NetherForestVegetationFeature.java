/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;

public class NetherForestVegetationFeature
extends Feature<NetherForestVegetationConfig> {
    public NetherForestVegetationFeature(Codec<NetherForestVegetationConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NetherForestVegetationConfig> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        BlockState blockState = worldGenLevel.getBlockState(blockPos.below());
        NetherForestVegetationConfig netherForestVegetationConfig = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        if (!blockState.is(BlockTags.NYLIUM)) {
            return false;
        }
        int n = blockPos.getY();
        if (n < worldGenLevel.getMinY() + 1 || n + 1 > worldGenLevel.getMaxY()) {
            return false;
        }
        int n2 = 0;
        for (int i = 0; i < netherForestVegetationConfig.spreadWidth * netherForestVegetationConfig.spreadWidth; ++i) {
            BlockPos blockPos2 = blockPos.offset(randomSource.nextInt(netherForestVegetationConfig.spreadWidth) - randomSource.nextInt(netherForestVegetationConfig.spreadWidth), randomSource.nextInt(netherForestVegetationConfig.spreadHeight) - randomSource.nextInt(netherForestVegetationConfig.spreadHeight), randomSource.nextInt(netherForestVegetationConfig.spreadWidth) - randomSource.nextInt(netherForestVegetationConfig.spreadWidth));
            BlockState blockState2 = netherForestVegetationConfig.stateProvider.getState(randomSource, blockPos2);
            if (!worldGenLevel.isEmptyBlock(blockPos2) || blockPos2.getY() <= worldGenLevel.getMinY() || !blockState2.canSurvive(worldGenLevel, blockPos2)) continue;
            worldGenLevel.setBlock(blockPos2, blockState2, 2);
            ++n2;
        }
        return n2 > 0;
    }
}

