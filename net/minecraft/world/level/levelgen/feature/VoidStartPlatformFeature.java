/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VoidStartPlatformFeature
extends Feature<NoneFeatureConfiguration> {
    private static final BlockPos PLATFORM_OFFSET = new BlockPos(8, 3, 8);
    private static final ChunkPos PLATFORM_ORIGIN_CHUNK = new ChunkPos(PLATFORM_OFFSET);
    private static final int PLATFORM_RADIUS = 16;
    private static final int PLATFORM_RADIUS_CHUNKS = 1;

    public VoidStartPlatformFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private static int checkerboardDistance(int n, int n2, int n3, int n4) {
        return Math.max(Math.abs(n - n3), Math.abs(n2 - n4));
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        ChunkPos chunkPos = new ChunkPos(featurePlaceContext.origin());
        if (VoidStartPlatformFeature.checkerboardDistance(chunkPos.x, chunkPos.z, VoidStartPlatformFeature.PLATFORM_ORIGIN_CHUNK.x, VoidStartPlatformFeature.PLATFORM_ORIGIN_CHUNK.z) > 1) {
            return true;
        }
        BlockPos blockPos = PLATFORM_OFFSET.atY(featurePlaceContext.origin().getY() + PLATFORM_OFFSET.getY());
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = chunkPos.getMinBlockZ(); i <= chunkPos.getMaxBlockZ(); ++i) {
            for (int j = chunkPos.getMinBlockX(); j <= chunkPos.getMaxBlockX(); ++j) {
                if (VoidStartPlatformFeature.checkerboardDistance(blockPos.getX(), blockPos.getZ(), j, i) > 16) continue;
                mutableBlockPos.set(j, blockPos.getY(), i);
                if (mutableBlockPos.equals(blockPos)) {
                    worldGenLevel.setBlock(mutableBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                    continue;
                }
                worldGenLevel.setBlock(mutableBlockPos, Blocks.STONE.defaultBlockState(), 2);
            }
        }
        return true;
    }
}

