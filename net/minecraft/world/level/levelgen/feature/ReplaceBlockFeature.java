/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature
extends Feature<ReplaceBlockConfiguration> {
    public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ReplaceBlockConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        ReplaceBlockConfiguration replaceBlockConfiguration = featurePlaceContext.config();
        for (OreConfiguration.TargetBlockState targetBlockState : replaceBlockConfiguration.targetStates) {
            if (!targetBlockState.target.test(worldGenLevel.getBlockState(blockPos), featurePlaceContext.random())) continue;
            worldGenLevel.setBlock(blockPos, targetBlockState.state, 2);
            break;
        }
        return true;
    }
}

