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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowstoneFeature
extends Feature<NoneFeatureConfiguration> {
    public GlowstoneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        RandomSource randomSource = featurePlaceContext.random();
        if (!worldGenLevel.isEmptyBlock(blockPos)) {
            return false;
        }
        BlockState blockState = worldGenLevel.getBlockState(blockPos.above());
        if (!(blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.BASALT) || blockState.is(Blocks.BLACKSTONE))) {
            return false;
        }
        worldGenLevel.setBlock(blockPos, Blocks.GLOWSTONE.defaultBlockState(), 2);
        for (int i = 0; i < 1500; ++i) {
            BlockPos blockPos2 = blockPos.offset(randomSource.nextInt(8) - randomSource.nextInt(8), -randomSource.nextInt(12), randomSource.nextInt(8) - randomSource.nextInt(8));
            if (!worldGenLevel.getBlockState(blockPos2).isAir()) continue;
            int n = 0;
            for (Direction direction : Direction.values()) {
                if (worldGenLevel.getBlockState(blockPos2.relative(direction)).is(Blocks.GLOWSTONE)) {
                    ++n;
                }
                if (n > 1) break;
            }
            if (n != true) continue;
            worldGenLevel.setBlock(blockPos2, Blocks.GLOWSTONE.defaultBlockState(), 2);
        }
        return true;
    }
}

