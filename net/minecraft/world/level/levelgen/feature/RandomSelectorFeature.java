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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature
extends Feature<RandomFeatureConfiguration> {
    public RandomSelectorFeature(Codec<RandomFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomFeatureConfiguration> featurePlaceContext) {
        RandomFeatureConfiguration randomFeatureConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
        BlockPos blockPos = featurePlaceContext.origin();
        for (WeightedPlacedFeature weightedPlacedFeature : randomFeatureConfiguration.features) {
            if (!(randomSource.nextFloat() < weightedPlacedFeature.chance)) continue;
            return weightedPlacedFeature.place(worldGenLevel, chunkGenerator, randomSource, blockPos);
        }
        return randomFeatureConfiguration.defaultFeature.value().place(worldGenLevel, chunkGenerator, randomSource, blockPos);
    }
}

