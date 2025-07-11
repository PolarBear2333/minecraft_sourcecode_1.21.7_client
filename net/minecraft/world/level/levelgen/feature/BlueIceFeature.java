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

public class BlueIceFeature
extends Feature<NoneFeatureConfiguration> {
    public BlueIceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        if (blockPos.getY() > worldGenLevel.getSeaLevel() - 1) {
            return false;
        }
        if (!worldGenLevel.getBlockState(blockPos).is(Blocks.WATER) && !worldGenLevel.getBlockState(blockPos.below()).is(Blocks.WATER)) {
            return false;
        }
        boolean bl = false;
        for (Direction object : Direction.values()) {
            if (object == Direction.DOWN || !worldGenLevel.getBlockState(blockPos.relative(object)).is(Blocks.PACKED_ICE)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        worldGenLevel.setBlock(blockPos, Blocks.BLUE_ICE.defaultBlockState(), 2);
        block1: for (int i = 0; i < 200; ++i) {
            BlockPos blockPos2;
            BlockState blockState;
            int n = randomSource.nextInt(5) - randomSource.nextInt(6);
            int n2 = 3;
            if (n < 2) {
                n2 += n / 2;
            }
            if (n2 < 1 || !(blockState = worldGenLevel.getBlockState(blockPos2 = blockPos.offset(randomSource.nextInt(n2) - randomSource.nextInt(n2), n, randomSource.nextInt(n2) - randomSource.nextInt(n2)))).isAir() && !blockState.is(Blocks.WATER) && !blockState.is(Blocks.PACKED_ICE) && !blockState.is(Blocks.ICE)) continue;
            for (Direction direction : Direction.values()) {
                BlockState blockState2 = worldGenLevel.getBlockState(blockPos2.relative(direction));
                if (!blockState2.is(Blocks.BLUE_ICE)) continue;
                worldGenLevel.setBlock(blockPos2, Blocks.BLUE_ICE.defaultBlockState(), 2);
                continue block1;
            }
        }
        return true;
    }
}

