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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowAndFreezeFeature
extends Feature<NoneFeatureConfiguration> {
    public SnowAndFreezeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int n = blockPos.getX() + i;
                int n2 = blockPos.getZ() + j;
                int n3 = worldGenLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, n, n2);
                mutableBlockPos.set(n, n3, n2);
                mutableBlockPos2.set(mutableBlockPos).move(Direction.DOWN, 1);
                Biome biome = worldGenLevel.getBiome(mutableBlockPos).value();
                if (biome.shouldFreeze(worldGenLevel, mutableBlockPos2, false)) {
                    worldGenLevel.setBlock(mutableBlockPos2, Blocks.ICE.defaultBlockState(), 2);
                }
                if (!biome.shouldSnow(worldGenLevel, mutableBlockPos)) continue;
                worldGenLevel.setBlock(mutableBlockPos, Blocks.SNOW.defaultBlockState(), 2);
                BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos2);
                if (!blockState.hasProperty(SnowyDirtBlock.SNOWY)) continue;
                worldGenLevel.setBlock(mutableBlockPos2, (BlockState)blockState.setValue(SnowyDirtBlock.SNOWY, true), 2);
            }
        }
        return true;
    }
}

